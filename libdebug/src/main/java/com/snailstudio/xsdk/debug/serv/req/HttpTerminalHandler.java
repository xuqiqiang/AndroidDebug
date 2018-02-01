package com.snailstudio.xsdk.debug.serv.req;

import com.snailstudio.xsdk.debug.Constants.Config;
import com.snailstudio.xsdk.debug.serv.support.Progress;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * 访问终端请求处理
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpTerminalHandler extends HttpFBHandler {

    static final String TAG = "HttpOpenTerminalHandler";

    public HttpTerminalHandler(String webRoot) {
        super(webRoot);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {

        HttpEntity entity = mViewFactory.renderTemp(request, "terminal.html", null);
        String contentType = "text/html;charset=" + Config.ENCODING;
        response.setHeader("Content-Type", contentType);
        response.setEntity(entity);

        Progress.clear();
    }

}
