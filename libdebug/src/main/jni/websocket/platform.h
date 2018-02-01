#ifndef __WEBSOCKET_PLATFORM_H__
#define __WEBSOCKET_PLATFORM_H__

//#define _DEBUG_

#ifdef __ANDROID__

#include <jni.h>
#include <android/log.h>
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "ProjectName", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "ProjectName", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "ProjectName", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "ProjectName", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "ProjectName", __VA_ARGS__)
#if defined(_DEBUG_)
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG, "ProjectName", __VA_ARGS__)
#endif
#endif

#endif

