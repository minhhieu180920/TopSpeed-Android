package com.topspeed.audio.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * Quản lý Google Text-To-Speech cho game
 * Dùng để đọc menu, thông tin race, số đếm, thời gian
 */
class TTSManager(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "TTSManager"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var speechRate = 1.0f
    private var pitch = 1.0f
    private var volume = 1.0f

    // Queue cho các announcement quan trọng
    private val announcementQueue = mutableListOf<String>()
    private var isSpeaking = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language - ưu tiên tiếng Việt, fallback English
            val languageResult = tts?.setLanguage(Locale("vi", "VN"))

            if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English
                tts?.setLanguage(Locale.ENGLISH)
                Log.w(TAG, "Vietnamese not supported, using English")
            }

            isInitialized = true
            Log.i(TAG, "TTS initialized successfully")

            // Process any queued announcements
            processQueue()
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
        }
    }

    fun initialize() {
        tts = TextToSpeech(context, this)
    }

    /**
     * Đọc một chuỗi văn bản
     * @param text Văn bản cần đọc
     * @param priority Độ ưu tiên (cao sẽ đọc ngay, thấp sẽ chờ queue)
     * @param interrupt Có làm gián đoạn câu đang đọc không
     */
    fun speak(text: String, priority: Priority = Priority.NORMAL, interrupt: Boolean = false) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, queuing: $text")
            announcementQueue.add(text)
            return
        }

        Log.d(TAG, "Speaking: $text")

        val queueMode = if (interrupt || priority == Priority.HIGH) {
            TextToSpeech.QUEUE_FLUSH
        } else {
            TextToSpeech.QUEUE_ADD
        }

        tts?.speak(text, queueMode, null, null)
    }

    /**
     * Đọc số (dùng cho đếm vòng, thời gian, vị trí)
     */
    fun speakNumber(number: Int) {
        speak(number.toString(), Priority.NORMAL)
    }

    /**
     * Đọc thời gian theo định dạng phút:giây.phần trăm
     */
    fun speakTime(totalSeconds: Int) {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val timeString = when {
            minutes > 0 -> "$minutes phút $seconds giây"
            else -> "$seconds giây"
        }
        speak(timeString, Priority.HIGH)
    }

    /**
     * Đọc thời gian chi tiết (cho lap time)
     */
    fun speakTimeDetailed(totalSeconds: Int) {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val timeString = if (minutes > 0) {
            "$minutes phút $seconds giây"
        } else {
            "$seconds phẩy ${totalSeconds % 100} giây"
        }
        speak(timeString, Priority.HIGH)
    }

    /**
     * Đọc phần trăm
     */
    fun speakPercent(percent: Int) {
        speak("$percent phần trăm", Priority.NORMAL)
    }

    /**
     * Dừng đọc ngay lập tức
     */
    fun stop() {
        tts?.stop()
        isSpeaking = false
    }

    /**
     * Tạm dừng đọc
     */
    fun pause() {
        // TTS Android không có pause native, dùng stop thay thế
        // Có thể implement custom queue handling
    }

    /**
     * Tiếp tục đọc
     */
    fun resume() {
        processQueue()
    }

    /**
     * Đặt tốc độ đọc
     */
    fun setSpeechRate(rate: Float) {
        speechRate = rate
        tts?.setSpeechRate(rate)
    }

    /**
     * Đặt giọng cao độ
     */
    fun setPitch(pitch: Float) {
        this.pitch = pitch
        tts?.setPitch(pitch)
    }

    /**
     * Đặt âm lượng TTS
     */
    fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
    }

    /**
     * Lấy âm lượng TTS hiện tại
     */
    fun getVolume(): Float = volume

    /**
     * Kiểm tra đang đọc không
     */
    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }

    /**
     * Đặt ngôn ngữ
     */
    fun setLanguage(locale: Locale): Boolean {
        val result = tts?.setLanguage(locale)
        return result != TextToSpeech.LANG_MISSING_DATA &&
               result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    /**
     * Xử lý queue announcements
     */
    private fun processQueue() {
        if (announcementQueue.isEmpty()) return

        val text = announcementQueue.removeAt(0)
        speak(text, Priority.NORMAL, false)

        // Process next after delay
        if (announcementQueue.isNotEmpty()) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                processQueue()
            }, 1500)
        }
    }

    /**
     * Shutdown TTS
     */
    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.i(TAG, "TTS shutdown")
    }

    enum class Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
