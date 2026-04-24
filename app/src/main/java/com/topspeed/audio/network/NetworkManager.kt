package com.topspeed.audio.network

import android.content.Context
import android.util.Log
import com.topspeed.audio.audio.TTSManager
import com.topspeed.audio.game.GameState
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Network Manager - Quản lý kết nối đến server multiplayer
 * Sử dụng Socket.io protocol
 */
class NetworkManager(
    private val ttsManager: TTSManager,
    private val context: Context
) {
    companion object {
        private const val TAG = "NetworkManager"
    }

    // User preferences
    private val userPrefs = UserPreferences(context)

    // Connection state
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING
    }

    var connectionState = ConnectionState.DISCONNECTED
        private set

    var isConnected = false
        private set

    // Player info
    var playerId: String? = null
        private set
    var playerName: String = userPrefs.playerName  // Load from preferences
    var roomCode: String? = null
        private set
    var isHost = false

    // Listeners
    private val eventListeners = mutableMapOf<String, MutableList<(data: JSONObject) -> Unit>>()

    // HTTP client for REST calls
    private var baseUrl: String = ServerConfig.currentServerUrl

    /**
     * Kết nối đến server
     */
    fun connect(serverUrl: String = ServerConfig.currentServerUrl) {
        baseUrl = serverUrl
        ServerConfig.currentServerUrl = serverUrl

        Log.i(TAG, "Connecting to server: $serverUrl")

        if (connectionState == ConnectionState.CONNECTING ||
            connectionState == ConnectionState.CONNECTED) {
            Log.w(TAG, "Already connecting or connected")
            return
        }

        connectionState = ConnectionState.CONNECTING

        Thread {
            try {
                // Test connection với ping endpoint
                val response = httpGet("$serverUrl/api/topspeed/ping")

                if (response != null) {
                    connectionState = ConnectionState.CONNECTED
                    isConnected = true
                    Log.i(TAG, "Connected to server successfully")
                    ttsManager.speak("Đã kết nối server", TTSManager.Priority.HIGH)

                    // Emit connect event
                    emitEvent("connect", JSONObject().put("success", true))
                } else {
                    throw Exception("No response from server")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed: ${e.message}")
                connectionState = ConnectionState.DISCONNECTED
                isConnected = false
                ttsManager.speak("Kết nối thất bại", TTSManager.Priority.HIGH)
                emitEvent("connect_error", JSONObject().put("error", e.message))
            }
        }.start()
    }

    /**
     * Ngắt kết nối
     */
    fun disconnect() {
        Log.i(TAG, "Disconnecting from server")

        connectionState = ConnectionState.DISCONNECTED
        isConnected = false
        playerId = null
        roomCode = null

        ttsManager.speak("Đã ngắt kết nối", TTSManager.Priority.HIGH)
        emitEvent("disconnect", JSONObject())
    }

    /**
     * Tạo phòng mới (Host)
     */
    fun createRoom(trackId: Int = 0, laps: Int = 3): String? {
        if (!isConnected) {
            Log.w(TAG, "Cannot create room: not connected")
            return null
        }

        Log.i(TAG, "Creating room...")

        try {
            val body = JSONObject().apply {
                put("trackId", trackId)
                put("laps", laps)
                put("playerName", playerName)
            }

            val response = httpPost("$baseUrl/api/topspeed/room/create", body)

            if (response != null) {
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    roomCode = json.getString("roomCode")
                    playerId = json.getString("playerId")
                    isHost = true

                    Log.i(TAG, "Room created: $roomCode")
                    ttsManager.speak("Đã tạo phòng. Mã phòng: $roomCode", TTSManager.Priority.HIGH)

                    emitEvent("room_created", json)
                    return roomCode
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create room failed: ${e.message}")
        }

        ttsManager.speak("Tạo phòng thất bại", TTSManager.Priority.HIGH)
        return null
    }

    /**
     * Tham gia phòng
     */
    fun joinRoom(code: String): Boolean {
        if (!isConnected) {
            Log.w(TAG, "Cannot join room: not connected")
            return false
        }

        Log.i(TAG, "Joining room: $code")

        try {
            val body = JSONObject().apply {
                put("roomCode", code)
                put("playerName", playerName)
            }

            val response = httpPost("$baseUrl/api/topspeed/room/join", body)

            if (response != null) {
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    roomCode = code
                    playerId = json.getString("playerId")
                    isHost = false

                    Log.i(TAG, "Joined room: $roomCode")
                    ttsManager.speak("Đã vào phòng $code", TTSManager.Priority.HIGH)

                    emitEvent("room_joined", json)
                    return true
                } else {
                    val error = json.optString("error", "Không thể vào phòng")
                    Log.w(TAG, "Join room failed: $error")
                    ttsManager.speak(error, TTSManager.Priority.HIGH)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Join room failed: ${e.message}")
        }

        return false
    }

    /**
     * Rời phòng
     */
    fun leaveRoom() {
        if (roomCode == null) return

        Log.i(TAG, "Leaving room: $roomCode")

        try {
            val body = JSONObject().apply {
                put("roomCode", roomCode)
                put("playerId", playerId)
            }

            httpPost("$baseUrl/api/topspeed/room/leave", body)

        } catch (e: Exception) {
            Log.e(TAG, "Leave room failed: ${e.message}")
        }

        roomCode = null
        isHost = false
        ttsManager.speak("Đã rời phòng", TTSManager.Priority.HIGH)
        emitEvent("room_left", JSONObject())
    }

    /**
     * Lấy danh sách phòng
     */
    fun getRoomList(): List<RoomInfo> {
        if (!isConnected) return emptyList()

        try {
            val response = httpGet("$baseUrl/api/topspeed/room/list")
            if (response != null) {
                val json = JSONObject(response)
                val rooms = json.getJSONArray("rooms")
                val result = mutableListOf<RoomInfo>()

                for (i in 0 until rooms.length()) {
                    val room = rooms.getJSONObject(i)
                    result.add(RoomInfo(
                        code = room.getString("code"),
                        hostName = room.getString("hostName"),
                        playerCount = room.getInt("playerCount"),
                        trackName = room.optString("trackName", "Unknown"),
                        maxPlayers = room.optInt("maxPlayers", ServerConfig.MAX_PLAYERS)
                    ))
                }

                return result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get room list failed: ${e.message}")
        }

        return emptyList()
    }

    /**
     * Gửi race state đến server
     */
    fun sendRaceState(state: RaceStateData) {
        if (!isConnected || roomCode == null) return

        try {
            val body = JSONObject().apply {
                put("roomCode", roomCode)
                put("playerId", playerId)
                put("position", state.position)
                put("speed", state.speed)
                put("lap", state.lap)
                put("finished", state.finished)
                put("timestamp", System.currentTimeMillis())
            }

            // Gửi async (không đợi response)
            Thread {
                httpPost("$baseUrl/api/topspeed/race/state", body)
            }.start()

        } catch (e: Exception) {
            Log.e(TAG, "Send race state failed: ${e.message}")
        }
    }

    /**
     * Bắt đầu race (chỉ host)
     */
    fun startRace(): Boolean {
        if (!isHost || roomCode == null) {
            Log.w(TAG, "Only host can start race")
            return false
        }

        Log.i(TAG, "Starting race...")

        try {
            val body = JSONObject().apply {
                put("roomCode", roomCode)
                put("playerId", playerId)
            }

            val response = httpPost("$baseUrl/api/topspeed/race/start", body)

            if (response != null) {
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    ttsManager.speak("Bắt đầu đua!", TTSManager.Priority.HIGH)
                    emitEvent("race_started", json)
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Start race failed: ${e.message}")
        }

        return false
    }

    // ============ Event System ============

    /**
     * Đăng ký event listener
     */
    fun on(event: String, callback: (data: JSONObject) -> Unit) {
        val listeners = eventListeners.getOrPut(event) { mutableListOf() }
        listeners.add(callback)
    }

    /**
     * Xóa event listener
     */
    fun off(event: String, callback: (data: JSONObject) -> Unit) {
        eventListeners[event]?.remove(callback)
    }

    /**
     * Emit event nội bộ
     */
    private fun emitEvent(event: String, data: JSONObject) {
        eventListeners[event]?.forEach { it(data) }
    }

    // ============ HTTP Helpers ============

    private fun httpGet(url: String): String? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = ServerConfig.CONNECTION_TIMEOUT.toInt()
            connection.readTimeout = 5000

            val response = StringBuilder()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                }
            }
            connection.disconnect()
            response.toString()
        } catch (e: Exception) {
            Log.e(TAG, "HTTP GET failed: ${e.message}")
            null
        }
    }

    private fun httpPost(url: String, body: JSONObject): String? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = ServerConfig.CONNECTION_TIMEOUT.toInt()
            connection.readTimeout = 5000

            connection.outputStream.use { os ->
                os.write(body.toString().toByteArray())
            }

            val response = StringBuilder()
            if (connection.responseCode == HttpURLConnection.HTTP_OK ||
                connection.responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                }
            }
            connection.disconnect()
            response.toString()
        } catch (e: Exception) {
            Log.e(TAG, "HTTP POST failed: ${e.message}")
            null
        }
    }
}

/**
 * Room info data
 */
data class RoomInfo(
    val code: String,
    val hostName: String,
    val playerCount: Int,
    val trackName: String,
    val maxPlayers: Int
)

/**
 * Race state data gửi lên server
 */
data class RaceStateData(
    val position: Float,
    val speed: Float,
    val lap: Int,
    val finished: Boolean
)
