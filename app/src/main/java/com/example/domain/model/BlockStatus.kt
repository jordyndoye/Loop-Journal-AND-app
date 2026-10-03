package com.example.domain.model

/**
 * The status of a routine block on the board.
 *
 * Visual Rules:
 * - DONE: Captured (Amber / Paper indicator)
 * - MODIFIED: Adjusted block execution
 * - MISSED: Empty graphite ring, NEVER red
 * - SKIP: Planned intentional skip
 * - PENDING: Ahead on the tape
 */
enum class BlockStatus(val label: String) {
  PENDING("Pending"),
  DONE("Done"),
  MODIFIED("Modified"),
  MISSED("Missed"),
  SKIP("Skip")
}
