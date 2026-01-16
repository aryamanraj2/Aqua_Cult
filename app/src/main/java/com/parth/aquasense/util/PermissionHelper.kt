package com.parth.aquasense.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionHelper {
    const val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

    fun hasRecordAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            RECORD_AUDIO_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
