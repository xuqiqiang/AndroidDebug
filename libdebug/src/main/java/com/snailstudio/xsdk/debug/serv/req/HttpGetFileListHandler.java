package com.snailstudio.xsdk.debug.serv.req;

import android.content.Context;

import com.snailstudio.xsdk.debug.Constants.Config;
import com.snailstudio.xsdk.debug.serv.support.HttpGetParser;
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
 * 获取文件列表请求处理
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpGetFileListHandler implements HttpRequestHandler {

    private Context mContext;

    public HttpGetFileListHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {

        LogUtils.d("handle");
        HttpGetParser parser = new HttpGetParser();
        Map<String, String> params = parser.parse(request);
        String path = params.get("path");
        if (path == null) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        path = URLDecoder.decode(path, Config.ENCODING);
        LogUtils.d("handle path:" + path);

        String result = DatabaseHandler.getInstance(mContext).getFileListResponse(path);

        response.setEntity(new StringEntity(result, Config.ENCODING));

    }

}
