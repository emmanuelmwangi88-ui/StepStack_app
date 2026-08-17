package com.stepstack.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stepstack.app.MainActivity
import com.stepstack.app.R

/**
 * Notification categories, each with its own channel so users can mute
 * (e.g.) daily reminders in system settings while keeping milestones on.
 */
enum class NotificationCategory(
    val channelId: String,
    val channelName: String,
    val channelDescription: String,
    val importance: Int
) {
    REMINDER(
        "stepstack_reminders",
        "Daily Reminders",
        "Nudges to get moving each day",
        NotificationManager.IMPORTANCE_LOW
    ),
    MILESTONE(
        "stepstack_milestones",
        "Streaks & Achievements",
        "Streak milestones and unlocked achievements",
        NotificationManager.IMPORTANCE_HIGH
    ),
    SOCIAL(
        "stepstack_social",
        "Grace Days & Comebacks",
        "Grace day notices and comeback nudges",
        NotificationManager.IMPORTANCE_DEFAULT
    ),
    GENERAL(
        "stepstack_general",
        "General",
        "Other app notifications",
        NotificationManager.IMPORTANCE_DEFAULT
    );
}

/**
 * Maps each business "type" string to a channel/category and a stable
 * notification ID base, so repeat notifications of the same type REPLACE
 * the previous one instead of stacking indefinitely.
 */
private fun categoryFor(type: String): NotificationCategory = when (type) {
    "reminder" -> NotificationCategory.REMINDER
    "milestone", "achievement" -> NotificationCategory.MILESTONE
    "grace", "comeback" -> NotificationCategory.SOCIAL
    else -> NotificationCategory.GENERAL
}

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = NotificationCategory.values().map { cat ->
                NotificationChannel(cat.channelId, cat.channelName, cat.importance).apply {
                    description = cat.channelDescription
                }
            }
            notificationManager.createNotificationChannels(channels)
        }
    }

    /**
     * @param type business type string (e.g. "reminder", "milestone") — determines channel
     * @param stackable if true, each call gets a unique ID (good for achievements you
     *   want a history of); if false, subsequent calls REPLACE the previous notification
     *   of this type (good for daily reminders so they don't pile up)
     */
    fun showNotification(
        id: String,
        title: String,
        message: String,
        type: String,
        stackable: Boolean = false
    ) {
        // Respect the user's system-level notification permission/opt-out
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val category = categoryFor(type)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
            putExtra("notification_id", id)
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            id.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, category.channelId)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priorityFor(category))
            .setCategory(categoryHintFor(type))
            .setContentIntent(pendingContentIntent)
            .setAutoCancel(true)
            .setColor(0xFF4CAF50.toInt()) // Primary Green
            .setGroup(category.channelId)

        // Only reminders get a snooze action — snoozing an achievement doesn't make sense
        if (type == "reminder") {
            builder.addAction(
                0,
                "Snooze",
                snoozePendingIntent(id, type)
            )
        }

        val notificationId = if (stackable) id.hashCode() else type.hashCode()
        notificationManager.notify(notificationId, builder.build())

        maybePostGroupSummary(category)
    }

    private fun priorityFor(category: NotificationCategory) = when (category) {
        NotificationCategory.MILESTONE -> NotificationCompat.PRIORITY_HIGH
        NotificationCategory.REMINDER -> NotificationCompat.PRIORITY_LOW
        else -> NotificationCompat.PRIORITY_DEFAULT
    }

    private fun categoryHintFor(type: String) = when (type) {
        "reminder" -> NotificationCompat.CATEGORY_REMINDER
        "milestone", "achievement" -> NotificationCompat.CATEGORY_STATUS
        else -> NotificationCompat.CATEGORY_SOCIAL
    }

    private fun snoozePendingIntent(id: String, type: String): PendingIntent {
        val intent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra("notification_id", id)
            putExtra("notification_type", type)
        }
        return PendingIntent.getBroadcast(
            context,
            "$id-snooze".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Android groups notifications from the same app automatically on newer
     * versions, but posting an explicit summary keeps behavior consistent
     * across OS versions once 2+ notifications share a group.
     */
    private fun maybePostGroupSummary(category: NotificationCategory) {
        val summary = NotificationCompat.Builder(context, category.channelId)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setStyle(NotificationCompat.InboxStyle().setSummaryText(category.channelName))
            .setGroup(category.channelId)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(category.channelId.hashCode(), summary)
    }

    fun cancel(type: String) {
        notificationManager.cancel(type.hashCode())
    }
}

/**
 * Handles the inline "Snooze" action tapped directly from the notification
 * shade, without opening the app. Register this in AndroidManifest.xml.
 */
class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("notification_id") ?: return
        val type = intent.getStringExtra("notification_type") ?: return

        // Dismiss the notification that was snoozed
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(type.hashCode())

        // Persist the snooze via your existing SnoozeStore/ViewModel-backed
        // storage here (e.g. through a small repository singleton), since a
        // BroadcastReceiver has no direct access to an in-memory AppViewModel.
    }
}