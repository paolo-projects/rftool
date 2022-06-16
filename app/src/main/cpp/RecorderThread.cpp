//
// Created by paolo on 16/06/22.
//

#include "RecorderThread.h"

RecorderThread::RecorderThread(JNIEnv* env)
: env(env)
{
    env->GetJavaVM(&jvm);

    recorderThreadRunning = true;
    recorderThread = std::make_unique<std::thread>(&RecorderThread::executor, this);
}

RecorderThread::~RecorderThread() {
    recorderThreadRunning = false;
    if(recorderThread != nullptr) {
        recorderThread->join();
        recorderThread.reset();
    }
}

void RecorderThread::startRecording(const std::string& fileName) {
    if(fileHandle == nullptr) {
        std::unique_lock<std::mutex> lock(mtx);
        fileHandle = fopen(fileName.c_str(), "wb");
    }
}

void RecorderThread::stopRecording() {
    if(fileHandle != nullptr) {
        std::unique_lock<std::mutex> lock(mtx);
        fclose(fileHandle);
        fileHandle = nullptr;
    }
}

void RecorderThread::appendData(const std::vector<uint8_t>& data) {
    std::lock_guard<std::mutex> lock(mtx);
    queuedData.emplace(data);
    cv.notify_one();
}

void RecorderThread::executor() {
    JavaVMAttachArgs ja_Args;
    ja_Args.name = nullptr;
    ja_Args.group = nullptr;
    ja_Args.version = JNI_VERSION_1_6;

    jvm->AttachCurrentThread(&env, &ja_Args);

    while(recorderThreadRunning) {
        std::unique_lock<std::mutex> lock(mtx);
        cv.wait(lock, [&]() {
            return recorderThreadRunning && !queuedData.empty() && fileHandle != nullptr;
        });

        while(!queuedData.empty()) {
            auto& data = queuedData.front();
            fwrite(data.data(), data.size(), 1, fileHandle);
            queuedData.pop();
        }
    }

    jvm->DetachCurrentThread();
}