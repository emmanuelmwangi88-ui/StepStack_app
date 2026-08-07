package com.stepstack.app.notifications

import android.content.Context
import com.stepstack.app.data.AppNotification
import com.stepstack.app.viewmodel.AppViewModel
import java.util.Calendar
import java.util.UUID

class NotificationSystem(private val context: Context, private val vm: AppViewModel) {
    private val helper = NotificationHelper(context)

    private fun addNotification(title: String, message: String, type: String) {
        vm.appNotifications.add(0, AppNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = type
        ))
        helper.showNotification(title, message)
    }

    fun sendDailyReminder() {
        if (!vm.notificationSettings.dailyReminder) return
        
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val settings = vm.notificationSettings
        
        // Respect quiet hours
        if (hour >= settings.quietHoursStart || hour < settings.quietHoursEnd) return
        
        // Smart snoozing check
        val snoozeCount = vm.snoozeCounts["daily"] ?: 0
        if (snoozeCount >= 3) return
        
        addNotification(
            "Ready to move? \uD83D\uDEAA",
            "A quick bit of movement today can help you stay on track with your ${vm.user.goal.label} goal! You got this! \u2728",
            "reminder"
        )
    }

    fun notifyGraceDayUsed() {
        if (vm.notificationSettings.graceDayNotice) {
            addNotification(
                "Grace Day Applied \uD83C\uDF1F",
                "No worries \u2014 today\u0027s a grace day, your streak is safe. Take a cozy rest! \u2615",
                "grace"
            )
        }
    }

    fun notifyStreakMilestone(streak: Int) {
        if (!vm.notificationSettings.streakMilestones) return
        val milestones = listOf(7, 30, 60, 100)
        if (streak in milestones) {
            addNotification(
                "Amazing Progress! \uD83C\uDF8A",
                "Celebration! You\u0027ve hit a $streak-day streak! Keep that sparkle going! \u2728\uD83D\uDCAA",
                "milestone"
            )
        }
    }

    fun notifyComebackNudge() {
        if (vm.notificationSettings.comebackNudge) {
            addNotification(
                "Welcome back! \uD83D\uDC4B",
                "Whenever you\u0027re ready, a quick 10-min session can ease you back in. We missed you! \uD83D\uDE0A",
                "comeback"
            )
        }
    }

    fun notifyAchievement(title: String) {
        if (vm.notificationSettings.achievementUnlock) {
            addNotification(
                "New Achievement! \uD83C\uDFC6",
                "You earned: $title! Check your profile to see your shiny new badge. So proud! \uD83E\uDD73",
                "achievement"
            )
        }
    }

    fun notifyGeneral(title: String, message: String) {
        addNotification(title, message, "general")
    }

    fun scheduleDailyReminder(hour: Int, minute: Int) {
        // Mock implementation for demo
    }

    fun snoozeNotification(type: String) {
        val count = vm.snoozeCounts[type] ?: 0
        vm.snoozeCounts[type] = count + 1
    }
}
