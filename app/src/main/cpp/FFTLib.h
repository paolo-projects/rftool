//
// Created by paolo on 13/06/22.
//

#ifndef RFTOOL_FFTLIB_H
#define RFTOOL_FFTLIB_H

#include <jni.h>
#include <fftw3.h>
#include <complex>
#include <vector>

/**
 * Abstraction over the fft implementation
 */
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
    fftw_plan fftPlan;
    fftw_complex *inputFft;
    fftw_complex *outputFft;
};

template<size_t N>
void FFTLib::executeFft(const std::array<jdouble, N> &data, size_t count,
                        std::vector<jdouble> &outputBuffer) {
    double decimationFactor = (double) count / (2 * fftSize);

    for (int i = 0; i < fftSize; i++) {
        int n = (int) round(decimationFactor * i);
        inputFft[i][0] = data[n * 2];
        inputFft[i][1] = data[n * 2 + 1];
    }

    fftw_execute(fftPlan);

    outputBuffer.resize(fftSize * 2);

    for (int i = 0; i < fftSize; i++) {
        outputBuffer[i * 2] = outputFft[i][0];
        outputBuffer[i * 2 + 1] = outputFft[i][1];
    }
}

#endif //RFTOOL_FFTLIB_H
