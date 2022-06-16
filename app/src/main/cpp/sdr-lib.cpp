#include "sdr-lib.h"

#include <memory>

const char *TAG = "RfToolLib";
rtlsdr_dev *device = nullptr;
std::unique_ptr<FftThread> fftTrd;
std::unique_ptr<RecorderThread> recorderThread;

constexpr jdouble adcHalf = 255.0 / 2;
std::map<libusb_error, std::string> libusbErrorCodes{
        {LIBUSB_SUCCESS,
                "LIBUSB_SUCCESS"},
        {LIBUSB_ERROR_IO,
                "LIBUSB_ERROR_IO"},
        {LIBUSB_ERROR_INVALID_PARAM,
                "LIBUSB_ERROR_INVALID_PARAM"},
        {LIBUSB_ERROR_ACCESS,
                "LIBUSB_ERROR_ACCESS"},
        {LIBUSB_ERROR_NO_DEVICE,
                "LIBUSB_ERROR_NO_DEVICE"},
        {LIBUSB_ERROR_NOT_FOUND,
                "LIBUSB_ERROR_NOT_FOUND"},
        {LIBUSB_ERROR_BUSY,
                "LIBUSB_ERROR_BUSY"},
        {LIBUSB_ERROR_TIMEOUT,
                "LIBUSB_ERROR_TIMEOUT"},
        {LIBUSB_ERROR_OVERFLOW,
                "LIBUSB_ERROR_OVERFLOW"},
        {LIBUSB_ERROR_PIPE,
                "LIBUSB_ERROR_PIPE"},
        {LIBUSB_ERROR_INTERRUPTED,
                "LIBUSB_ERROR_INTERRUPTED"},
        {LIBUSB_ERROR_NO_MEM,
                "LIBUSB_ERROR_NO_MEM"},
        {LIBUSB_ERROR_NOT_SUPPORTED,
                "LIBUSB_ERROR_NOT_SUPPORTED"},
        {LIBUSB_ERROR_OTHER,
                "LIBUSB_ERROR_OTHER"},
};
std::vector<uint8_t> buffer;
std::vector<jdouble> outDataBuffer;
std::vector<int> gains;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_open(JNIEnv *env, jobject _this, jint fileDescriptor,
                                         jint sampleRate,
                                         jint centerFrequency,
                                         jint ppmError,
                                         jint gain,
                                         jint fftSamples) {
    int res = rtlsdr_open(&device, fileDescriptor);
    if (res < 0) {
        return false;
    }
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "RTL SDR Device with FD %d opened successfully",
                        fileDescriptor);

    res = rtlsdr_set_sample_rate(device, sampleRate);
    if (res < 0) {
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to set sample rate");
        rtlsdr_close(device);
        return false;
    }
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "Sample rate set successfully to %d Hz",
                        sampleRate);

    res = rtlsdr_set_center_freq(device, centerFrequency);
    if (res < 0) {
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to set center frequency");
        rtlsdr_close(device);
        return false;
    }
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "Center frequency set successfully to %d Hz",
                        centerFrequency);

    res = rtlsdr_set_freq_correction(device, ppmError);
    if (res < 0) {
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to set ppm error");
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "Ppm error set successfully to %d ppm",
                            ppmError);
    }

    if (gain > 0) {
        res = rtlsdr_set_tuner_gain_mode(device, 1);
        if (res < 0) {
            __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to enable manual gain");
        } else {
            __android_log_write(ANDROID_LOG_DEBUG, TAG, "Gain mode set to manual successfully");

            int trueGain = nearestGain(gain);
            res = rtlsdr_set_tuner_gain(device, trueGain);
            if (res < 0) {
                __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to set gain value");
            } else {
                __android_log_print(ANDROID_LOG_DEBUG, TAG, "Gain set successfully to %d",
                                    trueGain);
            }
        }
    } else {
        res = rtlsdr_set_tuner_gain_mode(device, 0);

        if (res < 0) {
            __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to set gain mode to automatic");
        } else {
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "Gain mode successfully set to automatic");
        }
    }

    res = rtlsdr_reset_buffer(device);
    if (res < 0) {
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to reset the device buffer");
    }

    fftTrd = std::make_unique<FftThread>(env, _this, fftSamples);
    fftTrd->start();

    recorderThread = std::make_unique<RecorderThread>(env);

    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_setSampleRate(JNIEnv *env, jobject _this, jint sampleRate) {
    if (rtlsdr_set_sample_rate(device, sampleRate) < 0) {
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to set sample rate");
        return false;
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "Sample rate set successfully to %d Hz",
                            sampleRate);
        return true;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_getSampleRate(JNIEnv *env, jobject _this) {
    return (jint) rtlsdr_get_sample_rate(device);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_setCenterFrequency(JNIEnv *env, jobject _this,
                                                       jint centerFrequency) {
    if (rtlsdr_set_center_freq(device, centerFrequency) < 0) {
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to set center frequency");
        return false;
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "Center frequency set successfully to %d Hz",
                            centerFrequency);
        return true;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_getCenterFrequency(JNIEnv *env, jobject _this) {
    return (jint) rtlsdr_get_center_freq(device);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_setPpmError(JNIEnv *env, jobject _this, jint ppmError) {
    if (rtlsdr_set_freq_correction(device, ppmError) < 0) {
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to set ppm error");
        return false;
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "Ppm error set successfully to %d ppm",
                            ppmError);
        return true;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_getPpmError(JNIEnv *env, jobject _this) {
    return (jint) rtlsdr_get_freq_correction(device);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_setGain(JNIEnv *env, jobject _this, jint gain) {
    int res;

    if (gain > 0) {
        res = rtlsdr_set_tuner_gain_mode(device, 1);
        if (res < 0) {
            __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to enable manual gain");
            return false;
        } else {
            __android_log_write(ANDROID_LOG_DEBUG, TAG, "Gain mode set to manual successfully");

            int trueGain = nearestGain(gain);
            res = rtlsdr_set_tuner_gain(device, trueGain);
            if (res < 0) {
                __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to set gain value");
                return false;
            } else {
                __android_log_print(ANDROID_LOG_DEBUG, TAG, "Gain set successfully to %d",
                                    trueGain);
                return true;
            }
        }
    } else {
        res = rtlsdr_set_tuner_gain_mode(device, 0);

        if (res < 0) {
            __android_log_write(ANDROID_LOG_ERROR, TAG, "Failed to set gain mode to automatic");
            return false;
        } else {
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "Gain mode successfully set to automatic");
            return true;
        }
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_getGain(JNIEnv *env, jobject _this) {
    return (jint) rtlsdr_get_tuner_gain(device);
}

extern "C" JNIEXPORT void JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_close(JNIEnv *env, jobject _this) {
    if (device != nullptr) {
        rtlsdr_close(device);
    }
    fftTrd.reset();
    recorderThread.reset();
}

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_tools_rftool_rtlsdr_RtlSdr_read(JNIEnv *env, jobject _this, jint size) {
    int goodSize = std::min((int) ceil(size / 512) * 512, 256 * (1 << 14));
    buffer.resize(goodSize);
    int bytesRead;
    int err;

    if ((err = rtlsdr_read_sync(device, buffer.data(), goodSize, &bytesRead)) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to read from device. %s",
                            libusbErrorCodes[(libusb_error) err].c_str());
        return env->NewDoubleArray(0);
    }

    outDataBuffer.resize(goodSize);

    jdoubleArray resultArray = env->NewDoubleArray(bytesRead);

    for (int i = 0; i < bytesRead; i++) {
        outDataBuffer[i] = (buffer[i] - adcHalf) / adcHalf;
    }

    fftTrd->push(outDataBuffer);

    env->SetDoubleArrayRegion(resultArray, 0, goodSize, outDataBuffer.data());
    return resultArray;
}

extern "C" JNIEXPORT void JNICALL
    Java_com_tools_rftool_rtlsdr_RtlSdr_setColorMap(JNIEnv* env, jobject _this, jstring colorMap) {
    const char* mapUtf = env->GetStringUTFChars(colorMap, nullptr);

    if(fftTrd != nullptr) {
        if (strcmp(mapUtf, "grayscale") == 0) {
            fftTrd->setColorMap(COLOR_MAP_GRAYSCALE);
        } else if (strcmp(mapUtf, "heat") == 0) {
            fftTrd->setColorMap(COLOR_MAP_HEAT);
        } else if (strcmp(mapUtf, "rainbow") == 0) {
            fftTrd->setColorMap(COLOR_MAP_RAINBOW);
        }
    }

    env->ReleaseStringUTFChars(colorMap, mapUtf);
}

extern "C" JNIEXPORT void JNICALL
    Java_com_tools_rftool_rtlsdr_Recorder_startRecording(JNIEnv* env, jobject _this, jstring filePath) {
    const char* pathUtf = env->GetStringUTFChars(filePath, nullptr);
    std::string path(pathUtf);
    env->ReleaseStringUTFChars(filePath, pathUtf);
    recorderThread->startRecording(path);
}

extern "C" JNIEXPORT void JNICALL
Java_com_tools_rftool_rtlsdr_Recorder_stopRecording(JNIEnv* env, jobject _this) {
    recorderThread->stopRecording();
}

int nearestGain(int targetGain) {
    int i, r, err1, err2, count, nearest;
    count = rtlsdr_get_tuner_gains(device, nullptr);
    if (count <= 0) {
        return 0;
    }
    gains.resize(count);
    count = rtlsdr_get_tuner_gains(device, gains.data());
    nearest = gains[0];
    for (i = 0; i < count; i++) {
        err1 = abs(targetGain - nearest);
        err2 = abs(targetGain - gains[i]);
        if (err2 < err1) {
            nearest = gains[i];
        }
    }

    return nearest;
}