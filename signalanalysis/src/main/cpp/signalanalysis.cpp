#include <jni.h>
#include <string>
#include <vector>
#include "SignalDataPoint.h"
#include "ThreeBuckets.h"

std::vector<SignalDataPoint> inputData;
std::vector<SignalDataPoint> outputData;
jclass class_signalDataPoint;
jfieldID signalDataPoint_time;
jfieldID signalDataPoint_value;

using LTB = LargestTriangleThreeBuckets<SignalDataPoint, double, &SignalDataPoint::x, &SignalDataPoint::y>;

extern "C" JNIEXPORT void JNICALL
        Java_com_tools_signalanalysis_utils_ThreeBuckets_init(JNIEnv* env, jobject _this, jint downSampledSize) {
    outputData.resize(downSampledSize);
    class_signalDataPoint = env->FindClass("com/tools/signalanalysis/adapter/SignalDataPoint");
    signalDataPoint_time = env->GetFieldID(class_signalDataPoint, "time", "J");
    signalDataPoint_value = env->GetFieldID(class_signalDataPoint, "value", "D");
}

extern "C" JNIEXPORT void JNICALL
Java_com_tools_signalanalysis_utils_ThreeBuckets_downSample
        (JNIEnv* env, jobject _this, jobjectArray data, jobjectArray outData) {
    jsize size = env->GetArrayLength(data);
    inputData.resize(size);

    for(int i = 0; i < size; i++) {
        jobject entry = env->GetObjectArrayElement(data, i);
        jlong time = env->GetLongField(entry, signalDataPoint_time);
        jdouble value = env->GetDoubleField(entry, signalDataPoint_value);

        inputData[i] = {
                (double)time,
                value
        };
    }

    LTB::Downsample(inputData.begin(), inputData.size(), outputData.begin(), outputData.size());

    for(int i = 0; i < outputData.size(); i++) {
        jobject entry = env->GetObjectArrayElement(outData, i);
        env->SetLongField(entry, signalDataPoint_time, (jlong)outputData[i].x);
        env->SetDoubleField(entry, signalDataPoint_value, outputData[i].y);
    }
}