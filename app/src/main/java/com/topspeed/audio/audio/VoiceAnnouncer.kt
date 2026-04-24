package com.topspeed.audio.audio

import android.util.Log

/**
 * Voice Announcer - đọc thông tin race cho người khiếm thị
 * Sử dụng TTS thay thế cho người lồng tiếng trong menu
 */
class VoiceAnnouncer(
    private val ttsManager: TTSManager
) {
    companion object {
        private const val TAG = "VoiceAnnouncer"
    }

    private var lastAnnouncement = ""
    private var lastAnnouncementTime = 0L
    private val announcementCooldown = 2000L // ms

    /**
     * Đọc thông tin race
     */
    fun announceRaceInfo(
        lap: Int,
        totalLaps: Int,
        position: Int,
        totalRacers: Int,
        speed: Int
    ) {
        val announcement = "Vòng $lap trên $totalLaps. Vị trí thứ $position trên $totalRacers. Tốc độ $speed km/h"

        if (canAnnounce(announcement)) {
            ttsManager.speak(announcement, TTSManager.Priority.HIGH)
            lastAnnouncement = announcement
            lastAnnouncementTime = System.currentTimeMillis()
        }
    }

    /**
     * Đọc thời gian lap
     */
    fun announceLapTime(timeMs: Int) {
        val seconds = timeMs / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        val announcement = if (minutes > 0) {
            "Thời gian vòng: $minutes phút $remainingSeconds giây"
        } else {
            "Thời gian vòng: $seconds giây"
        }

        if (canAnnounce(announcement)) {
            ttsManager.speak(announcement, TTSManager.Priority.HIGH)
            lastAnnouncement = announcement
            lastAnnouncementTime = System.currentTimeMillis()
        }
    }

    /**
     * Đọc vị trí
     */
    fun announcePosition(position: Int, total: Int) {
        val ordinal = getOrdinal(position)
        val announcement = "$ordinal trên $total"

        if (canAnnounce(announcement)) {
            ttsManager.speak(announcement, TTSManager.Priority.HIGH)
            lastAnnouncement = announcement
            lastAnnouncementTime = System.currentTimeMillis()
        }
    }

    /**
     * Đọc tốc độ
     */
    fun announceSpeed(speed: Int) {
        val announcement = "$speed km/h"

        // Chỉ đọc khi speed thay đổi đáng kể
        if (canAnnounce(announcement) && speed % 20 == 0) {
            ttsManager.speak(announcement)
            lastAnnouncement = announcement
            lastAnnouncementTime = System.currentTimeMillis()
        }
    }

    /**
     * Đọc số gear
     */
    fun announceGear(gear: Int) {
        val announcement = if (gear == 0) {
            "Số N"
        } else {
            "Số $gear"
        }

        if (canAnnounce(announcement)) {
            ttsManager.speak(announcement)
            lastAnnouncement = announcement
            lastAnnouncementTime = System.currentTimeMillis()
        }
    }

    /**
     * Đọc cảnh báo curve
     */
    fun announceCurve(direction: String) {
        if (canAnnounce(direction)) {
            ttsManager.speak(direction, TTSManager.Priority.HIGH)
            lastAnnouncement = direction
            lastAnnouncementTime = System.currentTimeMillis()
        }
    }

    /**
     * Đọc loại bề mặt đường
     */
    fun announceSurface(surface: String) {
        if (canAnnounce(surface)) {
            ttsManager.speak(surface)
            lastAnnouncement = surface
            lastAnnouncementTime = System.currentTimeMillis()
        }
    }

    /**
     * Đọc thông tin lap nhanh nhất
     */
    fun announceBestLap(timeMs: Int) {
        val seconds = timeMs / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val millis = timeMs % 1000

        val announcement = if (minutes > 0) {
            "Lap nhanh nhất: $minutes phút $remainingSeconds giây"
        } else {
            "Lap nhanh nhất: $seconds phẩy ${millis / 10} giây"
        }

        ttsManager.speak(announcement, TTSManager.Priority.HIGH)
    }

    /**
     * Đọc số vòng hoàn thành
     */
    fun announceLapComplete(lap: Int) {
        val announcement = "Hoàn thành vòng $lap"
        ttsManager.speak(announcement, TTSManager.Priority.HIGH)
    }

    /**
     * Đọc countdown
     */
    fun announceCountdown(count: Int) {
        ttsManager.speak(count.toString(), TTSManager.Priority.URGENT)
    }

    /**
     * Đọc race start
     */
    fun announceRaceStart() {
        ttsManager.speak("Bắt đầu!", TTSManager.Priority.URGENT)
    }

    /**
     * Đọc race finish
     */
    fun announceRaceFinish(position: Int, totalTime: Int) {
        val seconds = totalTime / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        val timeString = if (minutes > 0) {
            "$minutes phút $remainingSeconds giây"
        } else {
            "$remainingSeconds giây"
        }

        ttsManager.speak("Hoàn thành cuộc đua! Vị trí thứ $position. Thời gian: $timeString", TTSManager.Priority.URGENT)
    }

    /**
     * Kiểm tra có thể đọc không (tránh đọc quá nhanh)
     */
    private fun canAnnounce(text: String): Boolean {
        // Luôn đọc nếu là text mới
        if (text != lastAnnouncement) {
            return true
        }

        // Kiểm tra cooldown
        val timeSinceLastAnnouncement = System.currentTimeMillis() - lastAnnouncementTime
        return timeSinceLastAnnouncement >= announcementCooldown
    }

    /**
     * Lấy số thứ tự tiếng Việt
     */
    private fun getOrdinal(n: Int): String {
        return when {
            n == 1 -> "thứ nhất"
            n == 2 -> "thứ hai"
            n == 3 -> "thứ ba"
            n == 4 -> "thứ tư"
            n == 5 -> "thứ năm"
            n == 6 -> "thứ sáu"
            n == 7 -> "thứ bảy"
            n == 8 -> "thứ tám"
            n == 9 -> "thứ chín"
            n == 10 -> "thứ mười"
            else -> "thứ $n"
        }
    }
}
