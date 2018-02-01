package com.snailstudio.xsdk.debug.serv;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 模板缓存过滤器
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class TempCacheFilter {

    /**
     * 缓存过滤集
     */
    private static final Set<String> cacheSet;

    static {
        cacheSet = new HashSet<String>();
    }

    public static void addCacheTemp(String tempFile) {
        cacheSet.add(tempFile);
    }

    public static void addCacheTemps(String... tempFiles) {
        cacheSet.addAll(Arrays.asList(tempFiles));
    }

    public static boolean isCacheTemp(String tempFile) {
        return cacheSet.contains(tempFile);
    }
}
