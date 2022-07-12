//
// Created by paolo on 11/07/22.
//

#ifndef RFTOOL_SOUNDPLAYER_H
#define RFTOOL_SOUNDPLAYER_H

#include <stdexcept>
#include <DataBuffer.h>
#include <oboe/AudioStream.h>
#include <oboe/AudioStreamBuilder.h>
#include <oboe/Utilities.h>

class SoundPlayerException: public std::runtime_error
{
public:
    SoundPlayerException(const char* msg): std::runtime_error(msg){}
    SoundPlayerException(const std::string& msg): std::runtime_error(msg){}
};

class SoundPlayer {
public:
    SoundPlayer(int sampleRate);
    ~SoundPlayer();
    void play();
    void pause();
    bool isRunning() const;
    void enqueue(const DataBuffer<int16_t>& data);

private:
    static constexpr int DATA_WRITE_TIMEOUT_NS = 100000;
    int sampleRate;
    std::shared_ptr<oboe::AudioStream> stream;
    bool running = false;
};


#endif //RFTOOL_SOUNDPLAYER_H
