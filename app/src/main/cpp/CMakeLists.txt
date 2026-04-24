# CMakeLists.txt cho TopSpeed Audio Racing
cmake_minimum_required(VERSION 3.22.1)

project("topspeed" VERSION 1.0.0)

# C++17 standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Android specific flags
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra")

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)

# Include directories
include_directories(
    ${CMAKE_SOURCE_DIR}/game_logic
    ${CMAKE_SOURCE_DIR}/android_audio
)

# Source files
set(SOURCES
    jni_bridge.cpp
    game_logic/Car.cpp
    game_logic/Track.cpp
    game_logic/RaceLogic.cpp
    android_audio/AudioEngine.cpp
    android_audio/Sound3D.cpp
)

# Create shared library
add_library(
    topspeed
    SHARED
    ${SOURCES}
)

# Link libraries
target_link_libraries(
    topspeed
    ${log-lib}
    ${android-lib}
)

# Compiler definitions
target_compile_definitions(
    topspeed
    PRIVATE
    ANDROID_BUILD
    __ANDROID__
)
