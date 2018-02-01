package com.snailstudio.xsdk.debug.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.snailstudio.xsdk.debug.receiver.NetworkReceiver;
import com.snailstudio.xsdk.debug.receiver.OnNetworkListener;
import com.snailstudio.xsdk.debug.receiver.OnStorageListener;
import com.snailstudio.xsdk.debug.receiver.StorageReceiver;
import com.snailstudio.xsdk.debug.receiver.WSReceiver;
import com.snailstudio.xsdk.debug.utils.CommonUtil;
import com.snailstudio.xsdk.debug.utils.LogUtils;

/**
 * 应用后台服务
 *
 * @deprecated see {@link KeepAliveService}
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class WSService extends Service implements OnNetworkListener, OnStorageListener {

    public static final String ACTION = "com.snailstudio.xsdk.service.WS";
    static final String TAG = "WSService";
    public boolean isWebServAvailable = false;

    private boolean isNetworkAvailable;
    private boolean isStorageMounted;

    @Override
    public void onCreate() {
        super.onCreate();
        NetworkReceiver.register(this, this);
        StorageReceiver.register(this, this);

        CommonUtil mCommonUtil = CommonUtil.getSingleton();
        isNetworkAvailable = mCommonUtil.isNetworkAvailable();
        isStorageMounted = mCommonUtil.isExternalStorageMounted();

        isWebServAvailable = isNetworkAvailable && isStorageMounted;
        notifyWebServAvailable(isWebServAvailable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NetworkReceiver.unregister(this);
        StorageReceiver.unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(boolean isWifi) {
        isNetworkAvailable = true;
        notifyWebServAvailableChanged();
    }

    @Override
    public void onDisconnected() {
        isNetworkAvailable = false;
        notifyWebServAvailableChanged();
    }

    @Override
    public void onMounted() {
        isStorageMounted = true;
        notifyWebServAvailableChanged();
    }

    @Override
    public void onUnmounted() {
        isStorageMounted = false;
        notifyWebServAvailableChanged();
    }

    private void notifyWebServAvailable(boolean isAvailable) {
        LogUtils.dLog(TAG, "isAvailable:" + isAvailable);
        // Notify if web service is available.
        String action = isAvailable ? WSReceiver.ACTION_SERV_AVAILABLE
                : WSReceiver.ACTION_SERV_UNAVAILABLE;
        Intent intent = new Intent(action);
        sendBroadcast(intent, WSReceiver.PERMIT_WS_RECEIVER);
    }

    private void notifyWebServAvailableChanged() {
        boolean isAvailable = isNetworkAvailable && isStorageMounted;
        if (isAvailable != isWebServAvailable) {
            notifyWebServAvailable(isAvailable);
            isWebServAvailable = isAvailable;
        }
    }

}
