package com.snailstudio.xsdk.debug.serv.terminal;

import com.snailstudio.xsdk.debug.utils.LogUtils;

import java.io.OutputStream;

/**
 * WebSocket服务端
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class TermWebSocket {

    public static OutputStream mTermOut;

    static {
        System.loadLibrary("websocket");
    }

    public static String onReceiveData(byte[] data, int length) {
        LogUtils.d("onReceiveData：" + length);
        if (mTermOut != null) {
            try {
                mTermOut.write(data, 0, length);
                mTermOut.flush();
            } catch (Exception e) {
                // Ignore exception
                // We don't really care if the receiver isn't listening.
                // We just make a best effort to answer the query.
                e.printStackTrace();
            }
        }
        LogUtils.d("onReceiveData finish");
        return null;
    }

    public native int socket();

    public native int accept();

    public native void run();

    public native void send(byte[] data, int length);
}
