package com.snailstudio.xsdk.debug.serv.support;

import java.util.HashMap;
import java.util.Map;

/**
 * 进度缓存类
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class Progress {

    private static Map<String, Integer> progressMap = new HashMap<String, Integer>();

    public static void update(String id, int progress) {
        progressMap.put(id, progress);
        if (progress == 100) {
            progressMap.remove(id);
        }
    }

    public static int get(String id) {
        Integer p = progressMap.get(id);
        return p == null ? -1 : p;
    }

    public static void clear() {
        progressMap.clear();
    }

}
