package com.snailstudio.xsdk.debug.ui.view;

import com.snailstudio.xsdk.debug.Constants.Config;

import net.asfun.jangod.template.TemplateEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 模板处理器
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class TempHandler {

    /* package */ static final TemplateEngine engine;

    static {
        engine = new TemplateEngine();
        /* 设定模板目录 */
        engine.getConfiguration().setWorkspace(Config.SERV_TEMP_DIR);
        /* 设定全局变量 */
        Map<String, Object> globalBindings = new HashMap<String, Object>();
        globalBindings.put("SERV_ROOT_DIR", Config.SERV_ROOT_DIR);
        engine.setEngineBindings(globalBindings);
    }

    /**
     * 渲染模板，获得html
     */
    public static String render(String tempFile, Map<String, Object> data) throws IOException {
        return engine.process(tempFile, data);
    }

}
