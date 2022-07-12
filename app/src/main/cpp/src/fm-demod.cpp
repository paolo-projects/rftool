//
// Created by paolo on 11/07/22.
//

#include "fm-demod.h"

FmDemodManager fmDemodManager(1600000, 1e3f);

extern "C" JNIEXPORT void JNICALL
    Java_com_tools_rftool_rtlsdr_RtlSdr_enableFmPlayback(JNIEnv* env, jobject _this) {
    fmDemodManager.enablePlayback();
}

extern "C" JNIEXPORT void JNICALL
    Java_com_tools_rftool_rtlsdr_RtlSdr_disableFmPlayback(JNIEnv* env, jobject _this) {
    fmDemodManager.disablePlayback();
}

extern "C" JNIEXPORT void JNICALL
    Java_com_tools_rftool_rtlsdr_RtlSdr_setPlaybackDigitalGain(JNIEnv* env, jobject _this, jfloat digitalGain) {
    fmDemodManager.updateDigitalGain(digitalGain);
}