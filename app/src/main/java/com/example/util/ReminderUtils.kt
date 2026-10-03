package com.example.util

import com.example.domain.model.AnchorType
import com.example.domain.model.DayBlock

object ReminderUtils {

  /**
   * Generates a quiet single-line question based on the block name/type.
   * Rules:
   * - One quiet line, a question.
   * - Spine blocks: Wake -> "How did you wake?", Sleep -> "Winding down?" or "Time to wind down?"
   * - Common blocks: Breakfast -> "Did you eat breakfast?", Training -> "How did training go?"
   * - General: "How did [title] go?" or "Did you [verb]...?"
   * - Prohibited words: "missed", "overdue", "streak", "don't forget", "dont forget", "failed", "you failed"
   */
  fun generateReminderQuestion(block: DayBlock): String {
    return generateReminderQuestion(block.title, block.anchorType)
  }

  fun generateReminderQuestion(title: String, anchorType: AnchorType? = null): String {
    val cleanTitle = title.trim()
    val lower = cleanTitle.lowercase()

    if (anchorType == AnchorType.SPINE_WAKE || lower.startsWith("wake") || lower == "wake") {
      return "How did you wake?"
    }
    if (anchorType == AnchorType.SPINE_SLEEP || lower.startsWith("sleep") || lower == "sleep" || lower.contains("wind down")) {
      return "Winding down?"
    }
    if (lower.contains("breakfast")) {
      return "Did you eat breakfast?"
    }
    if (lower.contains("lunch") || lower.contains("midday meal") || lower.contains("noon meal")) {
      return "Did you have lunch?"
    }
    if (lower.contains("dinner") || lower.contains("evening meal")) {
      return "Did you have dinner?"
    }
    if (lower.contains("training") || lower.contains("workout") || lower.contains("exercise") || lower.contains("gym")) {
      return "How did training go?"
    }
    if (lower.contains("deep work") || lower.contains("execution")) {
      return "How did deep work go?"
    }
    if (lower.contains("walk")) {
      return "Did you take your walk?"
    }
    if (lower.contains("read")) {
      return "Time for reading?"
    }

    // Default polite quiet question without any urgency or forbidden words
    return "How did $cleanTitle go?"
  }
}
