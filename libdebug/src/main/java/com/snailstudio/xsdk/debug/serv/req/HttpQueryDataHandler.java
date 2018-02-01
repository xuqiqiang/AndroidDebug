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
 * 查询请求处理
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpQueryDataHandler implements HttpRequestHandler {

    static final String TAG = "HttpQueryDataHandler";
    private Context mContext;

    public HttpQueryDataHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        HttpGetParser parser = new HttpGetParser();
        Map<String, String> params = parser.parse(request);
        String dbName = params.get("dbname");
        String query = params.get("query");
        LogUtils.d("handle dbName:" + dbName + ", query:" + query);
        if (dbName == null || query == null) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        dbName = URLDecoder.decode(dbName, Config.ENCODING);
        try {
            query = URLDecoder.decode(query, Config.ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogUtils.d("handle dbName:" + dbName + ", query:" + query);

        String result = DatabaseHandler.getInstance(mContext).executeQueryAndGetResponse(dbName, query);

        response.setEntity(new StringEntity(result, Config.ENCODING));
    }

}
