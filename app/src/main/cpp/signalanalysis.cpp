#include <jni.h>
#include <vector>
#include "signalprocessing.h"

SignalProcessing* signalProcessing = nullptr;

extern "C" JNIEXPORT void JNICALL
    Java_com_tools_rftool_signalprocessing_SignalProcessing_init(
            JNIEnv* env, jobject _this
            ) {
    delete signalProcessing;
    signalProcessing = new SignalProcessing(1024);
}

extern "C" JNIEXPORT void JNICALL
Java_com_tools_rftool_signalprocessing_SignalProcessing_free(
        JNIEnv* env, jobject _this
) {
    delete signalProcessing;
    signalProcessing = nullptr;
}

extern "C" JNIEXPORT void JNICALL
    Java_com_tools_rftool_signalprocessing_SignalProcessing_filterDataToSignalFrequency
            (JNIEnv* env, jobject _this, jdoubleArray data, jint sampleRate, jdouble threshold) {
    if(signalProcessing == nullptr) {
        throw std::runtime_error("Call 'init' before calling this method");
    }

    jclass class_SignalProcessing = env->FindClass("com/tools/rftool/signalprocessing/SignalProcessing");
    jmethodID signalProcessing_onProgressUpdate = env->GetMethodID(class_SignalProcessing, "onProgressUpdate", "(D)V");

    auto progressUpdateCallback = [&](jdouble progress) {
        env->CallVoidMethod(_this, signalProcessing_onProgressUpdate, progress);
    };

    progressUpdateCallback(0.33);

    jsize dataSize = env->GetArrayLength(data);
    std::vector<double> dataVector(dataSize);
    env->GetDoubleArrayRegion(data, 0, dataSize, dataVector.data());

    double frequency = signalProcessing->getSignalFrequency(dataVector, threshold, sampleRate, progressUpdateCallback);
    SignalProcessing::bandPass(dataVector, sampleRate, (int)round(frequency), progressUpdateCallback);

    env->SetDoubleArrayRegion(data, 0, dataSize, dataVector.data());

    progressUpdateCallback(1.0);
}