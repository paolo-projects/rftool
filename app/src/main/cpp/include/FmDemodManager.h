//
// Created by paolo on 11/07/22.
//

#ifndef RFTOOL_FMDEMODMANAGER_H
#define RFTOOL_FMDEMODMANAGER_H

#include "SoundPlayer.h"
#include <FmDemodulator.h>
#include "common.h"

class FmDemodManager {
public:
    FmDemodManager() = delete;
    FmDemodManager(const FmDemodManager&) = delete;
    FmDemodManager(int sampleRate, float digitalGain);

    void pushFmData(const std::array<uint8_t, RTLSDR_MAX_READ_BUFFER>& data, size_t size);
    void updateSampleRate(int sampleRate);
    void updateDigitalGain(float digitalGain);

    void enablePlayback();
    void disablePlayback();
private:
    static constexpr int AUDIO_SAMPLE_RATE = 44100;
    SoundPlayer player;
    FmDemodulator demodulator;

    void fmDemodulatorCallback(const DataBuffer<int16_t>& data);
};


#endif //RFTOOL_FMDEMODMANAGER_H
