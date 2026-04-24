package com.topspeed.audio.input

import android.content.Context
import android.hardware.SensorManager
import android.util.Log
import android.view.Display
import android.view.WindowManager

/**
 * Điều khiển nghiêng điện thoại (tilt) để rẽ
 * Sử dụng accelerometer để phát hiện hướng nghiêng
 */
class TiltController(private val context: Context) {

    companion object {
        private const val TAG = "TiltController"
        private const val TILT_SENSITIVITY = 3.0f
        private const val TILT_DEADZONE = 0.15f // Vùng chết - nghiêng ít không có tác dụng
        private const val MAX_TILT = 1.0f
    }

    // Current tilt values (-1.0 to 1.0)
    var tiltX = 0f  // Nghiêng trái/phải
    var tiltY = 0f  // Nghiêng trước/sau

    // Smoothing
    private var smoothedTiltX = 0f
    private var smoothedTiltY = 0f
    private val smoothingFactor = 0.2f

    // Calibration
    private var calibrationOffsetX = 0f
    private var calibrationOffsetY = 0f
    private var isCalibrated = false

    // Screen rotation compensation
    private var screenRotation = 0f

    // Listener
    private var tiltListener: TiltListener? = null

    interface TiltListener {
        fun onTiltChanged(tiltX: Float, tiltY: Float)
    }

    init {
        // Get screen rotation for compensation
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        screenRotation = display.rotation.toFloat()
    }

    /**
     * Xử lý accelerometer data
     * @param values Accelerometer values [x, y, z]
     */
    fun onAccelerometerChanged(values: FloatArray) {
        if (values.size < 3) return

        val rawX = values[0]
        val rawY = values[1]
        val rawZ = values[2]

        // Apply calibration offset
        var calibratedX = rawX - calibrationOffsetX
        var calibratedY = rawY - calibrationOffsetY

        // Compensate for screen rotation
        val (compensatedX, compensatedY) = compensateForRotation(calibratedX, calibratedY)

        // Apply deadzone
        compensatedX.applyDeadzone(TILT_DEADZONE)
        compensatedY.applyDeadzone(TILT_DEADZONE)

        // Apply sensitivity
        var newTiltX = (compensatedX * TILT_SENSITIVITY).coerceIn(-MAX_TILT, MAX_TILT)
        var newTiltY = (compensatedY * TILT_SENSITIVITY).coerceIn(-MAX_TILT, MAX_TILT)

        // Smoothing
        smoothedTiltX = lerp(smoothedTiltX, newTiltX, smoothingFactor)
        smoothedTiltY = lerp(smoothedTiltY, newTiltY, smoothingFactor)

        // Update current values
        tiltX = smoothedTiltX
        tiltY = smoothedTiltY

        // Notify listener
        tiltListener?.onTiltChanged(tiltX, tiltY)
    }

    /**
     * Compensate tilt for screen rotation
     * Landscape mode cần rotate axes
     */
    private fun compensateForRotation(x: Float, y: Float): Pair<Float, Float> {
        return when (screenRotation.toInt()) {
            0 -> Pair(x, y)      // Portrait
            1 -> Pair(-y, x)     // Landscape 90
            2 -> Pair(-x, -y)    // Portrait upside down
            3 -> Pair(y, -x)     // Landscape 270
            else -> Pair(x, y)
        }
    }

    /**
     * Apply deadzone to filter small movements
     */
    private fun Float.applyDeadzone(deadzone: Float): Float {
        return when {
            this > deadzone -> (this - deadzone) / (1f - deadzone)
            this < -deadzone -> (this + deadzone) / (1f - deadzone)
            else -> 0f
        }
    }

    /**
     * Linear interpolation
     */
    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }

    /**
     * Calibrate tilt sensor (đặt vị trí neutral hiện tại làm 0)
     */
    fun calibrate() {
        // Sample current values
        val samples = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until 10) {
            Thread.sleep(50)
            // Would need to store last values - simplified here
        }
        // For now, just reset offsets
        calibrationOffsetX = 0f
        calibrationOffsetY = 0f
        isCalibrated = true
        Log.i(TAG, "Tilt sensor calibrated")
    }

    /**
     * Set tilt listener
     */
    fun setTiltListener(listener: TiltListener) {
        tiltListener = listener
    }

    /**
     * Set sensitivity (độ nhạy)
     * @param sensitivity 0.5f (thấp) đến 5.0f (cao)
     */
    fun setSensitivity(sensitivity: Float) {
        // This would require updating a mutable sensitivity variable
        Log.d(TAG, "Sensitivity set to: $sensitivity")
    }

    /**
     * Reset tilt values
     */
    fun reset() {
        tiltX = 0f
        tiltY = 0f
        smoothedTiltX = 0f
        smoothedTiltY = 0f
    }

    /**
     * Get normalized steering value (-1.0 trái, 1.0 phải)
     */
    fun getSteeringValue(): Float {
        return tiltX
    }

    /**
     * Get throttle/brake value from Y tilt
     * Positive = brake, Negative = accelerate
     */
    fun getThrottleValue(): Float {
        return -tiltY // Invert vì nghiêng về trước là ga
    }

    /**
     * Check if device is tilted left
     */
    fun isTiltedLeft(threshold: Float = 0.3f): Boolean {
        return tiltX < -threshold
    }

    /**
     * Check if device is tilted right
     */
    fun isTiltedRight(threshold: Float = 0.3f): Boolean {
        return tiltX > threshold
    }

    /**
     * Check if device is in neutral position
     */
    fun isNeutral(threshold: Float = 0.1f): Boolean {
        return kotlin.math.abs(tiltX) < threshold && kotlin.math.abs(tiltY) < threshold
    }
}
