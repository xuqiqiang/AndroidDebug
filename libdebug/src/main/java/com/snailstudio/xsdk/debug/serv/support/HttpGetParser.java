package com.snailstudio.xsdk.debug.serv.support;

import com.snailstudio.xsdk.debug.utils.LogUtils;

import org.apache.commons.fileupload.ParameterParser;
import org.apache.http.HttpRequest;

import java.io.IOException;
import java.util.Map;

/**
 * Get参数既简单解析
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpGetParser {

    private static final String GET_METHOD = "GET";

    public static boolean isGetMethod(HttpRequest request) {
        String method = request.getRequestLine().getMethod();
        return GET_METHOD.equalsIgnoreCase(method);
    }

    /**
     * @param request Http请求
     * @return 名称与值的Map集合
     * @throws IOException
     * @brief 解析请求的get信息
     * @warning 需保证是post请求且不是multipart的。
     */
    public Map<String, String> parse(HttpRequest request) {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        return parser.parse(getContent(request), '&');
    }

    public String getContent(HttpRequest request) {
        String uri = request.getRequestLine().getUri();
        LogUtils.d("uri:" + uri);
        int index = uri.indexOf('?');
        return index == -1 || index + 1 >= uri.length() ? null : uri.substring(index + 1);
    }

}
