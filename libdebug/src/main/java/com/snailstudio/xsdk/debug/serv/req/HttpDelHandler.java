package com.snailstudio.xsdk.debug.serv.req;

import com.snailstudio.xsdk.debug.Constants;
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

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * 删除文件请求处理
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpDelHandler implements HttpRequestHandler {

    private String webRoot;

    public HttpDelHandler(final String webRoot) {
        this.webRoot = webRoot;
    }

    public static boolean hasWsDir(File f) {
        String path = f.isDirectory() ? f.getAbsolutePath() + "/" : f.getAbsolutePath();
        return path.indexOf(Constants.APP_DIR_NAME) != -1;
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
        final File file = new File(path);

        deleteFile(file);
        response.setStatusCode(HttpStatus.SC_OK);
        StringEntity entity = new StringEntity(file.exists() ? "1" : "0", Config.ENCODING); // 1: 错误；0：成功。
        response.setEntity(entity);
        LogUtils.d("file.exists():" + file.exists());
    }

    /**
     * 递归删除File
     */
    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File f : files) {
                    deleteFile(f);
                }
            }
            if (!hasWsDir(file)) {
                file.delete();
            }
        } else {
            if (!hasWsDir(file)) {
                file.delete();
            }
        }
    }

}
