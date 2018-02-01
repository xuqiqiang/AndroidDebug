/*
 *
 *  *    Copyright (C) 2016 Snailstudio
 *  *    Copyright (C) 2011 Android Open Source Project
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */
package com.snailstudio.xsdk.debug.serv;

import android.content.Context;

import com.snailstudio.xsdk.debug.Constants.Config;
import com.snailstudio.xsdk.debug.serv.req.HttpAddTableDataHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpDelHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpDeleteTableDataHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpDownHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpDownloadDbHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpEditHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpFBHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpGetDbDataHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpGetDbListHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpGetFileListHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpGetTableListHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpOpenTerminalHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpProgressHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpQueryDataHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpTerminalHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpUpHandler;
import com.snailstudio.xsdk.debug.serv.req.HttpUpdateTableDataHandler;
import com.snailstudio.xsdk.debug.utils.CommonUtil;
import com.snailstudio.xsdk.debug.utils.LogUtils;

import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Web服务类
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class WebServer extends Thread {

    public static final int ERR_UNEXPECT = 0x0101;
    public static final int ERR_PORT_IN_USE = 0x0102;
    public static final int ERR_TEMP_NOT_FOUND = 0x0103;
    static final String TAG = "WebServer";
    static final boolean DEBUG = false || Config.DEV_MODE;
    /* package */static boolean isLoop;
    private int port;
    private String webRoot;
    private ServerSocket serverSocket;
    private OnWebServListener mListener;

    private ExecutorService pool; // 线程池

    private Context mContext;

    public WebServer(Context context, int port, final String webRoot) {
        super();
        this.mContext = context;
        this.port = port;
        this.webRoot = webRoot;
        isLoop = false;

        pool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try {
            // Decide if port is in use.
            if (CommonUtil.getSingleton().isLocalPortInUse(port)) {
                if (mListener != null) {
                    mListener.onError(ERR_PORT_IN_USE);
                }
                return;
            }

            LogUtils.d("run");
            // 创建服务器套接字
            serverSocket = new ServerSocket(port);
            // 设置端口重用
            serverSocket.setReuseAddress(true);
            // 创建HTTP协议处理器
            BasicHttpProcessor httpproc = new BasicHttpProcessor();
            // 增加HTTP协议拦截器
            httpproc.addInterceptor(new ResponseDate());
            httpproc.addInterceptor(new ResponseServer());
            httpproc.addInterceptor(new ResponseContent());
            httpproc.addInterceptor(new ResponseConnControl());
            // 创建HTTP服务
            HttpService httpService = new HttpService(httpproc,
                    new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());
            // 创建HTTP参数
            HttpParams params = new BasicHttpParams();
            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "WebServer/1.1");
            // 设置HTTP参数
            httpService.setParams(params);
            // 创建HTTP请求执行器注册表
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            // 增加HTTP请求执行器
            reqistry.register(UrlPattern.DOWNLOAD, new HttpDownHandler(webRoot));
            reqistry.register(UrlPattern.DELETE, new HttpDelHandler(webRoot));
            reqistry.register(UrlPattern.UPLOAD, new HttpUpHandler(webRoot));
            reqistry.register(UrlPattern.PROGRESS, new HttpProgressHandler());

            reqistry.register(UrlPattern.EDIT, new HttpEditHandler(mContext));

            reqistry.register(UrlPattern.GET_DB_LIST, new HttpGetDbListHandler(mContext));
            reqistry.register(UrlPattern.GET_DB_DATA, new HttpGetDbDataHandler(mContext));
            reqistry.register(UrlPattern.GET_TABLE_LIST, new HttpGetTableListHandler(mContext));
            reqistry.register(UrlPattern.DOWNLOAD_DB, new HttpDownloadDbHandler(mContext, webRoot));

            reqistry.register(UrlPattern.ADD_TABLE_DATA, new HttpAddTableDataHandler(mContext));
            reqistry.register(UrlPattern.UPDATE_TABLE_DATA, new HttpUpdateTableDataHandler(mContext));
            reqistry.register(UrlPattern.DELETE_TABLE_LIST, new HttpDeleteTableDataHandler(mContext));
            reqistry.register(UrlPattern.QUERY, new HttpQueryDataHandler(mContext));

            reqistry.register(UrlPattern.GET_File_LIST, new HttpGetFileListHandler(mContext));

            reqistry.register(UrlPattern.OPEN_TERMINAL, new HttpOpenTerminalHandler(mContext));
            reqistry.register(UrlPattern.TERMINAL, new HttpTerminalHandler(webRoot));

            reqistry.register(UrlPattern.BROWSE, new HttpFBHandler(webRoot));
            // 设置HTTP请求执行器
            httpService.setHandlerResolver(reqistry);
            // 回调通知服务开始
            if (mListener != null) {
                mListener.onStarted();
            }
            /* 循环接收各客户端 */
            isLoop = true;
            while (isLoop && !Thread.interrupted()) {
                // 接收客户端套接字
                Socket socket = serverSocket.accept();
                // 绑定至服务器端HTTP连接
                DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                conn.bind(socket, params);
                // 派送至WorkerThread处理请求
                Thread t = new WorkerThread(httpService, conn, mListener);
                t.setDaemon(true); // 设为守护线程
                pool.execute(t); // 执行
            }
        } catch (IOException e) {
            if (isLoop) { // 以排除close造成的异常
                // 回调通知服务出错
                if (mListener != null) {
                    mListener.onError(ERR_UNEXPECT);
                }
                if (DEBUG)
                    e.printStackTrace();
                isLoop = false;
            }
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                // 回调通知服务结束
                if (mListener != null) {
                    mListener.onStopped();
                }
            } catch (Exception e) {
                LogUtils.e(e, "run");
            }
        }
    }

    public void close() {
        isLoop = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            LogUtils.e(e, "close");
        }
    }

    public void setOnWebServListener(OnWebServListener mListener) {
        this.mListener = mListener;
    }

    public interface OnWebServListener {
        void onStarted();

        void onStopped();

        void onError(int code);
    }

}
