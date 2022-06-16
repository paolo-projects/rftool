//
// Created by paolo on 15/06/22.
//
#include "fft-thread.h"

FftThread::FftThread(JNIEnv* env, jobject instance, int nSamples)
    : nSamples(nSamples), env(env), instance(env->NewGlobalRef(instance)) {
    bitmapClass = env->FindClass("android/graphics/Bitmap");
    bitmap_getPixels = env->GetMethodID(bitmapClass, "getPixels", "([IIIIIII)V");
    bitmap_setPixels = env->GetMethodID(bitmapClass, "setPixels", "([IIIIIII)V");
    bitmap_getHeight = env->GetMethodID(bitmapClass, "getHeight", "()I");
    bitmap_getWidth = env->GetMethodID(bitmapClass, "getWidth", "()I");
    rtlSdrClass = env->FindClass("com/tools/rftool/rtlsdr/RtlSdr");
    bitmapFieldID = env->GetFieldID(rtlSdrClass, "_bitmap", "Landroid/graphics/Bitmap;");
    rtlSdr_notifyBitmapChanged = env->GetMethodID(rtlSdrClass, "notifyBitmapChanged",
                                                            "()V");
    rtlSdr_notifyFftAbsoluteMax = env->GetMethodID(rtlSdrClass, "notifyFftAbsoluteMax", "(D)V");

    env->GetJavaVM(&jvm);
}

FftThread::~FftThread() {
    if(fftThread != nullptr) {
        stop();
    }
    env->DeleteGlobalRef(instance);
}

void FftThread::start() {
    fftThreadRunning = true;
    if(fftThread == nullptr) {
        fftThread = std::make_unique<std::thread>(&FftThread::executor, this);
    }
}

void FftThread::push(const std::vector<jdouble>& data) {
    std::lock_guard<std::mutex> lock(mtx);
    dataQueue.emplace(data);
    cv.notify_one();
}

void FftThread::executor() {
    JavaVMAttachArgs ja_Args;
    ja_Args.name = nullptr;
    ja_Args.group = nullptr;
    ja_Args.version = JNI_VERSION_1_6;

    jvm->AttachCurrentThread(&env, &ja_Args);

    while(fftThreadRunning) {
        std::unique_lock<std::mutex> lock(mtx);
        cv.wait(lock, [&]() {
            return fftThreadRunning && !dataQueue.empty();
        });

        while(!dataQueue.empty()) {
            std::vector<jdouble> &data = dataQueue.front();
            updateBitmap(data);
            dataQueue.pop();
        }
    }

    jvm->DetachCurrentThread();
}

void FftThread::stop() {
    if(fftThread != nullptr) {
        fftThreadRunning = false;
        fftThread->join();
        fftThread.reset();
    }
}

void FftThread::setColorMap(COLOR_MAP_TYPE mapType) {
    std::unique_lock<std::mutex> lock(mtx);
    switch(mapType) {
        default:
        case COLOR_MAP_GRAYSCALE:
            colorMap = std::make_unique<GrayscaleColorMap>();
            break;
        case COLOR_MAP_HEAT:
            colorMap = std::make_unique<HeatColorMap>();
            break;
        case COLOR_MAP_RAINBOW:
            colorMap = std::make_unique<RainbowColorMap>();
            break;
    }
}

void FftThread::updateBitmap(const std::vector<jdouble> &data) {
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
    executeFft(data, nSamples, fftDataBuffer);
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
    double factor = (double) (fftDataBuffer.size() - 2) / (bitmapWidth - 1);
    for (int i = 0; i < bitmapWidth; i++) {
        int fftIndex = (int) round(i * factor);
        double magnitude = sqrt(sqr(fftDataBuffer[fftIndex]) + sqr(fftDataBuffer[fftIndex + 1]));

        jint color = colorMap->getColor((magnitude - minFFT) / maxFFT);
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
