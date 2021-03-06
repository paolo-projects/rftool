//
// Created by paolo on 21/06/22.
//

#include "SignalProcessing.h"

SignalProcessing::SignalProcessing(int fftSize) : fftSize(fftSize) {
    inData = (fftw_complex *) fftw_malloc(fftSize * sizeof(fftw_complex));
    outData = (fftw_complex *) fftw_malloc(fftSize * sizeof(fftw_complex));
}

SignalProcessing::~SignalProcessing() {
    fftw_free(inData);
    fftw_free(outData);
}

void SignalProcessing::bandPass(std::vector<double> &data, int sampleRate, int frequency,
                                const std::function<void(jdouble)> &progressCallback) {
    std::vector<ComplexNumber<double>> input(data.size() / 2);

    /*
    for (int i = 0; i < data.size() / 2; i += 2) {
        input[i].r = data[i * 2];
        input[i].i  =data[i * 2 + 1];
    }
    */
    memcpy(input.data(), data.data(), sizeof(double)*data.size());

    BandPass<double> filter(frequency, sampleRate);
    auto output = filter.filter(input);

    memcpy(data.data(), output.data(), sizeof(ComplexNumber<double>)*output.size());
    /*
    for (int i = 0; i < data.size() / 2; i += 2) {
        data[i * 2] = output[i].r;
        data[i * 2 + 1] = output[i].i;
    }
    */

    progressCallback(1.0);
}

double SignalProcessing::getSignalFrequency(const std::vector<double> &data, double signalThreshold,
                                            int sampleRate,
                                            const std::function<void(jdouble)> &progressCallback) {

    double maxIndexAvg = 0.0;
    unsigned int maxIndexCount = 0;

    fftw_plan fft_plan = fftw_plan_dft_1d(fftSize, inData,
                                          outData, FFTW_FORWARD, FFTW_ESTIMATE);

    const int fftCount = std::floor(data.size() / (fftSize * 2));
    for (int i = 0; i < fftCount; i += 2) {
        // Copy into the FFT input buffer
        memcpy(inData, &data[i * fftSize * 2], fftSize * 2 * sizeof(double));

        // Execute FFT
        fftw_execute(fft_plan);

        double maxFft = 0.0;
        int maxFftIndex = 0;

        // Get the maximums
        for (int n = 1; n < fftSize; n++) {
            double magnitude = sqrt(sqr(outData[n][0]) + sqr(outData[n][1]));
            if (magnitude > maxFft) {
                maxFft = magnitude;
                maxFftIndex = n;
            }
        }

        if (maxFft >= signalThreshold) {
            maxIndexAvg += maxFftIndex;
            maxIndexCount++;
        }

        if (i % 64 == 0) {
            progressCallback((double) i / fftCount * 0.33 + 0.33);
        }
    }

    fftw_destroy_plan(fft_plan);

    int freqIndex = (int) round(maxIndexAvg / maxIndexCount);

    return fftIndexToFrequency(freqIndex, sampleRate);
}

double SignalProcessing::fftIndexToFrequency(unsigned int index, int sampleRate) {
    if (index == 0) {
        return 0.0;
    } else if (index < fftSize / 2) {
        return (double) index / fftSize * sampleRate;
    } else {
        double fftSizeHalf = (double) fftSize / 2;
        return -(1.0 - (index - fftSizeHalf) / fftSizeHalf) * ((double) sampleRate / 2);
    }
}