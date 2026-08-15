package com.stepstack.app.util

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryManager {
    
    // NOTE: Replace these with your actual Cloudinary credentials
    private const val CLOUD_NAME = "dnt3lcyoj"
    private const val API_KEY = "478831677768694"
    private const val API_SECRET = "aMrLWuMhbalQvbnHrNy1MD00bpY"

    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to CLOUD_NAME,
            "api_key" to API_KEY,
            "api_secret" to API_SECRET,
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
        MediaManager.get().upload(uri)
            .option("folder", "stepstack")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: resultData["url"] as? String
                    if (url != null) {
                        onSuccess(url)
                    } else {
                        onError("Upload failed: No URL returned")
                    }
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    onError(error.description)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }
}
