package com.snailstudio.xsdk.remoteproxy;

import android.content.Context;

import com.snailstudio.xsdk.remoteproxy.client.ProxyClientContainer;
import com.snailstudio.xsdk.remoteproxy.common.LogUtils;
import com.snailstudio.xsdk.remoteproxy.common.container.Container;
import com.snailstudio.xsdk.remoteproxy.protocol.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import io.netty.util.internal.SystemPropertyUtil;

/**
 * 容器启动工具类.
 * <p>
 * Created by xuqiqiang on 2017/12/5.
 */
public class ContainerHelper {

    private static final String TAG = ContainerHelper.class.getSimpleName();
    private static Logger logger = LoggerFactory.getLogger(ContainerHelper.class);
    private static List<Container> cachedContainers;

    public static void noUnsafe() {
        LogUtils.d("io.netty.noUnsafe:" + SystemPropertyUtil.getBoolean("io.netty.noUnsafe", false));
        System.setProperty("io.netty.noUnsafe", "true");
        LogUtils.d("io.netty.noUnsafe:" + SystemPropertyUtil.getBoolean("io.netty.noUnsafe", false));
    }

    private static void start(List<Container> containers) {

        cachedContainers = containers;
        noUnsafe();
        // 启动所有容器
        startContainers();
    }

    public static boolean start(Context context, final String clientKey) {
        if (!Utils.initialize(context, clientKey))
            return false;
        new Thread() {

            @Override
            public void run() {
                Utils.running = true;
                ContainerHelper.start(Arrays.asList(new Container[]{new ProxyClientContainer()}));
            }
        }.start();
        return true;
    }

    public static void stop() {
        new Thread() {

            @Override
            public void run() {

                stopContainers();
                Utils.running = false;
            }
        }.start();
    }

    private static void startContainers() {
        for (Container container : cachedContainers) {
            logger.info("starting container [{}]", container.getClass().getName());
            container.start();
            logger.info("container [{}] started", container.getClass().getName());
        }
    }

    private static void stopContainers() {
        for (Container container : cachedContainers) {
            logger.info("stopping container [{}]", container.getClass().getName());
            try {
                container.stop();
                logger.info("container [{}] stopped", container.getClass().getName());
            } catch (Exception ex) {
                logger.warn("container stopped with error", ex);
            }
        }
    }
}
