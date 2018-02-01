package com.snailstudio.xsdk.debug.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.snailstudio.xsdk.debug.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用广播接收者
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class WSReceiver extends BroadcastReceiver {

    public static final String ACTION_SERV_AVAILABLE = "com.snailstudio.xsdk.action.SERV_AVAILABLE";
    public static final String ACTION_SERV_UNAVAILABLE = "com.snailstudio.xsdk.action.SERV_UNAVAILABLE";
    public static final String PERMIT_WS_RECEIVER = "com.snailstudio.xsdk.ws.permission.WS_RECEIVER";
    static final String TAG = "WSReceiver";
    private static Map<Context, WSReceiver> mReceiverMap = new HashMap<Context, WSReceiver>();

    private OnWsListener mListener;

    public WSReceiver(OnWsListener listener) {
        mListener = listener;
    }

    /**
     * 注册
     */
    public static void register(Context context, OnWsListener listener) {
        if (mReceiverMap.containsKey(context)) {
            LogUtils.dLog(TAG, "This context already registered.");
            return;
        }

        WSReceiver receiver = new WSReceiver(listener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SERV_AVAILABLE);
        filter.addAction(ACTION_SERV_UNAVAILABLE);
        context.registerReceiver(receiver, filter);

        mReceiverMap.put(context, receiver);

        LogUtils.dLog(TAG, "WSReceiver registered.");
    }

    /**
     * 注销
     */
    public static void unregister(Context context) {
        WSReceiver receiver = mReceiverMap.remove(context);
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            LogUtils.dLog(TAG, "WSReceiver unregistered.");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.dLog(TAG, action);
        if (mListener == null) {
            return;
        }
        if (ACTION_SERV_AVAILABLE.equals(action)) {
            mListener.onServAvailable();
        } else { // ACTION_SERV_UNAVAILABLE
            mListener.onServUnavailable();
        }
    }

}
