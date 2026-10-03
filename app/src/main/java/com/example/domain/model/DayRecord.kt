package com.example.domain.model

import com.example.util.DateTimeUtils

/**
 * Representation of a day's tape in the Loop Journal.
 * Rule: Store dates only as yyyy-MM-dd. Never store a display string as the date.
 * Show the date in the phone locale. Keep the saved value canonical.
 */
data class DayRecord(
  val id: String = "",
  val dateIso: String, // Canonical yyyy-MM-dd
  val isCaptured: Boolean = false,
  val isClosed: Boolean = false,
  val energyRating: Float = 0f, // 1 to 5 (legacy)
  val stressRating: Float = 0f, // 1 to 5 (legacy)
  val wentToPlan: String = "",
  val didNotGoToPlan: String = "",
  val inTheWay: String = "",
  val playbackNote: String = "",
  val observedCount: Int = 0,
  val totalCount: Int = 8,
  val wakeEnergy: Int? = null,
  val wakeMood: Int? = null,
  val wakeStress: Int? = null,
  val sleepEnergy: Int? = null,
  val sleepMood: Int? = null,
  val sleepStress: Int? = null
) {
  val hasWakeReadings: Boolean
    get() = wakeEnergy != null || wakeMood != null || wakeStress != null

  val hasSleepReadings: Boolean
    get() = sleepEnergy != null || sleepMood != null || sleepStress != null

  val formattedDate: String
    get() = DateTimeUtils.formatHeaderDate(dateIso)

  val dayOfWeek: String
    get() = DateTimeUtils.formatDayOfWeekShort(dateIso)
}
