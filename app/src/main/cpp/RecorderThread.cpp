//
// Created by paolo on 16/06/22.
//

#include "RecorderThread.h"

RecorderThread::RecorderThread(JNIEnv *env)
        : env(env) {
    env->GetJavaVM(&jvm);

    jclass recorderClass = env->FindClass("com/tools/rftool/rtlsdr/Recorder");
    recorder_onRecordingStarted = env->GetMethodID(recorderClass, "onRecordingStarted", "()V");
    recorder_onRecordingCompleted = env->GetMethodID(recorderClass, "onRecordingCompleted", "()V");

    recorderThreadRunning = true;
    recorderThread = std::make_unique<std::thread>(&RecorderThread::executor, this);
}

RecorderThread::~RecorderThread() {
    recorderThreadRunning = false;
    if (recorderThread != nullptr) {
        recorderThread->join();
        recorderThread.reset();
    }
}

void RecorderThread::startRecording(jobject instance, const std::string &fileName) {
    if (fileHandle == nullptr) {
        if(recorderInstance != nullptr) {
            env->DeleteGlobalRef(recorderInstance);
        }
        recorderInstance = env->NewGlobalRef(instance);
        recordingDuration = {};

        std::unique_lock<std::mutex> lock(mtx);
        fileHandle = fopen(fileName.c_str(), "wb");
        if (fileHandle != nullptr) {
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "File opened for recording: %s",
                                fileName.c_str());
        } else {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "Error opening file: %s", fileName.c_str());
        }

        env->CallVoidMethod(recorderInstance, recorder_onRecordingStarted);
    }
}

void RecorderThread::startRecording(jobject instance, const std::string &fileName, long durationMs) {
    if (fileHandle == nullptr) {
        if(recorderInstance != nullptr) {
            env->DeleteGlobalRef(recorderInstance);
        }
        recorderInstance = env->NewGlobalRef(instance);

        recordingDuration = durationMs;
        recordingStartTime = std::chrono::system_clock::now();

        std::unique_lock<std::mutex> lock(mtx);
        fileHandle = fopen(fileName.c_str(), "wb");
        if (fileHandle != nullptr) {
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "File opened for recording: %s",
                                fileName.c_str());
        } else {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "Error opening file: %s", fileName.c_str());
        }

        env->CallVoidMethod(recorderInstance, recorder_onRecordingStarted);
    }
}

void RecorderThread::stopRecording() {
    if (fileHandle != nullptr) {
        std::unique_lock<std::mutex> lock(mtx);
        fclose(fileHandle);
        fileHandle = nullptr;
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "File recording closed");

        if(recorderInstance != nullptr) {
            env->CallVoidMethod(recorderInstance, recorder_onRecordingCompleted);
            env->DeleteGlobalRef(recorderInstance);
            recorderInstance = nullptr;
        }
    }
}

void RecorderThread::appendData(const std::vector<uint8_t> &data) {
    std::lock_guard<std::mutex> lock(mtx);
    if (fileHandle != nullptr) {
        queuedData.emplace(data);
        cv.notify_one();
    }
}

void RecorderThread::executor() {
    JavaVMAttachArgs ja_Args;
    ja_Args.name = nullptr;
    ja_Args.group = nullptr;
    ja_Args.version = JNI_VERSION_1_6;

    jvm->AttachCurrentThread(&env, &ja_Args);

    while (recorderThreadRunning) {
        std::unique_lock<std::mutex> lock(mtx);
        cv.wait(lock, [&]() {
            return recorderThreadRunning && !queuedData.empty() && fileHandle != nullptr;
        });

        __android_log_write(ANDROID_LOG_DEBUG, TAG, "Writing data to file");

        while (!queuedData.empty()) {
            auto &data = queuedData.front();
            fwrite(data.data(), data.size(), 1, fileHandle);
            queuedData.pop();
        }

        auto nowTime = std::chrono::system_clock::now();
        if (!recordingDuration.isEmpty() && std::chrono::duration_cast<std::chrono::milliseconds>(
                nowTime - recordingStartTime).count() > recordingDuration.get()) {
            stopRecording();
        }
    }

    jvm->DetachCurrentThread();
}