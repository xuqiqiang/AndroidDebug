package com.snailstudio.xsdk.debug.serv;

import com.snailstudio.xsdk.debug.utils.CommonUtil;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Gzip压缩过滤器
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class GzipFilter {

    /**
     * Gzip压缩过滤集
     */
    private static final Set<String> gzipSet;

    static {
        gzipSet = new HashSet<String>();
        addGzipExtension("htm");
        addGzipExtension("html");
        addGzipExtension("js");
        addGzipExtension("css");
    }

    public static void addGzipExtension(String extension) {
        gzipSet.add(extension);
    }

    public static void addGzipExtensions(String... extensions) {
        gzipSet.addAll(Arrays.asList(extensions));
    }

    public static boolean isGzipExtension(String extension) {
        return gzipSet.contains(extension);
    }

    public static boolean isGzipFile(File file) {
        return gzipSet.contains(CommonUtil.getSingleton().getExtension(file));
    }

}
