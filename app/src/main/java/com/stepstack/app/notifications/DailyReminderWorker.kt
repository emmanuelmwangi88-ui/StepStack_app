package com.stepstack.app.notifications

import android.content.Context
import android.util.Log
import androidx.work.*
import com.stepstack.app.data.AppDatabase
import com.stepstack.app.viewmodel.AppViewModel
import java.util.*
import java.util.concurrent.TimeUnit

class DailyReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("DailyReminderWorker", "Executing daily reminder check...")
        
        // We need a context and VM state. 
        // In a real app, we might use a Repository to check settings instead of the VM directly.
        // But for this project, let's trigger the notification logic.
        
        // Since we can't easily access the shared AppViewModel instance from a background worker
        // without a DI framework like Hilt, we'll use a temporary one or just call the notification helper directly.
        // However, the logic for "should send" is in NotificationSystem which depends on VM.
        
        val notificationHelper = NotificationHelper(applicationContext)
        val prefs = applicationContext.getSharedPreferences("stepstack_prefs", Context.MODE_PRIVATE)
        
        val dailyEnabled = prefs.getBoolean("step_reminders", true) // Default to true if not found
        if (!dailyEnabled) return Result.success()

        // Simple reminder
        notificationHelper.showNotification(
            id = UUID.randomUUID().toString(),
            title = "Ready to move? \uD83D\uDEAA",
            message = "A quick bit of movement today can help you stay on track with your fitness goals! \u2728",
            type = "reminder",
            stackable = false
        )

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "DailyReminderWork"

        fun schedule(context: Context, hour: Int, minute: Int) {
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val delay = calendar.timeInMillis - now
            
            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            Log.i("DailyReminderWorker", "Scheduled daily reminder for $hour:$minute (delay: ${delay/1000}s)")
        }
    }
}
