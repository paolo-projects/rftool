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
        sdr-lib.cpp fft-lib.cpp fft-thread.cpp)

message(STATUS "Target arch for LibUSB: ${CMAKE_ANDROID_ARCH_ABI}")

#add_library(libusb-${CMAKE_ANDROID_ARCH_ABI} SHARED IMPORTED)
#set_target_properties(libusb-${CMAKE_ANDROID_ARCH_ABI}
#        PROPERTIES
#        IMPORTED_SONAME libusb/libs/${CMAKE_ANDROID_ARCH_ABI}/libusb1.0.so)

add_library(rtlsdr_shared SHARED librtlsdr/src/librtlsdr.c librtlsdr/src/tuner_e4k.c librtlsdr/src/tuner_fc0012.c
        librtlsdr/src/tuner_fc0013.c librtlsdr/src/tuner_fc2580.c librtlsdr/src/tuner_r82xx.c)

target_link_libraries(rtlsdr_shared "${PROJECT_SOURCE_DIR}/libusb/libs/${CMAKE_ANDROID_ARCH_ABI}/libusb1.0.so")

#target_link_libraries(rtlsdr_shared
#        -Llibusb/libs/${CMAKE_ANDROID_ARCH_ABI}
#        -lunrooted_android)

target_include_directories(rtlsdr_shared PUBLIC librtlsdr/include)
target_include_directories(rtlsdr_shared PUBLIC libusb)

target_link_libraries(rftool rtlsdr_shared)
target_include_directories(rftool PUBLIC librtlsdr/include)

#add_subdirectory(${PROJECT_SOURCE_DIR}/fftw)
#target_link_libraries(rftool fftw3)
#target_include_directories(rftool PUBLIC ${PROJECT_SOURCE_DIR}/fftw/api)

set(KISSFFT_DATATYPE "double" CACHE STRING "Principal datatype of kissfft: double, float (default), int16_t, int32_t, simd")
set(KISSFFT_TEST OFF CACHE BOOL "")
set(KISSFFT_TOOLS OFF CACHE BOOL "")

add_subdirectory(kissfft)
target_link_libraries(rftool kissfft)
target_include_directories(rftool PUBLIC kissfft)

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