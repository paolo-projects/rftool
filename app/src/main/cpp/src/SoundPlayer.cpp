//
// Created by paolo on 11/07/22.
//

#include "SoundPlayer.h"

SoundPlayer::SoundPlayer(int sampleRate) : sampleRate(sampleRate) {
    oboe::AudioStreamBuilder builder;
    builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
    builder.setSharingMode(oboe::SharingMode::Shared);
    builder.setChannelCount(1);
    builder.setContentType(oboe::ContentType::Music);
    builder.setDirection(oboe::Direction::Output);
    builder.setFormat(oboe::AudioFormat::I16);
    builder.setSampleRate(sampleRate);

    oboe::Result res;
    if((res = builder.openStream(stream)) != oboe::Result::OK) {
        throw SoundPlayerException("Error opening stream: " + std::string(oboe::convertToText(res)));
    }
}

SoundPlayer::~SoundPlayer() {
    if(stream != nullptr && stream->getState() == oboe::StreamState::Open) {
        stream->close();
    }
}

void SoundPlayer::play() {
    if(stream != nullptr && !running) {
        stream->requestStart();
        running = true;
    }
}

void SoundPlayer::pause() {
    if(stream != nullptr && running) {
        stream->requestPause();
        running = false;
    }
}

bool SoundPlayer::isRunning() const {
    return running;
}

void SoundPlayer::enqueue(const DataBuffer<int16_t>& data) {
    stream->write(data.get(), data.size(), DATA_WRITE_TIMEOUT_NS);
}