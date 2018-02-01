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
 * 获取数据库数据请求处理
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpGetDbDataHandler implements HttpRequestHandler {

    static final String TAG = "HttpGetDbDataHandler";
    private Context mContext;

    public HttpGetDbDataHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        LogUtils.d("handle");
        HttpGetParser parser = new HttpGetParser();
        Map<String, String> params = parser.parse(request);
        String tableName = params.get("tablename");
        LogUtils.d("handle tableName:" + tableName);
        if (tableName == null) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        tableName = URLDecoder.decode(tableName, Config.ENCODING);
        LogUtils.d("handle tableName:" + tableName);
        String result = DatabaseHandler.getInstance(mContext).getAllDataFromTheTableResponse(tableName);

        response.setEntity(new StringEntity(result, Config.ENCODING));
    }

}
