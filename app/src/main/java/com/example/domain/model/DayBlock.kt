package com.example.domain.model

/**
 * A scheduled or observed block on the routine board.
 * Reflects real day conditions: late work, partner evenings that stretch bedtime,
 * training after short sleep, meals heavy or skipped.
 */
data class DayBlock(
  val id: String,
  val title: String,
  val intendedTime: String, // HH:mm 24-hour set in My System
  val anchorType: AnchorType,
  val status: BlockStatus = BlockStatus.PENDING,
  val cause: String? = null,
  val note: String? = null,
  val actualTime: String? = null,
  val dateIso: String = "", // Canonical yyyy-MM-dd
  val stampedTime: String? = null, // HH:mm 24-hour stamped when tapped
  val plannedTime: String = intendedTime, // Today's own planned time for this date
  val remindMe: Boolean = true
)
