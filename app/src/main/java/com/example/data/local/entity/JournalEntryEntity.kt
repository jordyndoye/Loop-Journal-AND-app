package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a night close journal entry.
 * Mandatory field: dateIso (yyyy-MM-dd).
 */
@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
  @PrimaryKey val dateIso: String, // yyyy-MM-dd
  val wentToPlan: String = "",
  val didNotGoToPlan: String = "",
  val inTheWay: String = "",
  val energyRating: Float = 3.0f,
  val stressRating: Float = 2.0f,
  val isClosed: Boolean = false,
  val closedAtTime: String? = null // HH:mm 24-hour
)
