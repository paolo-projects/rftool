//
// Created by paolo on 13/06/22.
//

#ifndef RFTOOL_FFTLIB_H
#define RFTOOL_FFTLIB_H

#include <jni.h>
#include <kiss_fft.h>
#include <vector>

struct FFTLib {
    FFTLib(int fftSize);

    ~FFTLib();

    template<size_t N>
    void executeFft(const std::array<jdouble, N> &data, size_t count,
                    std::vector<jdouble> &outputBuffer);

    static void
    executeFft(const std::vector<jdouble> &data, int nSamples, std::vector<jdouble> &outputBuffer);

private:
    int fftSize;
    kiss_fft_cfg cfg;
    std::vector<kiss_fft_cpx> inputFft;
    std::vector<kiss_fft_cpx> outputFft;

    static std::vector<kiss_fft_cpx> staticInputFft;
    static std::vector<kiss_fft_cpx> staticOutputFft;
};

template<size_t N>
void FFTLib::executeFft(const std::array<jdouble, N> &data, size_t count,
                        std::vector<jdouble> &outputBuffer) {
    inputFft.resize(fftSize);
    outputFft.resize(fftSize);

    double decimationFactor = (double) count / (2 * fftSize);

    for (int i = 0; i < fftSize; i++) {
        int n = (int) round(decimationFactor * i);
        inputFft[i].r = data[n * 2];
        inputFft[i].i = data[n * 2 + 1];
    }

    kiss_fft_cfg cfg = kiss_fft_alloc(fftSize, false, nullptr, nullptr);
    kiss_fft(cfg, inputFft.data(), outputFft.data());
    kiss_fft_free(cfg);

    outputBuffer.resize(fftSize * 2);

    for (int i = 0; i < fftSize; i++) {
        outputBuffer[i * 2] = outputFft[i].r;
        outputBuffer[i * 2 + 1] = outputFft[i].i;
    }
}

#endif //RFTOOL_FFTLIB_H
