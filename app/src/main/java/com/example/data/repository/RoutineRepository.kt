package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.dao.DayDao
import com.example.data.local.entity.BlockOutcomeEntity
import com.example.data.local.entity.DayRecordEntity
import com.example.data.local.entity.InsightsDraftEntity
import com.example.data.local.entity.JournalEntryEntity
import com.example.data.local.entity.toDomain
import com.example.data.service.InsightsDraftService
import com.example.domain.model.AnchorType
import com.example.domain.model.BlockStatus
import com.example.domain.model.BreakpointChain
import com.example.domain.model.DayBlock
import com.example.domain.model.DayRecord
import com.example.domain.model.Experiment
import com.example.domain.model.ExperimentStatus
import com.example.domain.model.ExperimentSuggestion
import com.example.domain.model.TapeSynthesis
import com.example.ui.components.ReelDay
import com.example.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Loop Journal Data & Telemetry Repository
 *
 * Rules:
 * - Delete every hardcoded date, weekday, and label.
 * - Use java.time with ZoneId.systemDefault().
 * - Store dates only as yyyy-MM-dd. Never store a display string as the date.
 * - Store times as HH:mm, 24-hour.
 * - Show the date in the phone locale. Keep the saved value canonical.
 * - Today is LocalDate.now(ZoneId.systemDefault()). After midnight, Today must become the new day.
 * - The week reel is Monday to Sunday of the current week in the phone timezone. Mark today.
 *   Fill a segment only if that yyyy-MM-dd was captured.
 * - Block times like 06:30 stay the times set in My System.
 *   Use the clock only to highlight the current block and to stamp when Done, Modified, Missed, or Skip was tapped.
 * - Room rows for a day, a block outcome, and a journal entry must include dateIso.
 *   Today loads only rows for today's dateIso.
 * - If the timezone changes, old dateIso values stay as saved. New rows use the new zone.
 */
class RoutineRepository {

  // Current active date (canonical yyyy-MM-dd) based on phone clock and system default timezone
  private val _activeDateIso = MutableStateFlow(DateTimeUtils.todayIso())
  val activeDateIso: StateFlow<String> = _activeDateIso.asStateFlow()

  // System routine template blocks (defined in My System)
  private val _systemBlocks = MutableStateFlow(createInitialSystemBlocks())
  val systemBlocks: StateFlow<List<DayBlock>> = _systemBlocks.asStateFlow()

  // In-memory cache of block outcomes per canonical dateIso
  private val blockOutcomesByDate = mutableMapOf<String, List<DayBlock>>()

  // In-memory cache of DayRecord per canonical dateIso
  private val dayRecordsByDate = mutableMapOf<String, DayRecord>()

  // Sets of captured and missed dateIsos (canonical yyyy-MM-dd)
  private val capturedDateIsos = mutableSetOf<String>()
  private val missedDateIsos = mutableSetOf<String>()

  // Active blocks for Today (loads only rows for today's dateIso)
  private val _blocks = MutableStateFlow<List<DayBlock>>(emptyList())
  val blocks: StateFlow<List<DayBlock>> = _blocks.asStateFlow()

  // Week reel: Monday to Sunday of the current week in the phone timezone
  private val _weekReel = MutableStateFlow<List<ReelDay>>(emptyList())
  val weekReel: StateFlow<List<ReelDay>> = _weekReel.asStateFlow()

  // Today's day record
  private val _todayRecord = MutableStateFlow(createEmptyDayRecord(DateTimeUtils.todayIso()))
  val todayRecord: StateFlow<DayRecord> = _todayRecord.asStateFlow()

  // Past day records (sealed day tapes)
  private val _pastRecords = MutableStateFlow<List<DayRecord>>(emptyList())
  val pastRecords: StateFlow<List<DayRecord>> = _pastRecords.asStateFlow()

  private val _tapeSynthesis = MutableStateFlow<TapeSynthesis?>(null)
  val tapeSynthesis: StateFlow<TapeSynthesis?> = _tapeSynthesis.asStateFlow()

  private val _activeExperiment = MutableStateFlow<Experiment?>(null)
  val activeExperiment: StateFlow<Experiment?> = _activeExperiment.asStateFlow()

  private val _pastExperiments = MutableStateFlow<List<Experiment>>(emptyList())
  val pastExperiments: StateFlow<List<Experiment>> = _pastExperiments.asStateFlow()

  private val _suggestedExperiment = MutableStateFlow<ExperimentSuggestion?>(null)
  val suggestedExperiment: StateFlow<ExperimentSuggestion?> = _suggestedExperiment.asStateFlow()

  @Volatile
  private var lastDraftFingerprint: String = ""

  private var dayDao: DayDao? = null
  private var appContext: Context? = null
  private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  init {
    loadDay(_activeDateIso.value)
  }

  fun initContext(context: Context) {
    appContext = context.applicationContext
  }

  fun initRoom(dao: DayDao) {
    dayDao = dao
    ioScope.launch {
      try {
        val todayIso = _activeDateIso.value
        val saved = dao.getBlockOutcomes(todayIso)
        if (saved.isNotEmpty()) {
          val savedMap = saved.associateBy { it.blockId }
          val merged = _systemBlocks.value.map { templateBlock ->
            savedMap[templateBlock.id]?.toDomain() ?: templateBlock.copy(
              dateIso = todayIso,
              status = BlockStatus.PENDING,
              plannedTime = templateBlock.intendedTime
            )
          }
          _blocks.value = merged
          blockOutcomesByDate[todayIso] = merged
          syncTodayReelStatus()
        }

        val journal = dao.getJournalEntry(todayIso)
        if (journal != null) {
          _todayRecord.update {
            it.copy(
              playbackNote = journal.wentToPlan,
              energyRating = journal.energyRating,
              stressRating = journal.stressRating
            )
          }
        }

        val savedDayRecord = dao.getDayRecord(todayIso)
        if (savedDayRecord != null) {
          _todayRecord.update {
            it.copy(
              wakeEnergy = savedDayRecord.wakeEnergy,
              wakeMood = savedDayRecord.wakeMood,
              wakeStress = savedDayRecord.wakeStress,
              sleepEnergy = savedDayRecord.sleepEnergy,
              sleepMood = savedDayRecord.sleepMood,
              sleepStress = savedDayRecord.sleepStress
            )
          }
        }

        val allSavedRecords = dao.getAllDayRecords()
        if (allSavedRecords.isNotEmpty()) {
          val domainRecords = allSavedRecords.map { it.toDomain() }
          _pastRecords.value = domainRecords
          allSavedRecords.forEach { rec ->
            dayRecordsByDate[rec.dateIso] = rec.toDomain()
            if (rec.isCaptured) {
              capturedDateIsos.add(rec.dateIso)
            }
          }
        }

        val allOutcomes = dao.getAllBlockOutcomes()
        if (allOutcomes.isNotEmpty()) {
          val byDate = allOutcomes.groupBy { it.dateIso }
          byDate.forEach { (date, outcomes) ->
            blockOutcomesByDate[date] = outcomes.map { it.toDomain() }
            if (outcomes.any { it.status != BlockStatus.PENDING.name }) {
              capturedDateIsos.add(date)
            }
          }
        }

        val savedDraft = dao.getInsightsDraft("current_draft")
        if (savedDraft != null) {
          lastDraftFingerprint = savedDraft.fingerprint
          parseAndApplyDraftJson(savedDraft.rawJson)
        }
      } catch (e: Throwable) {
        // Safe: keep existing in-memory state
      }
    }
  }

  fun saveWakeReadings(energy: Int?, mood: Int?, stress: Int?, dateIso: String = _activeDateIso.value) {
    _todayRecord.update {
      it.copy(wakeEnergy = energy, wakeMood = mood, wakeStress = stress)
    }
    dayRecordsByDate[dateIso] = _todayRecord.value
    persistDayRecordToRoom(_todayRecord.value)
  }

  fun saveSleepReadings(energy: Int?, mood: Int?, stress: Int?, dateIso: String = _activeDateIso.value) {
    _todayRecord.update {
      it.copy(sleepEnergy = energy, sleepMood = mood, sleepStress = stress)
    }
    dayRecordsByDate[dateIso] = _todayRecord.value
    persistDayRecordToRoom(_todayRecord.value)
  }

  private fun persistDayRecordToRoom(rec: DayRecord) {
    val dao = dayDao ?: return
    ioScope.launch {
      try {
        dao.upsertDayRecord(
          DayRecordEntity(
            dateIso = rec.dateIso,
            isCaptured = rec.isCaptured,
            isClosed = rec.isClosed,
            observedCount = rec.observedCount,
            totalCount = rec.totalCount,
            playbackNote = rec.playbackNote,
            wakeEnergy = rec.wakeEnergy,
            wakeMood = rec.wakeMood,
            wakeStress = rec.wakeStress,
            sleepEnergy = rec.sleepEnergy,
            sleepMood = rec.sleepMood,
            sleepStress = rec.sleepStress
          )
        )
      } catch (_: Exception) {}
    }
  }

  private fun persistBlockToRoom(block: DayBlock) {
    val dao = dayDao ?: return
    ioScope.launch {
      try {
        dao.upsertBlockOutcome(block.toEntity(_activeDateIso.value))
      } catch (e: Exception) {
        // Silent
      }
    }
  }

  private fun persistJournalToRoom() {
    val dao = dayDao ?: return
    val rec = _todayRecord.value
    ioScope.launch {
      try {
        dao.upsertJournalEntry(
          JournalEntryEntity(
            dateIso = _activeDateIso.value,
            wentToPlan = rec.playbackNote,
            energyRating = rec.energyRating,
            stressRating = rec.stressRating,
            isClosed = true,
            closedAtTime = DateTimeUtils.currentTime24()
          )
        )
      } catch (e: Exception) {
        // Silent
      }
    }
  }

  fun updateNightNote(noteText: String) {
    _todayRecord.update { it.copy(playbackNote = noteText) }
    persistJournalToRoom()
  }

  fun updateRatings(energy: Float, stress: Float) {
    _todayRecord.update { it.copy(energyRating = energy, stressRating = stress) }
    persistJournalToRoom()
  }

  /**
   * Checks whether the phone clock date has transitioned (e.g. past midnight).
   * If the day has rolled over, Today becomes the new day and loads only rows for the new dateIso.
   */
  fun ensureToday(): Boolean {
    val currentTodayIso = DateTimeUtils.todayIso()
    if (currentTodayIso != _activeDateIso.value) {
      // Save outcomes for the day that just concluded
      blockOutcomesByDate[_activeDateIso.value] = _blocks.value
      _activeDateIso.value = currentTodayIso
      loadDay(currentTodayIso)
      return true
    }
    return false
  }

  /**
   * Loads data strictly for the specified dateIso.
   * Today loads only rows for today's dateIso.
   */
  fun loadDay(dateIso: String) {
    _activeDateIso.value = dateIso
    // 1. Load or instantiate block outcomes for dateIso
    // Do not overwrite a saved day from the template on app launch.
    val outcomes = blockOutcomesByDate.getOrPut(dateIso) {
      _systemBlocks.value.map { block ->
        block.copy(
          dateIso = dateIso,
          status = BlockStatus.PENDING,
          stampedTime = null,
          cause = null,
          note = null,
          plannedTime = block.intendedTime
        )
      }
    }
    _blocks.value = outcomes

    // 2. Load or instantiate DayRecord for dateIso
    val record = dayRecordsByDate.getOrPut(dateIso) {
      createEmptyDayRecord(dateIso)
    }
    _todayRecord.value = record

    // 3. Rebuild the 7-segment week reel for Monday to Sunday of the current week
    refreshWeekReel()
  }

  /**
   * Stamps when Done, Modified, Missed, or Skip was tapped.
   * Stores time as HH:mm 24-hour from the phone clock.
   */
  fun updateBlockStatus(
    blockId: String,
    newStatus: BlockStatus,
    note: String? = null,
    cause: String? = null
  ) {
    ensureToday()
    val stampTime = DateTimeUtils.currentTime24()
    val currentDateIso = _activeDateIso.value

    var updatedOutcome: DayBlock? = null

    _blocks.update { currentList ->
      currentList.map { block ->
        if (block.id == blockId) {
          val updated = block.copy(
            status = newStatus,
            note = note ?: block.note,
            cause = cause ?: block.cause,
            stampedTime = stampTime,
            dateIso = currentDateIso
          )
          updatedOutcome = updated
          updated
        } else {
          block
        }
      }
    }

    // Persist to in-memory date store
    blockOutcomesByDate[currentDateIso] = _blocks.value
    syncTodayReelStatus()

    // Immediately save to Room
    updatedOutcome?.let { persistBlockToRoom(it) }
  }

  fun toggleBlockReminder(blockId: String, remindMe: Boolean) {
    _systemBlocks.update { currentList ->
      currentList.map { if (it.id == blockId) it.copy(remindMe = remindMe) else it }
    }
    _blocks.update { currentList ->
      currentList.map { if (it.id == blockId) it.copy(remindMe = remindMe) else it }
    }
    blockOutcomesByDate[_activeDateIso.value] = _blocks.value
    val target = _blocks.value.find { it.id == blockId }
    if (target != null) {
      persistBlockToRoom(target)
    }
  }

  fun addBlock(block: DayBlock) {
    _systemBlocks.update { currentList ->
      val updated = currentList.toMutableList()
      val sleepIndex = updated.indexOfLast { it.anchorType == AnchorType.SPINE_SLEEP }
      if (sleepIndex != -1) {
        updated.add(sleepIndex, block)
      } else {
        updated.add(block)
      }
      updated
    }
    // Also add to today's active blocks if currently in PENDING state
    _blocks.update { currentList ->
      val updated = currentList.toMutableList()
      val sleepIndex = updated.indexOfLast { it.anchorType == AnchorType.SPINE_SLEEP }
      val newBlockOutcome = block.copy(dateIso = _activeDateIso.value, status = BlockStatus.PENDING)
      if (sleepIndex != -1) {
        updated.add(sleepIndex, newBlockOutcome)
      } else {
        updated.add(newBlockOutcome)
      }
      updated
    }
    blockOutcomesByDate[_activeDateIso.value] = _blocks.value
    syncTodayReelStatus()
  }

  /**
   * Updates a block's template in My System, and conditionally updates today's plannedTime:
   * - Saves the new time on the template for tomorrow onward.
   * - Also updates today's plannedTime only if both are true:
   *   no outcome has been saved for that block today (status == PENDING),
   *   and the current phone time is still before today's plannedTime.
   * - If today's block is Done, Modified, Missed, or Skip, keep today's plannedTime.
   * - If the phone clock is already past today's plannedTime, keep today's plannedTime.
   */
  fun updateBlock(updatedBlock: DayBlock) {
    // 1. Save the new time on the template for tomorrow onward
    _systemBlocks.update { currentList ->
      currentList.map { if (it.id == updatedBlock.id) updatedBlock.copy(plannedTime = updatedBlock.intendedTime) else it }
    }

    // 2. Rules for updating today's plannedTime
    val currentPhoneTime = DateTimeUtils.currentTime24()
    val todayBlock = _blocks.value.find { it.id == updatedBlock.id }

    val shouldUpdateToday = if (todayBlock != null) {
      val hasNoOutcome = (todayBlock.status == BlockStatus.PENDING)
      val isBeforePlannedTime = DateTimeUtils.isTimeBefore(currentPhoneTime, todayBlock.plannedTime)
      hasNoOutcome && isBeforePlannedTime
    } else {
      false
    }

    _blocks.update { currentList ->
      currentList.map { block ->
        if (block.id == updatedBlock.id) {
          if (shouldUpdateToday) {
            block.copy(
              title = updatedBlock.title,
              intendedTime = updatedBlock.intendedTime,
              plannedTime = updatedBlock.intendedTime,
              anchorType = updatedBlock.anchorType,
              note = updatedBlock.note ?: block.note,
              remindMe = updatedBlock.remindMe
            )
          } else {
            // Keep today's plannedTime! The new time starts tomorrow.
            block.copy(
              title = updatedBlock.title,
              anchorType = updatedBlock.anchorType,
              note = updatedBlock.note ?: block.note,
              remindMe = updatedBlock.remindMe
            )
          }
        } else {
          block
        }
      }
    }
    blockOutcomesByDate[_activeDateIso.value] = _blocks.value
    syncTodayReelStatus()
  }

  fun deleteBlock(blockId: String) {
    _systemBlocks.update { currentList ->
      currentList.filterNot { it.id == blockId && !it.anchorType.isSpine }
    }
    _blocks.update { currentList ->
      currentList.filterNot { it.id == blockId && !it.anchorType.isSpine }
    }
    blockOutcomesByDate[_activeDateIso.value] = _blocks.value
    syncTodayReelStatus()
  }

  /**
   * Night Close: seals the day tape.
   * Rows are saved with canonical dateIso.
   */
  fun commitNightClose(
    wentToPlan: String,
    didNotGoToPlan: String,
    inTheWay: String,
    energy: Float,
    stress: Float
  ) {
    ensureToday()
    val dateIso = _activeDateIso.value
    val observed = _blocks.value.count { it.status != BlockStatus.PENDING }
    val total = _blocks.value.size

    val closed = _todayRecord.value.copy(
      dateIso = dateIso,
      wentToPlan = wentToPlan.trim(),
      didNotGoToPlan = didNotGoToPlan.trim(),
      inTheWay = inTheWay.trim(),
      energyRating = energy,
      stressRating = stress,
      isCaptured = true,
      isClosed = true,
      observedCount = observed,
      totalCount = total
    )

    _todayRecord.value = closed
    dayRecordsByDate[dateIso] = closed
    capturedDateIsos.add(dateIso)

    if (_blocks.value.any { it.status == BlockStatus.MISSED }) {
      missedDateIsos.add(dateIso)
    }

    _pastRecords.update { current ->
      listOf(closed) + current.filterNot { it.dateIso == dateIso }
    }

    refreshWeekReel()
  }

  fun resetToday() {
    val dateIso = _activeDateIso.value
    _blocks.value = _systemBlocks.value.map {
      it.copy(dateIso = dateIso, status = BlockStatus.PENDING, stampedTime = null)
    }
    blockOutcomesByDate[dateIso] = _blocks.value
    capturedDateIsos.remove(dateIso)
    missedDateIsos.remove(dateIso)
    refreshWeekReel()
  }

  /**
   * Rule: A reel segment lights amber when the day was CAPTURED,
   * including days with missed blocks.
   * If at least one block has been processed (DONE, MODIFIED, MISSED, or SKIP),
   * the day is captured and lights amber.
   */
  private fun syncTodayReelStatus() {
    val dateIso = _activeDateIso.value
    val anyObserved = _blocks.value.any { it.status != BlockStatus.PENDING }
    val hasMissed = _blocks.value.any { it.status == BlockStatus.MISSED }

    if (anyObserved) {
      capturedDateIsos.add(dateIso)
    } else {
      capturedDateIsos.remove(dateIso)
    }

    if (hasMissed) {
      missedDateIsos.add(dateIso)
    } else {
      missedDateIsos.remove(dateIso)
    }

    refreshWeekReel()
  }

  private fun refreshWeekReel() {
    _weekReel.value = DateTimeUtils.buildCurrentWeekReel(
      todayIso = _activeDateIso.value,
      capturedDateIsos = capturedDateIsos,
      missedDateIsos = missedDateIsos
    )
  }

  private fun createEmptyDayRecord(dateIso: String): DayRecord {
    return DayRecord(
      id = "rec_$dateIso",
      dateIso = dateIso,
      isCaptured = capturedDateIsos.contains(dateIso),
      isClosed = false,
      energyRating = 0.0f,
      stressRating = 0.0f,
      wentToPlan = "",
      didNotGoToPlan = "",
      inTheWay = "",
      playbackNote = "",
      observedCount = 0,
      totalCount = _systemBlocks.value.size
    )
  }

  /**
   * 8 Blocks configured in My System (the ongoing plan / template for tomorrow onward):
   * Stored times as HH:mm 24-hour.
   */
  private fun createInitialSystemBlocks(): List<DayBlock> {
    return listOf(
      DayBlock(
        id = "b1",
        title = "Wake Spine",
        intendedTime = "04:30",
        plannedTime = "04:30",
        anchorType = AnchorType.SPINE_WAKE
      ),
      DayBlock(
        id = "b2",
        title = "Morning Movement / Training",
        intendedTime = "07:15",
        plannedTime = "07:15",
        anchorType = AnchorType.FLEXIBLE
      ),
      DayBlock(
        id = "b3",
        title = "Deep Focus Block 1",
        intendedTime = "09:00",
        plannedTime = "09:00",
        anchorType = AnchorType.HARD
      ),
      DayBlock(
        id = "b4",
        title = "Midday Meal & Walking Reset",
        intendedTime = "12:30",
        plannedTime = "12:30",
        anchorType = AnchorType.FLEXIBLE
      ),
      DayBlock(
        id = "b5",
        title = "Execution Block 2",
        intendedTime = "14:00",
        plannedTime = "14:00",
        anchorType = AnchorType.HARD
      ),
      DayBlock(
        id = "b6",
        title = "Evening Shutdown",
        intendedTime = "18:00",
        plannedTime = "18:00",
        anchorType = AnchorType.HARD
      ),
      DayBlock(
        id = "b7",
        title = "Dinner & Partner Evening",
        intendedTime = "18:45",
        plannedTime = "18:45",
        anchorType = AnchorType.FLEXIBLE
      ),
      DayBlock(
        id = "b8",
        title = "Sleep & Wind-Down",
        intendedTime = "22:30",
        plannedTime = "22:30",
        anchorType = AnchorType.SPINE_SLEEP
      )
    )
  }

  // --- Experiments & Synthesis Engine ---

  fun updateExperimentObservationNotes(notes: String) {
    _activeExperiment.update { it?.copy(observationNotes = notes) }
  }

  fun recordExperimentDecision(
    decision: ExperimentStatus,
    notes: String,
    modifiedIntervention: String? = null
  ) {
    val current = _activeExperiment.value ?: return

    when (decision) {
      ExperimentStatus.KEPT -> {
        val keptExperiment = current.copy(
          status = ExperimentStatus.KEPT,
          observationNotes = notes.ifBlank { current.observationNotes }
        )
        _pastExperiments.update { listOf(keptExperiment) + it.filterNot { exp -> exp.id == current.id } }
        val integratedBlock = DayBlock(
          id = "b_exp_${current.number}",
          title = current.title,
          intendedTime = "21:30",
          anchorType = AnchorType.FLEXIBLE,
          status = BlockStatus.PENDING,
          note = "Integrated into System from Experiment #${current.number}"
        )
        addBlock(integratedBlock)
        _activeExperiment.value = keptExperiment
      }
      ExperimentStatus.MODIFIED -> {
        val modifiedExp = current.copy(
          status = ExperimentStatus.MODIFIED,
          singleIntervention = modifiedIntervention?.trim()?.ifBlank { null } ?: current.singleIntervention,
          dayCount = 1,
          observationNotes = notes.ifBlank { "Modified hypothesis for further testing." }
        )
        _activeExperiment.value = modifiedExp
      }
      ExperimentStatus.ABANDONED -> {
        val abandonedExp = current.copy(
          status = ExperimentStatus.ABANDONED,
          observationNotes = notes.ifBlank { "Abandoned: variable did not resolve core breakpoint." }
        )
        _pastExperiments.update { listOf(abandonedExp) + it.filterNot { exp -> exp.id == current.id } }
        _activeExperiment.value = abandonedExp
      }
      ExperimentStatus.ACTIVE -> {
        _activeExperiment.update { it?.copy(status = ExperimentStatus.ACTIVE) }
      }
    }
  }

  fun promoteSuggestionToExperiment(suggestion: ExperimentSuggestion) {
    _activeExperiment.value = Experiment(
      id = "exp_${suggestion.number}",
      number = suggestion.number,
      title = suggestion.title,
      hypothesis = suggestion.hypothesis,
      singleIntervention = suggestion.singleIntervention,
      metricToWatch = suggestion.metricToWatch,
      status = ExperimentStatus.ACTIVE,
      dayCount = 1,
      totalDays = suggestion.targetDurationDays,
      observationNotes = "Initiated from Insights suggestion."
    )
  }

  fun createNewExperiment(
    title: String,
    hypothesis: String,
    intervention: String,
    metric: String,
    durationDays: Int = 7
  ) {
    val nextNumber = (_pastExperiments.value.maxOfOrNull { it.number } ?: 3) + 1
    _activeExperiment.value = Experiment(
      id = "exp_$nextNumber",
      number = nextNumber,
      title = title.trim(),
      hypothesis = hypothesis.trim(),
      singleIntervention = intervention.trim(),
      metricToWatch = metric.trim(),
      status = ExperimentStatus.ACTIVE,
      dayCount = 1,
      totalDays = durationDays,
      observationNotes = "Initiated single variable test."
    )
  }

  fun getCapturedDaysCount(): Int {
    val fromRecords = dayRecordsByDate.values.filter { it.isCaptured }.map { it.dateIso }
    val fromOutcomes = blockOutcomesByDate.filter { (_, blocks) -> blocks.any { it.status != BlockStatus.PENDING } }.keys
    return (capturedDateIsos + fromRecords + fromOutcomes).size
  }

  fun getCapturedDayRecords(): List<DayRecord> {
    val capturedDates = (capturedDateIsos +
      dayRecordsByDate.values.filter { it.isCaptured }.map { it.dateIso } +
      blockOutcomesByDate.filter { (_, blocks) -> blocks.any { it.status != BlockStatus.PENDING } }.keys
    ).sortedDescending()

    return capturedDates.map { dateIso ->
      dayRecordsByDate[dateIso]
        ?: _pastRecords.value.find { it.dateIso == dateIso }
        ?: DayRecord(
          id = "rec_$dateIso",
          dateIso = dateIso,
          isCaptured = true,
          observedCount = blockOutcomesByDate[dateIso]?.count { it.status != BlockStatus.PENDING } ?: 0
        )
    }
  }

  /**
   * After Today is visible and the database has opened, if there are at least 7 captured days
   * and marks changed since the last draft, start one background call. Not on the launch path.
   * Under 7 captured days, show "Not enough days yet." and do not call the model.
   * If the call fails, times out, or the phone is offline, keep the last note and do not crash.
   * Never write model output back into the day log.
   */
  suspend fun checkAndTriggerSilentInsightsDraft() {
    val dao = dayDao ?: return
    try {
      val allRecords = dao.getAllDayRecords()
      val allOutcomes = dao.getAllBlockOutcomes()
      val outcomesByDate = allOutcomes.groupBy { it.dateIso }

      val capturedDates = mutableSetOf<String>()
      capturedDates.addAll(capturedDateIsos)
      allRecords.filter { it.isCaptured }.forEach { capturedDates.add(it.dateIso) }
      outcomesByDate.forEach { (date, list) ->
        if (list.any { it.status != BlockStatus.PENDING.name }) {
          capturedDates.add(date)
        }
      }

      // Under 7 captured days, show “Not enough days yet.” and do not call the model.
      if (capturedDates.size < 7) {
        return
      }

      // Send only that user's stored days: date, block, planned time, outcome, cause, woke ratings, before-sleep ratings
      val storedDays = capturedDates.sorted().map { date ->
        val record = allRecords.find { it.dateIso == date }?.toDomain() ?: dayRecordsByDate[date]
        val blocks = outcomesByDate[date]?.map { it.toDomain() } ?: blockOutcomesByDate[date] ?: emptyList()
        InsightsDraftService.StoredDayData(
          dateIso = date,
          blocks = blocks,
          record = record
        )
      }

      val currentFingerprint = computeMarksFingerprint(storedDays)
      if (currentFingerprint == lastDraftFingerprint && _tapeSynthesis.value != null) {
        // Marks have not changed since last draft; do not start another call
        return
      }

      val result = InsightsDraftService.generateSilentDraft(storedDays)
      if (result != null) {
        lastDraftFingerprint = currentFingerprint
        // Save the JSON against this user
        dao.upsertInsightsDraft(
          InsightsDraftEntity(
            id = "current_draft",
            rawJson = result.rawJson,
            fingerprint = currentFingerprint,
            updatedAt = System.currentTimeMillis()
          )
        )
        _tapeSynthesis.value = result.synthesis
        _suggestedExperiment.value = result.suggestedExperiment
      }
    } catch (t: Throwable) {
      Log.w("RoutineRepository", "Silent insights draft call completed safely", t)
    }
  }

  private fun parseAndApplyDraftJson(rawJson: String) {
    try {
      val obj = org.json.JSONObject(rawJson)
      val playback = obj.optString("playback", "")
      val chainsList = mutableListOf<BreakpointChain>()
      val chainJson = obj.opt("chain")
      if (chainJson is org.json.JSONArray) {
        for (i in 0 until chainJson.length()) {
          val cObj = chainJson.optJSONObject(i) ?: continue
          chainsList.add(
            BreakpointChain(
              id = "chain_${i + 1}",
              title = cObj.optString("title", "Observed Friction Chain"),
              trigger = cObj.optString("trigger", ""),
              mechanism = cObj.optString("mechanism", ""),
              downstreamImpact = cObj.optString("downstreamImpact", ""),
              frequencyNote = cObj.optString("frequencyNote", "")
            )
          )
        }
      } else if (chainJson is org.json.JSONObject) {
        chainsList.add(
          BreakpointChain(
            id = "chain_1",
            title = chainJson.optString("title", "Observed Friction Chain"),
            trigger = chainJson.optString("trigger", ""),
            mechanism = chainJson.optString("mechanism", ""),
            downstreamImpact = chainJson.optString("downstreamImpact", ""),
            frequencyNote = chainJson.optString("frequencyNote", "")
          )
        )
      }

      var suggestion: ExperimentSuggestion? = null
      val expObj = obj.optJSONObject("experiment")
      if (expObj != null) {
        val title = expObj.optString("title", "").trim()
        val intervention = expObj.optString("singleIntervention", "").trim()
        if (title.isNotBlank() && intervention.isNotBlank()) {
          suggestion = ExperimentSuggestion(
            id = "sug_draft",
            number = (_pastExperiments.value.maxOfOrNull { it.number } ?: 0) + 1,
            title = title,
            hypothesis = expObj.optString("hypothesis", "Testing single variable to address friction."),
            singleIntervention = intervention,
            metricToWatch = expObj.optString("metricToWatch", "Routine completion"),
            targetDurationDays = 7
          )
        }
      }

      _tapeSynthesis.value = TapeSynthesis(
        playbackNote = playback,
        daysAnalyzed = capturedDateIsos.size,
        capturedDaysCount = capturedDateIsos.size,
        totalBreakpointsCount = chainsList.size,
        chains = chainsList,
        oneSuggestedExperiment = suggestion
      )
      _suggestedExperiment.value = suggestion
    } catch (e: Throwable) {
      Log.w("RoutineRepository", "Failed to parse saved draft JSON", e)
    }
  }

  private fun computeMarksFingerprint(days: List<InsightsDraftService.StoredDayData>): String {
    val sb = StringBuilder()
    for (day in days) {
      sb.append(day.dateIso).append(";")
      day.record?.let {
        sb.append(it.wakeEnergy).append(",").append(it.wakeMood).append(",").append(it.wakeStress).append(";")
        sb.append(it.sleepEnergy).append(",").append(it.sleepMood).append(",").append(it.sleepStress).append(";")
      }
      for (b in day.blocks) {
        sb.append(b.id).append("=").append(b.plannedTime).append("=").append(b.status.name).append("=").append(b.cause ?: "").append(";")
      }
      sb.append("|")
    }
    return try {
      val md = java.security.MessageDigest.getInstance("SHA-256")
      val digest = md.digest(sb.toString().toByteArray(Charsets.UTF_8))
      digest.fold("") { str, it -> str + "%02x".format(it) }
    } catch (_: Exception) {
      sb.toString().hashCode().toString()
    }
  }

  fun keepSuggestedExperiment(suggestion: ExperimentSuggestion) {
    val kept = Experiment(
      id = "exp_${suggestion.number}",
      number = suggestion.number,
      title = suggestion.title,
      hypothesis = suggestion.hypothesis,
      singleIntervention = suggestion.singleIntervention,
      metricToWatch = suggestion.metricToWatch,
      status = ExperimentStatus.KEPT,
      dayCount = 0,
      totalDays = suggestion.targetDurationDays,
      observationNotes = "Kept intervention: ${suggestion.singleIntervention}"
    )
    _pastExperiments.update { listOf(kept) + it.filterNot { exp -> exp.id == kept.id } }
    _suggestedExperiment.value = null
  }

  fun modifySuggestedExperiment(
    suggestion: ExperimentSuggestion,
    newIntervention: String,
    notes: String
  ) {
    val modified = Experiment(
      id = "exp_${suggestion.number}",
      number = suggestion.number,
      title = suggestion.title,
      hypothesis = suggestion.hypothesis,
      singleIntervention = newIntervention.ifBlank { suggestion.singleIntervention },
      metricToWatch = suggestion.metricToWatch,
      status = ExperimentStatus.MODIFIED,
      dayCount = 0,
      totalDays = suggestion.targetDurationDays,
      observationNotes = notes.ifBlank { "Modified hypothesis/intervention." }
    )
    _pastExperiments.update { listOf(modified) + it.filterNot { exp -> exp.id == modified.id } }
    _suggestedExperiment.value = null
  }

  fun abandonSuggestedExperiment(suggestion: ExperimentSuggestion, notes: String) {
    val abandoned = Experiment(
      id = "exp_${suggestion.number}",
      number = suggestion.number,
      title = suggestion.title,
      hypothesis = suggestion.hypothesis,
      singleIntervention = suggestion.singleIntervention,
      metricToWatch = suggestion.metricToWatch,
      status = ExperimentStatus.ABANDONED,
      dayCount = 0,
      totalDays = suggestion.targetDurationDays,
      observationNotes = notes.ifBlank { "Abandoned suggestion." }
    )
    _pastExperiments.update { listOf(abandoned) + it.filterNot { exp -> exp.id == abandoned.id } }
    _suggestedExperiment.value = null
  }

  companion object {
    val instance by lazy { RoutineRepository() }
  }
}

fun DayBlock.toEntity(dateIso: String): BlockOutcomeEntity {
  return BlockOutcomeEntity(
    id = "${dateIso}_${this.id}",
    dateIso = dateIso,
    blockId = this.id,
    title = this.title,
    intendedTime = this.intendedTime,
    anchorType = this.anchorType.name,
    status = this.status.name,
    stampedTime = this.stampedTime,
    cause = this.cause,
    note = this.note,
    plannedTime = this.plannedTime,
    remindMe = this.remindMe
  )
}

fun BlockOutcomeEntity.toDomain(): DayBlock {
  return DayBlock(
    id = this.blockId,
    title = this.title,
    intendedTime = this.intendedTime,
    anchorType = try { AnchorType.valueOf(this.anchorType) } catch (_: Exception) { AnchorType.FLEXIBLE },
    status = try { BlockStatus.valueOf(this.status) } catch (_: Exception) { BlockStatus.PENDING },
    stampedTime = this.stampedTime,
    cause = this.cause,
    note = this.note,
    dateIso = this.dateIso,
    plannedTime = this.plannedTime.ifBlank { this.intendedTime },
    remindMe = this.remindMe
  )
}
