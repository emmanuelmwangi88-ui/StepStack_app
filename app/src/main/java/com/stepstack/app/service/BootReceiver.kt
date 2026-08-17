package com.stepstack.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            // On Android 14+ (API 34), starting a foreground service of type 'health'
            // requires the ACTIVITY_RECOGNITION permission. If not granted, starting
            // the service will throw a SecurityException.
            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
            } else true

            if (!hasPermission) {
                Log.w("BootReceiver", "StepTrackerService NOT started: ACTIVITY_RECOGNITION permission not granted.")
                return
            }

            Log.i("BootReceiver", "Starting StepTrackerService after boot/update")
            val serviceIntent = Intent(context, StepTrackerService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to start StepTrackerService", e)
            }
        }
    }
}
