package com.snailstudio.xsdk.remoteproxy.client.listener;

import io.netty.channel.Channel;

public interface ProxyChannelBorrowListener {

    void success(Channel channel);

    void error(Throwable cause);

}
