package com.snailstudio.xsdk.debug.ui.view;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;

import java.io.IOException;

/**
 * 视图渲染基类
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 *
 * @param <T> 内容类型
 * @param <A> 参数类型
 */
public abstract class BaseView<T, A> {

    /**
     * 渲染内容为Http实体
     *
     * @param request Http请求
     * @param content 输入内容
     * @return Http实体
     * @throws IOException
     */
    public HttpEntity render(HttpRequest request, T content) throws IOException {
        return this.render(request, content, null);
    }

    /**
     * 渲染内容为Http实体
     *
     * @param request Http请求
     * @param content 输入内容
     * @param args    输入参数
     * @return Http实体
     * @throws IOException
     */
    public abstract HttpEntity render(HttpRequest request, T content, A args) throws IOException;

}
