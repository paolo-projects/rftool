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
#include <array>

#include "FFTLib.h"
#include "../ColorMap.h"
#include "../ThreadSafeBucket.h"

#define sqr(x) ((x)*(x))

enum COLOR_MAP_TYPE {
    COLOR_MAP_GRAYSCALE,
    COLOR_MAP_HEAT,
    COLOR_MAP_RAINBOW
};

struct SdrData {
    ~SdrData() {
        delete[] data;
    }
    uint8_t* data = nullptr;
    size_t count = 0;
};

class FftThread {
public:
    FftThread(JNIEnv* env, jobject instance, int nSamples);
    ~FftThread();

    void start();
    template<size_t N>
    void push(const std::array<uint8_t , N>& data, size_t count);
    void stop();

    void setColorMap(COLOR_MAP_TYPE mapType);
    void setFftN(int fftN);

private:
    void executor();
    template<size_t N>
    void updateBitmap(const std::array<jdouble, N> &data, size_t count);

private:
    const char* TAG = "FftThread";
    static constexpr double adcHalf = 127.5;

    int nSamples;

    std::unique_ptr<ColorMap> colorMap = std::make_unique<GrayscaleColorMap>();

    JavaVM* jvm = nullptr;
    JNIEnv* env = nullptr;
    jobject instance = nullptr;
    ThreadSafeBucket<SdrData> latestData;
    FFTLib fftLib;
    //rigtorp::SPSCQueue<std::vector<uint8_t>> dataQueue;
    //std::queue<std::vector<uint8_t>> dataQueue;
    //std::condition_variable cv;
    //std::mutex mtx;
    bool fftThreadRunning = false;
    std::unique_ptr<std::thread> fftThread;
    std::vector<jint> bitmapPixels;
    std::vector<jdouble> fftDataBuffer;
    std::array<jdouble, 256 * (1 << 14)> scaledDataBuffer;

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


template<size_t N>
void FftThread::push(const std::array<uint8_t , N>& data, size_t count) {
    namespace chr = std::chrono;

    auto start_time = chr::high_resolution_clock::now();
    uint8_t* sdrData = new uint8_t[count];
    std::copy(data.cbegin(), data.cbegin() + count, sdrData);
    SdrData* d = new SdrData{sdrData, count};
    latestData.put(d);
    auto end_time = chr::high_resolution_clock::now();

    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "FFTthread push overhead %d ms", chr::duration_cast<chr::milliseconds>(end_time - start_time).count());
}

template<size_t N>
void FftThread::updateBitmap(const std::array<jdouble, N> &data, size_t count) {
    jobject bitmap = env->GetObjectField(instance, bitmapFieldID);

    jint bitmapWidth = env->CallIntMethod(bitmap, bitmap_getWidth);
    jint bitmapHeight = env->CallIntMethod(bitmap, bitmap_getHeight);
    jintArray jBitmapPixels = env->NewIntArray(bitmapHeight * bitmapWidth);
    bitmapPixels.resize(bitmapWidth * bitmapHeight);

    // Get all the pixels
    env->CallVoidMethod(bitmap, bitmap_getPixels, jBitmapPixels, 0, bitmapWidth, 0, 0, bitmapWidth,
                        bitmapHeight);
    env->GetIntArrayRegion(jBitmapPixels, 0, bitmapWidth * bitmapHeight, bitmapPixels.data());

    // Shift the pixels down by one row
    std::rotate(bitmapPixels.begin(), bitmapPixels.begin() + bitmapPixels.size() - bitmapWidth,
                bitmapPixels.end());

    // Execute the FFT
    fftLib.executeFft(data, count, fftDataBuffer);
    double minFFT = sqrt(sqr(fftDataBuffer[2]) + sqr(fftDataBuffer[3]));
    double maxFFT = minFFT;
    double maxFFTAbs = minFFT;

    for (int i = 0; i < fftDataBuffer.size(); i += 2) {
        double magnitude = sqrt(sqr(fftDataBuffer[i]) + sqr(fftDataBuffer[i + 1]));
        minFFT = std::min(minFFT, magnitude);
        maxFFT = std::max(maxFFT, magnitude);
        if(i > 0) {
            maxFFTAbs = std::max(maxFFTAbs, magnitude);
        }
    }

    // Center the FFT to the carrier frequency
    std::rotate(fftDataBuffer.begin(), fftDataBuffer.begin() + fftDataBuffer.size() / 2, fftDataBuffer.end());

    // Assign the FFT to the first row of the bitmap through a color-map algorithm
    double factor = (double) (fftDataBuffer.size() - 1) / (bitmapWidth - 1);
    for (int i = 0; i < bitmapWidth; i++) {
        // Here the pixels [0, width - 1] are mapped linearly to the FFT values [0, N-1]
        int fftIndex = (int) round(i * factor);
        double magnitude = sqrt(sqr(fftDataBuffer[fftIndex]) + sqr(fftDataBuffer[fftIndex + 1]));

        jint color = colorMap->getColor((magnitude - minFFT) / (maxFFT - minFFT));
        bitmapPixels[i] = color;
    }

    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Computed bitmap. magnitudes min %.3f, max %.3f",
                        minFFT, maxFFT);

    // Set the pixels into the bitmap
    env->SetIntArrayRegion(jBitmapPixels, 0, bitmapWidth * bitmapHeight, bitmapPixels.data());
    env->CallVoidMethod(bitmap, bitmap_setPixels, jBitmapPixels, 0, bitmapWidth, 0, 0, bitmapWidth,
                        bitmapHeight);

    env->DeleteLocalRef(jBitmapPixels);

    // Notify RtlSdr class that bitmap has changed
    env->CallVoidMethod(instance, rtlSdr_notifyBitmapChanged);

    // Send FFT absolute max to RtlSdr class
    env->CallVoidMethod(instance, rtlSdr_notifyFftAbsoluteMax, maxFFTAbs);
}

#endif //RFTOOL_FFTTHREAD_H
