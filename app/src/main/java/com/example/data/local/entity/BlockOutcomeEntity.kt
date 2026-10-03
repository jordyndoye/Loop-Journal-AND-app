package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a block outcome.
 * Mandatory fields: dateIso (yyyy-MM-dd), intendedTime (HH:mm), stampedTime (HH:mm).
 */
@Entity(tableName = "block_outcomes")
data class BlockOutcomeEntity(
  @PrimaryKey val id: String, // e.g. "${dateIso}_${blockId}"
  val dateIso: String, // yyyy-MM-dd
  val blockId: String,
  val title: String,
  val intendedTime: String, // HH:mm 24-hour
  val anchorType: String,
  val status: String, // PENDING, DONE, MODIFIED, MISSED, SKIP
  val stampedTime: String? = null, // HH:mm 24-hour stamped when tapped
  val cause: String? = null,
  val note: String? = null,
  val plannedTime: String = intendedTime,
  val remindMe: Boolean = true
)
