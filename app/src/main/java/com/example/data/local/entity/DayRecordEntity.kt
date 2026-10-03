package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a day record.
 * Mandatory field: dateIso (yyyy-MM-dd).
 */
@Entity(tableName = "day_records")
data class DayRecordEntity(
  @PrimaryKey val dateIso: String, // yyyy-MM-dd
  val isCaptured: Boolean = false,
  val isClosed: Boolean = false,
  val observedCount: Int = 0,
  val totalCount: Int = 8,
  val playbackNote: String = "",
  val wakeEnergy: Int? = null,
  val wakeMood: Int? = null,
  val wakeStress: Int? = null,
  val sleepEnergy: Int? = null,
  val sleepMood: Int? = null,
  val sleepStress: Int? = null
)
