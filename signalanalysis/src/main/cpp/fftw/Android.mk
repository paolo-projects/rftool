LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(LOCAL_PATH)/api/*.c $(LOCAL_PATH)/dft/*.c $(LOCAL_PATH)/dft/scalar/*.c $(LOCAL_PATH)/dft/scalar/codelets/*.c

LOCAL_C_INCLUDES :=

LOCAL_CFLAGS := 

LOCAL_LDLIBS :=

LOCAL_MODULE := fftw

include $(BUILD_SHARED_LIBRARY)
