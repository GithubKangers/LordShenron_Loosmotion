LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_ANDROID_LIBRARIES := \
    android-support-v14-preference \
    android-support-v13 \
    android-support-v7-appcompat \
    android-support-v7-preference \
    android-support-v7-recyclerview \
    android-support-v4

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := zenmotions
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_SDK_VERSION := current
LOCAL_USE_AAPT2 := true

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_DEX_PREOPT := false


include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))﻿
