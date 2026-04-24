/**
 * Track class header - port từ Track.cpp của game gốc Top Speed 3
 */

#ifndef TOPSPEED_TRACK_H
#define TOPSPEED_TRACK_H

#include <string>
#include <vector>

namespace topspeed {

// Surface types - port từ game gốc
enum class SurfaceType {
    ASPHALT,
    GRAVEL,
    WATER,
    SAND,
    SNOW
};

// Curve directions - port từ game gốc
enum class CurveDirection {
    STRAIGHT,
    EASY_LEFT,
    LEFT,
    HARD_LEFT,
    HAIRPIN_LEFT,
    EASY_RIGHT,
    RIGHT,
    HARD_RIGHT,
    HAIRPIN_RIGHT
};

// Road segment
struct RoadSegment {
    int startPosition;
    int endPosition;
    CurveDirection curveDirection;
    SurfaceType surfaceType;
    int laneWidth;

    int getLength() const { return endPosition - startPosition; }
    bool contains(int position) const {
        return position >= startPosition && position < endPosition;
    }
    float getFriction() const {
        switch (surfaceType) {
            case SurfaceType::ASPHALT: return 1.0f;
            case SurfaceType::GRAVEL: return 0.7f;
            case SurfaceType::WATER: return 0.5f;
            case SurfaceType::SAND: return 0.4f;
            case SurfaceType::SNOW: return 0.3f;
        }
        return 1.0f;
    }
};

// Track feature for announcements
struct TrackFeature {
    int position;
    enum Type {
        CURVE,
        SURFACE_CHANGE
    } type;
    CurveDirection curveDirection;
    SurfaceType newSurface;
    bool announced;

    TrackFeature() : position(0), type(CURVE), curveDirection(CurveDirection::STRAIGHT),
                     newSurface(SurfaceType::ASPHALT), announced(false) {}
};

class Track {
public:
    Track(int trackId = 0);
    virtual ~Track();

    bool load();
    void unload();

    // Get segment at position
    RoadSegment* getSegmentAt(float position);

    // Get upcoming features
    std::vector<TrackFeature*> getUpcomingFeatures(float position, int lookAhead = 200);

    // Get surface at position
    SurfaceType getSurfaceAt(float position);

    // Get curve direction at position
    CurveDirection getCurveDirectionAt(float position);

    // Check if on track
    bool isOnTrack(float positionX) const;

    // Getters
    float getLength() const { return m_length; }
    int getLaneWidth() const { return m_laneWidth; }
    const std::string& getName() const { return m_name; }

    // Lap records
    void saveLapRecord(int timeMs, const std::string& driver);
    int getBestLapTime() const { return m_bestLapTime; }

private:
    void createDefaultTrack();
    void createCircuitTrack();
    void createAdventureTrack();

    int m_trackId;
    std::string m_name;
    float m_length;
    int m_laneWidth;

    std::vector<RoadSegment> m_segments;
    std::vector<TrackFeature> m_features;

    int m_bestLapTime;
    bool m_loaded;
};

} // namespace topspeed

#endif // TOPSPEED_TRACK_H
