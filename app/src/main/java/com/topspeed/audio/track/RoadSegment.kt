package com.topspeed.audio.track

/**
 * Surface type enum - port từ game gốc
 */
enum class SurfaceType {
    ASPHALT,    // Đường nhựa
    GRAVEL,     // Đường đá sỏi
    WATER,      // Nước
    SAND,       // Cát
    SNOW        // Tuyết
}

/**
 * Curve direction enum - port từ game gốc
 */
enum class CurveDirection {
    STRAIGHT,       // Thẳng
    EASY_LEFT,      // Cong nhẹ trái
    LEFT,           // Cong trái
    HARD_LEFT,      // Cong gắt trái
    HAIRPIN_LEFT,   // Cong tóc bẹp trái
    EASY_RIGHT,     // Cong nhẹ phải
    RIGHT,          // Cong phải
    HARD_RIGHT,     // Cong gắt phải
    HAIRPIN_RIGHT   // Cong tóc bẹp phải
}

/**
 * Đoạn đường - một phần của track
 * Port từ Track::Road trong game gốc
 */
data class RoadSegment(
    val startPosition: Int,     // Vị trí bắt đầu (pixels từ start line)
    val endPosition: Int,       // Vị trí kết thúc
    val curveDirection: CurveDirection,  // Hướng cong của đoạn
    val surfaceType: SurfaceType,        // Loại bề mặt
    val laneWidth: Int          // Chiều rộng đường (pixels)
) {
    /**
     * Chiều dài đoạn đường
     */
    val length: Int
        get() = endPosition - startPosition

    /**
     * Kiểm tra vị trí có thuộc đoạn này không
     */
    fun contains(position: Int): Boolean {
        return position >= startPosition && position < endPosition
    }

    /**
     * Get progress through segment (0.0 - 1.0)
     */
    fun getProgress(position: Int): Float {
        if (position < startPosition) return 0f
        if (position > endPosition) return 1f
        return (position - startPosition).toFloat() / length
    }

    /**
     * Get position within segment
     */
    fun getPositionInSegment(position: Int): Int {
        return position - startPosition
    }

    /**
     * Is this a straight segment?
     */
    fun isStraight(): Boolean {
        return curveDirection == CurveDirection.STRAIGHT
    }

    /**
     * Is this a curve?
     */
    fun isCurve(): Boolean {
        return curveDirection != CurveDirection.STRAIGHT
    }

    /**
     * Is this a hard curve?
     */
    fun isHardCurve(): Boolean {
        return curveDirection == CurveDirection.HARD_LEFT ||
               curveDirection == CurveDirection.HARD_RIGHT ||
               curveDirection == CurveDirection.HAIRPIN_LEFT ||
               curveDirection == CurveDirection.HAIRPIN_RIGHT
    }

    /**
     * Get surface friction coefficient
     */
    fun getFriction(): Float {
        return when (surfaceType) {
            SurfaceType.ASPHALT -> 1.0f
            SurfaceType.GRAVEL -> 0.7f
            SurfaceType.WATER -> 0.5f
            SurfaceType.SAND -> 0.4f
            SurfaceType.SNOW -> 0.3f
        }
    }

    /**
     * Get recommended speed for this segment
     */
    fun getRecommendedSpeed(): Float {
        val baseSpeed = when (curveDirection) {
            CurveDirection.STRAIGHT -> 180f
            CurveDirection.EASY_LEFT, CurveDirection.EASY_RIGHT -> 120f
            CurveDirection.LEFT, CurveDirection.RIGHT -> 90f
            CurveDirection.HARD_LEFT, CurveDirection.HARD_RIGHT -> 60f
            CurveDirection.HAIRPIN_LEFT, CurveDirection.HAIRPIN_RIGHT -> 30f
        }

        // Adjust for surface
        return baseSpeed * getFriction()
    }
}
