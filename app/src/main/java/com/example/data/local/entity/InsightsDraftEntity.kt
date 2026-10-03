package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to persist the silent insights draft JSON locally against the user.
 */
@Entity(tableName = "insights_draft")
data class InsightsDraftEntity(
  @PrimaryKey val id: String = "current_draft",
  val rawJson: String,
  val fingerprint: String,
  val updatedAt: Long = System.currentTimeMillis()
)
