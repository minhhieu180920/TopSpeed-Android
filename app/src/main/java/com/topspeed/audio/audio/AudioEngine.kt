package com.topspeed.audio.audio

import android.content.Context
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import java.io.FileDescriptor

/**
 * Audio Engine quản lý tất cả âm thanh trong game
 * - SoundPool cho SFX ngắn (engine, crash, horn)
 * - AudioTrack cho âm thanh dài (music ambient)
 * - 3D spatialization cho âm thanh theo vị trí
 */
class AudioEngine(private val context: Context) {

    companion object {
        private const val TAG = "AudioEngine"
        private const val MAX_SOUNDS = 100
        private const val SAMPLE_RATE = 44100
    }

    // SoundPool cho SFX
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<String, Int>()
    private val soundPlayers = mutableMapOf<String, Int>() // soundId -> streamId

    // AudioTrack cho âm thanh dài/ambient
    private var audioTrack: AudioTrack? = null

    // 3D Listener position (cho spatial audio)
    var listenerX = 0f
    var listenerY = 0f
    var listenerZ = 0f

    // Master volume
    var masterVolume = 1.0f
    var sfxVolume = 1.0f
    var musicVolume = 0.7f

    // Volume listeners for UI updates
    var onVolumeChanged: ((Float, Float) -> Unit)? = null

    // Volume listeners for UI updates
    var onVolumeChanged: ((Float, Float) -> Unit)? = null

    // State
    var isInitialized = false
        private set
    private var isPaused = false

    /**
     * Khởi tạo audio engine
     */
    fun initialize(): Boolean {
        Log.i(TAG, "Initializing AudioEngine")

        try {
            // Create SoundPool with audio attributes
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(MAX_SOUNDS)
                .setAudioAttributes(audioAttributes)
                .build()

            // Load all game sounds
            loadAllSounds()

            isInitialized = true
            Log.i(TAG, "AudioEngine initialized successfully")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioEngine", e)
            return false
        }
    }

    /**
     * Load tất cả âm thanh từ res/raw
     */
    private fun loadAllSounds() {
        Log.d(TAG, "Loading all sounds from res/raw")

        // Load từ res/raw sử dụng reflection để lấy tất cả R.raw resources
        loadSoundsFromRawResources()

        Log.d(TAG, "Total sounds loaded: ${soundIds.size}")
    }

    /**
     * Load tất cả sounds từ res/raw bằng reflection
     */
    private fun loadSoundsFromRawResources() {
        try {
            val rawClass = com.topspeed.audio.R.raw::class.java
            val fields = rawClass.declaredFields

            for (field in fields) {
                if (field.name.contains("_")) {
                    try {
                        val resourceId = field.getInt(null)
                        val soundName = field.name
                        loadSoundFromRawId(soundName, resourceId)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sounds from raw resources", e)
        }
    }

    /**
     * Load một sound file từ res/raw bằng resource ID
     */
    private fun loadSoundFromRawId(name: String, resourceId: Int) {
        try {
            val afd = context.resources.openRawResourceFd(resourceId)
            if (afd != null) {
                val soundId = soundPool?.load(afd, 1)
                if (soundId != null && soundId != -1) {
                    soundIds[name] = soundId
                    Log.d(TAG, "Loaded sound: $name (id=$soundId)")
                }
                afd.close()
            }
        } catch (e: Exception) {
            // Silently ignore missing resources
        }
    }

    /**
     * Play một sound effect
     * @param name Tên sound
     * @param loop Có lặp không (-1 cho infinite loop)
     * @param volume Độ lớn (0.0 - 1.0)
     * @param priority Độ ưu tiên
     * @return streamId để điều khiển sau
     */
    fun playSound(
        name: String,
        loop: Boolean = false,
        volume: Float = 1.0f,
        priority: Int = 1
    ): Int? {
        if (!isInitialized) {
            Log.w(TAG, "AudioEngine not initialized, cannot play: $name")
            return null
        }

        val soundId = soundIds[name]
        if (soundId == null) {
            Log.w(TAG, "Sound not found: $name")
            return null
        }

        val actualVolume = volume * sfxVolume * masterVolume
        val loopMode = if (loop) -1 else 0

        val streamId = soundPool?.play(
            soundId,
            actualVolume,
            actualVolume,
            priority,
            loopMode,
            1.0f // rate
        )

        if (streamId != null && streamId != -1) {
            soundPlayers[name] = streamId
            Log.d(TAG, "Playing sound: $name (streamId=$streamId)")
        }

        return streamId
    }

    /**
     * Play sound với hiệu ứng 3D spatialization
     * @param name Tên sound
     * @param soundX Vị trí X của âm thanh
     * @param soundY Vị trí Y của âm thanh
     * @param volume Volume gốc
     */
    fun playSound3D(
        name: String,
        soundX: Float,
        soundY: Float,
        volume: Float = 1.0f
    ): Int? {
        // Tính toán pan dựa trên vị trí tương đối với listener
        val dx = soundX - listenerX
        val distance = kotlin.math.sqrt(dx * dx + (soundY - listenerY) * (soundY - listenerY))

        // Pan: -1 (trái) đến 1 (phải)
        val pan = (dx / (distance + 1)).coerceIn(-1f, 1f)

        // Volume giảm theo khoảng cách (inverse square law)
        val distanceVolume = (1.0f / (distance + 1)).coerceIn(0f, 1f)

        // Play với pan và volume tính toán
        return playSoundWithPan(name, pan, volume * distanceVolume)
    }

    /**
     * Play sound với pan stereo
     */
    private fun playSoundWithPan(
        name: String,
        pan: Float,
        volume: Float = 1.0f
    ): Int? {
        val soundId = soundIds[name]
        if (soundId == null) return null

        // Convert pan (-1 to 1) to left/right volumes
        val leftVolume: Float
        val rightVolume: Float

        if (pan < 0) {
            leftVolume = 1.0f
            rightVolume = 1.0f + pan // pan is negative
        } else {
            leftVolume = 1.0f - pan
            rightVolume = 1.0f
        }

        val actualVolume = volume * sfxVolume * masterVolume

        val streamId = soundPool?.play(
            soundId,
            leftVolume * actualVolume,
            rightVolume * actualVolume,
            1,
            0,
            1.0f
        )

        if (streamId != null && streamId != -1) {
            soundPlayers[name] = streamId
        }

        return streamId
    }

    /**
     * Dừng một sound đang play
     */
    fun stopSound(name: String) {
        val streamId = soundPlayers[name]
        if (streamId != null) {
            soundPool?.stop(streamId)
            soundPlayers.remove(name)
            Log.d(TAG, "Stopped sound: $name")
        }
    }

    /**
     * Dừng tất cả sounds
     */
    fun stopAllSounds() {
        soundPool?.autoResume() // Stop all
        soundPlayers.clear()
        Log.d(TAG, "Stopped all sounds")
    }

    /**
     * Pause một sound
     */
    fun pauseSound(name: String) {
        // SoundPool không có pause per sound, cần autoResume implementation
        stopSound(name)
    }

    /**
     * Resume một sound
     */
    fun resumeSound(name: String) {
        playSound(name)
    }

    /**
     * Set volume cho một sound đang play
     */
    fun setSoundVolume(name: String, volume: Float) {
        val streamId = soundPlayers[name]
        if (streamId != null) {
            val actualVolume = volume * sfxVolume * masterVolume
            soundPool?.setVolume(streamId, actualVolume, actualVolume)
        }
    }

    /**
     * Set pitch cho một sound (tốc độ play)
     */
    fun setSoundRate(name: String, rate: Float) {
        val streamId = soundPlayers[name]
        if (streamId != null) {
            soundPool?.setRate(streamId, rate)
        }
    }

    /**
     * Cập nhật vị trí listener cho 3D audio
     */
    fun updateListenerPosition(x: Float, y: Float, z: Float) {
        listenerX = x
        listenerY = y
        listenerZ = z
    }

    /**
     * Pause audio engine
     */
    fun pause() {
        if (!isInitialized) return
        isPaused = true
        soundPool?.autoPause()
        Log.d(TAG, "AudioEngine paused")
    }

    /**
     * Resume audio engine
     */
    fun resume() {
        if (!isInitialized) return
        isPaused = false
        soundPool?.autoResume()
        Log.d(TAG, "AudioEngine resumed")
    }

    /**
     * Shutdown audio engine
     */
    fun shutdown() {
        Log.i(TAG, "Shutting down AudioEngine")
        stopAllSounds()
        soundPool?.release()
        soundPool = null
        audioTrack?.release()
        audioTrack = null
        soundIds.clear()
        soundPlayers.clear()
        isInitialized = false
    }

    /**
     * Kiểm tra sound có sẵn không
     */
    fun hasSound(name: String): Boolean {
        return soundIds.containsKey(name)
    }

    /**
     * Preload một sound để giảm latency
     */
    fun preloadSound(name: String) {
        // SoundPool tự động preload khi load
        Log.d(TAG, "Preloading sound: $name")
    }

    /**
     * Lấy danh sách tất cả sounds đã load
     */
    fun getLoadedSounds(): List<String> {
        return soundIds.keys.toList()
    }

    /**
     * Set âm lượng hiệu ứng âm thanh
     */
    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
        onVolumeChanged?.invoke(sfxVolume, musicVolume)
        Log.d(TAG, "SFX volume set to: $sfxVolume")
    }

    /**
     * Set âm lượng nhạc nền
     */
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        onVolumeChanged?.invoke(sfxVolume, musicVolume)
        Log.d(TAG, "Music volume set to: $musicVolume")
    }

    /**
     * Set tất cả volumes từ preferences
     */
    fun setVolumesFromPreferences(sfx: Float, music: Float) {
        sfxVolume = sfx.coerceIn(0f, 1f)
        musicVolume = music.coerceIn(0f, 1f)
        Log.d(TAG, "Volumes set from prefs - SFX: $sfxVolume, Music: $musicVolume")
    }
}
