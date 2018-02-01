package com.snailstudio.xsdk.debug.serv.support;

import org.apache.commons.fileupload.ParameterParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Post参数既简单解析
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class HttpPostParser {

    private static final String POST_METHOD = "POST";

    public static boolean isPostMethod(HttpRequest request) {
        String method = request.getRequestLine().getMethod();
        return POST_METHOD.equalsIgnoreCase(method);
    }

    /**
     * @param request Http请求
     * @return 名称与值的Map集合
     * @throws IOException
     * @brief 解析请求的post信息
     * @warning 需保证是post请求且不是multipart的。
     */
    public Map<String, String> parse(HttpRequest request) throws IOException {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        return parser.parse(getContent(request), '&');
    }

    public String getContent(HttpRequest request) throws IOException {
        HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
        return EntityUtils.toString(entity);
    }

}
