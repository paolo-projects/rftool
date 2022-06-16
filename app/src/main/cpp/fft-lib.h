//
// Created by paolo on 13/06/22.
//

#ifndef RFTOOL_FFT_LIB_H
#define RFTOOL_FFT_LIB_H

#include <jni.h>
#include <kiss_fft.h>
#include <vector>

void executeFft(const std::vector<jdouble>& data, int nSamples, std::vector<jdouble>& outputBuffer);

#endif //RFTOOL_FFT_LIB_H
