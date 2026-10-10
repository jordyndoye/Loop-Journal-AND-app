package com.example.data.service

import android.util.Log
import com.example.data.local.dao.DayDao
import com.example.data.local.entity.BlockOutcomeEntity
import com.example.data.local.entity.DayRecordEntity
import com.example.data.local.entity.SystemBlockEntity
import com.example.domain.model.DayBlock
import com.example.domain.model.DayRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object CloudMirror {
  private const val TAG = "CloudMirror"

  private fun db(): FirebaseFirestore? = try {
    if (FirebaseAuth.getInstance().currentUser == null) null else FirebaseFirestore.getInstance()
  } catch (t: Throwable) {
    Log.w(TAG, "Firestore unavailable", t)
    null
  }

  private fun uid(): String? = FirebaseAuth.getInstance().currentUser?.uid

  fun saveProfile() {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val firestore = db() ?: return
    firestore.collection("users").document(user.uid)
      .set(mapOf("email" to (user.email ?: ""), "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
      .addOnFailureListener { Log.w(TAG, "profile write failed", it) }
  }

  fun saveSystem(blocks: List<DayBlock>) {
    val id = uid() ?: return
    val firestore = db() ?: return
    val payload = blocks.mapIndexed { index, block ->
      mapOf(
        "id" to block.id,
        "title" to block.title,
        "intendedTime" to block.intendedTime,
        "anchorType" to block.anchorType.name,
        "sortOrder" to index,
        "note" to block.note,
        "remindMe" to block.remindMe
      )
    }
    firestore.collection("users").document(id).collection("system").document("board")
      .set(mapOf("blocks" to payload, "updatedAt" to System.currentTimeMillis()))
      .addOnFailureListener { Log.w(TAG, "system write failed", it) }
  }

  fun saveBlock(block: DayBlock, dateIso: String) {
    val id = uid() ?: return
    val firestore = db() ?: return
    firestore.collection("users").document(id).collection("days").document(dateIso)
      .collection("blocks").document(block.id)
      .set(
        mapOf(
          "title" to block.title,
          "intendedTime" to block.intendedTime,
          "plannedTime" to block.plannedTime,
          "anchorType" to block.anchorType.name,
          "outcome" to block.status.name,
          "cause" to block.cause,
          "note" to block.note,
          "stampedTime" to block.stampedTime,
          "remindMe" to block.remindMe
        ),
        SetOptions.merge()
      )
      .addOnFailureListener { Log.w(TAG, "block write failed", it) }
  }

  fun saveDay(record: DayRecord) {
    val id = uid() ?: return
    val firestore = db() ?: return
    firestore.collection("users").document(id).collection("days").document(record.dateIso)
      .set(
        mapOf(
          "isCaptured" to record.isCaptured,
          "isClosed" to record.isClosed,
          "playbackNote" to record.playbackNote,
          "wakeEnergy" to record.wakeEnergy,
          "wakeMood" to record.wakeMood,
          "wakeStress" to record.wakeStress,
          "sleepEnergy" to record.sleepEnergy,
          "sleepMood" to record.sleepMood,
          "sleepStress" to record.sleepStress
        ),
        SetOptions.merge()
      )
      .addOnFailureListener { Log.w(TAG, "day write failed", it) }
  }

  suspend fun restoreInto(dao: DayDao) {
    val id = uid() ?: return
    val firestore = db() ?: return
    try {
      val board = firestore.collection("users").document(id)
        .collection("system").document("board").get().await()
      val rawBlocks = board.get("blocks") as? List<*>
      if (!rawBlocks.isNullOrEmpty()) {
        val entities = rawBlocks.mapIndexedNotNull { index, item ->
          val map = item as? Map<*, *> ?: return@mapIndexedNotNull null
          val blockId = map["id"] as? String ?: return@mapIndexedNotNull null
          SystemBlockEntity(
            id = blockId,
            title = map["title"] as? String ?: "Block",
            intendedTime = map["intendedTime"] as? String ?: "00:00",
            anchorType = map["anchorType"] as? String ?: "FLEXIBLE",
            sortOrder = (map["sortOrder"] as? Number)?.toInt() ?: index,
            note = map["note"] as? String,
            remindMe = map["remindMe"] as? Boolean ?: true
          )
        }
        if (entities.isNotEmpty()) dao.upsertSystemBlocks(entities)
      }

      val days = firestore.collection("users").document(id).collection("days").get().await()
      for (day in days.documents) {
        val dateIso = day.id
        dao.upsertDayRecord(
          DayRecordEntity(
            dateIso = dateIso,
            isCaptured = day.getBoolean("isCaptured") ?: false,
            isClosed = day.getBoolean("isClosed") ?: false,
            playbackNote = day.getString("playbackNote") ?: "",
            wakeEnergy = day.getLong("wakeEnergy")?.toInt(),
            wakeMood = day.getLong("wakeMood")?.toInt(),
            wakeStress = day.getLong("wakeStress")?.toInt(),
            sleepEnergy = day.getLong("sleepEnergy")?.toInt(),
            sleepMood = day.getLong("sleepMood")?.toInt(),
            sleepStress = day.getLong("sleepStress")?.toInt()
          )
        )
        val blocks = day.reference.collection("blocks").get().await()
        val outcomes = blocks.documents.map { block ->
          val planned = block.getString("plannedTime") ?: block.getString("intendedTime") ?: "00:00"
          BlockOutcomeEntity(
            id = "${dateIso}_${block.id}",
            dateIso = dateIso,
            blockId = block.id,
            title = block.getString("title") ?: "Block",
            intendedTime = block.getString("intendedTime") ?: planned,
            anchorType = block.getString("anchorType") ?: "FLEXIBLE",
            status = block.getString("outcome") ?: "PENDING",
            stampedTime = block.getString("stampedTime"),
            cause = block.getString("cause"),
            note = block.getString("note"),
            plannedTime = planned,
            remindMe = block.getBoolean("remindMe") ?: true
          )
        }
        if (outcomes.isNotEmpty()) dao.upsertBlockOutcomes(outcomes)
      }
    } catch (t: Throwable) {
      Log.w(TAG, "Restore failed; keeping local copy", t)
    }
  }
}
