//
// Created by paolo on 16/06/22.
//

#ifndef RFTOOL_RECORDERTHREAD_H
#define RFTOOL_RECORDERTHREAD_H

#include <stdio.h>
#include <string>
#include <queue>
#include <condition_variable>
#include <mutex>
#include <thread>
#include <jni.h>

class RecorderThread {
public:
    RecorderThread(JNIEnv* env);
    ~RecorderThread();

    void startRecording(const std::string& fileName);
    void stopRecording();

    void appendData(const std::vector<uint8_t>& data);
private:
    JNIEnv* env;
    JavaVM* jvm{};
    FILE* fileHandle = nullptr;
    std::queue<std::vector<uint8_t>> queuedData;
    std::mutex mtx;
    std::condition_variable cv;
    bool recorderThreadRunning = false;
    std::unique_ptr<std::thread> recorderThread;

private:
    void executor();
};


#endif //RFTOOL_RECORDERTHREAD_H
