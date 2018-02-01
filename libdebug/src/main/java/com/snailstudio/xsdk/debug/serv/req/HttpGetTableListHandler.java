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
import java.util.Map;

/**
 * 获取表列表请求处理
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpGetTableListHandler implements HttpRequestHandler {

    static final String TAG = "HttpGetDbListHandler";
    private Context mContext;

    public HttpGetTableListHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        HttpGetParser parser = new HttpGetParser();
        Map<String, String> params = parser.parse(request);
        String database = params.get("database");
        if (database == null) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        LogUtils.d("handle database:" + database);
        String result = DatabaseHandler.getInstance(mContext).getTableListResponse(database);

        response.setEntity(new StringEntity(result, Config.ENCODING));
    }

}
