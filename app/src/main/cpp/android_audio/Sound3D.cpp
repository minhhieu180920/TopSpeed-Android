/**
 * Sound3D implementation - 3D spatial audio processing
 */

#include "Sound3D.h"
#include <cmath>
#include <algorithm>

#ifndef M_PI
#define M_PI 3.14159265358979323846f
#endif

namespace topspeed {

// ============================================================================
// Listener3D
// ============================================================================

Listener3D::Listener3D()
    : m_position(0, 0, 0)
    , m_orientation(0, 0, -1)  // Looking down -Z
    , m_velocity(0, 0, 0)
    , m_dopplerFactor(1.0f)
    , m_rolloffFactor(1.0f)
{
}

Listener3D::~Listener3D() {
}

void Listener3D::setPosition(float x, float y, float z) {
    m_position = Vector3(x, y, z);
}

void Listener3D::setOrientation(float dirX, float dirY, float dirZ) {
    m_orientation = Vector3(dirX, dirY, dirZ).normalize();
}

void Listener3D::setVelocity(float velX, float velY, float velZ) {
    m_velocity = Vector3(velX, velY, velZ);
}

// ============================================================================
// SoundSource3D
// ============================================================================

SoundSource3D::SoundSource3D()
    : m_position(0, 0, 0)
    , m_velocity(0, 0, 0)
{
}

SoundSource3D::~SoundSource3D() {
}

void SoundSource3D::setPosition(float x, float y, float z) {
    m_position = Vector3(x, y, z);
}

void SoundSource3D::setVelocity(float velX, float velY, float velZ) {
    m_velocity = Vector3(velX, velY, velZ);
}

float SoundSource3D::getPan(const Listener3D& listener) const {
    // Vector from listener to source
    Vector3 toSource(
        m_position.x - listener.m_position.x,
        m_position.y - listener.m_position.y,
        m_position.z - listener.m_position.z
    );

    // Project onto listener's right vector (assuming Y is up, right is +X)
    float relativeX = toSource.x;
    float distance = toSource.length();

    if (distance < 0.001f) {
        return 0.0f;  // Source at listener position
    }

    // Pan from -1 (left) to 1 (right)
    float pan = relativeX / distance;
    return std::clamp(pan, -1.0f, 1.0f);
}

float SoundSource3D::getVolume(const Listener3D& listener, float rolloff) const {
    float dx = m_position.x - listener.m_position.x;
    float dy = m_position.y - listener.m_position.y;
    float dz = m_position.z - listener.m_position.z;

    float distance = sqrtf(dx * dx + dy * dy + dz * dz);

    // Inverse square law with rolloff
    float volume = 1.0f / (1.0f + distance * rolloff);
    return std::clamp(volume, 0.0f, 1.0f);
}

float SoundSource3D::getPitchWithDoppler(const Listener3D& listener, float dopplerFactor) const {
    // Calculate relative velocity along the line between source and listener
    Vector3 toSource(
        m_position.x - listener.m_position.x,
        m_position.y - listener.m_position.y,
        m_position.z - listener.m_position.z
    );

    float distance = toSource.length();
    if (distance < 0.001f) {
        return 1.0f;  // No doppler at zero distance
    }

    Vector3 direction = toSource.normalize();

    // Project velocities onto direction vector
    float sourceVelAlongLine = m_velocity.dot(direction);
    float listenerVelAlongLine = listener.m_velocity.dot(direction);

    // Doppler formula: f' = f * (v + vl) / (v + vs)
    // Where v is speed of sound (~343 m/s)
    const float speedOfSound = 343.0f;

    float numerator = speedOfSound + listenerVelAlongLine * dopplerFactor;
    float denominator = speedOfSound + sourceVelAlongLine * dopplerFactor;

    if (denominator <= 0) {
        return 1.0f;  // Avoid division by zero or negative
    }

    float dopplerShift = numerator / denominator;
    return std::clamp(dopplerShift, 0.5f, 2.0f);
}

} // namespace topspeed
