/**
 * Car class header - port từ Car.cpp của game gốc Top Speed 3
 * Quản lý vật lý xe và trạng thái
 */

#ifndef TOPSPEED_CAR_H
#define TOPSPEED_CAR_H

#include "Track.h"

namespace topspeed {

// Car state enum
enum class CarState {
    STOPPED,
    STARTING,
    RUNNING,
    SLIPPING,
    CRASHING,
    STOPPING
};

// Car type enum
enum class CarType {
    SPORT,
    FORMULA,
    SUV,
    TRUCK,
    CLASSIC
};

// Car parameters - port từ game gốc
struct CarParameters {
    int engineSound;
    int startSound;
    int hornSound;
    int throttleSound;
    int crashSound;
    int monoCrashSound;
    int brakeSound;
    int backfireSound;
    int hasWipers;
    int acceleration;
    int deceleration;
    int topspeed;
    int idlefreq;
    int topfreq;
    int shiftfreq;
    int gears;
    int steering;
    int steeringFactor;
};

class Car {
public:
    Car(int vehicleId = 0);
    virtual ~Car();

    // Initialization
    bool initialize();
    void finalize();

    // State control
    void start();
    void stop();
    void crash();
    void run(float elapsed);

    // Input
    void setAccelerating(bool accelerating);
    void setBraking(bool braking);
    void setSteering(float value);

    // Getters
    CarState getState() const { return m_state; }
    float getPosition() const { return m_position; }
    float getPositionX() const { return m_positionX; }
    float getPositionY() const { return m_positionY; }
    float getSpeed() const { return m_speed; }
    int getGear() const { return m_gear; }
    float getRPM() const { return m_rpm; }
    bool isEngineRunning() const { return m_engineRunning; }

    // Setters
    void setTrack(Track* track) { m_track = track; }

private:
    void updatePhysics(float elapsed);
    void updateEngineSound();
    void updateGear();
    void updatePosition(float elapsed);

    // State
    CarState m_state;
    CarType m_carType;
    CarParameters m_params;

    // Physics
    float m_position;      // Position on track
    float m_positionX;     // Lateral position
    float m_positionY;     // Vertical position (not used in 2D)
    float m_speed;         // Current speed (km/h)
    int m_gear;            // Current gear (0-6)
    float m_rpm;           // Engine RPM

    // Engine
    bool m_engineRunning;
    float m_enginePitch;

    // Input
    bool m_accelerating;
    bool m_braking;
    float m_steering;

    // Track reference
    Track* m_track;
};

} // namespace topspeed

#endif // TOPSPEED_CAR_H
