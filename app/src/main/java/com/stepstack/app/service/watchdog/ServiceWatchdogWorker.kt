package com.stepstack.app.service.watchdog

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.*
import com.stepstack.app.service.StepTrackerService
import java.util.concurrent.TimeUnit

class ServiceWatchdogWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("ServiceWatchdogWorker", "Checking StepTrackerService status...")
        
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        } else true

        if (!hasPermission) {
            Log.w("ServiceWatchdogWorker", "StepTrackerService NOT started: ACTIVITY_RECOGNITION permission not granted.")
            return Result.success()
        }

        val serviceIntent = Intent(applicationContext, StepTrackerService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(serviceIntent)
            } else {
                applicationContext.startService(serviceIntent)
            }
            Log.d("ServiceWatchdogWorker", "StepTrackerService start intent sent.")
        } catch (e: Exception) {
            Log.e("ServiceWatchdogWorker", "Failed to start StepTrackerService from watchdog", e)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "StepTrackerWatchdogWork"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val request = PeriodicWorkRequestBuilder<ServiceWatchdogWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.i("ServiceWatchdogWorker", "Watchdog scheduled every 15 minutes")
        }
    }
}
