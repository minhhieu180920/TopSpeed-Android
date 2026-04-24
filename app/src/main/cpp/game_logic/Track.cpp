/**
 * Track class implementation - port từ Track.cpp của game gốc Top Speed 3
 */

#include "Track.h"
#include <algorithm>

namespace topspeed {

const int DEFAULT_TRACK_LENGTH = 50000;
const int DEFAULT_LANE_WIDTH = 8000;

Track::Track(int trackId)
    : m_trackId(trackId)
    , m_name("")
    , m_length(DEFAULT_TRACK_LENGTH)
    , m_laneWidth(DEFAULT_LANE_WIDTH)
    , m_bestLapTime(0)
    , m_loaded(false)
{
}

Track::~Track() {
    unload();
}

bool Track::load() {
    if (m_loaded) return true;

    switch (m_trackId) {
        case 0:
            createDefaultTrack();
            break;
        case 1:
            createCircuitTrack();
            break;
        case 2:
            createAdventureTrack();
            break;
        default:
            createDefaultTrack();
    }

    m_loaded = true;
    return true;
}

void Track::unload() {
    m_segments.clear();
    m_features.clear();
    m_loaded = false;
}

void Track::createDefaultTrack() {
    m_name = "Default Circuit";
    m_segments.clear();
    m_features.clear();

    int pos = 0;

    // Start straight
    m_segments.push_back({0, 5000, CurveDirection::STRAIGHT, SurfaceType::ASPHALT, m_laneWidth});
    pos = 5000;

    // Easy left
    m_segments.push_back({pos, pos + 3000, CurveDirection::EASY_LEFT, SurfaceType::ASPHALT, m_laneWidth});
    m_features.push_back({pos + 500, TrackFeature::CURVE, CurveDirection::EASY_LEFT, SurfaceType::ASPHALT, false});
    pos += 3000;

    // Straight
    m_segments.push_back({pos, pos + 8000, CurveDirection::STRAIGHT, SurfaceType::ASPHALT, m_laneWidth});
    pos += 8000;

    // Hard right
    m_segments.push_back({pos, pos + 4000, CurveDirection::HARD_RIGHT, SurfaceType::ASPHALT, m_laneWidth});
    m_features.push_back({pos + 500, TrackFeature::CURVE, CurveDirection::HARD_RIGHT, SurfaceType::ASPHALT, false});
    pos += 4000;

    // Gravel section
    m_segments.push_back({pos, pos + 5000, CurveDirection::STRAIGHT, SurfaceType::GRAVEL, m_laneWidth});
    m_features.push_back({pos, TrackFeature::SURFACE_CHANGE, CurveDirection::STRAIGHT, SurfaceType::GRAVEL, false});
    pos += 5000;

    // Hairpin left
    m_segments.push_back({pos, pos + 3000, CurveDirection::HAIRPIN_LEFT, SurfaceType::GRAVEL, m_laneWidth});
    m_features.push_back({pos + 500, TrackFeature::CURVE, CurveDirection::HAIRPIN_LEFT, SurfaceType::GRAVEL, false});
    pos += 3000;

    // Back to asphalt - finish
    m_segments.push_back({pos, pos + 10000, CurveDirection::STRAIGHT, SurfaceType::ASPHALT, m_laneWidth});
    m_features.push_back({pos, TrackFeature::SURFACE_CHANGE, CurveDirection::STRAIGHT, SurfaceType::ASPHALT, false});

    m_length = pos + 10000;
}

void Track::createCircuitTrack() {
    m_name = "Professional Circuit";
    m_segments.clear();
    m_features.clear();

    int pos = 0;

    // Start/Finish straight
    m_segments.push_back({pos, pos + 8000, CurveDirection::STRAIGHT, SurfaceType::ASPHALT, m_laneWidth});
    pos += 8000;

    // Hairpin right
    m_segments.push_back({pos, pos + 2000, CurveDirection::HAIRPIN_RIGHT, SurfaceType::ASPHALT, m_laneWidth});
    m_features.push_back({pos + 500, TrackFeature::CURVE, CurveDirection::HAIRPIN_RIGHT, SurfaceType::ASPHALT, false});
    pos += 2000;

    // Long straight
    m_segments.push_back({pos, pos + 15000, CurveDirection::STRAIGHT, SurfaceType::ASPHALT, m_laneWidth});
    pos += 15000;

    // Easy left
    m_segments.push_back({pos, pos + 4000, CurveDirection::EASY_LEFT, SurfaceType::ASPHALT, m_laneWidth});
    m_features.push_back({pos + 500, TrackFeature::CURVE, CurveDirection::EASY_LEFT, SurfaceType::ASPHALT, false});
    pos += 4000;

    // Hard right
    m_segments.push_back({pos, pos + 5000, CurveDirection::HARD_RIGHT, SurfaceType::ASPHALT, m_laneWidth});
    m_features.push_back({pos + 500, TrackFeature::CURVE, CurveDirection::HARD_RIGHT, SurfaceType::ASPHALT, false});
    pos += 5000;

    // Back to start
    m_segments.push_back({pos, DEFAULT_TRACK_LENGTH * 2, CurveDirection::STRAIGHT, SurfaceType::ASPHALT, m_laneWidth});

    m_length = DEFAULT_TRACK_LENGTH * 2;
}

void Track::createAdventureTrack() {
    m_name = "Adventure Trail";
    m_segments.clear();
    m_features.clear();

    int pos = 0;

    // Start on asphalt
    m_segments.push_back({pos, pos + 5000, CurveDirection::STRAIGHT, SurfaceType::ASPHALT, m_laneWidth});
    pos += 5000;

    // Curve left onto gravel
    m_segments.push_back({pos, pos + 4000, CurveDirection::LEFT, SurfaceType::GRAVEL, m_laneWidth});
    m_features.push_back({pos + 500, TrackFeature::CURVE, CurveDirection::LEFT, SurfaceType::GRAVEL, false});
    m_features.push_back({pos, TrackFeature::SURFACE_CHANGE, CurveDirection::STRAIGHT, SurfaceType::GRAVEL, false});
    pos += 4000;

    // Water crossing
    m_segments.push_back({pos, pos + 3000, CurveDirection::STRAIGHT, SurfaceType::WATER, m_laneWidth});
    m_features.push_back({pos, TrackFeature::SURFACE_CHANGE, CurveDirection::STRAIGHT, SurfaceType::WATER, false});
    pos += 3000;

    // Sand section
    m_segments.push_back({pos, pos + 6000, CurveDirection::EASY_RIGHT, SurfaceType::SAND, m_laneWidth});
    m_features.push_back({pos + 500, TrackFeature::CURVE, CurveDirection::EASY_RIGHT, SurfaceType::SAND, false});
    m_features.push_back({pos, TrackFeature::SURFACE_CHANGE, CurveDirection::STRAIGHT, SurfaceType::SAND, false});
    pos += 6000;

    // Snow section
    m_segments.push_back({pos, pos + 8000, CurveDirection::HARD_LEFT, SurfaceType::SNOW, m_laneWidth});
    m_features.push_back({pos + 500, TrackFeature::CURVE, CurveDirection::HARD_LEFT, SurfaceType::SNOW, false});
    m_features.push_back({pos, TrackFeature::SURFACE_CHANGE, CurveDirection::STRAIGHT, SurfaceType::SNOW, false});
    pos += 8000;

    // Back to asphalt
    m_segments.push_back({pos, DEFAULT_TRACK_LENGTH * 3, CurveDirection::STRAIGHT, SurfaceType::ASPHALT, m_laneWidth});
    m_features.push_back({pos, TrackFeature::SURFACE_CHANGE, CurveDirection::STRAIGHT, SurfaceType::ASPHALT, false});

    m_length = DEFAULT_TRACK_LENGTH * 3;
}

RoadSegment* Track::getSegmentAt(float position) {
    int pos = static_cast<int>(position) % static_cast<int>(m_length);

    for (auto& segment : m_segments) {
        if (segment.contains(pos)) {
            return &segment;
        }
    }
    return nullptr;
}

std::vector<TrackFeature*> Track::getUpcomingFeatures(float position, int lookAhead) {
    std::vector<TrackFeature*> result;
    int pos = static_cast<int>(position) % static_cast<int>(m_length);
    int endPos = pos + lookAhead;

    for (auto& feature : m_features) {
        if (feature.position > pos && feature.position <= endPos && !feature.announced) {
            result.push_back(&feature);
        }
    }

    return result;
}

SurfaceType Track::getSurfaceAt(float position) {
    RoadSegment* segment = getSegmentAt(position);
    if (segment) {
        return segment->surfaceType;
    }
    return SurfaceType::ASPHALT;
}

CurveDirection Track::getCurveDirectionAt(float position) {
    RoadSegment* segment = getSegmentAt(position);
    if (segment) {
        return segment->curveDirection;
    }
    return CurveDirection::STRAIGHT;
}

bool Track::isOnTrack(float positionX) const {
    return std::abs(positionX) <= m_laneWidth / 2.0f;
}

void Track::saveLapRecord(int timeMs, const std::string& driver) {
    if (m_bestLapTime == 0 || timeMs < m_bestLapTime) {
        m_bestLapTime = timeMs;
    }
}

} // namespace topspeed
