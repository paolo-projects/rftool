//
// Created by paolo on 15/06/22.
//
#include "FftThread.h"

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

void FftThread::executor() {
    JavaVMAttachArgs ja_Args;
    ja_Args.name = nullptr;
    ja_Args.group = nullptr;
    ja_Args.version = JNI_VERSION_1_6;

    jvm->AttachCurrentThread(&env, &ja_Args);

    while(fftThreadRunning) {
        SdrData* rtlSdrData = latestData.claim();
        if(rtlSdrData) {
            std::transform(rtlSdrData->data, rtlSdrData->data + rtlSdrData->count,
                           scaledDataBuffer.begin(), [](uint8_t value) {
                        return (value - adcHalf) / adcHalf;
                    });
            updateBitmap(scaledDataBuffer, rtlSdrData->count);
            delete rtlSdrData;
        }
    }

    latestData.clear();

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
