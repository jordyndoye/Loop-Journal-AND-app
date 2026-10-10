package com.example

import com.example.data.local.entity.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class JournalEntryTest {

  @Test
  fun testJournalEntryEntityFields() {
    val entry = JournalEntry(
      date = "2026-10-03",
      reflection = "Productive morning, kept deep focus without interruption.",
      breakPoints = "Evening shutdown drifted due to urgent request."
    )

    assertEquals("2026-10-03", entry.date)
    assertEquals("2026-10-03", entry.dateIso)
    assertEquals("Productive morning, kept deep focus without interruption.", entry.reflection)
    assertEquals("Evening shutdown drifted due to urgent request.", entry.breakPoints)
    assertFalse(entry.isClosed)
  }

  @Test
  fun testJournalEntryBackwardCompatibilityConstructor() {
    val entry = JournalEntry(
      dateIso = "2026-10-04",
      wentToPlan = "Wake anchor on time.",
      inTheWay = "Call delayed lunch."
    )

    assertEquals("2026-10-04", entry.date)
    assertEquals("2026-10-04", entry.dateIso)
    assertEquals("Wake anchor on time.", entry.reflection)
    assertEquals("Call delayed lunch.", entry.breakPoints)
  }
}
