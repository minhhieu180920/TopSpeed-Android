package com.topspeed.audio.network

import android.content.Context
import android.content.SharedPreferences

/**
 * User Preferences - Lưu trữ thông tin người dùng cục bộ
 * Bao gồm tên người chơi, cài đặt game
 */
class UserPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "topspeed_prefs"

        // Keys
        private const val KEY_PLAYER_NAME = "player_name"
        private const val KEY_PLAYER_ID = "player_id"
        private const val KEY_LAST_ROOM = "last_room"
        private const val KEY_PREFERRED_TRACK = "preferred_track"
        private const val KEY_PREFERRED_LAPS = "preferred_laps"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_TILT_ENABLED = "tilt_enabled"

        // Volume keys (0.0 - 1.0)
        private const val KEY_TTS_VOLUME = "tts_volume"
        private const val KEY_MUSIC_VOLUME = "music_volume"
        private const val KEY_SFX_VOLUME = "sfx_volume"

        // Default values
        private const val DEFAULT_PLAYER_NAME = "Người Chơi"
        private const val DEFAULT_VOLUME = 0.8f
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ============ Player Name ============

    /**
     * Lấy tên người chơi
     */
    var playerName: String
        get() = prefs.getString(KEY_PLAYER_NAME, DEFAULT_PLAYER_NAME) ?: DEFAULT_PLAYER_NAME
        set(value) {
            prefs.edit().putString(KEY_PLAYER_NAME, value).apply()
        }

    /**
     * Lấy player ID đã lưu
     */
    var playerId: String?
        get() = prefs.getString(KEY_PLAYER_ID, null)
        set(value) {
            prefs.edit().putString(KEY_PLAYER_ID, value).apply()
        }

    /**
     * Kiểm tra đã có tên người dùng chưa
     */
    fun hasPlayerName(): Boolean {
        val name = prefs.getString(KEY_PLAYER_NAME, null)
        return !name.isNullOrBlank() && name != DEFAULT_PLAYER_NAME
    }

    /**
     * Lưu thông tin người chơi (từ server response)
     */
    fun savePlayerInfo(id: String, name: String) {
        prefs.edit()
            .putString(KEY_PLAYER_ID, id)
            .putString(KEY_PLAYER_NAME, name)
            .apply()
    }

    // ============ Room History ============

    /**
     * Lưu mã phòng cuối
     */
    var lastRoomCode: String?
        get() = prefs.getString(KEY_LAST_ROOM, null)
        set(value) {
            prefs.edit().putString(KEY_LAST_ROOM, value).apply()
        }

    // ============ Game Preferences ============

    /**
     * Track ưa thích
     */
    var preferredTrack: Int
        get() = prefs.getInt(KEY_PREFERRED_TRACK, 0)
        set(value) {
            prefs.edit().putInt(KEY_PREFERRED_TRACK, value).apply()
        }

    /**
     * Số vòng ưa thích
     */
    var preferredLaps: Int
        get() = prefs.getInt(KEY_PREFERRED_LAPS, 3)
        set(value) {
            prefs.edit().putInt(KEY_PREFERRED_LAPS, value).apply()
        }

    /**
     * Bật/tắt âm thanh
     */
    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
        }

    /**
     * Bật/tắt TTS
     */
    var ttsEnabled: Boolean
        get() = prefs.getBoolean(KEY_TTS_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_TTS_ENABLED, value).apply()
        }

    /**
     * Bật/tắt điều khiển bằng nghiêng điện thoại
     */
    var tiltEnabled: Boolean
        get() = prefs.getBoolean(KEY_TILT_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_TILT_ENABLED, value).apply()
        }

    // ============ Volume Settings ============

    /**
     * Âm lượng TTS (0.0 - 1.0)
     */
    var ttsVolume: Float
        get() = prefs.getFloat(KEY_TTS_VOLUME, DEFAULT_VOLUME)
        set(value) {
            prefs.edit().putFloat(KEY_TTS_VOLUME, value.coerceIn(0f, 1f)).apply()
        }

    /**
     * Âm lượng nhạc (0.0 - 1.0)
     */
    var musicVolume: Float
        get() = prefs.getFloat(KEY_MUSIC_VOLUME, DEFAULT_VOLUME)
        set(value) {
            prefs.edit().putFloat(KEY_MUSIC_VOLUME, value.coerceIn(0f, 1f)).apply()
        }

    /**
     * Âm lượng hiệu ứng (0.0 - 1.0)
     */
    var sfxVolume: Float
        get() = prefs.getFloat(KEY_SFX_VOLUME, DEFAULT_VOLUME)
        set(value) {
            prefs.edit().putFloat(KEY_SFX_VOLUME, value.coerceIn(0f, 1f)).apply()
        }

    /**
     * Xóa tất cả dữ liệu
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
}