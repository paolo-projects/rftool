//
// Created by paolo on 11/07/22.
//

#include "FmDemodManager.h"

FmDemodManager::FmDemodManager(int sampleRate, float digitalGain)
    : demodulator(
            [this](const DataBuffer<int16_t>& audioData) {
                fmDemodulatorCallback(audioData); },
            sampleRate, AUDIO_SAMPLE_RATE, digitalGain),
            player(AUDIO_SAMPLE_RATE) {

}

void FmDemodManager::pushFmData(const std::array<uint8_t, RTLSDR_MAX_READ_BUFFER>& data, size_t size) {
    if(player.isRunning()) {
        demodulator.demodulate(DataBuffer<uint8_t>(data.data(), size));
    }
}

void FmDemodManager::updateSampleRate(int sampleRate) {
    demodulator.setSampleRate(sampleRate);
}

void FmDemodManager::updateDigitalGain(float digitalGain) {
    demodulator.setDigitalGain(digitalGain);
}

void FmDemodManager::fmDemodulatorCallback(const DataBuffer<int16_t>& data) {
    player.enqueue(data);
}

void FmDemodManager::enablePlayback() {
    player.play();
}

void FmDemodManager::disablePlayback() {
    player.pause();
}