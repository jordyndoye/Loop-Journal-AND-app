package com.example.util

import com.example.ui.components.ReelDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * System Clock & Date Engine
 *
 * Rules:
 * - Use java.time with ZoneId.systemDefault().
 * - Store dates only as yyyy-MM-dd. Never store a display string as the date.
 * - Store times as HH:mm, 24-hour.
 * - Show the date in the phone locale. Keep the saved value canonical.
 * - Today is LocalDate.now(ZoneId.systemDefault()). After midnight, Today must become the new day.
 * - The week reel is Monday to Sunday of the current week in the phone timezone. Mark today. Fill a segment only if that yyyy-MM-dd was captured.
 */
object DateTimeUtils {

  val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE // "yyyy-MM-dd"
  val TIME_24H_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  /** Current zone from phone clock */
  fun currentZone(): ZoneId = ZoneId.systemDefault()

  /** Today's dateIso (yyyy-MM-dd) based on phone clock and current zone */
  fun todayIso(zone: ZoneId = currentZone()): String {
    return LocalDate.now(zone).format(ISO_DATE_FORMATTER)
  }

  /** Current time in HH:mm 24-hour format */
  fun currentTime24(zone: ZoneId = currentZone()): String {
    return LocalTime.now(zone).format(TIME_24H_FORMATTER)
  }

  /**
   * Format a canonical yyyy-MM-dd date into the phone locale display string.
   * Example: "Friday, Oct 2" in en-US.
   */
  fun formatHeaderDate(
    dateIso: String,
    locale: Locale = Locale.getDefault()
  ): String {
    return try {
      val localDate = LocalDate.parse(dateIso, ISO_DATE_FORMATTER)
      val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", locale)
      localDate.format(formatter)
    } catch (_: Exception) {
      dateIso
    }
  }

  /**
   * Short day of week abbreviation in phone locale (e.g. "FRI")
   */
  fun formatDayOfWeekShort(
    dateIso: String,
    locale: Locale = Locale.getDefault()
  ): String {
    return try {
      val localDate = LocalDate.parse(dateIso, ISO_DATE_FORMATTER)
      localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).uppercase(locale)
    } catch (_: Exception) {
      ""
    }
  }

  /**
   * Constructs the week reel: Monday to Sunday of the current week in the phone timezone.
   * Marks today.
   * Fills a segment (isCaptured = true) ONLY if that yyyy-MM-dd was captured.
   */
  fun buildCurrentWeekReel(
    todayIso: String,
    capturedDateIsos: Set<String>,
    missedDateIsos: Set<String>,
    zone: ZoneId = currentZone(),
    locale: Locale = Locale.getDefault()
  ): List<ReelDay> {
    val today = try {
      LocalDate.parse(todayIso, ISO_DATE_FORMATTER)
    } catch (_: Exception) {
      LocalDate.now(zone)
    }

    // Monday of the current week
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    return (0..6).map { offset ->
      val date = monday.plusDays(offset.toLong())
      val dateIso = date.format(ISO_DATE_FORMATTER)
      val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale) // "M", "T", "W", "T", "F", "S", "S"
      val dateNumber = date.dayOfMonth
      val isToday = (date == today)
      val isCaptured = capturedDateIsos.contains(dateIso)
      val hasMissed = missedDateIsos.contains(dateIso)

      ReelDay(
        dayLabel = dayLabel,
        dateNumber = dateNumber,
        isCaptured = isCaptured,
        isToday = isToday,
        hasMissedBlocks = hasMissed,
        dateIso = dateIso
      )
    }
  }

  /**
   * Returns true if time1 is chronologically strictly before time2 (24-hour format HH:mm).
   */
  fun isTimeBefore(time1: String, time2: String): Boolean {
    return try {
      val t1 = LocalTime.parse(time1.trim(), TIME_24H_FORMATTER)
      val t2 = LocalTime.parse(time2.trim(), TIME_24H_FORMATTER)
      t1.isBefore(t2)
    } catch (_: Exception) {
      time1.trim() < time2.trim()
    }
  }

  /**
   * Determines which block is currently active based on the phone's 24-hour clock.
   * Block times like "06:30", "07:15", "09:00" stay the times set in My System.
   * Clock is used only to highlight the current block.
   */
  fun findCurrentBlockId(
    blocksWithTimes: List<Pair<String, String>>, // list of (blockId, intendedTime)
    nowTimeStr: String = currentTime24()
  ): String? {
    if (blocksWithTimes.isEmpty()) return null
    val sorted = blocksWithTimes.sortedBy { it.second }
    // The current block is the latest block whose scheduled time <= nowTimeStr
    val active = sorted.lastOrNull { it.second <= nowTimeStr } ?: sorted.first()
    return active.first
  }
}
