package com.topspeed.audio.game

/**
 * Các trạng thái của game
 */
enum class GameState {
    MENU,           // Đang ở menu chính
    RACING,         // Đang đua
    COUNTDOWN,      // Đếm ngược trước khi bắt đầu
    PAUSED,         // Tạm dừng
    FINISHED,       // Hoàn thành cuộc đua
    CRASHED         // Xe bị va chạm
}

/**
 * Các chế độ game
 */
enum class GameMode {
    QUICK_START,    // Khởi động nhanh
    TIME_TRIAL,     // Thử thách thời gian
    SINGLE_RACE,    // Đua đơn
    MULTIPLAYER,    // Nhiều người chơi
    CAREER          // Sự nghiệp
}

/**
 * Thông tin lap hiện tại
 */
data class LapInfo(
    val currentLap: Int,
    val totalLaps: Int,
    val lapTime: Int,         // ms
    val bestLapTime: Int,     // ms
    val totalTime: Int,       // ms
    val position: Int,
    val totalRacers: Int,
    val speed: Int,           // km/h
    val gear: Int
)

/**
 * Loại bề mặt đường
 */
enum class SurfaceType {
    ASPHALT,    // Đường nhựa
    GRAVEL,     // Đường đá
    WATER,      // Nước
    SAND,       // Cát
    SNOW        // Tuyết
}
