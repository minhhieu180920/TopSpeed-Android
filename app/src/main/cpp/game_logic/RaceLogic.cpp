/**
 * RaceLogic class implementation - port từ Level.cpp/Game.cpp của game gốc Top Speed 3
 */

#include "RaceLogic.h"
#include <cstdlib>
#include <cstring>

namespace topspeed {

RaceLogic::RaceLogic()
    : m_state(RaceState::MENU)
    , m_paused(false)
    , m_initialized(false)
    , m_car(nullptr)
    , m_track(nullptr)
    , m_currentLap(0)
    , m_totalLaps(3)
    , m_raceStartTime(0)
    , m_lapStartTime(0)
    , m_currentLapTime(0)
    , m_bestLapTime(0)
    , m_position(1)
    , m_totalRacers(1)
    , m_countdownTimer(0)
    , m_countdownValue(3)
    , m_accelerating(false)
    , m_braking(false)
    , m_steering(0)
{
}

RaceLogic::~RaceLogic() {
    shutdown();
}

bool RaceLogic::initialize() {
    if (m_initialized) return true;

    // Create track and car
    m_track = new Track(0);
    m_track->load();

    m_car = new Car(0);
    m_car->setTrack(m_track);
    m_car->initialize();

    m_initialized = true;
    return true;
}

void RaceLogic::shutdown() {
    if (m_car) {
        delete m_car;
        m_car = nullptr;
    }
    if (m_track) {
        delete m_track;
        m_track = nullptr;
    }
    m_initialized = false;
}

void RaceLogic::startRace() {
    m_state = RaceState::COUNTDOWN;
    m_countdownTimer = 1.0f;
    m_countdownValue = 3;
    m_currentLap = 0;
    m_currentLapTime = 0;
    m_raceStartTime = 0;
    m_lapStartTime = 0;
}

void RaceLogic::pause() {
    if (m_state == RaceState::RACING) {
        m_state = RaceState::PAUSED;
        m_paused = true;
    }
}

void RaceLogic::resume() {
    if (m_state == RaceState::PAUSED) {
        m_state = RaceState::RACING;
        m_paused = false;
    }
}

void RaceLogic::stop() {
    m_state = RaceState::MENU;
    m_paused = false;
    if (m_car) {
        m_car->stop();
    }
}

void RaceLogic::update(float deltaTime) {
    if (!m_initialized || m_paused) return;

    switch (m_state) {
        case RaceState::COUNTDOWN:
            updateCountdown(deltaTime);
            break;

        case RaceState::RACING:
            updateRacing(deltaTime);
            break;

        case RaceState::FINISHED:
        case RaceState::CRASHED:
        case RaceState::PAUSED:
        case RaceState::MENU:
            break;
    }
}

void RaceLogic::updateCountdown(float deltaTime) {
    m_countdownTimer -= deltaTime;

    if (m_countdownTimer <= 0) {
        m_countdownValue--;

        if (m_countdownValue < 0) {
            // Race start!
            m_state = RaceState::RACING;
            m_raceStartTime = clock();
            m_lapStartTime = m_raceStartTime;
            if (m_car) {
                m_car->start();
            }
        } else {
            m_countdownTimer = 1.0f;
        }
    }
}

void RaceLogic::updateRacing(float deltaTime) {
    if (!m_car || !m_track) return;

    // Update car physics
    m_car->setAccelerating(m_accelerating);
    m_car->setBraking(m_braking);
    m_car->setSteering(m_steering);
    m_car->run(deltaTime);

    // Update track position
    m_track->getSegmentAt(m_car->getPosition());

    // Check lap completion
    checkLapCompletion();

    // Check collisions
    checkCollisions();
}

void RaceLogic::checkLapCompletion() {
    if (!m_car || !m_track) return;

    float trackLength = m_track->getLength();
    float carPosition = m_car->getPosition();

    // Check if completed a lap
    float lapsCompleted = carPosition / trackLength;
    int newLap = static_cast<int>(lapsCompleted);

    if (newLap > m_currentLap) {
        // Lap completed
        m_currentLap = newLap;

        // Calculate lap time
        long currentTime = clock();
        m_currentLapTime = static_cast<int>((currentTime - m_lapStartTime) / 1000);
        m_lapStartTime = currentTime;

        // Check for best lap
        if (m_bestLapTime == 0 || m_currentLapTime < m_bestLapTime) {
            m_bestLapTime = m_currentLapTime;
        }

        // Check if race finished
        if (m_currentLap >= m_totalLaps) {
            m_state = RaceState::FINISHED;
        }
    }
}

void RaceLogic::checkCollisions() {
    if (!m_car || !m_track) return;

    // Check if car is on track
    float positionX = m_car->getPositionX();
    if (!m_track->isOnTrack(positionX)) {
        // Off track - slow down
        float currentSpeed = m_car->getSpeed();
        m_car->setSteering(0);  // Lose control
        // Speed reduction handled in Car class via surface friction
    }
}

void RaceLogic::setAcceleration(bool accelerating) {
    m_accelerating = accelerating;
}

void RaceLogic::setBraking(bool braking) {
    m_braking = braking;
}

void RaceLogic::setSteering(float value) {
    m_steering = value;
}

void RaceLogic::startEngine() {
    if (m_car) {
        m_car->start();
    }
}

void RaceLogic::stopEngine() {
    if (m_car) {
        m_car->stop();
    }
}

float RaceLogic::getCarSpeed() const {
    if (m_car) {
        return m_car->getSpeed();
    }
    return 0.0f;
}

float RaceLogic::getCarPosition() const {
    if (m_car) {
        return m_car->getPosition();
    }
    return 0.0f;
}

int RaceLogic::getCarGear() const {
    if (m_car) {
        return m_car->getGear();
    }
    return 0;
}

float RaceLogic::getCarRPM() const {
    if (m_car) {
        return m_car->getRPM();
    }
    return 0.0f;
}

bool RaceLogic::isEngineRunning() const {
    if (m_car) {
        return m_car->isEngineRunning();
    }
    return false;
}

float RaceLogic::getTrackLength() const {
    if (m_track) {
        return m_track->getLength();
    }
    return 0.0f;
}

const char* RaceLogic::getCurrentSurface() const {
    if (m_track && m_car) {
        SurfaceType surface = m_track->getSurfaceAt(m_car->getPosition());
        switch (surface) {
            case SurfaceType::ASPHALT: return "asphalt";
            case SurfaceType::GRAVEL: return "gravel";
            case SurfaceType::WATER: return "water";
            case SurfaceType::SAND: return "sand";
            case SurfaceType::SNOW: return "snow";
        }
    }
    return "asphalt";
}

const char* RaceLogic::getUpcomingCurve() const {
    if (m_track && m_car) {
        CurveDirection curve = m_track->getCurveDirectionAt(m_car->getPosition() + 200);
        switch (curve) {
            case CurveDirection::STRAIGHT: return "straight";
            case CurveDirection::EASY_LEFT: return "easy left";
            case CurveDirection::LEFT: return "left";
            case CurveDirection::HARD_LEFT: return "hard left";
            case CurveDirection::HAIRPIN_LEFT: return "hairpin left";
            case CurveDirection::EASY_RIGHT: return "easy right";
            case CurveDirection::RIGHT: return "right";
            case CurveDirection::HARD_RIGHT: return "hard right";
            case CurveDirection::HAIRPIN_RIGHT: return "hairpin right";
        }
    }
    return "straight";
}

int RaceLogic::getLapTime() const {
    return m_currentLapTime;
}

int RaceLogic::getBestLapTime() const {
    return m_bestLapTime;
}

} // namespace topspeed
