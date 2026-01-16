package com.parth.aquasense

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for AquaSense
 *
 * @HiltAndroidApp: Triggers Hilt code generation
 * This annotation is required for Hilt to work
 * It generates a base class that manages dependency injection for the entire app
 */
@HiltAndroidApp
class AquaSenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // App initialization code goes here
    }
}
