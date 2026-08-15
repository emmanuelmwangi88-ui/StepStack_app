package com.stepstack.app

import android.app.Application
import com.stepstack.app.util.CloudinaryManager

class StepStackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CloudinaryManager.init(this)
    }
}
