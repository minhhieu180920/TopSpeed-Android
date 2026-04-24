package com.topspeed.audio

import android.app.Application
import android.util.Log

/**
 * Application class cho TopSpeed Audio Racing
 * Khởi tạo global state và native library
 */
class TopSpeedApplication : Application() {

    companion object {
        private const val TAG = "TopSpeedApp"

        // Load native library
        init {
            System.loadLibrary("topspeed")
            Log.i(TAG, "Native library loaded successfully")
        }

        // Singleton instance
        lateinit var instance: TopSpeedApplication
            private set

        // Game state global
        var isGameRunning: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i(TAG, "Application created")
    }

    fun setGameRunning(running: Boolean) {
        isGameRunning = running
    }

    /**
     * Native method declarations
     */
    external fun nativeInit()
    external fun nativeShutdown()
}
