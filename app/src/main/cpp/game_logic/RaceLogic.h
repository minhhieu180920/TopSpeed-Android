/**
 * RaceLogic class header - port từ Level.cpp/Game.cpp của game gốc
 * Quản lý logic cuộc đua
 */

#ifndef TOPSPEED_RACELOGIC_H
#define TOPSPEED_RACELOGIC_H

#include "Car.h"
#include "Track.h"

namespace topspeed {

// Race state
enum class RaceState {
    MENU,
    COUNTDOWN,
    RACING,
    PAUSED,
    FINISHED,
    CRASHED
};

class RaceLogic {
public:
    RaceLogic();
    virtual ~RaceLogic();

    // Lifecycle
    bool initialize();
    void shutdown();

    // Race control
    void startRace();
    void pause();
    void resume();
    void stop();
    void update(float deltaTime);

    // Car control
    void setAcceleration(bool accelerating);
    void setBraking(bool braking);
    void setSteering(float value);
    void startEngine();
    void stopEngine();

    // Car state queries
    float getCarSpeed() const;
    float getCarPosition() const;
    int getCarGear() const;
    float getCarRPM() const;
    bool isEngineRunning() const;

    // Track info
    float getTrackLength() const;
    const char* getCurrentSurface() const;
    const char* getUpcomingCurve() const;

    // Lap management
    int getCurrentLap() const { return m_currentLap; }
    int getTotalLaps() const { return m_totalLaps; }
    int getLapTime() const;
    int getBestLapTime() const;

    // State
    RaceState getState() const { return m_state; }
    bool isPaused() const { return m_paused; }

private:
    void updateCountdown(float deltaTime);
    void updateRacing(float deltaTime);
    void checkLapCompletion();
    void checkCollisions();

    RaceState m_state;
    bool m_paused;
    bool m_initialized;

    // Game objects
    Car* m_car;
    Track* m_track;

    // Race state
    int m_currentLap;
    int m_totalLaps;
    long m_raceStartTime;
    long m_lapStartTime;
    int m_currentLapTime;
    int m_bestLapTime;
    int m_position;
    int m_totalRacers;

    // Countdown
    float m_countdownTimer;
    int m_countdownValue;

    // Input
    bool m_accelerating;
    bool m_braking;
    float m_steering;
};

} // namespace topspeed

#endif // TOPSPEED_RACELOGIC_H
