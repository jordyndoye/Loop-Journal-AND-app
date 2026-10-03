package com.example.receiver

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.domain.model.BlockStatus
import com.example.domain.model.DayBlock
import com.example.util.DateTimeUtils
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Robust notification scheduling manager that verifies notification permissions
 * and block data existence before attempting to enqueue any reminders,
 * ensuring complete stability on cold starts and across all Android versions.
 */
object ReminderScheduler {

  private const val TAG = "ReminderScheduler"
  private const val MINUTES_AFTER_PLANNED = 5L // Fires a few minutes after planned time

  fun hasNotificationPermission(context: Context?): Boolean {
    if (context == null) return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      try {
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
      } catch (e: Throwable) {
        false
      }
    } else {
      true
    }
  }

  fun createNotificationChannel(context: Context?) {
    if (context == null || !hasNotificationPermission(context)) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (manager.getNotificationChannel(ReminderReceiver.CHANNEL_ID) == null) {
          val channel = NotificationChannel(
            ReminderReceiver.CHANNEL_ID,
            ReminderReceiver.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
          ).apply {
            description = "Quiet questions for unmarked routine blocks"
            enableVibration(false)
            setShowBadge(false)
          }
          manager.createNotificationChannel(channel)
        }
      }
    } catch (e: Throwable) {
      Log.w(TAG, "Failed to create notification channel", e)
    }
  }

  fun getRequestCode(blockId: String): Int {
    return blockId.hashCode() and 0x7FFFFFFF
  }

  fun isValidBlockForScheduling(block: DayBlock?, dateIso: String? = null): Boolean {
    if (block == null) return false
    if (block.id.isBlank()) return false
    if (block.plannedTime.isBlank()) return false
    if (!block.remindMe) return false
    if (block.status != BlockStatus.PENDING) return false
    val activeDate = dateIso ?: DateTimeUtils.todayIso()
    val todayIso = DateTimeUtils.todayIso()
    if (activeDate != todayIso) return false
    return true
  }

  fun scheduleBlockReminder(
    context: Context?,
    block: DayBlock?,
    dateIso: String? = null
  ) {
    if (context == null || block == null) return
    val activeDate = dateIso ?: DateTimeUtils.todayIso()

    // 1. Verify notification permission
    if (!hasNotificationPermission(context)) {
      return
    }

    // 2. Verify block validity and pending status
    if (!isValidBlockForScheduling(block, activeDate)) {
      cancelBlockReminder(context, block.id)
      return
    }

    // 3. Verify reminder time calculation
    val targetMillis = calculateReminderTimeMillis(block.plannedTime, activeDate)
    if (targetMillis == null) {
      cancelBlockReminder(context, block.id)
      return
    }

    val nowMillis = System.currentTimeMillis()
    if (targetMillis <= nowMillis) {
      // A time already passed today does not fire late.
      cancelBlockReminder(context, block.id)
      return
    }

    // 4. Safely enqueue alarm with system AlarmManager
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, ReminderReceiver::class.java).apply {
        action = "com.example.ACTION_GENTLE_REMINDER_${block.id}"
        putExtra(ReminderReceiver.EXTRA_BLOCK_ID, block.id)
        putExtra(ReminderReceiver.EXTRA_DATE_ISO, activeDate)
        putExtra(ReminderReceiver.EXTRA_BLOCK_TITLE, block.title)
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        getRequestCode(block.id),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val canExact = try {
          alarmManager.canScheduleExactAlarms()
        } catch (e: Throwable) {
          false
        }
        if (canExact) {
          alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            targetMillis,
            pendingIntent
          )
        } else {
          alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            targetMillis,
            pendingIntent
          )
        }
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          targetMillis,
          pendingIntent
        )
      } else {
        alarmManager.set(
          AlarmManager.RTC_WAKEUP,
          targetMillis,
          pendingIntent
        )
      }
      Log.d(TAG, "Scheduled reminder for ${block.title} at $targetMillis")
    } catch (e: SecurityException) {
      Log.w(TAG, "Exact alarm permission missing or denied; skipping without throwing", e)
    } catch (e: Throwable) {
      Log.w(TAG, "Error scheduling reminder; skipping without throwing", e)
    }
  }

  fun cancelBlockReminder(context: Context?, blockId: String?) {
    if (context == null || blockId.isNullOrBlank()) return
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, ReminderReceiver::class.java).apply {
        action = "com.example.ACTION_GENTLE_REMINDER_$blockId"
      }
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        getRequestCode(blockId),
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
      )
      if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Cancelled reminder for block $blockId")
      }
    } catch (e: Throwable) {
      Log.w(TAG, "Error cancelling reminder", e)
    }
  }

  fun rescheduleAll(
    context: Context?,
    blocks: List<DayBlock>?,
    dateIso: String? = null
  ) {
    if (context == null || blocks == null) return
    val activeDate = dateIso ?: DateTimeUtils.todayIso()

    if (!hasNotificationPermission(context)) {
      return
    }

    try {
      blocks.forEach { block ->
        if (isValidBlockForScheduling(block, activeDate)) {
          scheduleBlockReminder(context, block, activeDate)
        } else {
          cancelBlockReminder(context, block.id)
        }
      }
    } catch (e: Throwable) {
      Log.w(TAG, "Error in rescheduleAll", e)
    }
  }

  fun calculateReminderTimeMillis(timeHhMm: String?, dateIso: String?): Long? {
    if (timeHhMm.isNullOrBlank() || dateIso.isNullOrBlank()) return null
    try {
      val parsedDate = LocalDate.parse(dateIso.trim())
      val timeParts = timeHhMm.trim().split(":")
      if (timeParts.size < 2) return null
      val hour = timeParts[0].toIntOrNull() ?: return null
      val min = timeParts[1].toIntOrNull() ?: return null
      val localTime = LocalTime.of(hour, min)

      val plannedDateTime = ZonedDateTime.of(
        parsedDate,
        localTime,
        ZoneId.systemDefault()
      )

      // Add a few minutes (5 mins) after planned time
      val reminderDateTime = plannedDateTime.plusMinutes(MINUTES_AFTER_PLANNED)
      return reminderDateTime.toInstant().toEpochMilli()
    } catch (e: Throwable) {
      return null
    }
  }
}
