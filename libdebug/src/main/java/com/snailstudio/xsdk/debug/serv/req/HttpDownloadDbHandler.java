package com.snailstudio.xsdk.debug.serv.req;

import android.content.Context;

import com.snailstudio.xsdk.debug.utils.LogUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 下载数据库请求处理
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpDownloadDbHandler extends HttpDownHandler {

    static final String TAG = "HttpDownloadDbHandler";
    private Context mContext;

    public HttpDownloadDbHandler(Context context, String webRoot) {
        super(webRoot);
        this.mContext = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        LogUtils.d("handle");

        final File file = DatabaseHandler.getInstance(mContext).getSelectedDBFile();

        if (file == null || !file.isFile()) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                write(file, outstream);
            }
        });
        response.setStatusCode(HttpStatus.SC_OK);
        response.addHeader("Content-Description", "File Transfer");
        response.setHeader("Content-Type", "application/octet-stream");
        response.addHeader("Content-Disposition", "attachment;filename=" + encodeFilename(file));
        response.setHeader("Content-Transfer-Encoding", "binary");
        // 在某平板自带浏览器上下载，比较下能成功下载的响应头，这里少了Content-Length。但设了，反而下不了了。
        response.setEntity(entity);

    }

}
