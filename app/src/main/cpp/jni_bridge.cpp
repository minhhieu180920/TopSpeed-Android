/**
 * JNI Bridge cho TopSpeed Audio Racing
 * Kết nối Kotlin code với native C++ game logic
 */

#include <jni.h>
#include <android/log.h>
#include <string>
#include "game_logic/Car.h"
#include "game_logic/Track.h"
#include "game_logic/RaceLogic.h"
#include "android_audio/AudioEngine.h"

#define LOG_TAG "TopSpeedJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global pointers cho game objects
static RaceLogic* g_raceLogic = nullptr;
static AudioEngine* g_audioEngine = nullptr;

// JNI Initialization
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    LOGD("TopSpeed JNI loaded successfully");
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    LOGD("TopSpeed JNI unloading");

    // Cleanup
    if (g_raceLogic != nullptr) {
        delete g_raceLogic;
        g_raceLogic = nullptr;
    }
    if (g_audioEngine != nullptr) {
        delete g_audioEngine;
        g_audioEngine = nullptr;
    }
}

// ============================================================================
// Native Game Initialization
// ============================================================================

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_TopSpeedApplication_nativeInit(JNIEnv* env, jobject thiz) {
    LOGD("Initializing native game engine");

    // Initialize audio engine
    g_audioEngine = new AudioEngine();
    g_audioEngine->initialize();

    // Initialize race logic
    g_raceLogic = new RaceLogic();
    g_raceLogic->initialize();

    LOGD("Native game engine initialized");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_TopSpeedApplication_nativeShutdown(JNIEnv* env, jobject thiz) {
    LOGD("Shutting down native game engine");

    if (g_raceLogic != nullptr) {
        g_raceLogic->shutdown();
    }
    if (g_audioEngine != nullptr) {
        g_audioEngine->shutdown();
    }
}

// ============================================================================
// Race Control
// ============================================================================

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeStartRace(JNIEnv* env, jobject thiz) {
    LOGD("Starting race");
    if (g_raceLogic != nullptr) {
        g_raceLogic->startRace();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_game_GameLoop_nativePauseRace(JNIEnv* env, jobject thiz) {
    LOGD("Pausing race");
    if (g_raceLogic != nullptr) {
        g_raceLogic->pause();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeResumeRace(JNIEnv* env, jobject thiz) {
    LOGD("Resuming race");
    if (g_raceLogic != nullptr) {
        g_raceLogic->resume();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeStopRace(JNIEnv* env, jobject thiz) {
    LOGD("Stopping race");
    if (g_raceLogic != nullptr) {
        g_raceLogic->stop();
    }
}

// ============================================================================
// Car Control
// ============================================================================

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeSetAcceleration(JNIEnv* env, jobject thiz, jboolean accelerating) {
    if (g_raceLogic != nullptr) {
        g_raceLogic->setAcceleration(accelerating);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeSetBraking(JNIEnv* env, jobject thiz, jboolean braking) {
    if (g_raceLogic != nullptr) {
        g_raceLogic->setBraking(braking);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeSetSteering(JNIEnv* env, jobject thiz, jfloat value) {
    if (g_raceLogic != nullptr) {
        g_raceLogic->setSteering(value);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeStartEngine(JNIEnv* env, jobject thiz) {
    LOGD("Starting engine");
    if (g_raceLogic != nullptr) {
        g_raceLogic->startEngine();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeStopEngine(JNIEnv* env, jobject thiz) {
    LOGD("Stopping engine");
    if (g_raceLogic != nullptr) {
        g_raceLogic->stopEngine();
    }
}

// ============================================================================
// Car State Queries
// ============================================================================

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeGetCarSpeed(JNIEnv* env, jobject thiz) {
    if (g_raceLogic != nullptr) {
        return g_raceLogic->getCarSpeed();
    }
    return 0.0f;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeGetCarPosition(JNIEnv* env, jobject thiz) {
    if (g_raceLogic != nullptr) {
        return g_raceLogic->getCarPosition();
    }
    return 0.0f;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeGetCarGear(JNIEnv* env, jobject thiz) {
    if (g_raceLogic != nullptr) {
        return g_raceLogic->getCarGear();
    }
    return 0;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeGetCarRPM(JNIEnv* env, jobject thiz) {
    if (g_raceLogic != nullptr) {
        return g_raceLogic->getCarRPM();
    }
    return 0.0f;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeIsEngineRunning(JNIEnv* env, jobject thiz) {
    if (g_raceLogic != nullptr) {
        return g_raceLogic->isEngineRunning();
    }
    return JNI_FALSE;
}

// ============================================================================
// Track Info
// ============================================================================

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeGetTrackLength(JNIEnv* env, jobject thiz) {
    if (g_raceLogic != nullptr) {
        return g_raceLogic->getTrackLength();
    }
    return 0.0f;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeGetCurrentSurface(JNIEnv* env, jobject thiz) {
    if (g_raceLogic != nullptr) {
        const char* surface = g_raceLogic->getCurrentSurface();
        return env->NewStringUTF(surface);
    }
    return env->NewStringUTF("asphalt");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeGetUpcomingCurve(JNIEnv* env, jobject thiz) {
    if (g_raceLogic != nullptr) {
        const char* curve = g_raceLogic->getUpcomingCurve();
        return env->NewStringUTF(curve);
    }
    return env->NewStringUTF("straight");
}

// ============================================================================
// Game Update
// ============================================================================

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_game_GameLoop_nativeUpdate(JNIEnv* env, jobject thiz, jfloat deltaTime) {
    if (g_raceLogic != nullptr && g_audioEngine != nullptr) {
        g_raceLogic->update(deltaTime);

        // Update audio based on game state
        float speed = g_raceLogic->getCarSpeed();
        float rpm = g_raceLogic->getCarRPM();
        int gear = g_raceLogic->getCarGear();

        g_audioEngine->updateEngineSound(speed, rpm, gear);
    }
}

// ============================================================================
// Audio Control
// ============================================================================

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_audio_AudioEngine_nativePlaySound(JNIEnv* env, jobject thiz, jstring soundName, jboolean loop) {
    if (g_audioEngine != nullptr) {
        const char* name = env->GetStringUTFChars(soundName, nullptr);
        g_audioEngine->playSound(name, loop);
        env->ReleaseStringUTFChars(soundName, name);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_audio_AudioEngine_nativeStopSound(JNIEnv* env, jobject thiz, jstring soundName) {
    if (g_audioEngine != nullptr) {
        const char* name = env->GetStringUTFChars(soundName, nullptr);
        g_audioEngine->stopSound(name);
        env->ReleaseStringUTFChars(soundName, name);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_audio_AudioEngine_nativeSetVolume(JNIEnv* env, jobject thiz, jstring soundName, jfloat volume) {
    if (g_audioEngine != nullptr) {
        const char* name = env->GetStringUTFChars(soundName, nullptr);
        g_audioEngine->setVolume(name, volume);
        env->ReleaseStringUTFChars(soundName, name);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_audio_AudioEngine_nativeSetPitch(JNIEnv* env, jobject thiz, jstring soundName, jfloat pitch) {
    if (g_audioEngine != nullptr) {
        const char* name = env->GetStringUTFChars(soundName, nullptr);
        g_audioEngine->setPitch(name, pitch);
        env->ReleaseStringUTFChars(soundName, name);
    }
}

// ============================================================================
// 3D Audio Position
// ============================================================================

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_audio_AudioEngine_nativeUpdateListenerPosition(JNIEnv* env, jobject thiz, jfloat x, jfloat y, jfloat z) {
    if (g_audioEngine != nullptr) {
        g_audioEngine->setListenerPosition(x, y, z);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_topspeed_audio_audio_AudioEngine_nativePlaySound3D(JNIEnv* env, jobject thiz, jstring soundName, jfloat x, jfloat y, jfloat volume) {
    if (g_audioEngine != nullptr) {
        const char* name = env->GetStringUTFChars(soundName, nullptr);
        g_audioEngine->playSound3D(name, x, y, volume);
        env->ReleaseStringUTFChars(soundName, name);
    }
}
