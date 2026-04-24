package com.topspeed.audio.track

import android.util.Log
import com.topspeed.audio.car.CarModel

/**
 * Track feature cho announcements
 */
sealed class TrackFeature {
    abstract val position: Int
    abstract var announced: Boolean

    data class Curve(
        override val position: Int,
        val direction: CurveDirection,
        override var announced: Boolean = false
    ) : TrackFeature()

    data class SurfaceChange(
        override val position: Int,
        val newSurface: SurfaceType,
        override var announced: Boolean = false
    ) : TrackFeature()
}

/**
 * Track feature cho announcements
 */
sealed class TrackFeature {
    abstract val position: Int
    abstract var announced: Boolean

    data class Curve(
        override val position: Int,
        val direction: CurveDirection,
        override var announced: Boolean = false
    ) : TrackFeature()

    data class SurfaceChange(
        override val position: Int,
        val newSurface: SurfaceType,
        override var announced: Boolean = false
    ) : TrackFeature()
}

/**
 * Model đường đua - port từ Track.cpp của game gốc
 * Quản lý thông tin track, các đoạn đường, curves, và surface
 */
class TrackModel(
    private val trackId: Int
) {
    companion object {
        private const val TAG = "TrackModel"

        // Track constants
        const val DEFAULT_TRACK_LENGTH = 50000    // pixels
        const val DEFAULT_LANE_WIDTH = 8000       // pixels
        const val CURVE_LOOKAHEAD = 200           // pixels
    }

    // Track data
    var length = DEFAULT_TRACK_LENGTH
        private set
    var laneWidth = DEFAULT_LANE_WIDTH
        private set
    var trackName = ""
        private set

    // Track segments
    private val roadSegments = mutableListOf<RoadSegment>()
    private val trackFeatures = mutableListOf<TrackFeature>()

    // Lap records
    private var bestLapTime = 0
    private var lapRecords = mutableListOf<LapRecord>()

    // Loaded state
    var isLoaded = false
        private set

    /**
     * Load track data
     */
    fun load(): Boolean {
        Log.d(TAG, "Loading track $trackId")

        when (trackId) {
            0 -> loadDefaultTrack()
            1 -> loadCircuitTrack()
            2 -> loadAdventureTrack()
            else -> loadDefaultTrack()
        }

        isLoaded = true
        Log.d(TAG, "Track loaded: $trackName, length=$length")
        return true
    }

    /**
     * Load default track (cơ bản cho quick start)
     */
    private fun loadDefaultTrack() {
        trackName = "Default Circuit"
        length = DEFAULT_TRACK_LENGTH

        // Create segments with various features
        createDefaultSegments()
    }

    /**
     * Load circuit track
     */
    private fun loadCircuitTrack() {
        trackName = "Professional Circuit"
        length = DEFAULT_TRACK_LENGTH * 2

        createCircuitSegments()
    }

    /**
     * Load adventure track
     */
    private fun loadAdventureTrack() {
        trackName = "Adventure Trail"
        length = DEFAULT_TRACK_LENGTH * 3

        createAdventureSegments()
    }

    /**
     * Tạo các đoạn đường default
     */
    private fun createDefaultSegments() {
        roadSegments.clear()
        trackFeatures.clear()

        var currentPosition = 0

        // Start straight
        roadSegments.add(RoadSegment(
            startPosition = 0,
            endPosition = 5000,
            curveDirection = CurveDirection.STRAIGHT,
            surfaceType = SurfaceType.ASPHALT,
            laneWidth = laneWidth
        ))
        trackFeatures.add(TrackFeature.Curve(5000, CurveDirection.STRAIGHT))

        // Easy left curve
        currentPosition = 5000
        roadSegments.add(RoadSegment(
            startPosition = currentPosition,
            endPosition = currentPosition + 3000,
            curveDirection = CurveDirection.EASY_LEFT,
            surfaceType = SurfaceType.ASPHALT,
            laneWidth = laneWidth
        ))
        trackFeatures.add(TrackFeature.Curve(currentPosition + 500, CurveDirection.EASY_LEFT))

        // Straight section
        currentPosition += 3000
        roadSegments.add(RoadSegment(
            startPosition = currentPosition,
            endPosition = currentPosition + 8000,
            curveDirection = CurveDirection.STRAIGHT,
            surfaceType = SurfaceType.ASPHALT,
            laneWidth = laneWidth
        ))

        // Hard right curve
        currentPosition += 8000
        roadSegments.add(RoadSegment(
            startPosition = currentPosition,
            endPosition = currentPosition + 4000,
            curveDirection = CurveDirection.HARD_RIGHT,
            surfaceType = SurfaceType.ASPHALT,
            laneWidth = laneWidth
        ))
        trackFeatures.add(TrackFeature.Curve(currentPosition + 500, CurveDirection.HARD_RIGHT))

        // Surface change to gravel
        currentPosition += 4000
        roadSegments.add(RoadSegment(
            startPosition = currentPosition,
            endPosition = currentPosition + 5000,
            curveDirection = CurveDirection.STRAIGHT,
            surfaceType = SurfaceType.GRAVEL,
            laneWidth = laneWidth
        ))
        trackFeatures.add(TrackFeature.SurfaceChange(currentPosition, SurfaceType.GRAVEL))

        // Hairpin left
        currentPosition += 5000
        roadSegments.add(RoadSegment(
            startPosition = currentPosition,
            endPosition = currentPosition + 3000,
            curveDirection = CurveDirection.HAIRPIN_LEFT,
            surfaceType = SurfaceType.GRAVEL,
            laneWidth = laneWidth
        ))
        trackFeatures.add(TrackFeature.Curve(currentPosition + 500, CurveDirection.HAIRPIN_LEFT))

        // Back to asphalt
        currentPosition += 3000
        roadSegments.add(RoadSegment(
            startPosition = currentPosition,
            endPosition = currentPosition + 10000,
            curveDirection = CurveDirection.STRAIGHT,
            surfaceType = SurfaceType.ASPHALT,
            laneWidth = laneWidth
        ))
        trackFeatures.add(TrackFeature.SurfaceChange(currentPosition, SurfaceType.ASPHALT))

        // Finish line
        length = currentPosition + 10000
    }

    /**
     * Tạo circuit track chuyên nghiệp
     */
    private fun createCircuitSegments() {
        roadSegments.clear()
        trackFeatures.clear()

        // Professional circuit with multiple curves
        var pos = 0

        // Start/Finish straight
        roadSegments.add(RoadSegment(pos, pos + 8000, CurveDirection.STRAIGHT, SurfaceType.ASPHALT, laneWidth))
        pos += 8000

        // Hairpin right (chicane)
        roadSegments.add(RoadSegment(pos, pos + 2000, CurveDirection.HAIRPIN_RIGHT, SurfaceType.ASPHALT, laneWidth))
        trackFeatures.add(TrackFeature.Curve(pos + 500, CurveDirection.HAIRPIN_RIGHT))
        pos += 2000

        // Long straight
        roadSegments.add(RoadSegment(pos, pos + 15000, CurveDirection.STRAIGHT, SurfaceType.ASPHALT, laneWidth))
        pos += 15000

        // Easy left
        roadSegments.add(RoadSegment(pos, pos + 4000, CurveDirection.EASY_LEFT, SurfaceType.ASPHALT, laneWidth))
        trackFeatures.add(TrackFeature.Curve(pos + 500, CurveDirection.EASY_LEFT))
        pos += 4000

        // Hard right
        roadSegments.add(RoadSegment(pos, pos + 5000, CurveDirection.HARD_RIGHT, SurfaceType.ASPHALT, laneWidth))
        trackFeatures.add(TrackFeature.Curve(pos + 500, CurveDirection.HARD_RIGHT))
        pos += 5000

        // Back to start
        roadSegments.add(RoadSegment(pos, length, CurveDirection.STRAIGHT, SurfaceType.ASPHALT, laneWidth))
    }

    /**
     * Tạo adventure track với nhiều surface types
     */
    private fun createAdventureSegments() {
        roadSegments.clear()
        trackFeatures.clear()

        var pos = 0

        // Start on asphalt
        roadSegments.add(RoadSegment(pos, pos + 5000, CurveDirection.STRAIGHT, SurfaceType.ASPHALT, laneWidth))
        pos += 5000

        // Curve left onto gravel
        roadSegments.add(RoadSegment(pos, pos + 4000, CurveDirection.LEFT, SurfaceType.GRAVEL, laneWidth))
        trackFeatures.add(TrackFeature.Curve(pos + 500, CurveDirection.LEFT))
        trackFeatures.add(TrackFeature.SurfaceChange(pos, SurfaceType.GRAVEL))
        pos += 4000

        // Water crossing
        roadSegments.add(RoadSegment(pos, pos + 3000, CurveDirection.STRAIGHT, SurfaceType.WATER, laneWidth))
        trackFeatures.add(TrackFeature.SurfaceChange(pos, SurfaceType.WATER))
        pos += 3000

        // Sand section
        roadSegments.add(RoadSegment(pos, pos + 6000, CurveDirection.EASY_RIGHT, SurfaceType.SAND, laneWidth))
        trackFeatures.add(TrackFeature.Curve(pos + 500, CurveDirection.EASY_RIGHT))
        trackFeatures.add(TrackFeature.SurfaceChange(pos, SurfaceType.SAND))
        pos += 6000

        // Snow section
        roadSegments.add(RoadSegment(pos, pos + 8000, CurveDirection.HARD_LEFT, SurfaceType.SNOW, laneWidth))
        trackFeatures.add(TrackFeature.Curve(pos + 500, CurveDirection.HARD_LEFT))
        trackFeatures.add(TrackFeature.SurfaceChange(pos, SurfaceType.SNOW))
        pos += 8000

        // Back to asphalt
        roadSegments.add(RoadSegment(pos, length, CurveDirection.STRAIGHT, SurfaceType.ASPHALT, laneWidth))
        trackFeatures.add(TrackFeature.SurfaceChange(pos, SurfaceType.ASPHALT))
    }

    /**
     * Get road segment tại vị trí
     */
    fun getRoadSegmentAt(position: Float): RoadSegment? {
        val normalizedPosition = position % length
        return roadSegments.find {
            normalizedPosition >= it.startPosition && normalizedPosition < it.endPosition
        }
    }

    /**
     * Get upcoming features (curves, surface changes)
     * @param position Vị trí hiện tại
     * @param lookAhead Khoảng cách nhìn về trước (pixels)
     */
    fun getUpcomingFeatures(position: Float, lookAhead: Int = CURVE_LOOKAHEAD): List<TrackFeature> {
        val normalizedPosition = position % length
        val endPosition = normalizedPosition + lookAhead

        return trackFeatures.filter { feature ->
            feature.position > normalizedPosition &&
            feature.position <= endPosition &&
            !feature.announced
        }
    }

    /**
     * Get surface type tại vị trí
     */
    fun getSurfaceAt(position: Float): SurfaceType {
        return getRoadSegmentAt(position)?.surfaceType ?: SurfaceType.ASPHALT
    }

    /**
     * Get curve direction tại vị trí
     */
    fun getCurveDirectionAt(position: Float): CurveDirection {
        return getRoadSegmentAt(position)?.curveDirection ?: CurveDirection.STRAIGHT
    }

    /**
     * Check if position is on track
     */
    fun isOnTrack(positionX: Float, positionY: Float): Boolean {
        return kotlin.math.abs(positionX) <= laneWidth / 2
    }

    /**
     * Get track width tại vị trí
     */
    fun getTrackWidthAt(position: Float): Float {
        return getRoadSegmentAt(position)?.laneWidth?.toFloat() ?: laneWidth.toFloat()
    }

    /**
     * Update car position on track
     */
    fun updateCarPosition(car: CarModel?) {
        if (car == null) return

        // Get current segment
        val segment = getRoadSegmentAt(car.position)

        // Apply surface effects
        segment?.let {
            when (it.surfaceType) {
                SurfaceType.ASPHALT -> {
                    // Normal grip
                }
                SurfaceType.GRAVEL -> {
                    // Reduced grip - slow down
                    car.speed *= 0.8f
                }
                SurfaceType.WATER -> {
                    // Hydroplane risk
                    car.speed *= 0.7f
                }
                SurfaceType.SAND -> {
                    // High resistance
                    car.speed *= 0.6f
                }
                SurfaceType.SNOW -> {
                    // Low grip
                    car.speed *= 0.5f
                }
            }
        }

        // Check if car is off track
        if (!isOnTrack(car.positionX, car.positionY)) {
            // Off track - slow down significantly
            car.speed *= 0.5f

            // Play off-track sound
            // audioEngine.playSound("en/gameplay/offtrack")
        }
    }

    /**
     * Get distance to finish line
     */
    fun getDistanceToFinish(position: Float): Float {
        val normalizedPosition = position % length
        return length - normalizedPosition
    }

    /**
     * Get lap percentage
     */
    fun getLapPercentage(position: Float): Float {
        val normalizedPosition = position % length
        return normalizedPosition / length
    }

    /**
     * Save lap record
     */
    fun saveLapRecord(lapTime: Int, driverName: String) {
        lapRecords.add(LapRecord(lapTime, driverName, System.currentTimeMillis()))
        lapRecords.sortBy { it.time }

        if (bestLapTime == 0 || lapTime < bestLapTime) {
            bestLapTime = lapTime
        }
    }

    /**
     * Get best lap time
     */
    fun getBestLapTime(): Int {
        return bestLapTime
    }

    /**
     * Get lap records
     */
    fun getLapRecords(): List<LapRecord> {
        return lapRecords.sortedBy { it.time }
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        roadSegments.clear()
        trackFeatures.clear()
        lapRecords.clear()
    }
}

/**
 * Lap record data
 */
data class LapRecord(
    val time: Int,          // ms
    val driver: String,
    val timestamp: Long
)
