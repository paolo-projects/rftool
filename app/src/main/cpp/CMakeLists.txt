# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("rftool")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        rftool

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/sdr-lib.cpp src/fft/FFTLib.cpp src/fft/FftThread.cpp src/RecorderThread.cpp
        src/signalanalysis.cpp src/SignalProcessing.cpp src/SoundPlayer.cpp src/fm-demod.cpp
        src/FmDemodManager.cpp)

target_include_directories(rftool PUBLIC include)

message(STATUS "Target arch for LibUSB: ${CMAKE_ANDROID_ARCH_ABI}")

# RtlSdr --- USB RTL SDR communication
add_library(rtlsdr_shared SHARED libs/librtlsdr/src/librtlsdr.c libs/librtlsdr/src/tuner_e4k.c
        libs/librtlsdr/src/tuner_fc0012.c libs/librtlsdr/src/tuner_fc0013.c
        libs/librtlsdr/src/tuner_fc2580.c libs/librtlsdr/src/tuner_r82xx.c)

#set(LIBUSB_SRCS libs/libusb/libusb/core.c libs/libusb/libusb/descriptor.c
#        libs/libusb/libusb/hotplug.c libs/libusb/libusb/io.c libs/libusb/libusb/sync.c
#        libs/libusb/libusb/strerror.c libs/libusb/libusb/os/linux_usbfs.c
#        libs/libusb/libusb/os/events_posix.c libs/libusb/libusb/os/threads_posix.c
#        libs/libusb/libusb/os/linux_netlink.c)
#add_library(libusb ${LIBUSB_SRCS})
#target_include_directories(libusb PUBLIC libs/libusb libs/libusb/libusb libs/libusb/libusb/os)
#target_compile_options(libusb -fvisibility=hidden -pthread)

target_link_libraries(rtlsdr_shared
        "${PROJECT_SOURCE_DIR}/libs/libusb/android-build/${CMAKE_ANDROID_ARCH_ABI}/libusb1.0.so")

target_include_directories(rtlsdr_shared PUBLIC libs/librtlsdr/include)
target_include_directories(rtlsdr_shared PUBLIC libs/libusb/libusb)

target_link_libraries(rftool rtlsdr_shared)
target_include_directories(rftool PUBLIC libs/librtlsdr/include)

target_include_directories(rftool PUBLIC libs/readerwriterqueue)

# FFTW3 --- Fourier transform and filtering
add_subdirectory(libs/fftw)
target_link_libraries(rftool fftw3)
target_include_directories(rftool PUBLIC libs/fftw)
target_include_directories(rftool PUBLIC libs/fftw/api)

# FmDemod --- FM demodulation
add_subdirectory(libs/fm-demod)
target_link_libraries(FmDemod fftw3)
target_include_directories(FmDemod PUBLIC libs/fftw)
target_include_directories(FmDemod PUBLIC libs/fftw/api)

target_link_libraries(rftool FmDemod)
target_include_directories(rftool PUBLIC libs/fm-demod/include)

# Oboe --- Audio playback
add_subdirectory(libs/oboe)
target_link_libraries(rftool oboe)
target_include_directories(rftool PUBLIC libs/oboe/include)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        rftool

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})