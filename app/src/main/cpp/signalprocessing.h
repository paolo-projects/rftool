//
// Created by paolo on 21/06/22.
//

#ifndef RFTOOL_SIGNALPROCESSING_H
#define RFTOOL_SIGNALPROCESSING_H

#include <jni.h>
#include <complex.h>
#include <fftw3.h>
#include <algorithm>
#include <vector>

#include "BandPass.h"

#define sqr(x) ((x)*(x))

class SignalProcessing {
public:
    SignalProcessing(int fftSize);
    ~SignalProcessing();

    double
    getSignalFrequency(const std::vector<double> &data, double signalThreshold, int sampleRate,
                       const std::function<void(jdouble)> &progressCallback);

    static void bandPass(std::vector<double> &data, int sampleRate, int frequency,
                  const std::function<void(jdouble)> &progressCallback);

private:
    int fftSize;
    fftw_complex* inData;
    fftw_complex* outData;

    double fftIndexToFrequency(unsigned int index, int sampleRate);
};

#endif //RFTOOL_SIGNALPROCESSING_H
