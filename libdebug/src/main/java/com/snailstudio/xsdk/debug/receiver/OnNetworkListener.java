package com.snailstudio.xsdk.debug.receiver;

public interface OnNetworkListener {

    /**
     * @param isWifi 是否Wifi连接
     * @brief 网络连接了
     */
    void onConnected(boolean isWifi);

    /**
     * @brief 网络断开了
     */
    void onDisconnected();

}
