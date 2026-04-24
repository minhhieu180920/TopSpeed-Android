/**
 * AudioEngine implementation - Native audio engine cho Android
 */

#include "AudioEngine.h"
#include <android/log.h>
#include <cstring>
#include <algorithm>
#include <cmath>

#define LOG_TAG "TopSpeedAudio"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace topspeed {

// Engine sound state
static float g_lastEnginePitch = 1.0f;
static int g_engineStreamId = -1;

AudioEngine::AudioEngine()
    : m_initialized(false)
    , m_masterVolume(1.0f)
    , m_sfxVolume(1.0f)
    , m_listenerX(0)
    , m_listenerY(0)
    , m_listenerZ(0)
    , m_nextStreamId(1)
{
}

AudioEngine::~AudioEngine() {
    shutdown();
}

bool AudioEngine::initialize() {
    LOGD("Initializing AudioEngine");

    // Note: Actual audio initialization would use Oboe or OpenSL ES
    // For this port, we're using Kotlin's SoundPool which is easier for Android
    // This native class provides the interface for future native audio implementation

    m_initialized = true;
    LOGD("AudioEngine initialized (stub - using Kotlin audio)");
    return true;
}

void AudioEngine::shutdown() {
    LOGD("Shutting down AudioEngine");
    unloadAllSounds();
    m_initialized = false;
}

SoundId AudioEngine::loadSound(const char* filename) {
    if (!m_initialized) return INVALID_SOUND;

    // Create sound data
    SoundData* data = new SoundData();
    data->name = filename;
    data->buffer = nullptr;  // Would load actual audio data
    data->size = 0;
    data->sampleRate = 44100;
    data->channels = 2;

    SoundId id = m_sounds.size();
    m_sounds[id] = data;
    m_soundNames[filename] = id;

    LOGD("Loaded sound: %s (id=%d)", filename, id);
    return id;
}

SoundId AudioEngine::loadSoundFromAsset(const char* assetPath) {
    // Would load from Android assets
    return loadSound(assetPath);
}

void AudioEngine::unloadSound(SoundId id) {
    auto it = m_sounds.find(id);
    if (it != m_sounds.end()) {
        delete it->second;
        m_sounds.erase(it);
    }
}

void AudioEngine::unloadAllSounds() {
    for (auto& pair : m_sounds) {
        delete pair.second;
    }
    m_sounds.clear();
    m_soundNames.clear();
}

SoundId AudioEngine::findSoundByName(const char* name) {
    auto it = m_soundNames.find(name);
    if (it != m_soundNames.end()) {
        return it->second;
    }
    return INVALID_SOUND;
}

int AudioEngine::playSound(SoundId id, bool loop) {
    if (!m_initialized || id == INVALID_SOUND) return -1;

    PlayingSound ps;
    ps.soundId = id;
    ps.streamId = m_nextStreamId++;
    ps.looping = loop;
    ps.volume = 1.0f;
    ps.pitch = 1.0f;
    ps.pan = 0.0f;

    m_playingSounds[ps.streamId] = ps;
    LOGD("Playing sound id=%d, streamId=%d, loop=%d", id, ps.streamId, loop);

    return ps.streamId;
}

int AudioEngine::playSound(const char* name, bool loop) {
    SoundId id = findSoundByName(name);
    if (id == INVALID_SOUND) {
        LOGW("Sound not found: %s", name);
        return -1;
    }
    return playSound(id, loop);
}

void AudioEngine::stopSound(int streamId) {
    auto it = m_playingSounds.find(streamId);
    if (it != m_playingSounds.end()) {
        m_playingSounds.erase(it);
        LOGD("Stopped sound streamId=%d", streamId);
    }
}

void AudioEngine::stopSound(const char* name) {
    // Stop all instances of this sound
    for (auto it = m_playingSounds.begin(); it != m_playingSounds.end(); ) {
        if (it->second.soundId == findSoundByName(name)) {
            it = m_playingSounds.erase(it);
        } else {
            ++it;
        }
    }
}

void AudioEngine::stopAllSounds() {
    m_playingSounds.clear();
    LOGD("Stopped all sounds");
}

void AudioEngine::setVolume(int streamId, float volume) {
    auto it = m_playingSounds.find(streamId);
    if (it != m_playingSounds.end()) {
        it->second.volume = std::clamp(volume, 0.0f, 1.0f);
    }
}

void AudioEngine::setMasterVolume(float volume) {
    m_masterVolume = std::clamp(volume, 0.0f, 1.0f);
}

void AudioEngine::setSFXVolume(float volume) {
    m_sfxVolume = std::clamp(volume, 0.0f, 1.0f);
}

void AudioEngine::setPitch(int streamId, float pitch) {
    auto it = m_playingSounds.find(streamId);
    if (it != m_playingSounds.end()) {
        it->second.pitch = std::clamp(pitch, 0.5f, 2.0f);
    }
}

void AudioEngine::setPitch(const char* name, float pitch) {
    SoundId id = findSoundByName(name);
    if (id != INVALID_SOUND) {
        for (auto& pair : m_playingSounds) {
            if (pair.second.soundId == id) {
                pair.second.pitch = std::clamp(pitch, 0.5f, 2.0f);
            }
        }
    }
}

void AudioEngine::setListenerPosition(float x, float y, float z) {
    m_listenerX = x;
    m_listenerY = y;
    m_listenerZ = z;
}

void AudioEngine::playSound3D(const char* name, float x, float y, float volume) {
    // Calculate pan based on sound position relative to listener
    float dx = x - m_listenerX;
    float dy = y - m_listenerY;
    float distance = std::sqrt(dx * dx + dy * dy);

    // Pan: -1 (left) to 1 (right)
    float pan = dx / (distance + 1.0f);
    pan = std::clamp(pan, -1.0f, 1.0f);

    // Volume decreases with distance
    float distanceVolume = 1.0f / (distance + 1.0f);
    distanceVolume = std::clamp(distanceVolume, 0.0f, 1.0f);

    // Play sound with pan and volume
    SoundId id = findSoundByName(name);
    if (id != INVALID_SOUND) {
        int streamId = playSound(id, false);
        if (streamId != -1) {
            setVolume(streamId, volume * distanceVolume);

            // Store pan for this sound
            auto it = m_playingSounds.find(streamId);
            if (it != m_playingSounds.end()) {
                it->second.pan = pan;
            }
        }
    }
}

void AudioEngine::updateEngineSound(float speed, float rpm, int gear) {
    // Calculate target pitch from RPM
    float targetPitch = 0.5f + (rpm / 8000.0f) * 1.5f;
    targetPitch = std::clamp(targetPitch, 0.5f, 2.0f);

    // Smooth pitch transition
    float pitchDelta = targetPitch - g_lastEnginePitch;
    if (std::abs(pitchDelta) > 0.01f) {
        g_lastEnginePitch = targetPitch;

        // Update engine sound pitch
        setPitch("en/gameplay/engine", g_lastEnginePitch);
    }

    // Calculate volume based on speed
    float volume = 0.5f + (speed / 200.0f) * 0.5f;
    volume = std::clamp(volume, 0.3f, 1.0f);

    // Update engine volume
    auto it = m_soundNames.find("en/gameplay/engine");
    if (it != m_soundNames.end()) {
        for (auto& pair : m_playingSounds) {
            if (pair.second.soundId == it->second && pair.second.looping) {
                pair.second.volume = volume;
            }
        }
    }
}

bool AudioEngine::isPlaying(SoundId id) const {
    for (const auto& pair : m_playingSounds) {
        if (pair.second.soundId == id) {
            return true;
        }
    }
    return false;
}

} // namespace topspeed
