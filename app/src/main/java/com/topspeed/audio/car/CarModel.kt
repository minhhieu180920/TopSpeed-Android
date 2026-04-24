package com.topspeed.audio.car

import android.util.Log
import com.topspeed.audio.audio.AudioEngine

/**
 * Model xe - port từ Car.cpp của game gốc
 * Quản lý vật lý xe, âm thanh engine, và trạng thái
 */
class CarModel(
    private val carId: Int,
    private val audioEngine: AudioEngine
) {
    companion object {
        private const val TAG = "CarModel"

        // Car parameters - port từ Car::Parameters trong game gốc
        const val DEFAULT_ACCELERATION = 500      // pixel/s^2
        const val DEFAULT_DECELERATION = 800      // pixel/s^2 (phanh)
        const val DEFAULT_TOPSPEED = 180          // km/h
        const val DEFAULT_STEERING = 300          // pixel/s
        const val IDLE_FREQ = 800                 // Hz
        const val TOP_FREQ = 4000                 // Hz
    }

    // Car state
    var state = CarState.STOPPED
        private set
    var position = 0f          // Vị trí trên track (pixels)
    var speed = 0f             // Tốc độ hiện tại (km/h)
    var gear = 0               // Số hiện tại (0 = N, 1-6 = số)
    var rpm = 0f               // Vòng quay động cơ
    var positionX = 0          // Vị trí X trên đường
    var positionY = 0          // Vị trí Y trên đường

    // Physics parameters
    private var acceleration = DEFAULT_ACCELERATION
    private var deceleration = DEFAULT_DECELERATION
    private var topSpeed = DEFAULT_TOPSPEED
    private var steeringFactor = 1.0f
    private var manualTransmission = false

    // Engine sound state
    private var engineSoundId: Int? = null
    private var isEngineRunning = false
    private var enginePitch = 1.0f
    private var lastRpmUpdate = 0L

    // Car type
    private var carType: CarType = CarType.SPORT

    // Listener cho events
    var listener: CarListener? = null

    interface CarListener {
        fun onStart()
        fun onCrash()
        fun onGearChange(gear: Int)
    }

    /**
     * Load car data và âm thanh
     */
    fun load(): Boolean {
        Log.d(TAG, "Loading car $carId")

        // Load car parameters based on carId
        loadCarParameters()

        // Load sounds
        loadSounds()

        return true
    }

    /**
     * Load parameters cho xe này
     */
    private fun loadCarParameters() {
        when (carId) {
            0 -> {
                // Default car - balanced
                acceleration = DEFAULT_ACCELERATION
                topSpeed = DEFAULT_TOPSPEED
                carType = CarType.SPORT
            }
            1 -> {
                // Fast car - higher top speed
                acceleration = DEFAULT_ACCELERATION * 0.9f
                topSpeed = DEFAULT_TOPSPEED * 1.2f
                carType = CarType.FORMULA
            }
            2 -> {
                // Heavy car - slower acceleration
                acceleration = DEFAULT_ACCELERATION * 0.8f
                topSpeed = DEFAULT_TOPSPEED * 0.9f
                carType = CarType.SUV
            }
            // Add more car types as needed
        }

        Log.d(TAG, "Car $carId loaded: type=$carType, topSpeed=$topSpeed, acceleration=$acceleration")
    }

    /**
     * Load âm thanh cho xe
     */
    private fun loadSounds() {
        // Engine sound - loop
        audioEngine.playSound("en/gameplay/engine", loop = true, volume = 0.8f)

        // Preload other sounds
        audioEngine.preloadSound("en/gameplay/carstart")
        audioEngine.preloadSound("en/gameplay/crash")
        audioEngine.preloadSound("en/gameplay/brake")
        audioEngine.preloadSound("en/gameplay/backfire")
    }

    /**
     * Update physics
     * @param deltaTime Thời gian giữa 2 frame (ms)
     * @param isAccelerating Người chơi đang tăng tốc
     * @param isBraking Người chơi đang phanh
     * @param steeringInput Input rẽ (-1.0 đến 1.0)
     */
    fun update(deltaTime: Long, isAccelerating: Boolean, isBraking: Boolean, steeringInput: Float) {
        if (state != CarState.RUNNING && state != CarState.STARTING) return

        val dt = deltaTime / 1000.0f  // Convert to seconds

        // Update speed based on input
        updateSpeed(dt, isAccelerating, isBraking)

        // Update RPM and engine sound
        updateRPM(dt)
        updateEngineSound()

        // Update gear
        updateGear()

        // Update position on track
        updatePosition(dt)

        // Update lateral position (steering)
        updateSteering(dt, steeringInput)

        // Update state
        updateState()
    }

    private fun updateSpeed(dt: Float, isAccelerating: Boolean, isBraking: Boolean) {
        when {
            isAccelerating && !isBraking -> {
                // Accelerating
                speed += acceleration * dt
                if (speed > topSpeed) speed = topSpeed
            }
            isBraking -> {
                // Braking
                speed -= deceleration * dt
                if (speed < 0) speed = 0f
            }
            else -> {
                // Coasting - natural deceleration
                speed -= deceleration * 0.3f * dt
                if (speed < 0) speed = 0f
            }
        }
    }

    private fun updateRPM(dt: Float) {
        // Calculate RPM based on speed and gear
        val gearRatio = when (gear) {
            0 -> 3.0f   // Neutral
            1 -> 3.5f
            2 -> 2.5f
            3 -> 1.8f
            4 -> 1.3f
            5 -> 1.0f
            6 -> 0.8f
            else -> 1.0f
        }

        // Base RPM from speed
        val speedRatio = speed / topSpeed
        rpm = (speedRatio * gearRatio * 8000).coerceIn(800f, 8000f)

        // Add some variation for engine sound
        rpm += (Math.random().toFloat() - 0.5f) * 200f
    }

    private fun updateEngineSound() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRpmUpdate < 100) return  // Update every 100ms
        lastRpmUpdate = currentTime

        // Calculate pitch from RPM
        val newPitch = 0.5f + (rpm / 8000f) * 1.5f
        if (newPitch != enginePitch) {
            enginePitch = newPitch
            audioEngine.setSoundRate("en/gameplay/engine", enginePitch)
        }

        // Update volume based on speed
        val volume = 0.5f + (speed / topSpeed) * 0.5f
        audioEngine.setSoundVolume("en/gameplay/engine", volume)
    }

    private fun updateGear() {
        if (manualTransmission) return  // Player controls gear

        // Automatic transmission
        val shiftPoints = floatArrayOf(
            30f,   // 1 -> 2
            60f,   // 2 -> 3
            90f,   // 3 -> 4
            120f,  // 4 -> 5
            150f   // 5 -> 6
        )

        val oldGear = gear

        if (speed > 5) {
            gear = when {
                speed < shiftPoints[0] -> 1
                speed < shiftPoints[1] -> 2
                speed < shiftPoints[2] -> 3
                speed < shiftPoints[3] -> 4
                speed < shiftPoints[4] -> 5
                else -> 6
            }
        } else {
            gear = 0  // Neutral when stopped
        }

        if (gear != oldGear && gear > 0) {
            listener?.onGearChange(gear)
            // Play shift sound
            audioEngine.playSound("en/gameplay/shift", volume = 0.3f)
        }
    }

    private fun updatePosition(dt: Float) {
        // Update position on track (pixels per second)
        val pixelsPerSecond = speed * 3.6f  // km/h -> pixels/s (scaled)
        position += pixelsPerSecond * dt
    }

    private fun updateSteering(dt: Float, steeringInput: Float) {
        // Steering affects positionX (lateral position on track)
        val steeringSpeed = DEFAULT_STEERING * steeringFactor
        val steeringAmount = steeringInput * steeringSpeed * dt

        positionX += steeringAmount

        // Clamp to track bounds (simplified - actual track width varies)
        positionX = positionX.coerceIn(-4000f, 4000f)
    }

    private fun updateState() {
        when (state) {
            CarState.STOPPED -> {
                if (speed > 0) state = CarState.RUNNING
            }
            CarState.STARTING -> {
                if (speed > 10) state = CarState.RUNNING
            }
            CarState.RUNNING -> {
                if (speed <= 0) state = CarState.STOPPED
            }
            else -> { }
        }
    }

    /**
     * Start engine
     */
    fun startEngine() {
        if (isEngineRunning) return

        Log.d(TAG, "Starting engine")
        audioEngine.playSound("en/gameplay/carstart", volume = 1.0f)

        Thread {
            Thread.sleep(500)  // Wait for start sound
            isEngineRunning = true
            state = CarState.STARTING
            engineSoundId = audioEngine.playSound("en/gameplay/engine", loop = true, volume = 0.5f)
            listener?.onStart()
        }.start()
    }

    /**
     * Stop engine
     */
    fun stopEngine() {
        if (!isEngineRunning) return

        Log.d(TAG, "Stopping engine")
        isEngineRunning = false
        state = CarState.STOPPED
        speed = 0f
        gear = 0
        rpm = 0f

        engineSoundId?.let {
            audioEngine.stopSound("en/gameplay/engine")
        }
    }

    /**
     * Crash the car
     */
    fun crash() {
        Log.d(TAG, "Car crashed!")
        state = CarState.CRASHING
        speed = 0f

        // Play crash sound
        audioEngine.playSound("en/gameplay/crash", volume = 1.0f)

        listener?.onCrash()
    }

    /**
     * Mini crash (bump)
     */
    fun miniCrash(newPosition: Int) {
        Log.d(TAG, "Mini crash at position $newPosition")
        positionX = newPosition.toFloat()

        // Play bump sound
        audioEngine.playSound("en/gameplay/bump", volume = 0.5f)
    }

    /**
     * Honk horn
     */
    fun honk() {
        audioEngine.playSound("en/gameplay/horn", volume = 0.7f)
    }

    /**
     * Backfire sound
     */
    fun backfire() {
        if (speed > 100 && Math.random() < 0.1) {
            audioEngine.playSound("en/gameplay/backfire", volume = 0.4f)
        }
    }

    /**
     * Get car state as string
     */
    fun getStateString(): String {
        return when (state) {
            CarState.STOPPED -> "Dừng"
            CarState.STARTING -> "Đang đề"
            CarState.RUNNING -> "Đang chạy"
            CarState.SLIPPING -> "Đang trượt"
            CarState.CRASHING -> "Đang va chạm"
            CarState.STOPPING -> "Đang dừng"
        }
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopEngine()
        audioEngine.stopAllSounds()
    }

    /**
     * Set manual transmission
     */
    fun setManualTransmission(manual: Boolean) {
        manualTransmission = manual
    }

    /**
     * Shift up (manual transmission)
     */
    fun shiftUp() {
        if (!manualTransmission) return
        if (gear < 6) {
            gear++
            listener?.onGearChange(gear)
        }
    }

    /**
     * Shift down (manual transmission)
     */
    fun shiftDown() {
        if (!manualTransmission) return
        if (gear > 0) {
            gear--
            listener?.onGearChange(gear)
        }
    }
}

/**
 * Car state enum
 */
enum class CarState {
    STOPPED,
    STARTING,
    RUNNING,
    SLIPPING,
    CRASHING,
    STOPPING
}

/**
 * Car type enum
 */
enum class CarType {
    SPORT,
    FORMULA,
    SUV,
    TRUCK,
    CLASSIC
}
