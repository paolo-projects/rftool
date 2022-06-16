//
// Created by paolo on 15/06/22.
//

#ifndef RFTOOL_FFTTHREAD_H
#define RFTOOL_FFTTHREAD_H

#include <jni.h>
#include <android/log.h>
#include <queue>
#include <condition_variable>
#include <thread>
#include <memory>

#include "fft-lib.h"
#include "ColorMap.h"

#define sqr(x) ((x)*(x))

enum COLOR_MAP_TYPE {
    COLOR_MAP_GRAYSCALE,
    COLOR_MAP_HEAT,
    COLOR_MAP_RAINBOW
};

class FftThread {
public:
    FftThread(JNIEnv* env, jobject instance, int nSamples);
    ~FftThread();

    void start();
    void push(const std::vector<jdouble>& data);
    void stop();

    void setColorMap(COLOR_MAP_TYPE mapType);

private:
    void executor();
    void updateBitmap(const std::vector<jdouble> &data);

private:
    const char* TAG = "FftThread";

    int nSamples;

    std::unique_ptr<ColorMap> colorMap = std::make_unique<GrayscaleColorMap>();

    JavaVM* jvm = nullptr;
    JNIEnv* env = nullptr;
    jobject instance = nullptr;
    std::queue<std::vector<jdouble>> dataQueue;
    std::condition_variable cv;
    std::mutex mtx;
    bool fftThreadRunning = false;
    std::unique_ptr<std::thread> fftThread;
    std::vector<jint> bitmapPixels;
    std::vector<jdouble> fftDataBuffer;

    // Java Methods/Classes/Fields
    jclass bitmapClass;
    jmethodID bitmap_getPixels;
    jmethodID bitmap_setPixels;
    jmethodID bitmap_getHeight;
    jmethodID bitmap_getWidth;

    jclass rtlSdrClass;
    jfieldID bitmapFieldID;
    jmethodID rtlSdr_notifyBitmapChanged;
    jmethodID rtlSdr_notifyFftAbsoluteMax;
};

#endif //RFTOOL_FFTTHREAD_H
