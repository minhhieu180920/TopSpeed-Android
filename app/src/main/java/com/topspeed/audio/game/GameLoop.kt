package com.topspeed.audio.game

import android.util.Log
import com.topspeed.audio.audio.AudioEngine
import com.topspeed.audio.audio.TTSManager
import com.topspeed.audio.car.CarModel
import com.topspeed.audio.track.TrackModel
import com.topspeed.audio.audio.SurfaceType
import com.topspeed.audio.audio.CurveDirection
import com.topspeed.audio.audio.TrackFeature

/**
 * Game Loop chính - điều khiển toàn bộ game
 * Chạy ở 60 FPS
 */
class GameLoop(
    private val activity: com.topspeed.audio.TopSpeedActivity,
    private val audioEngine: AudioEngine,
    private val ttsManager: TTSManager
) {
    companion object {
        private const val TAG = "GameLoop"
        private const val TARGET_FPS = 60
        private const val FRAME_TIME_MS = 1000L / TARGET_FPS
    }

    var isInitialized = false
        private set
    var gameState = GameState.MENU
        private set
    var gameMode = GameMode.QUICK_START
        private set
    var isPaused = false
        private set
    var isEngineRunning = false
        private set

    // Game objects
    private var carModel: CarModel? = null
    private var trackModel: TrackModel? = null

    // Race state
    private var currentLap = 0
    private var totalLaps = 3
    private var raceTime = 0L  // ms
    private var lapStartTime = 0L
    private var bestLapTime = Int.MAX_VALUE
    private var currentPosition = 1
    private var totalRacers = 1

    // Menu state
    private var currentMenuItem = 0
    private val menuItems = listOf("Khởi động nhanh", "Thử thách thời gian", "Đua đơn", "Chơi multiplayer", "Cài đặt")

    // Input state
    private var isAccelerating = false
    private var isBraking = false
    private var steeringValue = 0f  // -1.0 (trái) đến 1.0 (phải)

    // Timing
    private var lastFrameTime = 0L
    private var accumulator = 0L

    // Event queue cho âm thanh
    private val eventQueue = mutableListOf<GameEvent>()

    /**
     * Khởi tạo game loop
     */
    fun startGame() {
        Log.i(TAG, "Starting game")
        isInitialized = true
        gameState = GameState.MENU
        lastFrameTime = System.currentTimeMillis()

        // Load default track and car
        loadTrack(0)
        loadCar(0)

        // Announce menu
        announceMenu()
    }

    /**
     * Pause game loop
     */
    fun pause() {
        if (isPaused) return
        isPaused = true
        Log.d(TAG, "Game paused")
    }

    /**
     * Resume game loop
     */
    fun resume() {
        if (!isPaused) return
        isPaused = false
        lastFrameTime = System.currentTimeMillis()
        Log.d(TAG, "Game resumed")

        if (gameState == GameState.RACING) {
            ttsManager.speak("Tiếp tục đua")
        }
    }

    /**
     * Shutdown game
     */
    fun shutdown() {
        Log.i(TAG, "Shutting down game")
        isInitialized = false
        carModel?.cleanup()
        trackModel?.cleanup()
        carModel = null
        trackModel = null
    }

    /**
     * Update game state
     * Called từ Activity's choreographer
     */
    fun update() {
        if (!isInitialized || isPaused) return

        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastFrameTime
        lastFrameTime = currentTime

        when (gameState) {
            GameState.MENU -> updateMenu(deltaTime)
            GameState.RACING -> updateRacing(deltaTime)
            GameState.COUNTDOWN -> updateCountdown(deltaTime)
            GameState.PAUSED -> { }
            GameState.FINISHED -> updateFinished(deltaTime)
            GameState.CRASHED -> updateCrashed(deltaTime)
        }

        // Process events
        processEvents()
    }

    private fun updateMenu(deltaTime: Long) {
        // Menu state - chờ input
    }

    private fun updateRacing(deltaTime: Long) {
        // Update race time
        raceTime += deltaTime

        // Update car physics
        carModel?.update(deltaTime, isAccelerating, isBraking, steeringValue)

        // Update track position
        trackModel?.updateCarPosition(carModel)

        // Check lap completion
        checkLapCompletion()

        // Announce curves and surfaces
        announceUpcomingFeatures()

        // Update lap info
        updateLapInfo()
    }

    private fun updateCountdown(deltaTime: Long) {
        // Countdown before race starts
    }

    private fun updateFinished(deltaTime: Long) {
        // Race finished - show results
    }

    private fun updateCrashed(deltaTime: Long) {
        // Car crashed - wait for restart
    }

    /**
     * Bắt đầu race
     */
    fun startRace() {
        Log.i(TAG, "Starting race")
        gameState = GameState.COUNTDOWN

        // Play countdown sounds
        playCountdown()
    }

    private fun playCountdown() {
        Thread {
            Thread.sleep(500)
            ttsManager.speak("3")
            Thread.sleep(1000)
            ttsManager.speak("2")
            Thread.sleep(1000)
            ttsManager.speak("1")
            Thread.sleep(1000)
            ttsManager.speak("Bắt đầu!")
            gameState = GameState.RACING
            lapStartTime = System.currentTimeMillis()
            isEngineRunning = true
            carModel?.startEngine()
        }.start()
    }

    /**
     * Chọn menu item
     */
    fun selectMenuItem() {
        if (gameState != GameState.MENU) return

        Log.d(TAG, "Selecting menu item: ${menuItems[currentMenuItem]}")

        when (currentMenuItem) {
            0 -> {
                // Quick start
                gameMode = GameMode.QUICK_START
                startRace()
            }
            1 -> {
                // Time trial
                gameMode = GameMode.TIME_TRIAL
                ttsManager.speak("Thử thách thời gian")
                startRace()
            }
            2 -> {
                // Single race
                gameMode = GameMode.SINGLE_RACE
                ttsManager.speak("Đua đơn")
                startRace()
            }
            3 -> {
                // Multiplayer
                ttsManager.speak("Chơi multiplayer")
                activity.connectToMultiplayerServer()
            }
            4 -> {
                // Settings - show player name dialog
                ttsManager.speak("Cài đặt")
                activity.openSettingsActivity()
            }
        }
    }

    /**
     * Menu item tiếp theo
     */
    fun nextMenuItem() {
        if (gameState != GameState.MENU) return
        currentMenuItem = (currentMenuItem + 1) % menuItems.size
        ttsManager.speak(menuItems[currentMenuItem], interrupt = true)
    }

    /**
     * Menu item trước
     */
    fun previousMenuItem() {
        if (gameState != GameState.MENU) return
        currentMenuItem = (currentMenuItem - 1 + menuItems.size) % menuItems.size
        ttsManager.speak(menuItems[currentMenuItem], interrupt = true)
    }

    /**
     * Mở menu
     */
    fun openMenu() {
        if (gameState == GameState.RACING) {
            gameState = GameState.PAUSED
            isPaused = true
            ttsManager.speak("Menu chính")
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.RACING
            isPaused = false
            ttsManager.speak("Tiếp tục đua")
        }
    }

    /**
     * Load track
     */
    private fun loadTrack(trackId: Int) {
        trackModel = TrackModel(trackId)
        trackModel?.load()
        Log.d(TAG, "Loaded track: $trackId")
    }

    /**
     * Load car
     */
    private fun loadCar(carId: Int) {
        carModel = CarModel(carId, audioEngine)
        carModel?.load()
        Log.d(TAG, "Loaded car: $carId")
    }

    /**
     * Tăng tốc
     */
    fun accelerate(active: Boolean) {
        isAccelerating = active
    }

    /**
     * Phanh
     */
    fun brake(active: Boolean) {
        isBraking = active
    }

    /**
     * Set steering từ tilt
     */
    fun setSteering(value: Float) {
        steeringValue = value
    }

    /**
     * Đề máy
     */
    fun startEngine() {
        if (!isEngineRunning) {
            carModel?.startEngine()
            isEngineRunning = true
            ttsManager.speak("Đã đề máy")
        }
    }

    /**
     * Tắt máy
     */
    fun stopEngine() {
        if (isEngineRunning) {
            carModel?.stopEngine()
            isEngineRunning = false
            ttsManager.speak("Đã tắt máy")
        }
    }

    /**
     * Toggle pause
     */
    fun togglePause() {
        isPaused = !isPaused
        if (isPaused) {
            gameState = GameState.PAUSED
        } else {
            gameState = GameState.RACING
        }
    }

    /**
     * Kiểm tra lap completion
     */
    private fun checkLapCompletion() {
        val position = carModel?.position ?: return
        val trackLength = trackModel?.length ?: return

        if (position >= trackLength * (currentLap + 1)) {
            // Completed lap
            currentLap++
            val lapTime = (System.currentTimeMillis() - lapStartTime).toInt()
            lapStartTime = System.currentTimeMillis()

            if (lapTime < bestLapTime) {
                bestLapTime = lapTime
                ttsManager.speak("Lap nhanh nhất mới")
            }

            ttsManager.speak("Hoàn thành vòng ${currentLap}")

            if (currentLap >= totalLaps) {
                finishRace()
            }
        }
    }

    /**
     * Cập nhật lap info
     */
    private fun updateLapInfo() {
        // Announce periodically
        if (raceTime % 30000 < 100) { // Every 30 seconds
            announceCurrentInfo()
        }
    }

    /**
     * Đọc thông tin hiện tại
     */
    fun announceCurrentInfo() {
        val lapInfo = getLapInfo()
        ttsManager.speak("Vòng ${lapInfo.currentLap} trên ${lapInfo.totalLaps}")
        Thread.sleep(500)
        ttsManager.speak("Vị trí thứ ${lapInfo.position}")
        Thread.sleep(500)
        ttsManager.speak("Tốc độ ${lapInfo.speed} cây số một giờ")
    }

    private fun getLapInfo(): LapInfo {
        return LapInfo(
            currentLap = currentLap + 1,
            totalLaps = totalLaps,
            lapTime = (System.currentTimeMillis() - lapStartTime).toInt(),
            bestLapTime = bestLapTime,
            totalTime = raceTime.toInt(),
            position = currentPosition,
            totalRacers = totalRacers,
            speed = carModel?.speed?.toInt() ?: 0,
            gear = carModel?.gear ?: 1
        )
    }

    /**
     * Announce upcoming features (curves, surface changes)
     */
    private fun announceUpcomingFeatures() {
        val position = carModel?.position ?: return
        val upcomingFeatures = trackModel?.getUpcomingFeatures(position, lookAhead = 200) ?: return

        for (feature in upcomingFeatures) {
            when (feature) {
                is TrackFeature.Curve -> announceCurve(feature)
                is TrackFeature.SurfaceChange -> announceSurfaceChange(feature)
            }
        }
    }

    private fun announceCurve(curve: TrackFeature.Curve) {
        val announced = curve.announced
        if (announced) return

        val direction = when (curve.direction) {
            CurveDirection.EASY_LEFT -> "Cong nhẹ bên trái"
            CurveDirection.LEFT -> "Cong trái"
            CurveDirection.HARD_LEFT -> "Cong gắt bên trái"
            CurveDirection.HAIRPIN_LEFT -> "Cong tóc bẹp trái"
            CurveDirection.EASY_RIGHT -> "Cong nhẹ bên phải"
            CurveDirection.RIGHT -> "Cong phải"
            CurveDirection.HARD_RIGHT -> "Cong gắt bên phải"
            CurveDirection.HAIRPIN_RIGHT -> "Cong tóc bẹp phải"
            else -> return
        }

        ttsManager.speak(direction)
        curve.announced = true
    }

    private fun announceSurfaceChange(surface: TrackFeature.SurfaceChange) {
        val announced = surface.announced
        if (announced) return

        val surfaceName = when (surface.newSurface) {
            SurfaceType.ASPHALT -> "Đường nhựa"
            SurfaceType.GRAVEL -> "Đường đá sỏi"
            SurfaceType.WATER -> "Đường nước"
            SurfaceType.SAND -> "Đường cát"
            SurfaceType.SNOW -> "Đường tuyết"
        }

        ttsManager.speak(surfaceName)
        surface.announced = true
    }

    /**
     * Kết thúc race
     */
    private fun finishRace() {
        gameState = GameState.FINISHED
        ttsManager.speak("Hoàn thành cuộc đua!")
        Thread.sleep(1000)

        val lapInfo = getLapInfo()
        ttsManager.speak("Vị trí thứ ${lapInfo.position}")
        Thread.sleep(500)
        ttsManager.speak("Thời gian: ${formatTime(lapInfo.totalTime)}")
        Thread.sleep(500)
        ttsManager.speak("Lap nhanh nhất: ${formatTime(lapInfo.bestLapTime)}")

        // Return to menu after delay
        Thread {
            Thread.sleep(5000)
            gameState = GameState.MENU
        }.start()
    }

    private fun formatTime(timeMs: Int): String {
        val seconds = timeMs / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "$minutes phút $remainingSeconds giây"
        } else {
            "$remainingSeconds giây"
        }
    }

    /**
     * Process event queue
     */
    private fun processEvents() {
        synchronized(eventQueue) {
            val currentTime = System.currentTimeMillis()
            val eventsToRemove = mutableListOf<GameEvent>()

            for (event in eventQueue) {
                if (currentTime >= event.triggerTime) {
                    executeEvent(event)
                    eventsToRemove.add(event)
                }
            }

            eventQueue.removeAll(eventsToRemove)
        }
    }

    /**
     * Add event to queue
     */
    private fun addEvent(event: GameEvent) {
        synchronized(eventQueue) {
            eventQueue.add(event)
        }
    }

    private fun executeEvent(event: GameEvent) {
        when (event.type) {
            EventType.PLAY_SOUND -> {
                audioEngine.playSound(event.soundName, volume = event.volume)
            }
            EventType.ANNOUNCE -> {
                ttsManager.speak(event.message)
            }
            EventType.VIBRATE -> {
                // Trigger vibration
            }
        }
    }

    private fun announceMenu() {
        ttsManager.speak("TopSpeed Audio Racing")
        Thread.sleep(1000)
        ttsManager.speak("Menu chính")
        Thread.sleep(500)
        ttsManager.speak(menuItems[currentMenuItem])
    }
}

// Event types
enum class EventType {
    PLAY_SOUND,
    ANNOUNCE,
    VIBRATE
}

data class GameEvent(
    val type: EventType,
    val triggerTime: Long,
    val soundName: String? = null,
    val message: String? = null,
    val volume: Float = 1.0f
)
