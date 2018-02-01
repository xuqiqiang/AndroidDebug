package com.snailstudio.xsdk.debug.serv.req;

import android.content.Context;

import com.snailstudio.xsdk.debug.Constants.Config;
import com.snailstudio.xsdk.debug.utils.LogUtils;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;

/**
 * 获取数据库列表请求处理
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpGetDbListHandler implements HttpRequestHandler {

    static final String TAG = "HttpGetDbListHandler";
    private Context mContext;

    public HttpGetDbListHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        LogUtils.d("handle");
        String result = DatabaseHandler.getInstance(mContext).getDBListResponse();
        response.setEntity(new StringEntity(result, Config.ENCODING));
    }

}
