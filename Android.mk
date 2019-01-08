LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := LineageSetupWizard
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_OVERRIDES_PACKAGES := Provision

LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_STATIC_ANDROID_LIBRARIES := \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-preference \
    android-support-v7-recyclerview \
    android-support-v13 \
    android-support-v14-preference

LOCAL_STATIC_JAVA_LIBRARIES := \
    libphonenumber \
    org.lineageos.platform.internal

LOCAL_STATIC_JAVA_LIBRARIES += \
    vendor.lineage.touch-V1.0-java

LOCAL_JAVA_LIBRARIES := \
    telephony-common

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_USE_AAPT2 := true

include frameworks/opt/setupwizard/navigationbar/common.mk
include frameworks/opt/setupwizard/library/common-gingerbread.mk

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
