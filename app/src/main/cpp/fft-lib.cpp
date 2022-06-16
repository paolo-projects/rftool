//
// Created by paolo on 13/06/22.
//

#include "fft-lib.h"

std::vector<kiss_fft_cpx> inputFft;
std::vector<kiss_fft_cpx> outputFft;
std::vector<jdouble> output;
std::vector<jdouble> outputData;

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_tools_rftool_fft_Fft_fft(JNIEnv *env, jobject _this,
                                  jdoubleArray data, jint nSamples) {
    std::vector<jdouble> inputData;

    jsize dataLength = env->GetArrayLength(data);
    inputData.resize(dataLength);
    env->GetDoubleArrayRegion(data, 0, dataLength, inputData.data());

    executeFft(inputData, nSamples, outputData);

    jdoubleArray outputArr = env->NewDoubleArray(nSamples * 2);
    env->SetDoubleArrayRegion(outputArr, 0, nSamples * 2, outputData.data());

    return outputArr;
}

void executeFft(const std::vector<jdouble> &data, int nSamples, std::vector<jdouble>& outputBuffer) {
    size_t dataLength = data.size();

    inputFft.resize(nSamples);
    outputFft.resize(nSamples);

    double decimationFactor = (double) dataLength / (2*nSamples);

    for (int i = 0; i < nSamples; i++) {
        int n = (int) round(decimationFactor * i);
        inputFft[i].r = data[n * 2];
        inputFft[i].i = data[n * 2 + 1];
    }

    kiss_fft_cfg cfg = kiss_fft_alloc(nSamples, false, nullptr, nullptr);
    kiss_fft(cfg, inputFft.data(), outputFft.data());

    outputBuffer.resize(nSamples * 2);

    for (int i = 0; i < nSamples; i++) {
        outputBuffer[i * 2] = outputFft[i].r;
        outputBuffer[i * 2 + 1] = outputFft[i].i;
    }
}