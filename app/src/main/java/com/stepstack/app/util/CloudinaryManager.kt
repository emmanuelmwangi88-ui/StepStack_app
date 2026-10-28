package com.stepstack.app.util

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryManager {
    
    private const val CLOUD_NAME = "dnt3lcyoj"
    // Using Unsigned Uploads to remove the need for API_KEY or API_SECRET in the app
    private const val UPLOAD_PRESET = "stepstack"

    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to CLOUD_NAME,
            "secure" to true
        )
        try {
            MediaManager.init(context, config)
        } catch (e: Exception) {
            // Already initialized
        }
    }

    fun uploadImage(
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Unsigned upload requires the upload_preset option
        MediaManager.get().upload(uri)
            .unsigned(UPLOAD_PRESET)
            .option("folder", "stepstack")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: resultData["url"] as? String
                    android.util.Log.d("CloudinaryManager", "Upload success: $url")
                    if (url != null) {
                        onSuccess(url)
                    } else {
                        onError("Upload failed: No URL returned")
                    }
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    android.util.Log.e("CloudinaryManager", "Upload error: ${error.description}")
                    onError(error.description)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }
}
