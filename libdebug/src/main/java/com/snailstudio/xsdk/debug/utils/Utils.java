package com.snailstudio.xsdk.debug.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.snailstudio.xsdk.debug.Constants;
import com.snailstudio.xsdk.debug.CrashHandler;
import com.snailstudio.xsdk.debug.serv.TempCacheFilter;

import net.asfun.jangod.lib.TagLibrary;
import net.asfun.jangod.lib.tag.AbsResTag;
import net.asfun.jangod.lib.tag.ResColorTag;
import net.asfun.jangod.lib.tag.ResStrTag;
import net.asfun.jangod.lib.tag.UUIDTag;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.NetworkInterface;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class Utils {

    private static final String TAG = "Utils";
    private static final String KEY_DEBUG_DEV_MODE = "DEBUG_DEV_MODE";

    private static final String marshmallowMacAddress = "02:00:00:00:00:00";
    private static final String fileAddressMac = "/sys/class/net/wlan0/address";

    private Utils() {
        // This class in not publicly instantiable
    }

    public static void initialize(Context context) {
        initAppDir(context);
        initJangod(context);
        initAppFilter();
        CommonUtil.createSingleton(context.getApplicationContext());

        if (!Constants.Config.DEV_MODE) {
            /* 全局异常崩溃处理 */
            new CrashHandler(context);
        }

        try {
            Constants.Config.DEV_MODE = Boolean.valueOf(context.getString(
                    context.getResources()
                            .getIdentifier(KEY_DEBUG_DEV_MODE,
                                    "string", context.getPackageName())));
        } catch (Exception ignored) {
        }
        LogUtils.initialize("libdebug", Constants.Config.DEV_MODE);
    }

    /**
     * @brief 初始化应用目录
     */
    private static void initAppDir(Context context) {
        CopyUtil mCopyUtil = new CopyUtil(context.getApplicationContext());
        // mCopyUtil.deleteFile(new File(Config.SERV_ROOT_DIR)); // 清理服务文件目录
        try {
            // 重新复制到SDCard，仅当文件不存在时
            mCopyUtil.assetsCopy("ws", Constants.Config.SERV_ROOT_DIR, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief 初始化Jangod，添加自定义内容
     */
    private static void initJangod(Context context) {
        AbsResTag.init(context.getApplicationContext());
        /* custom tags */
        TagLibrary.addTag(new ResStrTag());
        TagLibrary.addTag(new ResColorTag());
        TagLibrary.addTag(new UUIDTag());
        /* custom filters */
    }

    /**
     * @brief 初始化应用过滤器
     */
    private static void initAppFilter() {
        /* TempCacheFilter */
        TempCacheFilter.addCacheTemps("403.html", "404.html", "503.html");
        /* GzipFilter */
    }

    public static String detectMimeType(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        } else if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else {
            return "application/octet-stream";
        }
    }

    public static byte[] loadContent(String fileName, AssetManager assetManager) throws IOException {
        InputStream input = null;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            input = assetManager.open(fileName);
            byte[] buffer = new byte[1024];
            int size;
            while (-1 != (size = input.read(buffer))) {
                output.write(buffer, 0, size);
            }
            output.flush();
            return output.toByteArray();
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            try {
                if (null != input) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] getDatabase(String selectedDatabase, HashMap<String, File> databaseFiles) {
        if (TextUtils.isEmpty(selectedDatabase)) {
            return null;
        }

        byte[] byteArray = new byte[0];
        try {
            File file = databaseFiles.get(selectedDatabase);
            if (file == null)
                return null;
            if (file.length() <= 0)
                return byteArray;

//            if (file.length() < 256 * 1024) {
            byteArray = null;
            try {
                InputStream inputStream = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] b = new byte[(int) file.length()];
                int bytesRead;

                while ((bytesRead = inputStream.read(b)) != -1) {
                    bos.write(b, 0, bytesRead);
                }

                byteArray = bos.toByteArray();
            } catch (IOException e) {
                LogUtils.eLog(TAG, "getDatabase: ", e);
            }
//            }
//            else{
//                new Thread(){
//                    public void run(){
//
//                    }
//                }.start();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return byteArray;
    }

    public static String encodeUrl(String url) {
        String encodered_url = url;
        try {
            encodered_url = URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodered_url;
    }

    public static String decodeUrl(String url) {
        String encodered_url = url;
        try {
            encodered_url = URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodered_url;
    }

    public static String getAddressMAC(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String result = sharedPreferences.getString(Constants.SHARED_PREF_KEY_MAC, marshmallowMacAddress);
        if (!marshmallowMacAddress.equals(result))
            return result;

        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();

        result = wifiInf.getMacAddress();

        if (result == null || marshmallowMacAddress.equals(result)) {
            result = getAddressMacByInterface();
            LogUtils.i("result : " + result);
            try {
                if (marshmallowMacAddress.equals(result)) {
                    result = getAddressMacByFile(wifiMan);
                    LogUtils.i("result : " + result);
                }
            } catch (IOException e) {
                LogUtils.eLog("MobileAccess", "Erreur lecture propriete Adresse MAC");
            } catch (Exception e) {
                LogUtils.eLog("MobileAcces", "Erreur lecture propriete Adresse MAC ");
            }
        }
        if (result == null)
            result = marshmallowMacAddress;
        sharedPreferences.edit().putString(Constants.SHARED_PREF_KEY_MAC, result).apply();
        return result;
    }

    private static String getAddressMacByInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return marshmallowMacAddress;
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch (Exception e) {
            LogUtils.eLog("MobileAcces", "Erreur lecture propriete Adresse MAC ");
        }
        return marshmallowMacAddress;
    }

    private static String getAddressMacByFile(WifiManager wifiMan) throws Exception {
        String ret;
        int wifiState = wifiMan.getWifiState();

        wifiMan.setWifiEnabled(true);
        File fl = new File(fileAddressMac);
        FileInputStream fin = new FileInputStream(fl);
        ret = crunchifyGetStringFromStream(fin);
        fin.close();

        boolean enabled = WifiManager.WIFI_STATE_ENABLED == wifiState;
        wifiMan.setWifiEnabled(enabled);
        return ret;
    }

    private static String crunchifyGetStringFromStream(InputStream crunchifyStream) throws IOException {
        if (crunchifyStream != null) {
            Writer crunchifyWriter = new StringWriter();

            char[] crunchifyBuffer = new char[2048];
            try {
                Reader crunchifyReader = new BufferedReader(new InputStreamReader(crunchifyStream, "UTF-8"));
                int counter;
                while ((counter = crunchifyReader.read(crunchifyBuffer)) != -1) {
                    crunchifyWriter.write(crunchifyBuffer, 0, counter);
                }
            } finally {
                crunchifyStream.close();
            }
            return crunchifyWriter.toString();
        } else {
            return "No Contents";
        }
    }
}
