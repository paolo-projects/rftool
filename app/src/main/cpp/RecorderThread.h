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

#include "readerwriterqueue/readerwriterqueue.h"

class RecorderThread {
public:
    RecorderThread(JNIEnv *env);

    ~RecorderThread();

    void startRecording(JNIEnv *env, jobject recorderInstance, const std::string &fileName);

    void startRecording(JNIEnv *env, jobject recorderInstance, const std::string &fileName,
                        long durationMs);

    void stopRecording(JNIEnv *env);

    template<size_t N>
    void appendData(const std::array<uint8_t, N> &data, size_t count);

private:
    const char *TAG = "RecorderThread";

    JNIEnv *env;
    JavaVM *jvm{};
    std::unique_ptr<std::ofstream> recordingFile = nullptr;
    long recordingDuration = -1;
    std::chrono::time_point<std::chrono::system_clock> recordingStartTime;
    moodycamel::ReaderWriterQueue<std::vector<uint8_t>> queuedData;
    bool recorderThreadRunning = false;
    bool toStopOngoingRecording = false;
    std::unique_ptr<std::thread> recorderThread;

    // Java(Kotlin) fields,methods,classes
    jobject recorderInstance = nullptr;
    jmethodID recorder_onRecordingStarted;
    jmethodID recorder_onRecordingCompleted;

private:
    void executor();
};


template<size_t N>
void RecorderThread::appendData(const std::array<uint8_t, N> &data, size_t count) {
    auto nowTime = std::chrono::system_clock::now();
    auto startTime = std::chrono::high_resolution_clock::now();
    if (recordingFile != nullptr && !toStopOngoingRecording) {
        std::vector<uint8_t> dataToRecord(count);
        std::copy(data.cbegin(), data.cbegin() + count, dataToRecord.begin());
        queuedData.emplace(dataToRecord);

        if (recordingDuration > 0 && std::chrono::duration_cast<std::chrono::milliseconds>(
                nowTime - recordingStartTime).count() > recordingDuration) {
            __android_log_write(ANDROID_LOG_DEBUG, TAG, "Recording time elapsed. Stopping...");

            JavaVMAttachArgs args;
            args.version = JNI_VERSION_1_6;
            args.group = nullptr;
            args.name = nullptr;
            jvm->AttachCurrentThread(&env, &args);
            stopRecording(env);
            jvm->DetachCurrentThread();
        }
    }
    auto endTime = std::chrono::high_resolution_clock::now();
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Recorder file write overhead %d ms",
                        std::chrono::duration_cast<std::chrono::milliseconds>(
                                endTime - startTime).count());
}

#endif //RFTOOL_RECORDERTHREAD_H
