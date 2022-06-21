//
// Created by paolo on 13/06/22.
//

#ifndef RFTOOL_FFT_LIB_H
#define RFTOOL_FFT_LIB_H

#include <jni.h>
#include <kiss_fft.h>
#include <vector>

struct FFTLib {
    template<size_t N>
    static void executeFft(const std::array<jdouble, N> &data, size_t count, int nSamples,
                           std::vector<jdouble> &outputBuffer);

    static void
    executeFft(const std::vector<jdouble> &data, int nSamples, std::vector<jdouble> &outputBuffer);

private:
    static std::vector<kiss_fft_cpx> inputFft;
    static std::vector<kiss_fft_cpx> outputFft;
};

template<size_t N>
void FFTLib::executeFft(const std::array<jdouble, N> &data, size_t count, int nSamples,
                        std::vector<jdouble> &outputBuffer) {
    inputFft.resize(nSamples);
    outputFft.resize(nSamples);

    double decimationFactor = (double) count / (2 * nSamples);

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

#endif //RFTOOL_FFT_LIB_H
