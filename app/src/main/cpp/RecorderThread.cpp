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

    //recorderThreadRunning = true;
    //recorderThread = std::make_unique<std::thread>(&RecorderThread::executor, this);
}

RecorderThread::~RecorderThread() {
    /*recorderThreadRunning = false;
    if (recorderThread != nullptr) {
        recorderThread->join();
        recorderThread.reset();
    }*/
}

void RecorderThread::startRecording(JNIEnv* currentEnv, jobject instance, const std::string &fileName) {
    if (recordingFile == nullptr) {
        if(recorderInstance != nullptr) {
            currentEnv->DeleteGlobalRef(recorderInstance);
        }
        recorderInstance = instance;
        recordingDuration = -1;

        //std::unique_lock<std::mutex> lock(mtx);
        //fileHandle = fopen(fileName.c_str(), "wb");
        recordingFile = std::make_unique<std::ofstream>(fileName, std::ios_base::binary);
        if (recordingFile->good()) {
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "File opened for recording: %s",
                                fileName.c_str());
        } else {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "Error opening file: %s", fileName.c_str());
            recordingFile.reset();
        }

        currentEnv->CallVoidMethod(recorderInstance, recorder_onRecordingStarted);
    }
}

void RecorderThread::startRecording(JNIEnv* currentEnv, jobject instance, const std::string &fileName, long durationMs) {
    if (recordingFile == nullptr) {
        if(recorderInstance != nullptr) {
            currentEnv->DeleteGlobalRef(recorderInstance);
        }
        recorderInstance = instance;

        recordingDuration = durationMs;
        recordingStartTime = std::chrono::system_clock::now();

        recordingFile = std::make_unique<std::ofstream>(fileName, std::ios_base::binary);
        if (recordingFile->good()) {
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "File opened for recording: %s",
                                fileName.c_str());
        } else {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "Error opening file: %s", fileName.c_str());
            recordingFile.reset();
        }

        currentEnv->CallVoidMethod(recorderInstance, recorder_onRecordingStarted);
    }
}

void RecorderThread::stopRecording(JNIEnv* currentEnv) {
    if (recordingFile != nullptr) {
        recordingFile->close();
        recordingFile.reset();
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "File recording closed");
        //restoreOriginalSize();

        if(recorderInstance != nullptr) {
            currentEnv->CallVoidMethod(recorderInstance, recorder_onRecordingCompleted);
            currentEnv->DeleteGlobalRef(recorderInstance);
            recorderInstance = nullptr;
        }
    }
}