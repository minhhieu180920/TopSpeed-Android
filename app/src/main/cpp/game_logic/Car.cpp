/**
 * Car class implementation - port từ Car.cpp của game gốc Top Speed 3
 */

#include "Car.h"
#include <cmath>
#include <algorithm>

namespace topspeed {

// Default parameters
const CarParameters DEFAULT_PARAMS = {
    .engineSound = 0,
    .startSound = 1,
    .hornSound = 2,
    .throttleSound = 3,
    .crashSound = 4,
    .monoCrashSound = 5,
    .brakeSound = 6,
    .backfireSound = 7,
    .hasWipers = 0,
    .acceleration = 500,
    .deceleration = 800,
    .topspeed = 180,
    .idlefreq = 800,
    .topfreq = 4000,
    .shiftfreq = 100,
    .gears = 6,
    .steering = 300,
    .steeringFactor = 1
};

Car::Car(int vehicleId)
    : m_state(CarState::STOPPED)
    , m_carType(CarType::SPORT)
    , m_params(DEFAULT_PARAMS)
    , m_position(0)
    , m_positionX(0)
    , m_positionY(0)
    , m_speed(0)
    , m_gear(0)
    , m_rpm(0)
    , m_engineRunning(false)
    , m_enginePitch(1.0f)
    , m_accelerating(false)
    , m_braking(false)
    , m_steering(0)
    , m_track(nullptr)
{
    // Adjust parameters based on vehicle type
    switch (vehicleId) {
        case 0: // Default - balanced
            m_carType = CarType::SPORT;
            break;
        case 1: // Fast car
            m_params.topspeed = 220;
            m_params.acceleration = 450;
            m_carType = CarType::FORMULA;
            break;
        case 2: // Heavy car
            m_params.topspeed = 160;
            m_params.acceleration = 400;
            m_carType = CarType::SUV;
            break;
    }
}

Car::~Car() {
    finalize();
}

bool Car::initialize() {
    m_state = CarState::STOPPED;
    m_position = 0;
    m_positionX = 0;
    m_speed = 0;
    m_gear = 0;
    m_rpm = 0;
    m_engineRunning = false;
    return true;
}

void Car::finalize() {
    stop();
}

void Car::start() {
    if (m_engineRunning) return;

    m_engineRunning = true;
    m_state = CarState::STARTING;
}

void Car::stop() {
    m_engineRunning = false;
    m_state = CarState::STOPPED;
    m_speed = 0;
    m_gear = 0;
    m_rpm = 0;
}

void Car::crash() {
    m_state = CarState::CRASHING;
    m_speed = 0;
}

void Car::setAccelerating(bool accelerating) {
    m_accelerating = accelerating;
}

void Car::setBraking(bool braking) {
    m_braking = braking;
}

void Car::setSteering(float value) {
    m_steering = std::clamp(value, -1.0f, 1.0f);
}

void Car::run(float elapsed) {
    if (m_state != CarState::RUNNING && m_state != CarState::STARTING) {
        if (m_engineRunning && m_state == CarState::STARTING && m_speed > 10) {
            m_state = CarState::RUNNING;
        }
        return;
    }

    updatePhysics(elapsed);
    updateGear();
    updatePosition(elapsed);

    // Update state
    if (m_speed <= 0) {
        m_state = CarState::STOPPED;
    }
}

void Car::updatePhysics(float elapsed) {
    if (m_accelerating && !m_braking) {
        // Accelerating
        m_speed += m_params.acceleration * elapsed;
        if (m_speed > m_params.topspeed) {
            m_speed = m_params.topspeed;
        }
    } else if (m_braking) {
        // Braking
        m_speed -= m_params.deceleration * elapsed;
        if (m_speed < 0) {
            m_speed = 0;
        }
    } else {
        // Coasting
        m_speed -= m_params.deceleration * 0.3f * elapsed;
        if (m_speed < 0) {
            m_speed = 0;
        }
    }

    // Calculate RPM based on speed and gear
    float gearRatio = 1.0f;
    switch (m_gear) {
        case 0: gearRatio = 3.0f; break;
        case 1: gearRatio = 3.5f; break;
        case 2: gearRatio = 2.5f; break;
        case 3: gearRatio = 1.8f; break;
        case 4: gearRatio = 1.3f; break;
        case 5: gearRatio = 1.0f; break;
        case 6: gearRatio = 0.8f; break;
    }

    float speedRatio = m_speed / m_params.topspeed;
    m_rpm = speedRatio * gearRatio * 8000.0f;
    m_rpm = std::clamp(m_rpm, 800.0f, 8000.0f);

    // Add some variation
    m_rpm += (rand() % 200 - 100);
}

void Car::updateGear() {
    // Automatic transmission
    if (m_speed > 5) {
        if (m_speed < 30) m_gear = 1;
        else if (m_speed < 60) m_gear = 2;
        else if (m_speed < 90) m_gear = 3;
        else if (m_speed < 120) m_gear = 4;
        else if (m_speed < 150) m_gear = 5;
        else m_gear = 6;
    } else {
        m_gear = 0; // Neutral
    }
}

void Car::updatePosition(float elapsed) {
    // Convert km/h to pixels/s
    float pixelsPerSecond = m_speed * 3.6f;
    m_position += pixelsPerSecond * elapsed;

    // Lateral position from steering
    float steeringSpeed = m_params.steering * m_params.steeringFactor;
    m_positionX += m_steering * steeringSpeed * elapsed;

    // Clamp to track bounds
    if (m_track) {
        float trackWidth = m_track->getLaneWidth() / 2.0f;
        m_positionX = std::clamp(m_positionX, -trackWidth, trackWidth);
    } else {
        m_positionX = std::clamp(m_positionX, -4000.0f, 4000.0f);
    }
}

void Car::updateEngineSound() {
    // Calculate pitch from RPM
    m_enginePitch = 0.5f + (m_rpm / 8000.0f) * 1.5f;
}

} // namespace topspeed
