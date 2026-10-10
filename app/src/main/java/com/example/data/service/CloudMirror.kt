package com.example.data.service

import android.util.Log
import com.example.domain.model.DayBlock
import com.example.domain.model.DayRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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
      .set(
        mapOf(
          "email" to (user.email ?: ""),
          "updatedAt" to System.currentTimeMillis()
        ),
        SetOptions.merge()
      )
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
    firestore.collection("users").document(id)
      .collection("system").document("board")
      .set(mapOf("blocks" to payload, "updatedAt" to System.currentTimeMillis()))
      .addOnFailureListener { Log.w(TAG, "system write failed", it) }
  }

  fun saveBlock(block: DayBlock, dateIso: String) {
    val id = uid() ?: return
    val firestore = db() ?: return
    firestore.collection("users").document(id)
      .collection("days").document(dateIso)
      .collection("blocks").document(block.id)
      .set(
        mapOf(
          "title" to block.title,
          "plannedTime" to block.plannedTime,
          "outcome" to block.status.name,
          "cause" to block.cause,
          "note" to block.note,
          "stampedTime" to block.stampedTime
        ),
        SetOptions.merge()
      )
      .addOnFailureListener { Log.w(TAG, "block write failed", it) }
  }

  fun saveDay(record: DayRecord) {
    val id = uid() ?: return
    val firestore = db() ?: return
    firestore.collection("users").document(id)
      .collection("days").document(record.dateIso)
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
}
