package com.topspeed.audio.network

/**
 * Cấu hình server cho multiplayer
 * Kết nối đến server Node.js đang chạy dự án 2 trên Railway
 */
object ServerConfig {

    // Server URL của bạn (đã deploy trên Railway)
    const val RAILWAY_SERVER_URL = "https://topspeedapi-production.up.railway.app"

    // Local: http://localhost:3000 (emulator)
    // LAN: http://192.168.x.x:3000 (điện thoại thật cùng mạng)
    const val DEFAULT_SERVER_URL = "http://10.0.2.2:3000"  // Emulator localhost
    const val LAN_SERVER_URL = "http://192.168.1.100:3000" // LAN IP - THAY ĐỔI THEO IP MÁY BẠN

    // Production server - SỬ DỤNG URL CỦA MÁY BẠN
    const val PRODUCTION_SERVER_URL = RAILWAY_SERVER_URL  // https://topspeedapi-production.up.railway.app

    // Game room settings
    const val MAX_PLAYERS = 4
    const val ROOM_CODE_LENGTH = 4

    // Connection settings
    const val CONNECTION_TIMEOUT = 10000L // ms
    const val RECONNECT_DELAY = 3000L // ms
    const val MAX_RECONNECT_ATTEMPTS = 5

    // Sync settings
    const val SYNC_INTERVAL = 50L // ms (20 updates/second)
    const val LAG_COMPENSATION = 100L // ms

    /**
     * Lấy server URL hiện tại - MẶC ĐỊNH LÀ RAILWAY
     * Có thể thay đổi theo environment
     */
    var currentServerUrl: String = PRODUCTION_SERVER_URL  // Mặc định kết nối Railway
        private set

    /**
     * Set server URL theo môi trường
     */
    fun setServerUrl(url: String) {
        currentServerUrl = url
    }

    /**
     * Set local server (cho emulator)
     */
    fun useLocalServer() {
        currentServerUrl = DEFAULT_SERVER_URL
    }

    /**
     * Set LAN server (cho điện thoại thật)
     * @param ip Địa chỉ IP của máy chạy server
     */
    fun useLanServer(ip: String) {
        currentServerUrl = "http://$ip:3000"
    }

    /**
     * Set production server (Railway)
     */
    fun useProductionServer() {
        currentServerUrl = PRODUCTION_SERVER_URL
    }

    /**
     * Kiểm tra có phải localhost không
     */
    fun isLocalhost(): Boolean {
        return currentServerUrl.contains("localhost") ||
               currentServerUrl.contains("10.0.2.2") ||
               currentServerUrl.contains("127.0.0.1")
    }
}
