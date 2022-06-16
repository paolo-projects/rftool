//
// Created by paolo on 13/06/22.
//

#ifndef RFTOOL_SDR_LIB_H
#define RFTOOL_SDR_LIB_H

#include <jni.h>
#include <string>
#include <rtl-sdr.h>
#include <android/log.h>
#include <vector>
#include <algorithm>
#include <map>
#include <libusb.h>
#include <memory>

#include "fft-thread.h"

int nearestGain(int targetGain);

#endif //RFTOOL_SDR_LIB_H
