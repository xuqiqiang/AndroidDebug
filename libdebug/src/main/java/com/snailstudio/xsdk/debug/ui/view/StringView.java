package com.snailstudio.xsdk.debug.ui.view;

import com.snailstudio.xsdk.debug.Constants.Config;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.entity.StringEntity;

import java.io.IOException;

/**
 * 字符串视图渲染
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class StringView extends BaseView<String, Object[]> {

    /**
     * @param args 字符串格式化参数
     * @details 默认charset为{@link Config#ENCODING}
     * @see BaseView #render(Object, Object)
     */
    @Override
    public HttpEntity render(HttpRequest request, String content, Object[] args) throws IOException {
        if (args != null) {
            content = String.format(content, args);
        }
        return new StringEntity(content, Config.ENCODING);
    }

}
