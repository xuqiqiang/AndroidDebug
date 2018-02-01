package com.snailstudio.xsdk.debug.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;

/**
 * 通用工具
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class CommonUtil {

    private static Context mContext;
    /**
     * 年月日时分秒
     */
    public final String FORMAT_YMDHMS = "yyyy-MM-dd kk:mm:ss";
    /**
     * 年月日
     */
    public final String FORMAT_YMD = "yyyy-MM-dd";
    /**
     * 时分秒
     */
    public final String FORMAT_HMS = "kk:mm:ss";

    public static void createSingleton(Context context) {
        mContext = context;
    }

    public static CommonUtil getSingleton() {
        return Holder.instance;
    }

    /**
     * @param dirPath 目录路径
     * @return true: success; false: failure or already existed and not a
     * directory.
     * @brief 创建目录
     */
    public boolean makeDirs(String dirPath) {
        File file = new File(dirPath);
        if (file.exists()) {
            if (file.isDirectory()) {
                return true;
            }
            return false;
        }
        return file.mkdirs();
    }

    /**
     * 获取文件后缀名，不带`.`
     *
     * @param file 文件
     * @return 文件后缀
     */
    public String getExtension(File file) {
        String name = file.getName();
        int i = name.lastIndexOf('.');
        int p = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        return i > p ? name.substring(i + 1) : "";
    }

    /**
     * @brief 判断网络是否可用
     * @warning need ACCESS_NETWORK_STATE permission
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (null == info) {
            return false;
        }
        return info.isAvailable();
    }

    /**
     * @return null if network off
     * @brief 获取当前IP地址
     */
    public String getLocalIpAddress() {
        try {
            // 遍历网络接口
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                // 遍历IP地址
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                        .hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    // 非回传地址时返回
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @brief 判断外部存储是否挂载
     */
    public boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * @param path 文件系统路径
     * @brief 获取文件系统路径内的可用空间，单位bytes
     */
    public int getAvailableBytes(String path) {
        StatFs sf = new StatFs(path);
        int blockSize = sf.getBlockSize();
        int availCount = sf.getAvailableBlocks();
        return blockSize * availCount;
    }

    /**
     * @brief 存储大小格式化为可阅读的字串
     */
    public String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " "
                + units[digitGroups];
    }

    /**
     * @param servClsName 服务类名
     * @return 是否运行中
     * @brief 判断服务是否运行中
     */
    public boolean isServiceRunning(String servClsName) {
        ActivityManager mActivityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> mServiceList = mActivityManager
                .getRunningServices(Integer.MAX_VALUE);

        for (RunningServiceInfo servInfo : mServiceList) {
            if (servClsName.equals(servInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @brief 获取窗口默认显示信息
     */
    public Display getDisplay() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay();
    }

    /**
     * dp -> px
     *
     * @param dipValue dp单位的值
     * @return px单位的值
     */
    public int dp2px(float dipValue) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px - > dp
     *
     * @param pxValue px单位的值
     * @return dp单位的值
     */
    public int px2dp(float pxValue) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp -> px
     *
     * @param spValue sp单位的值
     * @return px单位的值
     */
    public int sp2px(float spValue) {
        float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * px -> sp
     *
     * @param pxValue px单位的值
     * @return sp单位的值
     */
    public int px2sp(float pxValue) {
        float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 获得当前时间
     */
    public CharSequence currentTime(CharSequence inFormat) {
        return DateFormat.format(inFormat, System.currentTimeMillis());
    }

    /**
     * @param port 端口
     * @return true: already in use, false: not.
     * @brief 检查本地端口是否被占用
     */
    public boolean isLocalPortInUse(int port) {
        boolean flag = true;
        try {
            flag = isPortInUse("127.0.0.1", port);
        } catch (Exception ignored) {

        }
        return flag;
    }

    /**
     * @param host 主机
     * @param port 端口
     * @return true: already in use, false: not.
     * @throws UnknownHostException
     * @brief 检查主机端口是否被占用
     */
    public boolean isPortInUse(String host, int port) throws UnknownHostException {
        boolean flag = false;
        InetAddress theAddress = InetAddress.getByName(host);
        try {
            Socket socket = new Socket(theAddress, port);
            socket.close();
            flag = true;
        } catch (IOException ignored) {

        }
        return flag;
    }

    public int getEnablePort(int start) {
        while (isLocalPortInUse(start)) {
            start++;
        }
        return start;
    }

    static final class Holder {
        static CommonUtil instance = new CommonUtil();
    }

}
