package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Room database entity for JournalEntry to store daily observations,
 * including fields for date, reflection, and identified 'break' points.
 */
@Entity(tableName = "journal_entries")
data class JournalEntry(
  @PrimaryKey
  @ColumnInfo(name = "dateIso")
  val date: String, // Canonical date (yyyy-MM-dd)

  @ColumnInfo(name = "reflection")
  val reflection: String = "", // Daily observations and reflections

  @ColumnInfo(name = "break_points")
  val breakPoints: String = "", // Identified 'break' points or friction triggers

  @ColumnInfo(name = "wentToPlan")
  val wentToPlan: String = reflection,

  @ColumnInfo(name = "didNotGoToPlan")
  val didNotGoToPlan: String = "",

  @ColumnInfo(name = "inTheWay")
  val inTheWay: String = breakPoints,

  @ColumnInfo(name = "energyRating")
  val energyRating: Float = 3.0f,

  @ColumnInfo(name = "stressRating")
  val stressRating: Float = 2.0f,

  @ColumnInfo(name = "isClosed")
  val isClosed: Boolean = false,

  @ColumnInfo(name = "closedAtTime")
  val closedAtTime: String? = null // HH:mm 24-hour
) {
  val dateIso: String
    get() = date

  @Ignore
  constructor(
    dateIso: String,
    wentToPlan: String = "",
    didNotGoToPlan: String = "",
    inTheWay: String = "",
    energyRating: Float = 3.0f,
    stressRating: Float = 2.0f,
    isClosed: Boolean = false,
    closedAtTime: String? = null,
    reflection: String = wentToPlan,
    breakPoints: String = inTheWay
  ) : this(
    date = dateIso,
    reflection = reflection.ifBlank { wentToPlan },
    breakPoints = breakPoints.ifBlank { inTheWay },
    wentToPlan = wentToPlan.ifBlank { reflection },
    didNotGoToPlan = didNotGoToPlan,
    inTheWay = inTheWay.ifBlank { breakPoints },
    energyRating = energyRating,
    stressRating = stressRating,
    isClosed = isClosed,
    closedAtTime = closedAtTime
  )
}
