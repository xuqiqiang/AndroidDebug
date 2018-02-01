package com.snailstudio.xsdk.remoteproxy.client;

import com.snailstudio.xsdk.remoteproxy.client.handlers.ClientChannelHandler;
import com.snailstudio.xsdk.remoteproxy.client.handlers.RealServerChannelHandler;
import com.snailstudio.xsdk.remoteproxy.client.listener.ChannelStatusListener;
import com.snailstudio.xsdk.remoteproxy.common.LogUtils;
import com.snailstudio.xsdk.remoteproxy.common.container.Container;
import com.snailstudio.xsdk.remoteproxy.protocol.IdleCheckHandler;
import com.snailstudio.xsdk.remoteproxy.protocol.ProxyMessage;
import com.snailstudio.xsdk.remoteproxy.protocol.ProxyMessageDecoder;
import com.snailstudio.xsdk.remoteproxy.protocol.ProxyMessageEncoder;
import com.snailstudio.xsdk.remoteproxy.protocol.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;

/**
 * Created by xuqiqiang on 2017/05/14.
 */
public class ProxyClientContainer implements Container, ChannelStatusListener {

    private static final int MAX_FRAME_LENGTH = 1024 * 1024;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int INITIAL_BYTES_TO_STRIP = 0;
    private static final int LENGTH_ADJUSTMENT = 0;
    private static final int MAX_RETRY_TIMES = 5;
    private static Logger logger = LoggerFactory.getLogger(ProxyClientContainer.class);
    private NioEventLoopGroup workerGroup;
    private Bootstrap bootstrap;

    private Bootstrap realServerBootstrap;
    private SSLContext sslContext;
    private long sleepTimeMill = 1000;
    private int retryTimes;

    public ProxyClientContainer() {
        init();
    }

    private void init() {
        workerGroup = new NioEventLoopGroup();
        realServerBootstrap = new Bootstrap();
        realServerBootstrap.group(workerGroup);
        realServerBootstrap.channel(NioSocketChannel.class);
        realServerBootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new RealServerChannelHandler());
            }
        });

        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
//                if (Config.getInstance().getBooleanValue("ssl.enable", false)) {
//                    if (sslContext == null) {
//                        sslContext = SslContextCreator.createSSLContext();
//                    }
//
//                    ch.pipeline().addLast(createSslHandler(sslContext));
//                }
                ch.pipeline().addLast(new ProxyMessageDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP));
                ch.pipeline().addLast(new ProxyMessageEncoder());
                ch.pipeline().addLast(new IdleCheckHandler(IdleCheckHandler.READ_IDLE_TIME, IdleCheckHandler.WRITE_IDLE_TIME - 10, 0));
                ch.pipeline().addLast(new ClientChannelHandler(realServerBootstrap, bootstrap, ProxyClientContainer.this));
            }
        });
    }

    @Override
    public void start() {
        if (Utils.checkKey()) {
            connectProxyServer();
        }
    }

    private ChannelHandler createSslHandler(SSLContext sslContext) {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(true);
        return new SslHandler(sslEngine);
    }

    private void connectProxyServer() {

        bootstrap.connect(Utils.INET_HOST, Utils.INET_PORT).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {

                    // 连接成功，向服务器发送客户端认证信息（clientKey）
                    ClientChannelMannager.setCmdChannel(future.channel());
                    ProxyMessage proxyMessage = new ProxyMessage();
                    proxyMessage.setType(ProxyMessage.C_TYPE_AUTH);
                    proxyMessage.setUri(Utils.CLIENT_KEY);
                    future.channel().writeAndFlush(proxyMessage);
                    sleepTimeMill = 1000;
                    LogUtils.d("connect proxy server success, %s", future.channel());
                    logger.info("connect proxy server success, {}", future.channel());
                } else {
                    LogUtils.d("connect proxy server error" + future.cause());
                    logger.warn("connect proxy server error", future.cause());

                    // 连接断开，发起重连
                    reconnectWait();
                    if (Utils.checkKey()) {
                        connectProxyServer();
                    }
                }
            }
        });
    }

    @Override
    public void stop() {
        if (workerGroup != null)
            workerGroup.shutdownGracefully();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LogUtils.d("channelInactive");
        reconnectWait();
        if (Utils.checkKey()) {
            stop();
            init();
            connectProxyServer();
        }
    }

    private void reconnectWait() {
        try {
            if (sleepTimeMill > 60000) {
                sleepTimeMill = 1000;
            }

            synchronized (this) {
                sleepTimeMill = sleepTimeMill * 2;
                wait(sleepTimeMill);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
