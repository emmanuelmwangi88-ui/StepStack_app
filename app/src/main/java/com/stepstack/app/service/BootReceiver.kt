package com.stepstack.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.stepstack.app.service.watchdog.ServiceWatchdogWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED || action == "com.stepstack.app.RESTART_TRACKER_SERVICE") {
            
            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
            } else true

            if (!hasPermission) {
                Log.w("BootReceiver", "StepTrackerService NOT started: ACTIVITY_RECOGNITION permission not granted.")
                return
            }

            Log.i("BootReceiver", "Starting StepTrackerService (action=$action)")
            val serviceIntent = Intent(context, StepTrackerService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                // Schedule watchdog as well
                ServiceWatchdogWorker.schedule(context)
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to start StepTrackerService", e)
            }
        }
    }
}
