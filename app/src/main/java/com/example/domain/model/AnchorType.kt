package com.example.domain.model

/**
 * Anchor types defining block rigidity and routine architecture.
 *
 * Real conditions:
 * - Spine is wake and sleep.
 * - Anchors can be Hard, Flexible, or Optional.
 */
enum class AnchorType(val displayName: String, val isSpine: Boolean = false) {
  SPINE_WAKE("Wake Spine", isSpine = true),
  SPINE_SLEEP("Sleep Spine", isSpine = true),
  HARD("Hard Anchor"),
  FLEXIBLE("Flexible Anchor"),
  OPTIONAL("Optional Anchor")
}
