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
 * 更新请求处理
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpUpdateTableDataHandler implements HttpRequestHandler {

    static final String TAG = "HttpUpdateTableDataHandler";
    private Context mContext;

    public HttpUpdateTableDataHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        HttpGetParser parser = new HttpGetParser();
        Map<String, String> params = parser.parse(request);
        String dbName = params.get("dbname");
        String tableName = params.get("tablename");
        String updatedData = params.get("updateddata");
        LogUtils.d("handle dbName:" + dbName + "tableName:" + tableName + ", updatedData:" + updatedData);
        if (dbName == null || tableName == null || updatedData == null) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        dbName = URLDecoder.decode(dbName, Config.ENCODING);
        tableName = URLDecoder.decode(tableName, Config.ENCODING);
        updatedData = URLDecoder.decode(updatedData, Config.ENCODING);
        LogUtils.d("handle dbName:" + dbName + ", tableName:" + tableName + ", updatedData:" + updatedData);

        String result = DatabaseHandler.getInstance(mContext).updateTableDataAndGetResponse(dbName, tableName, updatedData);

        response.setEntity(new StringEntity(result, Config.ENCODING));
    }

}
