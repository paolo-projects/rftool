//
// Created by paolo on 20/06/22.
//

#include "signalanalysisview.h"

jclass class_Bitmap;
jmethodID bitmap_getPixels;
jmethodID bitmap_setPixels;

jclass class_SignalAnalysisView;
jfieldID signalAnalysisView_bitmap;

jclass class_SignalDataPoint;
jfieldID _signalDataPoint_time;
jfieldID _signalDataPoint_value;

std::vector<SignalDataPoint> dataPoints;

extern "C" JNIEXPORT void JNICALL
Java_com_tools_signalanalysis_ui_SignalAnalysisView_initNative(JNIEnv *env, jobject _this) {
    class_Bitmap = env->FindClass("android/graphics/Bitmap");
    bitmap_getPixels = env->GetMethodID(class_Bitmap, "getPixels", "([IIIIIII)V");
    bitmap_setPixels = env->GetMethodID(class_Bitmap, "setPixels", "([IIIIIII)V");

    class_SignalAnalysisView = env->FindClass("com/tools/signalanalysis/ui/SignalAnalysisView");
    signalAnalysisView_bitmap = env->GetFieldID(class_SignalAnalysisView, "_bitmap",
                                                "Landroid/graphics/Bitmap;");

    class_SignalDataPoint = env->FindClass("com/tools/signalanalysis/adapter/SignalDataPoint");
    _signalDataPoint_time = env->GetFieldID(class_SignalDataPoint, "time", "J");
    _signalDataPoint_value = env->GetFieldID(class_SignalDataPoint, "value", "D");
}

extern "C" JNIEXPORT void JNICALL
Java_com_tools_signalanalysis_ui_SignalAnalysisView_onDrawNative(JNIEnv *env, jobject _this,
                                                                 jobjectArray data) {
    jobject bitmap = env->GetObjectField(_this, signalAnalysisView_bitmap);
    jsize dataSize = env->GetArrayLength(data);
    dataPoints.resize(dataSize);

    for(int i = 0; i < dataSize; i++) {
        jobject entry = env->GetObjectArrayElement(data, i);
        dataPoints[i] = {
                static_cast<double>(env->GetLongField(entry, _signalDataPoint_time)),
                env->GetDoubleField(entry, _signalDataPoint_value)
        };
    }


}