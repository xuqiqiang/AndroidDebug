package com.snailstudio.xsdk.debug.serv;

import com.snailstudio.xsdk.debug.serv.WebServer.OnWebServListener;
import com.snailstudio.xsdk.debug.utils.LogUtils;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;

import java.io.IOException;

/**
 * Web服务工作线程
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class WorkerThread extends Thread {

    private final HttpService httpservice;
    private final HttpServerConnection conn;
    private final OnWebServListener listener;

    public WorkerThread(HttpService httpservice, HttpServerConnection conn,
                        OnWebServListener listener) {
        super();
        this.httpservice = httpservice;
        this.conn = conn;
        this.listener = listener;
    }

    @Override
    public void run() {
        HttpContext context = new BasicHttpContext();
        try {
            while (WebServer.isLoop && !Thread.interrupted() && this.conn.isOpen()) {
                this.httpservice.handleRequest(this.conn, context);
            }
        } catch (ConnectionClosedException e) {
            LogUtils.e("Client closed connection");
        } catch (IOException e) {
            LogUtils.w("I/O error: " + e.getMessage());
            if (listener != null && e.getMessage() != null
                    && e.getMessage().startsWith("File not found >>> '")) {
                listener.onError(WebServer.ERR_TEMP_NOT_FOUND);
            }
        } catch (HttpException e) {
            LogUtils.e("Unrecoverable HTTP protocol violation: " + e.getMessage());
        } finally {
            try {
                this.conn.shutdown();
            } catch (IOException ignore) {
            }
        }
    }
}
