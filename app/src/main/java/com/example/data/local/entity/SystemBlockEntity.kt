package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for routine template blocks configured in My System.
 * Stores times like 06:30, 07:15, 09:00 as 24-hour HH:mm.
 */
@Entity(tableName = "system_blocks")
data class SystemBlockEntity(
  @PrimaryKey val id: String,
  val title: String,
  val intendedTime: String, // HH:mm 24-hour
  val anchorType: String,
  val sortOrder: Int,
  val note: String? = null,
  val remindMe: Boolean = true
)
