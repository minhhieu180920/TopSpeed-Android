/**
 * AudioEngine header - Native audio engine cho Android
 * Sử dụng Oboe/OpenSL ES cho low-latency audio
 */

#ifndef TOPSPEED_AUDIOENGINE_H
#define TOPSPEED_AUDIOENGINE_H

#include <string>
#include <map>

namespace topspeed {

// Sound handle
typedef int SoundId;
const SoundId INVALID_SOUND = -1;

// Audio engine class
class AudioEngine {
public:
    AudioEngine();
    virtual ~AudioEngine();

    // Initialization
    bool initialize();
    void shutdown();

    // Sound loading
    SoundId loadSound(const char* filename);
    SoundId loadSoundFromAsset(const char* assetPath);
    void unloadSound(SoundId id);
    void unloadAllSounds();

    // Playback control
    int playSound(SoundId id, bool loop = false);
    int playSound(const char* name, bool loop = false);
    void stopSound(int streamId);
    void stopSound(const char* name);
    void stopAllSounds();

    // Volume control
    void setVolume(int streamId, float volume);
    void setMasterVolume(float volume);
    void setSFXVolume(float volume);

    // Pitch/rate control
    void setPitch(int streamId, float pitch);
    void setPitch(const char* name, float pitch);

    // 3D audio
    void setListenerPosition(float x, float y, float z);
    void playSound3D(const char* name, float x, float y, float volume = 1.0f);

    // Engine sound update (for pitch shifting based on RPM)
    void updateEngineSound(float speed, float rpm, int gear);

    // State
    bool isInitialized() const { return m_initialized; }
    bool isPlaying(SoundId id) const;

private:
    struct SoundData {
        std::string name;
        void* buffer;      // PCM data
        unsigned int size;
        unsigned int sampleRate;
        unsigned int channels;
    };

    struct PlayingSound {
        SoundId soundId;
        int streamId;
        bool looping;
        float volume;
        float pitch;
        float pan;  // -1 (left) to 1 (right)
    };

    // Helper methods
    SoundId findSoundByName(const char* name);
    void updateSoundPosition(int streamId, float x, float y);

    // Sound data
    std::map<std::string, SoundId> m_soundNames;
    std::map<SoundId, SoundData*> m_sounds;
    std::map<int, PlayingSound> m_playingSounds;

    // State
    bool m_initialized;
    float m_masterVolume;
    float m_sfxVolume;

    // 3D listener
    float m_listenerX;
    float m_listenerY;
    float m_listenerZ;

    // Next stream ID
    int m_nextStreamId;
};

} // namespace topspeed

#endif // TOPSPEED_AUDIOENGINE_H
