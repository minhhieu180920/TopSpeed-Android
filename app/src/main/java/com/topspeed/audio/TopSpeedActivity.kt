package com.topspeed.audio

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration
import android.content.res.Configuration
import android.graphics.Path
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.*
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.topspeed.audio.audio.AudioEngine
import com.topspeed.audio.audio.TTSManager
import com.topspeed.audio.game.GameLoop
import com.topspeed.audio.game.GameState
import com.topspeed.audio.input.GestureController
import com.topspeed.audio.input.TiltController
import com.topspeed.audio.network.MultiplayerManager
import com.topspeed.audio.network.ServerConfig
import com.topspeed.audio.network.RoomInfo
import com.topspeed.audio.network.UserPreferences
import android.content.Intent
import android.content.Intent
import com.topspeed.audio.network.UserPreferences

/**
 * Activity chính cho TopSpeed Audio Racing
 * Quản lý game loop, audio, input và accessibility
 */
class TopSpeedActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        private const val TAG = "TopSpeedActivity"
        private var instance: TopSpeedActivity? = null

        fun getInstance(): TopSpeedActivity? = instance
    }

    private var ttsManagerInstance: TTSManager? = null
    private var audioEngineInstance: AudioEngine? = null

    fun getTTSManager(): TTSManager = ttsManagerInstance!!
    fun getAudioEngine(): AudioEngine = audioEngineInstance!!

    // Game components
    private lateinit var gameLoop: GameLoop
    private lateinit var audioEngine: AudioEngine
    private lateinit var ttsManager: TTSManager
    private lateinit var gestureController: GestureController
    private lateinit var tiltController: TiltController
    private lateinit var multiplayerManager: MultiplayerManager

    // Sensors
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Accessibility
    private lateinit var accessibilityManager: AccessibilityManager
    private var isTalkBackEnabled = false

    // Multiplayer state
    private var isInMultiplayerMode = false
    private var isConnectingToServer = false

    // Views
    private lateinit var rootLayout: View
    private lateinit var touchLayer: View

    // Screen dimensions for touch zones
    private var screenWidth = 0
    private var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Activity onCreate")

        // Set singleton instance
        instance = this

        // Set singleton instance
        instance = this

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Set volume to max for game
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Initialize views
        setContentView(R.layout.activity_main)
        rootLayout = findViewById(R.id.rootLayout)
        touchLayer = findViewById(R.id.touchLayer)

        // Get screen dimensions
        val displayMetrics = resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

        // Initialize managers
        initializeManagers()

        // Initialize game components
        initializeGameComponents()

        // Setup input handlers
        setupInputHandlers()

        // Request permissions
        requestRequiredPermissions()

        // Announce welcome
        announceGame()
    }

    private fun initializeManagers() {
        // Accessibility manager
        accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        isTalkBackEnabled = accessibilityManager.isTouchExplorationEnabled

        // Sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Tilt controller
        tiltController = TiltController(this)
    }

    private fun initializeGameComponents() {
        // Load user preferences first
        val prefs = UserPreferences(this)

        // Audio engine
        audioEngine = AudioEngine(this)
        audioEngine.initialize()
        audioEngineInstance = audioEngine
        // Apply saved volume settings
        audioEngine.setVolumesFromPreferences(prefs.sfxVolume, prefs.musicVolume)

        // TTS Manager
        ttsManager = TTSManager(this)
        ttsManager.initialize()
        ttsManagerInstance = ttsManager
        // Apply saved TTS volume
        ttsManager.setVolume(prefs.ttsVolume)

        // Game loop
        gameLoop = GameLoop(this, audioEngine, ttsManager)

        // Multiplayer manager
        multiplayerManager = MultiplayerManager(ttsManager, this)
        setupMultiplayerListeners()

        // Gesture controller
        gestureController = GestureController(this, object : GestureController.GestureListener {
            override fun onSingleTap(x: Float, y: Float) {
                handleSingleTap(x, y)
            }

            override fun onDoubleTap(x: Float, y: Float) {
                handleDoubleTap(x, y)
            }

            override fun onSwipeDown(x: Float, y: Float) {
                handleSwipeDown()
            }

            override fun onSwipeUp(x: Float, y: Float) {
                handleSwipeUp()
            }

            override fun onSwipeLeft(x: Float, y: Float) {
                handleSwipeLeft()
            }

            override fun onSwipeRight(x: Float, y: Float) {
                handleSwipeRight()
            }

            override fun onThreeFingerSwipeUp() {
                handleThreeFingerSwipeUp()
            }

            override fun onTwoFingerTap() {
                handleTwoFingerTap()
            }

            override fun onLongPress(x: Float, y: Float) {
                handleLongPress(x, y)
            }
        })

        // Link tilt controller to game
        tiltController.setTiltListener { tiltX, tiltY ->
            handleTilt(tiltX, tiltY)
        }
    }

    private fun setupInputHandlers() {
        // Touch listener for gestures
        touchLayer.setOnTouchListener { view, motionEvent ->
            gestureController.onTouchEvent(motionEvent)
            true
        }

        // Accessibility touch exploration
        if (isTalkBackEnabled) {
            touchLayer.isImportantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            touchLayer.setOnHoverListener { view, motionEvent ->
                handleHoverEvent(motionEvent)
                true
            }
        }
    }

    private fun handleSingleTap(x: Float, y: Float) {
        Log.d(TAG, "Single tap at x=$x, y=$y")

        // Tap bên trái màn hình để tăng tốc
        if (x < screenWidth / 2) {
            if (gameLoop.gameState == GameState.MENU) {
                gameLoop.selectMenuItem()
            } else if (gameLoop.gameState == GameState.RACING) {
                gameLoop.accelerate(true)
                vibrate(50)
            }
        }
    }

    private fun handleDoubleTap(x: Float, y: Float) {
        Log.d(TAG, "Double tap at x=$x, y=$y")

        // Double tap bên trái để đề máy/dừng xe
        if (x < screenWidth / 2) {
            if (gameLoop.gameState == GameState.RACING) {
                if (gameLoop.isEngineRunning()) {
                    gameLoop.stopEngine()
                    ttsManager.speak("Đã tắt máy")
                } else {
                    gameLoop.startEngine()
                    ttsManager.speak("Đã đề máy")
                }
                vibrate(100)
            }
        }
    }

    private fun handleSwipeDown() {
        Log.d(TAG, "Swipe down - brake")
        if (gameLoop.gameState == GameState.RACING) {
            gameLoop.brake(true)
            ttsManager.speak("Phanh")
            vibrate(longArrayOf(0, 50, 50, 50), -1)
        }
    }

    private fun handleSwipeUp() {
        Log.d(TAG, "Swipe up - select menu item")
        if (gameLoop.gameState == GameState.MENU) {
            gameLoop.selectMenuItem()
        } else if (gameLoop.gameState == GameState.RACING) {
            gameLoop.accelerate(true)
        }
    }

    private fun handleSwipeLeft() {
        Log.d(TAG, "Swipe left - previous menu item")
        if (gameLoop.gameState == GameState.MENU) {
            gameLoop.previousMenuItem()
        }
    }

    private fun handleSwipeRight() {
        Log.d(TAG, "Swipe right - next menu item")
        if (gameLoop.gameState == GameState.MENU) {
            gameLoop.nextMenuItem()
        }
    }

    private fun handleThreeFingerSwipeUp() {
        Log.d(TAG, "Three finger swipe up - open menu")
        gameLoop.openMenu()
        ttsManager.speak("Menu chính")
        vibrate(200)
    }

    private fun handleTwoFingerTap() {
        Log.d(TAG, "Two finger tap - pause")
        if (gameLoop.gameState == GameState.RACING) {
            gameLoop.togglePause()
            if (gameLoop.isPaused) {
                ttsManager.speak("Tạm dừng")
            } else {
                ttsManager.speak("Tiếp tục")
            }
            vibrate(150)
        }
    }

    private fun handleLongPress(x: Float, y: Float) {
        Log.d(TAG, "Long press at x=$x, y=$y")
        // Long press để nghe lại thông tin hiện tại
        gameLoop.announceCurrentInfo()
        vibrate(100)
    }

    private fun handleTilt(tiltX: Float, tiltY: Float) {
        // Nghiêng điện thoại để rẽ
        if (gameLoop.gameState == GameState.RACING && !gameLoop.isPaused) {
            // Tilt X: -1 (trái) đến 1 (phải)
            gameLoop.setSteering(tiltX)

            // Tilt Y: -1 (lên) đến 1 (xuống) - có thể dùng cho phanh/ga
            if (tiltY > 0.3f) {
                gameLoop.brake(true)
            } else {
                gameLoop.brake(false)
            }
        }
    }

    private fun handleHoverEvent(event: MotionEvent): Boolean {
        // Handle TalkBack touch exploration
        if (event.action == MotionEvent.ACTION_HOVER_ENTER) {
            val x = event.x
            val zone = if (x < screenWidth / 2) "trái" else "phải"
            ttsManager.speak("Vùng $zone. Chạm đúp để tác vụ")
        }
        return true
    }

    private fun announceGame() {
        Handler(Looper.getMainLooper()).postDelayed({
            ttsManager.speak("Chào mừng đến với TopSpeed Audio Racing")
            Handler(Looper.getMainLooper()).postDelayed({
                ttsManager.speak("Chạm đúp bên trái màn hình để đề máy xe")
                Handler(Looper.getMainLooper()).postDelayed({
                    ttsManager.speak("Nghiêng điện thoại để rẽ trái hoặc phải")
                    Handler(Looper.getMainLooper()).postDelayed({
                        ttsManager.speak("Vuốt xuống để phanh")
                        gameLoop.startGame()
                    }, 1500)
                }, 1500)
            }, 1500)
        }, 500)
    }

    // ============ Multiplayer Setup ============

    private fun setupMultiplayerListeners() {
        multiplayerManager.onConnectionStateChanged = { connected ->
            isInMultiplayerMode = connected
            if (connected) {
                ttsManager.speak("Đã kết nối multiplayer")
            }
        }

        multiplayerManager.onPlayerJoined = { player ->
            ttsManager.speak("${player.name} đã vào phòng")
        }

        multiplayerManager.onPlayerLeft = { playerId ->
            val player = multiplayerManager.getPlayers()[playerId]
            ttsManager.speak("${player?.name ?: "Người chơi"} đã rời")
        }

        multiplayerManager.onRaceStart = {
            // Start multiplayer race
            gameLoop.startRace()
        }
    }

    /**
     * Kết nối đến server multiplayer
     */
    fun connectToMultiplayerServer(serverUrl: String = ServerConfig.currentServerUrl) {
        if (isConnectingToServer) return

        // Kiểm tra và prompt tên người chơi nếu chưa có
        if (!multiplayerManager.hasPlayerName()) {
            ttsManager.speak("Vui lòng nhập tên của bạn trong cài đặt trước khi chơi multiplayer")
            return
        }

        isConnectingToServer = true
        ttsManager.speak(" Đang kết nối đến server...", TTSManager.Priority.HIGH)

        multiplayerManager.setServerUrl(serverUrl)
        multiplayerManager.connect(serverUrl)

        Handler(Looper.getMainLooper()).postDelayed({
            isConnectingToServer = false
        }, 5000)
    }

    /**
     * Tạo phòng multiplayer
     */
    fun createMultiplayerRoom(trackId: Int = 0, laps: Int = 3) {
        if (!multiplayerManager.isConnected()) {
            ttsManager.speak("Chưa kết nối server. Vui lòng kết nối trước.")
            return
        }

        val roomCode = multiplayerManager.createRoom(trackId, laps)
        if (roomCode != null) {
            ttsManager.speak("Đã tạo phòng. Mã: $roomCode")
            showRoomCode(roomCode)
        } else {
            ttsManager.speak("Tạo phòng thất bại")
        }
    }

    /**
     * Tham gia phòng multiplayer
     */
    fun joinMultiplayerRoom(code: String) {
        if (!multiplayerManager.isConnected()) {
            ttsManager.speak("Chưa kết nối server. Vui lòng kết nối trước.")
            return
        }

        val success = multiplayerManager.joinRoom(code)
        if (success) {
            ttsManager.speak("Đã vào phòng $code")
        } else {
            ttsManager.speak("Không thể vào phòng. Kiểm tra lại mã phòng.")
        }
    }

    /**
     * Rời phòng multiplayer
     */
    fun leaveMultiplayerRoom() {
        multiplayerManager.leaveRoom()
        ttsManager.speak("Đã rời phòng")
    }

    /**
     * Bắt đầu đua (chỉ host)
     */
    fun startMultiplayerRace() {
        if (multiplayerManager.startRace()) {
            ttsManager.speak("Bắt đầu!")
        } else {
            ttsManager.speak("Chỉ host mới có thể bắt đầu")
        }
    }

    /**
     * Lấy danh sách phòng
     */
    fun refreshRoomList(): List<RoomInfo> {
        return multiplayerManager.getAvailableRooms()
    }

    private fun showRoomCode(code: String) {
        Toast.makeText(this, "Mã phòng: $code", Toast.LENGTH_LONG).show()
    }

    /**
     * Lấy tên người chơi hiện tại
     */
    fun getPlayerName(): String {
        return multiplayerManager.getPlayerName()
    }

    /**
     * Cập nhật tên người chơi
     */
    fun setPlayerName(name: String) {
        if (name.isNotBlank()) {
            multiplayerManager.setPlayerName(name)
            ttsManager.speak("Đã lưu tên người chơi: $name")
        }
    }

    /**
     * Mở màn hình cài đặt (SettingsActivity - màn hình dọc)
     */
    fun openSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Mở màn hình cài đặt (SettingsActivity - màn hình dọc)
     */
    fun openSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Hiển thị dialog cài đặt đầy đủ
     */
    fun showSettingsDialog() {
        val prefs = UserPreferences(this)

        // Tạo layout động
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        // Tên người chơi
        val nameLabel = TextView(this).apply {
            text = "Tên người chơi:"
            setPadding(0, 0, 0, 10)
        }
        val nameInput = EditText(this).apply {
            hint = "Nhập tên của bạn"
            setText(prefs.playerName)
        }
        layout.addView(nameLabel)
        layout.addView(nameInput)

        // Thanh TTS
        val ttsLabel = TextView(this).apply {
            text = "Âm lượng TTS: ${(prefs.ttsVolume * 100).toInt()}%"
        }
        val ttsSeekBar = SeekBar(this).apply {
            max = 100
            progress = (prefs.ttsVolume * 100).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val volume = progress / 100f
                    ttsLabel.text = "Âm lượng TTS: $progress%"
                    ttsManager.setVolume(volume)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        layout.addView(ttsLabel)
        layout.addView(ttsSeekBar)

        // Thanh nhạc nền
        val musicLabel = TextView(this).apply {
            text = "Âm lượng nhạc: ${(prefs.musicVolume * 100).toInt()}%"
        }
        val musicSeekBar = SeekBar(this).apply {
            max = 100
            progress = (prefs.musicVolume * 100).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val volume = progress / 100f
                    musicLabel.text = "Âm lượng nhạc: $progress%"
                    audioEngine.setMusicVolume(volume)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        layout.addView(musicLabel)
        layout.addView(musicSeekBar)

        // Thanh hiệu ứng âm thanh
        val sfxLabel = TextView(this).apply {
            text = "Âm lượng hiệu ứng: ${(prefs.sfxVolume * 100).toInt()}%"
        }
        val sfxSeekBar = SeekBar(this).apply {
            max = 100
            progress = (prefs.sfxVolume * 100).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val volume = progress / 100f
                    sfxLabel.text = "Âm lượng hiệu ứng: $progress%"
                    audioEngine.setSfxVolume(volume)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        layout.addView(sfxLabel)
        layout.addView(sfxSeekBar)

        AlertDialog.Builder(this)
            .setTitle("Cài Đặt")
            .setView(layout)
            .setPositiveButton("Lưu") { _, _ ->
                // Lưu tên
                val name = nameInput.text.toString().trim()
                if (name.isNotBlank()) {
                    setPlayerName(name)
                }
                // Lưu volumes
                prefs.ttsVolume = ttsSeekBar.progress / 100f
                prefs.musicVolume = musicSeekBar.progress / 100f
                prefs.sfxVolume = sfxSeekBar.progress / 100f

                ttsManager.speak("Đã lưu cài đặt")
            }
            .setNegativeButton("Hủy", null)
            .setNeutralButton("Mặc định") { _, _ ->
                // Reset về mặc định
                nameInput.setText(prefs.playerName)
                ttsSeekBar.progress = 80
                musicSeekBar.progress = 70
                sfxSeekBar.progress = 80
            }
            .show()
    }

    /**
     * Hiển thị dialog nhập tên người chơi (cũ - giữ lại cho tương thích)
     */
    fun showPlayerNameDialog() {
        showSettingsDialog()
    }

    private fun requestRequiredPermissions() {
        // Request storage permission if needed
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            // No special permission needed for Android 13+
        }
    }

    private fun vibrate(duration: Long) {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }

    private fun vibrate(pattern: LongArray, repeat: Int) {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
        } else {
            vibrator.vibrate(pattern, repeat)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            tiltController.onAccelerometerChanged(event.values)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignore
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "Activity onResume")

        // Register sensor listener
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        // Resume game loop
        if (gameLoop.isInitialized) {
            gameLoop.resume()
        }

        // Resume audio
        audioEngine.resume()
    }

    override fun onPause() {
        Log.i(TAG, "Activity onPause")

        // Unregister sensor listener
        sensorManager.unregisterListener(this)

        // Pause game loop
        gameLoop.pause()

        // Pause audio
        audioEngine.pause()

        super.onPause()
    }

    override fun onDestroy() {
        Log.i(TAG, "Activity onDestroy")

        // Clear singleton instance
        instance = null

        // Cleanup
        gameLoop.shutdown()
        audioEngine.shutdown()
        ttsManager.shutdown()

        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i(TAG, "Configuration changed")

        // Update screen dimensions
        val displayMetrics = resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
    }
}
