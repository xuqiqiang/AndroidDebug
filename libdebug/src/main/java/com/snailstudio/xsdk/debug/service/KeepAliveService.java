/*
 *
 *  *    Copyright (C) 2016 Snailstudio
 *  *    Copyright (C) 2011 Android Open Source Project
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */
package com.snailstudio.xsdk.debug.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import com.snailstudio.xsdk.debug.Constants;
import com.snailstudio.xsdk.debug.serv.WebServer;
import com.snailstudio.xsdk.debug.serv.req.DatabaseHandler;
import com.snailstudio.xsdk.debug.utils.CommonUtil;
import com.snailstudio.xsdk.debug.utils.LogUtils;
import com.snailstudio.xsdk.debug.utils.Utils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 后台保活Service
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class KeepAliveService extends Service implements WebServer.OnWebServListener {

    public static final String KEY_CUSTOM_DATABASE = "customDatabaseFiles";
    private static final String TAG = KeepAliveService.class.getSimpleName();
    /**
     * 错误时自动恢复的次数。如果仍旧异常，则继续传递。
     */
    private static final int RESUME_COUNT = 3;
    /**
     * 错误时重置次数的时间间隔。
     */
    private static final int RESET_INTERVAL = 3000;
    private static final Object mWebServerLock = new Object();
    private static final Object mRemoteProxyLock = new Object();

    private int errCount = 0;
    private Timer mTimer = new Timer(true);
    private TimerTask resetTask;
    private int mPort;
    private WebServer webServer;
    private boolean isRemoteProxyRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.initialize(this);
        LogUtils.dLog(TAG, "onCreate");
    }

    private void openWebServer() {
        new Thread() {
            public void run() {
                synchronized (mWebServerLock) {
                    if (webServer == null) {
                        mPort = CommonUtil.getSingleton().getEnablePort(Constants.Config.PORT);
                        SharedPreferences sharedPreferences = getSharedPreferences(
                                Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        sharedPreferences.edit().putInt(Constants.SHARED_PREF_KEY_PORT, mPort).apply();
                        LogUtils.dLog(TAG, "mPort:" + mPort);
                        webServer = new WebServer(KeepAliveService.this, mPort, Constants.Config.WEBROOT);
                        webServer.setOnWebServListener(KeepAliveService.this);
                        webServer.setDaemon(true);
                        webServer.start();
                    }
                }
            }
        }.start();
    }

    private void closeWebServer() {
        synchronized (mWebServerLock) {
            if (webServer != null) {
                webServer.close();
                webServer = null;
            }
        }
    }

    private void initRemoteProxy() {
        synchronized (mRemoteProxyLock) {
            if (isRemoteProxyRunning)
                return;
            try {
                Class<?> containerHelper = Class.forName("com.snailstudio.xsdk.remoteproxy.ContainerHelper");
                Class[] argTypes = new Class[]{Context.class, String.class};
                Method start = containerHelper.getMethod("start", argTypes);
                isRemoteProxyRunning = (boolean) start.invoke(null, this,
                        Utils.getAddressMAC(this).replace(":", ""));
            } catch (Exception e) {
                LogUtils.e(e, "initRemoteProxy");
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LogUtils.dLog(TAG, "onStartCommand");

        if (intent != null) {
            Serializable serializable = intent.getSerializableExtra(KEY_CUSTOM_DATABASE);
            if (serializable != null && serializable instanceof HashMap) {
                LogUtils.d("setCustomDatabaseFiles");
                DatabaseHandler.getInstance(this)
                        .setCustomDatabaseFiles((HashMap) serializable);
            }
        }

        if (webServer == null)
            openWebServer();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("ContainerHelper.start()");
                initRemoteProxy();
            }
        }, 3000);


        int tenMinutes = 10 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + tenMinutes;
        Intent i = new Intent(this, KeepAliveService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (manager != null)
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeWebServer();

        synchronized (mRemoteProxyLock) {
            try {
                Class<?> containerHelper = Class.forName("com.snailstudio.xsdk.remoteproxy.ContainerHelper");
                Method stop = containerHelper.getMethod("stop");
                stop.invoke(null);
                isRemoteProxyRunning = false;
            } catch (Exception e) {
                LogUtils.e(e, "initRemoteProxy");
            }
        }

        LogUtils.dLog(TAG, "onDestroy");
    }

    @Override
    public void onStarted() {
        LogUtils.d("onStarted");
    }

    @Override
    public void onStopped() {
        LogUtils.d("onStopped");
    }

    @Override
    public void onError(int code) {
        LogUtils.d("onError:" + code);
        closeWebServer();
        errCount++;
        restartResetTask(RESET_INTERVAL);
        if (errCount <= RESUME_COUNT) {
            LogUtils.d("Retry times: " + errCount);
            openWebServer();
        } else {
            errCount = 0;
            cancelResetTask();
        }
    }

    private void cancelResetTask() {
        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
        }
    }

    private void restartResetTask(long delay) {
        cancelResetTask();
        resetTask = new TimerTask() {
            @Override
            public void run() {
                errCount = 0;
                resetTask = null;
                LogUtils.d("ResetTask executed.");
            }
        };
        mTimer.schedule(resetTask, delay);
    }
}
