package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.RoutineRepository
import com.example.domain.model.BlockStatus
import com.example.util.DateTimeUtils
import com.example.util.ReminderUtils

class ReminderReceiver : BroadcastReceiver() {

  companion object {
    private const val TAG = "ReminderReceiver"
    const val CHANNEL_ID = "loop_gentle_reminders"
    const val CHANNEL_NAME = "Gentle Reminders"
    const val NOTIFICATION_ID = 1001 // Single notification at a time, never a stack
    const val EXTRA_BLOCK_ID = "extra_block_id"
    const val EXTRA_DATE_ISO = "extra_date_iso"
    const val EXTRA_BLOCK_TITLE = "extra_block_title"
  }

  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null || intent == null) return
    try {
      if (!ReminderScheduler.hasNotificationPermission(context)) {
        return
      }

      val blockId = intent.getStringExtra(EXTRA_BLOCK_ID) ?: return
      val dateIso = intent.getStringExtra(EXTRA_DATE_ISO) ?: return
      val title = intent.getStringExtra(EXTRA_BLOCK_TITLE) ?: ""

      val todayIso = DateTimeUtils.todayIso()
      if (dateIso != todayIso) {
        // Not for today, ignore
        return
      }

      val repository = RoutineRepository.instance
      val currentBlocks = repository.blocks.value
      val targetBlock = currentBlocks.find { it.id == blockId }

      // It fires once, a few minutes after the planned time, only if that block has no outcome today.
      // If the block is already Done, Modified, Missed, or Skip, no reminder.
      if (targetBlock != null && targetBlock.status != BlockStatus.PENDING) {
        return
      }

      if (targetBlock != null && !targetBlock.remindMe) {
        return
      }

      val question = if (targetBlock != null) {
        ReminderUtils.generateReminderQuestion(targetBlock)
      } else {
        ReminderUtils.generateReminderQuestion(title)
      }

      showNotification(context, blockId, question)
    } catch (e: Throwable) {
      Log.w(TAG, "Error handling reminder broadcast", e)
    }
  }

  private fun showNotification(context: Context, blockId: String, question: String) {
    try {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

      ReminderScheduler.createNotificationChannel(context)

      // Tapping the notification opens that block
      val tapIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra(EXTRA_BLOCK_ID, blockId)
      }

      val pendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        tapIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      // Gentle notification: quiet single line question, no red, no alarm sound, no urgent vibration
      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(question)
        .setContentText("Loop Journal")
        .setStyle(NotificationCompat.BigTextStyle().bigText(question))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setVibrate(null) // Soft / silent default, user's choice in system settings
        .setOnlyAlertOnce(true)
        .build()

      notificationManager.notify(NOTIFICATION_ID, notification)
    } catch (e: Throwable) {
      Log.w(TAG, "Failed to display notification", e)
    }
  }
}
