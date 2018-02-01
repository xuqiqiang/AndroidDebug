package com.snailstudio.xsdk.remoteproxy.protocol;

import android.content.Context;
import android.text.TextUtils;

import com.snailstudio.xsdk.remoteproxy.common.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by xuqiqiang on 2017/05/14.
 */
public class Utils {

    private static final String KEY_REMOTE_PROXY_INET_HOST = "REMOTE_PROXY_INET_HOST";
    private static final String KEY_REMOTE_PROXY_INET_PORT = "REMOTE_PROXY_INET_PORT";
    private static final String KEY_REMOTE_PROXY_WEB_PORT = "REMOTE_PROXY_WEB_PORT";
    private static final String KEY_REMOTE_PROXY_CLIENT_KEY = "REMOTE_PROXY_CLIENT_KEY";
    private static final String KEY_REMOTE_PROXY_DEV_MODE = "REMOTE_PROXY_DEV_MODE";
    private static final String KEY_EXIST = "1";
    public static String INET_HOST;
    public static int INET_PORT = -1;
    public static int WEB_PORT = -1;
    public static String CLIENT_KEY;
    public static volatile boolean running;

    public static boolean initialize(Context context, String clientKey) {

        try {
            INET_HOST = context.getString(
                    context.getResources()
                            .getIdentifier(KEY_REMOTE_PROXY_INET_HOST,
                                    "string", context.getPackageName()));

            INET_PORT = Integer.valueOf(context.getString(
                    context.getResources()
                            .getIdentifier(KEY_REMOTE_PROXY_INET_PORT,
                                    "string", context.getPackageName())));

            WEB_PORT = Integer.valueOf(context.getString(
                    context.getResources()
                            .getIdentifier(KEY_REMOTE_PROXY_WEB_PORT,
                                    "string", context.getPackageName())));

            CLIENT_KEY = context.getString(
                    context.getResources()
                            .getIdentifier(KEY_REMOTE_PROXY_CLIENT_KEY,
                                    "string", context.getPackageName()));
        } catch (Exception ignored) {
        }

        try {
            Constants.Config.DEV_MODE = Boolean.valueOf(context.getString(
                    context.getResources()
                            .getIdentifier(KEY_REMOTE_PROXY_DEV_MODE,
                                    "string", context.getPackageName())));
        } catch (Exception ignored) {
        }
        LogUtils.initialize("libremoteproxy", Constants.Config.DEV_MODE);

        if (TextUtils.isEmpty(INET_HOST)
                || INET_PORT < 0
                || WEB_PORT < 0) {
            LogUtils.e("no config!");
            return false;
        }

        if (TextUtils.isEmpty(CLIENT_KEY))
            CLIENT_KEY = clientKey;
        LogUtils.i("CLIENT_KEY : " + CLIENT_KEY);

        if (TextUtils.isEmpty(CLIENT_KEY)) {
            return false;
        }

        return true;
    }

    public static boolean checkKey() {
        String res = queryKey();
        LogUtils.d("res:" + res);
        while (!KEY_EXIST.equals(res)) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!running)
                return false;
            res = queryKey();
            LogUtils.d("res:" + res);
        }
        return true;
    }

    private static String queryKey() {
        return queryKey("http://" + INET_HOST + ":" + WEB_PORT, CLIENT_KEY);
    }

    private static String queryKey(String path, String key) {
        path += "/querykey?key=" + key;
        LogUtils.d("path:" + path);
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                return dealResponseResult(is);
            } else {
                LogUtils.i(code + "");
                return null;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private static String dealResponseResult(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(byteArrayOutputStream.toByteArray());
    }

}
