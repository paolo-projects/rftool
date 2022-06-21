//
// Created by paolo on 13/06/22.
//

#include "FFTLib.h"

std::vector<kiss_fft_cpx> FFTLib::staticInputFft;
std::vector<kiss_fft_cpx> FFTLib::staticOutputFft;
std::vector<jdouble> output;
std::vector<jdouble> outputData;

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_tools_rftool_fft_Fft_fft(JNIEnv *env, jobject _this,
                                  jdoubleArray data, jint nSamples) {
    std::vector<jdouble> inputData;

    jsize dataLength = env->GetArrayLength(data);
    inputData.resize(dataLength);
    env->GetDoubleArrayRegion(data, 0, dataLength, inputData.data());

    FFTLib::executeFft(inputData, nSamples, outputData);

    jdoubleArray outputArr = env->NewDoubleArray(nSamples * 2);
    env->SetDoubleArrayRegion(outputArr, 0, nSamples * 2, outputData.data());

    return outputArr;
}

FFTLib::FFTLib(int fftSize)
: fftSize(fftSize) {
    cfg = kiss_fft_alloc(fftSize, false, nullptr, nullptr);
    inputFft.resize(fftSize);
    outputFft.resize(fftSize);
}

FFTLib::~FFTLib() {
    kiss_fft_free(cfg);
    cfg = nullptr;
}

void FFTLib::executeFft(const std::vector<jdouble> &data, int nSamples,
                        std::vector<jdouble> &outputBuffer) {
    staticInputFft.resize(nSamples);
    staticOutputFft.resize(nSamples);

    double decimationFactor = (double) data.size() / (2 * nSamples);

    for (int i = 0; i < nSamples; i++) {
        int n = (int) round(decimationFactor * i);
        staticInputFft[i].r = data[n * 2];
        staticInputFft[i].i = data[n * 2 + 1];
    }

    kiss_fft_cfg cfg = kiss_fft_alloc(nSamples, false, nullptr, nullptr);
    kiss_fft(cfg, staticInputFft.data(), staticOutputFft.data());
    kiss_fft_free(cfg);

    outputBuffer.resize(nSamples * 2);

    for (int i = 0; i < nSamples; i++) {
        outputBuffer[i * 2] = staticOutputFft[i].r;
        outputBuffer[i * 2 + 1] = staticOutputFft[i].i;
    }
}