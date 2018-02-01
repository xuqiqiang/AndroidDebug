package com.snailstudio.xsdk.debug.serv.req;

import android.content.Context;

import com.snailstudio.xsdk.debug.Constants.Config;
import com.snailstudio.xsdk.debug.serv.support.HttpPostParser;
import com.snailstudio.xsdk.debug.utils.LogUtils;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * 编辑数据库请求处理
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpEditHandler implements HttpRequestHandler {

    private Context mContext;

    public HttpEditHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        if (!Config.ALLOW_DELETE) {
            response.setStatusCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (!HttpPostParser.isPostMethod(request)) {
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            return;
        }

        HttpPostParser parser = new HttpPostParser();
        Map<String, String> params = parser.parse(request);
        String path = params.get("path");
        if (path == null) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        LogUtils.d("handle path:" + path);
        path = URLDecoder.decode(path, Config.ENCODING);
        LogUtils.d("handle path:" + path);
        path = URLDecoder.decode(path, Config.ENCODING);
        LogUtils.d("handle path:" + path);


        String result = DatabaseHandler.getInstance(mContext).addDBResponse(path);
        response.setStatusCode(HttpStatus.SC_OK);
        response.setEntity(new StringEntity(result, Config.ENCODING));

    }

}
