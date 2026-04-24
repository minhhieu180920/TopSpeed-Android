package com.topspeed.audio.network

import android.content.Context
import android.util.Log
import com.topspeed.audio.audio.TTSManager
import org.json.JSONObject

/**
 * Multiplayer Manager - Quản lý multiplayer cho TopSpeed
 * Kết nối đến server Node.js
 */
class MultiplayerManager(
    private val ttsManager: TTSManager,
    context: Context
) {
    companion object {
        private const val TAG = "MultiplayerManager"
    }

    // User preferences
    private val userPrefs = UserPreferences(context)

    // Network manager
    private val networkManager = NetworkManager(ttsManager, context)

    // Game state
    var isInRoom = false
        private set
    var isRaceStarted = false
        private set
    var isHost = false
        private set

    // Players in room
    private val players = mutableMapOf<String, PlayerInfo>()

    // Current player name from preferences
    var playerName: String = userPrefs.playerName

    // Current player name from preferences
    var playerName: String = userPrefs.playerName

    // Listeners
    var onPlayerJoined: ((PlayerInfo) -> Unit)? = null
    var onPlayerLeft: ((String) -> Unit)? = null
    var onRaceStart: (() -> Unit)? = null
    var onPlayerStateUpdate: ((String, RaceStateData) -> Unit)? = null
    var onRoomUpdate: ((List<RoomInfo>) -> Unit)? = null
    var onConnectionStateChanged: ((Boolean) -> Unit)? = null

    init {
        setupNetworkListeners()
    }

    private fun setupNetworkListeners() {
        networkManager.on("connect") { data ->
            Log.i(TAG, "Connected to server")
            onConnectionStateChanged?.invoke(true)
        }

        networkManager.on("connect_error") { data ->
            Log.e(TAG, "Connection error: ${data.optString("error")}")
            onConnectionStateChanged?.invoke(false)
        }

        networkManager.on("disconnect") { data ->
            Log.i(TAG, "Disconnected from server")
            onConnectionStateChanged?.invoke(false)
        }

        networkManager.on("room_created") { data ->
            Log.i(TAG, "Room created: ${data.optString("roomCode")}")
            isInRoom = true
            isHost = true
            ttsManager.speak("Đã tạo phòng thành công", TTSManager.Priority.HIGH)

            // Add self as player
            val playerId = data.optString("playerId")
            val playerName = data.optString("playerName", "Host")
            players[playerId] = PlayerInfo(playerId, playerName, isHost = true)
        }

        networkManager.on("room_joined") { data ->
            Log.i(TAG, "Joined room: ${data.optString("roomCode")}")
            isInRoom = true
            isHost = false

            // Add all players
            val playersArray = data.optJSONArray("players")
            playersArray?.let {
                for (i in 0 until it.length()) {
                    val player = it.getJSONObject(i)
                    val playerId = player.getString("id")
                    val playerName = player.getString("name")
                    players[playerId] = PlayerInfo(playerId, playerName, isHost = player.optBoolean("isHost", false))
                }
            }
        }

        networkManager.on("room_left") { data ->
            Log.i(TAG, "Left room")
            isInRoom = false
            isRaceStarted = false
            isHost = false
            players.clear()
        }

        networkManager.on("player_joined") { data ->
            val playerId = data.getString("playerId")
            val playerName = data.getString("playerName")
            players[playerId] = PlayerInfo(playerId, playerName, isHost = false)

            ttsManager.speak("Người chơi $playerName đã vào", TTSManager.Priority.HIGH)
            onPlayerJoined?.invoke(players[playerId]!!)
        }

        networkManager.on("player_left") { data ->
            val playerId = data.getString("playerId")
            val playerName = players[playerId]?.name ?: "Unknown"

            players.remove(playerId)
            ttsManager.speak("Người chơi $playerName đã rời", TTSManager.Priority.HIGH)
            onPlayerLeft?.invoke(playerId)
        }

        networkManager.on("race_started") { data ->
            Log.i(TAG, "Race starting!")
            isRaceStarted = true
            ttsManager.speak("Bắt đầu đua!", TTSManager.Priority.URGENT)
            onRaceStart?.invoke()
        }

        networkManager.on("player_state") { data ->
            val playerId = data.getString("playerId")
            if (playerId != networkManager.playerId) {
                val state = RaceStateData(
                    position = data.getDouble("position").toFloat(),
                    speed = data.getDouble("speed").toFloat(),
                    lap = data.getInt("lap"),
                    finished = data.getBoolean("finished")
                )
                onPlayerStateUpdate?.invoke(playerId, state)
            }
        }

        networkManager.on("race_finished") { data ->
            val playerId = data.getString("playerId")
            val playerName = players[playerId]?.name ?: "Unknown"
            val position = data.getInt("position")

            ttsManager.speak("Người chơi $playerName về đích thứ $position", TTSManager.Priority.HIGH)
        }
    }

    /**
     * Kết nối đến server
     */
    fun connect(serverUrl: String = ServerConfig.currentServerUrl) {
        networkManager.connect(serverUrl)
    }

    /**
     * Ngắt kết nối
     */
    fun disconnect() {
        if (isInRoom) {
            leaveRoom()
        }
        networkManager.disconnect()
    }

    /**
     * Tạo phòng mới
     */
    fun createRoom(trackId: Int = 0, laps: Int = 3): String? {
        val roomCode = networkManager.createRoom(trackId, laps)
        return roomCode
    }

    /**
     * Tham gia phòng
     */
    fun joinRoom(code: String): Boolean {
        return networkManager.joinRoom(code)
    }

    /**
     * Rời phòng
     */
    fun leaveRoom() {
        networkManager.leaveRoom()
        isInRoom = false
        isRaceStarted = false
        isHost = false
        players.clear()
    }

    /**
     * Lấy danh sách phòng
     */
    fun getAvailableRooms(): List<RoomInfo> {
        return networkManager.getRoomList()
    }

    /**
     * Bắt đầu race (chỉ host)
     */
    fun startRace(): Boolean {
        if (!isHost) {
            Log.w(TAG, "Only host can start race")
            return false
        }
        return networkManager.startRace()
    }

    /**
     * Gửi trạng thái race
     */
    fun sendRaceState(state: RaceStateData) {
        networkManager.sendRaceState(state)
    }

    /**
     * Kiểm tra đã kết nối chưa
     */
    fun isConnected(): Boolean {
        return networkManager.isConnected
    }

    /**
     * Lấy số người chơi trong phòng
     */
    fun getPlayerCount(): Int {
        return players.size
    }

    /**
     * Lấy thông tin người chơi
     */
    fun getPlayers(): Map<String, PlayerInfo> {
        return players.toMap()
    }

    /**
     * Lấy mã phòng hiện tại
     */
    fun getRoomCode(): String? {
        return networkManager.roomCode
    }

    /**
     * Set server URL
     */
    fun setServerUrl(url: String) {
        ServerConfig.currentServerUrl = url
    }

    /**
     * Set player name
     */
    fun setPlayerName(name: String) {
        playerName = name
        userPrefs.playerName = name
    }

    /**
     * Lấy tên người chơi hiện tại
     */
    fun getPlayerName(): String {
        return userPrefs.playerName
    }

    /**
     * Kiểm tra đã có tên người chơi chưa
     */
    fun hasPlayerName(): Boolean {
        return userPrefs.hasPlayerName()
    }

    /**
     * Lưu thông tin người chơi (từ server)
     */
    fun savePlayerInfo(id: String, name: String) {
        userPrefs.savePlayerInfo(id, name)
    }
}

/**
 * Thông tin người chơi
 */
data class PlayerInfo(
    val id: String,
    val name: String,
    val isHost: Boolean = false
)
