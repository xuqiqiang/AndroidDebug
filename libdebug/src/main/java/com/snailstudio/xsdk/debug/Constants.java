package com.snailstudio.xsdk.debug;

import android.os.Environment;

/**
 * 应用设置常量
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public final class Constants {

    public static final String ROOT_PATH = "/sdcard";
    public static final String ANR_PATH = "/data/anr";
    public static final String STORAGE_ANR = "ANR";
    public static final String STORAGE = "Storage";
    public static final String APP_SHARED_PREFERENCES = "APP_SHARED_PREFERENCES";
    public static final String PK = "pk";
    public static final String NAME = "name";
    public static final String NULL = "null";

    public static String APP_DIR_NAME = "/.ws/";
    public static String APP_DIR = Environment.getExternalStorageDirectory() + APP_DIR_NAME;

    public static String SHARED_PREF_NAME = "libdebug";
    public static String SHARED_PREF_KEY_MAC = "mac";
    public static String SHARED_PREF_KEY_PORT = "port";

    public static class Config {
        public static boolean DEV_MODE = false;
        /**
         * 服务资源文件
         */
        public static final String SERV_ROOT_DIR = APP_DIR + "root/";
        /**
         * 渲染模板目录
         */
        public static final String SERV_TEMP_DIR = SERV_ROOT_DIR + "temp/";
        /**
         * 统一编码
         */
        public static final String ENCODING = "UTF-8";
        /**
         * The threshold, in bytes, below which items will be retained in memory and above which they will be stored as a file.
         */
        public static final int THRESHOLD_UPLOAD = 1024 * 1024; // 1MB
        /**
         * GZip扩展名
         */
        public static final String EXT_GZIP = ".gz"; // used in cache
        /**
         * 文件缓存目录
         */
        public static final String FILE_CACHE_DIR = APP_DIR + "cache/";
        /**
         * 缓冲字节长度=1024*4B
         */
        public static final int BUFFER_LENGTH = 4096;
        public static int PORT = 8080;
        public static String WEBROOT = "/";
        /**
         * 是否允许下载
         */
        public static boolean ALLOW_DOWNLOAD = true;
        /**
         * 是否允许删除
         */
        public static boolean ALLOW_DELETE = true;
        /**
         * 是否允许上传
         */
        public static boolean ALLOW_UPLOAD = true;
        /**
         * 是否使用GZip
         */
        public static boolean USE_GZIP = true;
        /**
         * 是否使用文件缓存
         */
        public static boolean USE_FILE_CACHE = true;
    }

}
