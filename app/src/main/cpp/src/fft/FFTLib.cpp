//
// Created by paolo on 13/06/22.
//

#include "fft/FFTLib.h"

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
    inputFft = (fftw_complex *) fftw_malloc(fftSize * sizeof(fftw_complex));
    outputFft = (fftw_complex *) fftw_malloc(fftSize * sizeof(fftw_complex));
    fftPlan = fftw_plan_dft_1d(fftSize, inputFft,
                               outputFft, FFTW_FORWARD,
                               FFTW_ESTIMATE);
}

FFTLib::~FFTLib() {
    fftw_destroy_plan(fftPlan);
    fftw_free(inputFft);
    fftw_free(outputFft);
}

void FFTLib::setFftN(int fftN) {
    std::unique_lock<std::mutex> lock(mtx);
    fftw_free(inputFft);
    fftw_free(outputFft);

    inputFft = (fftw_complex*)fftw_malloc(fftN * sizeof(fftw_complex));
    outputFft = (fftw_complex*)fftw_malloc(fftN * sizeof(fftw_complex));
    fftSize = fftN;
}

void FFTLib::executeFft(const std::vector<jdouble> &data, int nSamples,
                        std::vector<jdouble> &outputBuffer) {

    auto *fftIn = (fftw_complex *)fftw_malloc(nSamples * sizeof(fftw_complex));
    auto *fftOut = (fftw_complex *)fftw_malloc(nSamples * sizeof(fftw_complex));

    double decimationFactor = (double) data.size() / (2 * nSamples);

    for (int i = 0; i < nSamples; i++) {
        int n = (int) round(decimationFactor * i);
        fftIn[i][0] = data[n * 2];
        fftIn[i][1] = data[n * 2 + 1];
    }

    fftw_plan fftwPlan = fftw_plan_dft_1d(nSamples,
                                          fftIn,
                                          fftOut,
                                          FFTW_FORWARD, FFTW_ESTIMATE);
    fftw_execute(fftwPlan);
    fftw_destroy_plan(fftwPlan);

    outputBuffer.resize(nSamples * 2);

    for (int i = 0; i < nSamples; i++) {
        outputBuffer[i * 2] = fftOut[i][0];
        outputBuffer[i * 2 + 1] = fftOut[i][1];
    }

    fftw_free(fftIn);
    fftw_free(fftOut);
}