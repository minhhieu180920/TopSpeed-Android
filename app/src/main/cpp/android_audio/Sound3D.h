/**
 * Sound3D header - 3D spatial audio processing
 */

#ifndef TOPSPEED_SOUND3D_H
#define TOPSPEED_SOUND3D_H

namespace topspeed {

// 3D Vector
struct Vector3 {
    float x, y, z;

    Vector3() : x(0), y(0), z(0) {}
    Vector3(float _x, float _y, float _z) : x(_x), y(_y), z(_z) {}

    float length() const {
        return sqrtf(x * x + y * y + z * z);
    }

    Vector3 normalize() const {
        float len = length();
        if (len > 0) {
            return Vector3(x / len, y / len, z / len);
        }
        return *this;
    }

    float dot(const Vector3& other) const {
        return x * other.x + y * other.y + z * other.z;
    }
};

// 3D Sound listener
class Listener3D {
public:
    Listener3D();
    virtual ~Listener3D();

    void setPosition(float x, float y, float z);
    void setOrientation(float dirX, float dirY, float dirZ);
    void setVelocity(float velX, float velY, float velZ);

    Vector3 getPosition() const { return m_position; }
    Vector3 getOrientation() const { return m_orientation; }

    // Doppler factor
    void setDopplerFactor(float factor) { m_dopplerFactor = factor; }
    float getDopplerFactor() const { return m_dopplerFactor; }

    // Rolloff factor
    void setRolloffFactor(float factor) { m_rolloffFactor = factor; }
    float getRolloffFactor() const { return m_rolloffFactor; }

private:
    Vector3 m_position;
    Vector3 m_orientation;
    Vector3 m_velocity;
    float m_dopplerFactor;
    float m_rolloffFactor;
};

// 3D Sound source
class SoundSource3D {
public:
    SoundSource3D();
    virtual ~SoundSource3D();

    void setPosition(float x, float y, float z);
    void setVelocity(float velX, float velY, float velZ);

    Vector3 getPosition() const { return m_position; }

    // Get pan based on listener position
    float getPan(const Listener3D& listener) const;

    // Get volume based on distance
    float getVolume(const Listener3D& listener, float rolloff) const;

    // Get pitch with doppler effect
    float getPitchWithDoppler(const Listener3D& listener, float dopplerFactor) const;

private:
    Vector3 m_position;
    Vector3 m_velocity;
};

} // namespace topspeed

#endif // TOPSPEED_SOUND3D_H
