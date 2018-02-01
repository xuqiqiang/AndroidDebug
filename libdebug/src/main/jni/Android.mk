LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := crypto
LOCAL_SRC_FILES := $(LOCAL_PATH)/websocket/libcrypto.a #libssl.a

LOCAL_C_INCLUDES += $(LOCAL_PATH)/websocket/include
include $(PREBUILT_STATIC_LIBRARY)


## websocket libs
include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog -ldl

LOCAL_MODULE:= websocket
LOCAL_SRC_FILES := $(LOCAL_PATH)/websocket/websocket.c $(LOCAL_PATH)/websocket/tcp_server.c

LOCAL_C_INCLUDES += $(LOCAL_PATH)/websocket/include

LOCAL_STATIC_LIBRARIES += crypto

include $(BUILD_SHARED_LIBRARY)

## terminal libs
include $(CLEAR_VARS)

LOCAL_MODULE := terminal
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS := \
	-llog \
	-lc \

LOCAL_SRC_FILES := \
	$(LOCAL_PATH)/terminal/process.cpp

include $(BUILD_SHARED_LIBRARY)

