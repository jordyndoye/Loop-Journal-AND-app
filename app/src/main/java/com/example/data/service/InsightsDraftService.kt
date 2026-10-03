package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import com.example.domain.model.BreakpointChain
import com.example.domain.model.DayBlock
import com.example.domain.model.DayRecord
import com.example.domain.model.ExperimentSuggestion
import com.example.domain.model.TapeSynthesis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Calls the Firebase function. The Gemini key stays in the Firebase project.
 */
object InsightsDraftService {

  private const val TAG = "InsightsDraftService"

  private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .build()
  }

  data class StoredDayData(
    val dateIso: String,
    val blocks: List<DayBlock>,
    val record: DayRecord?
  )

  data class DraftResult(
    val rawJson: String,
    val synthesis: TapeSynthesis,
    val suggestedExperiment: ExperimentSuggestion?
  )

  suspend fun generateSilentDraft(days: List<StoredDayData>): DraftResult? = withContext(Dispatchers.IO) {
    try {
      val functionUrl = try {
        BuildConfig.INSIGHTS_FUNCTION_URL
      } catch (_: Throwable) {
        ""
      }
      if (functionUrl.isBlank()) {
        Log.i(TAG, "Insights function URL is not set; keeping existing draft.")
        return@withContext null
      }

      val storedDays = JSONArray()
      for (day in days) {
        val dayObj = JSONObject()
        dayObj.put("date", day.dateIso)
        val blocksArray = JSONArray()
        for (block in day.blocks) {
          val blockObj = JSONObject()
          blockObj.put("block", block.title)
          blockObj.put("planned_time", block.plannedTime)
          blockObj.put("outcome", block.status.name)
          if (!block.cause.isNullOrBlank()) blockObj.put("cause", block.cause)
          blocksArray.put(blockObj)
        }
        dayObj.put("blocks", blocksArray)
        val woke = JSONObject()
        day.record?.wakeEnergy?.let { woke.put("energy", it) }
        day.record?.wakeMood?.let { woke.put("mood", it) }
        day.record?.wakeStress?.let { woke.put("stress", it) }
        if (woke.length() > 0) dayObj.put("woke_ratings", woke)
        val sleep = JSONObject()
        day.record?.sleepEnergy?.let { sleep.put("energy", it) }
        day.record?.sleepMood?.let { sleep.put("mood", it) }
        day.record?.sleepStress?.let { sleep.put("stress", it) }
        if (sleep.length() > 0) dayObj.put("before_sleep_ratings", sleep)
        storedDays.put(dayObj)
      }

      val body = JSONObject().put("days", storedDays).toString()
        .toRequestBody("application/json".toMediaType())
      val request = Request.Builder().url(functionUrl).post(body).build()
      val response = httpClient.newCall(request).execute()
      if (!response.isSuccessful) {
        Log.w(TAG, "Insights function returned ${response.code}")
        return@withContext null
      }

      val text = response.body?.string()?.trim().orEmpty()
      if (text.isBlank()) return@withContext null
      val draft = JSONObject(text)
      val playback = draft.optString("playback", "").trim()
      if (playback.isBlank()) return@withContext null

      val chains = mutableListOf<BreakpointChain>()
      val chainJson = draft.opt("chain")
      if (chainJson is JSONObject) {
        val title = chainJson.optString("title", "").trim()
        if (title.isNotBlank()) {
          chains.add(
            BreakpointChain(
              id = "chain_1",
              title = title,
              trigger = chainJson.optString("trigger", ""),
              mechanism = chainJson.optString("mechanism", ""),
              downstreamImpact = chainJson.optString("downstreamImpact", ""),
              frequencyNote = chainJson.optString("frequencyNote", "")
            )
          )
        }
      }

      var suggestion: ExperimentSuggestion? = null
      val exp = draft.optJSONObject("experiment")
      val expTitle = exp?.optString("title", "")?.trim().orEmpty()
      val intervention = exp?.optString("singleIntervention", "")?.trim().orEmpty()
      if (expTitle.isNotBlank() && intervention.isNotBlank()) {
        suggestion = ExperimentSuggestion(
          id = "sug_${System.currentTimeMillis()}",
          number = 1,
          title = expTitle,
          hypothesis = exp?.optString("hypothesis", "").orEmpty(),
          singleIntervention = intervention,
          metricToWatch = exp?.optString("metricToWatch", "").orEmpty(),
          targetDurationDays = 7
        )
      }

      DraftResult(
        rawJson = text,
        synthesis = TapeSynthesis(
          playbackNote = playback,
          daysAnalyzed = days.size,
          capturedDaysCount = days.size,
          totalBreakpointsCount = chains.size,
          chains = chains,
          oneSuggestedExperiment = suggestion
        ),
        suggestedExperiment = suggestion
      )
    } catch (t: Throwable) {
      Log.w(TAG, "Insights call failed; keeping last note", t)
      null
    }
  }
}
