package com.snailstudio.xsdk.debug.serv.req.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public final class NetworkUtils {

    private NetworkUtils() {
        // This class in not publicly instantiable
    }

    public static String getAddressLog(Context context, int port) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return "not available";
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        @SuppressLint("DefaultLocale") final String formattedIpAddress = String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        return "Open http://" + formattedIpAddress + ":" + port + " in your browser";
    }

}
