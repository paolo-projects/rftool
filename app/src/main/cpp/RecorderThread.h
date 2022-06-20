//
// Created by paolo on 16/06/22.
//

#ifndef RFTOOL_RECORDERTHREAD_H
#define RFTOOL_RECORDERTHREAD_H

#include <stdio.h>
#include <fstream>
#include <string>
#include <queue>
#include <condition_variable>
#include <mutex>
#include <thread>
#include <jni.h>
#include <chrono>
#include <functional>
#include <android/log.h>

class RecorderThread {
public:
    RecorderThread(JNIEnv* env, std::function<void()> restoreOriginalSize);
    ~RecorderThread();

    void startRecording(JNIEnv* env,jobject recorderInstance, const std::string& fileName);
    void startRecording(JNIEnv* env,jobject recorderInstance, const std::string& fileName, long durationMs);
    void stopRecording(JNIEnv* env);

    void appendData(const std::vector<uint8_t>& data);
private:
    const char* TAG = "RecorderThread";

    JNIEnv* env;
    JavaVM* jvm{};
    std::function<void()> restoreOriginalSize;
    //FILE* fileHandle = nullptr;
    std::unique_ptr<std::ofstream> recordingFile = nullptr;
    std::queue<std::vector<uint8_t>> queuedData;
    std::mutex mtx;
    std::condition_variable cv;
    bool recorderThreadRunning = false;
    std::unique_ptr<std::thread> recorderThread;
    long recordingDuration = -1;
    std::chrono::time_point<std::chrono::system_clock> recordingStartTime;

    // Java(Kotlin) fields,methods,classes
    jobject recorderInstance = nullptr;
    jmethodID recorder_onRecordingStarted;
    jmethodID recorder_onRecordingCompleted;

private:
    void executor();
};


#endif //RFTOOL_RECORDERTHREAD_H
