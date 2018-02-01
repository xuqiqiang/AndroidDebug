package com.snailstudio.xsdk.debug.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.snailstudio.xsdk.debug.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 网络状态接收者
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class NetworkReceiver extends BroadcastReceiver {

    static final String TAG = "NetworkReceiver";

    private static Map<Context, NetworkReceiver> mReceiverMap = new HashMap<Context, NetworkReceiver>();

    private OnNetworkListener mListener;

    public NetworkReceiver(OnNetworkListener listener) {
        mListener = listener;
    }

    /**
     * 注册
     */
    public static void register(Context context, OnNetworkListener listener) {
        if (mReceiverMap.containsKey(context)) {
            LogUtils.d("This context already registered.");
            return;
        }

        NetworkReceiver receiver = new NetworkReceiver(listener);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);

        mReceiverMap.put(context, receiver);
        LogUtils.d("NetworkReceiver registered.");
    }

    /**
     * 注销
     */
    public static void unregister(Context context) {
        NetworkReceiver receiver = mReceiverMap.remove(context);
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            LogUtils.d("NetworkReceiver unregistered.");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = null;
        if (conn != null)
            info = conn.getActiveNetworkInfo();

        LogUtils.d(TAG, intent.getAction() + "\ngetActiveNetworkInfo: " + info);

        if (info != null) {
            boolean isWifi = info.getType() == ConnectivityManager.TYPE_WIFI;
            if (mListener != null) {
                mListener.onConnected(isWifi);
            }
        } else {
            if (mListener != null) {
                mListener.onDisconnected();
            }
        }

    }
}
