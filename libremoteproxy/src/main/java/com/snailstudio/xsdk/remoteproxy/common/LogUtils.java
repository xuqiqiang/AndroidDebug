package com.snailstudio.xsdk.remoteproxy.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 日志工具
 * <p>
 * Created by xuqiqiang on 2017/4/28.
 */
public class LogUtils {
    private static final long MAX_FILE_SIZE = 1024 * 1024;
    private static final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private static final int MAX_FILE_SUM = 50;
    private static final int MAX_FILE_SUM_CACHE = 30;
    /**
     * Android's max limit for a log entry is ~4076 bytes,
     * so 4000 bytes is used as chunk size since default charset
     * is UTF-8
     */
    private static final int CHUNK_SIZE = 4000;
    private static final char TOP_LEFT_CORNER = '┌';
    private static final char BOTTOM_LEFT_CORNER = '└';
    private static final char MIDDLE_CORNER = '├';
    private static final char HORIZONTAL_LINE = '│';
    private static final String DOUBLE_DIVIDER = "────────────────────────────────────────────────────────";
    private static final String SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;
    public static int level = Log.VERBOSE;
    private static boolean DEBUG;
    private static String mTag = "TAG";
    private static File mDir;
    private static String mDefaultDate;

    public static void initialize(String tag, boolean debug) {
        LogUtils.mTag = tag;
        LogUtils.DEBUG = debug;
    }

    public static void initialize(Context context, String dirPath, String tag, boolean debug) {
        if (TextUtils.isEmpty(dirPath)) {
            dirPath = getDefaultPath(context);
        }
        LogUtils.mTag = tag;
        LogUtils.DEBUG = debug;
        mDir = new File(dirPath);
        if (!mDir.exists()) {
            if (!mDir.mkdirs())
                Log.e("LibLog", "Error occurred during creating mDir");
        }
    }

    public static void initDefaultDate() {
        mDefaultDate = FormatDate.getFormatDate();
    }

    public static void unregister() {
        mDir = null;
    }

    private static String getDefaultPath(Context context) {
        String path;
        if (context != null) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                path = Environment
                        .getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + context.getPackageName();
            } else {
                path = context.getCacheDir().getAbsolutePath()
                        + File.separator + context.getPackageName();
            }
        } else {
            path = Environment
                    .getExternalStorageDirectory().getAbsolutePath();
        }
        return path;
    }

    public static void v(String msg, Object... args) {
        log(Log.VERBOSE, null, null, null, msg, args);
    }

    public static void vLog(Object obj, String msg, Object... args) {
        log(Log.VERBOSE, null, getClassInfoByObject(obj), null, msg, args);
    }

    public static void d(String msg, Object... args) {
        log(Log.DEBUG, null, null, null, msg, args);
    }

    public static void dLog(Object obj, String msg, Object... args) {
        log(Log.DEBUG, null, getClassInfoByObject(obj), null, msg, args);
    }

    public static void i(String msg, Object... args) {
        log(Log.INFO, null, null, null, msg, args);
    }

    public static void iLog(Object obj, String msg, Object... args) {
        log(Log.INFO, null, getClassInfoByObject(obj), null, msg, args);
    }

    public static void w(String msg, Object... args) {
        log(Log.WARN, null, null, null, msg, args);
    }

    public static void wLog(Object obj, String msg, Object... args) {
        log(Log.WARN, null, getClassInfoByObject(obj), null, msg, args);
    }

    public static void e(String msg, Object... args) {
        log(Log.ERROR, null, null, null, msg, args);
    }

    public static void e(Throwable throwable, String msg, Object... args) {
        log(Log.ERROR, null, null, throwable, msg, args);
    }

    public static void eLog(Object obj, String msg, Object... args) {
        log(Log.ERROR, null, getClassInfoByObject(obj), null, msg, args);
    }

    public static synchronized void log(int priority,
                                        StackTraceElement ste,
                                        String tag, Throwable throwable, String msg, Object... args) {
        if (level > priority ||
                !DEBUG && mDir == null)
            return;
        if (TextUtils.isEmpty(tag)) {
            tag = mTag;
        }
        String message = createMessage(throwable, msg, args);
        if (ste == null)
            ste = new Throwable().getStackTrace()[2];
        tag += "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")";
        if (DEBUG) {
            println(priority, tag, message);
        }
        if (mDir != null) {
            String writeLog = FormatDate.getFormatTime()
                    + " " + logLevel(priority) + "/" + tag
                    + " : " + message + "\r\n";
            startWriteThread(writeLog);
        }
    }

    private static String logLevel(int value) {
        switch (value) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
                return "I";
            case Log.WARN:
                return "W";
            case Log.ERROR:
                return "E";
            case Log.ASSERT:
                return "A";
            default:
                return "U";
        }
    }

    private static void println(int priority, String tag, String message) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(TOP_BORDER).append("\r\n");

        byte[] bytes = message.getBytes();
        int length = bytes.length;
        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            logContent(logBuilder, new String(bytes, i, count));
        }
        logBuilder.append(BOTTOM_BORDER);

        Log.println(priority, tag, logBuilder.toString());
    }

    private static void logContent(StringBuilder logBuilder, String chunk) {
        String[] lines = chunk.split(System.getProperty("line.separator"));
        for (String line : lines) {
            logBuilder.append(HORIZONTAL_LINE).append(" ").append(line).append("\r\n");
        }
    }

    private static String getClassInfoByObject(Object obj) {
        if (obj == null) {
            return "null";
        }
        String simpleName = obj.getClass().getSimpleName();
        if ("String".equals(simpleName)) {
            return obj.toString();
        }
        if (TextUtils.isEmpty(simpleName)) {
            return "TAG";
        }
        return simpleName;
    }

    private static String createMessage(Throwable throwable, String message, Object... args) {
        if (args != null && args.length > 0) {
            try {
                message = String.format(message, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (throwable != null) {
            if (!TextUtils.isEmpty(message)) {
                message += " : " + getStackTraceString(throwable);
            } else {
                message = getStackTraceString(throwable);
            }
        }
        if (TextUtils.isEmpty(message)) {
            message = "Empty/NULL log message";
        }
        return message;
    }

    /**
     * Copied from "android.util.Log.getStackTraceString()" in order to avoid usage of Android stack
     * in unit tests.
     *
     * @return Stack trace in form of String
     */
    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    private static synchronized void startWriteThread(final String systemOut) {
        if (mDir == null)
            return;
        mExecutorService.execute(new Thread() {
            @Override
            public void run() {
                if (!write(systemOut))
                    Log.e("LibLog", "Error occurred during writing log : " + systemOut);
            }
        });
    }

    private static File getWriteFile() {
        String date = FormatDate.getFormatDate();
        int fileIndex = 1;
        File file;
        do {
            file = new File(mDir,
                    date + "-" + (fileIndex++) + ".log");
        } while (file.exists() && file.length() > MAX_FILE_SIZE);

        if (!file.exists()) {

            File f = handleOutdatedFiles();

            if (f != null)
                file = f;
            try {
                if (!file.createNewFile())
                    return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    private static boolean write(String info) {
        if (mDir == null)
            return false;
        File file = getWriteFile();
        if (file == null)
            return false;
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        byte[] b = info.getBytes();
        try {
            outputStream.write(b, 0, b.length);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private static List<File> getFileList(String date) {
        List<File> list = new ArrayList<>();
        int fileIndex = 1;
        File file;
        do {
            file = new File(mDir,
                    date + "-" + (fileIndex++) + ".log");
        } while (file.exists() && list.add(file));
        return list;
    }

    public static List<File> getYesterdayFileList() {
        return getFileList(FormatDate.getYesterdayFormatDate());
    }

    public static List<File> getYesterdayAndDefaultFileList() {
        List<File> list = getYesterdayFileList();
        list.addAll(getFileList(mDefaultDate));
        return list;
    }

    /**
     * 从指定日期的日志中删除sum个文件，并重置编号.
     *
     * @param date 指定日期.
     * @param sum  需要删除的文件个数，0表示删除所有文件.
     * @return {@link int} 删除的文件个数.
     */
    private static int deleteFileList(String date, int sum) {
        if (TextUtils.isEmpty(date))
            return 0;
        int fileIndex = 1;
        File file = null;
        do {
            if (file != null) {
                if (sum > 0) {
                    if (fileIndex - 1 <= sum)
                        file.delete();
                    else {
                        file.renameTo(new File(mDir,
                                date + "-" + (fileIndex - 1 - sum) + ".log"));
                    }
                } else {
                    file.delete();
                }
            }
            file = new File(mDir,
                    date + "-" + (fileIndex++) + ".log");
        } while (file.exists());
        int fileSum = fileIndex - 2;
        return sum <= 0 ? fileSum : fileSum < sum ? fileSum : sum;
    }

    private static int fixFileList(String date, int deleteIndex) {
        if (TextUtils.isEmpty(date) || deleteIndex <= 0)
            return 0;
        int fileIndex = deleteIndex + 1;
        File file = null;
        do {
            if (file != null) {
                file.renameTo(new File(mDir,
                        date + "-" + (fileIndex - 1 - deleteIndex) + ".log"));
            }
            file = new File(mDir,
                    date + "-" + (fileIndex++) + ".log");
        } while (file.exists());
        return fileIndex - 1 - deleteIndex;
    }

    /**
     * 清理过时的日志，如果文件数还是大于80，把日志删除到只剩50个，如果删除了今天的日志，会调整
     * 今天的日志的编号，并返回一个正确编号的文件.
     *
     * @return {@link File} 返回一个正确编号的文件.
     */
    private static File handleOutdatedFiles() {
        File[] files = mDir.listFiles();
        if (files != null && files.length > MAX_FILE_SUM + MAX_FILE_SUM_CACHE) {
            List<File> fileList = Arrays.asList(files);
            Collections.sort(fileList, new Comparator<File>() {

                @Override
                public int compare(File o1, File o2) {
                    long t1 = o1.lastModified();
                    long t2 = o2.lastModified();
                    if (t1 < t2)
                        return -1;
                    else if (t1 == t2)
                        return 0;
                    else
                        return 1;
                }
            });
            int deleteSum = files.length - MAX_FILE_SUM;
            String today = FormatDate.getFormatDate();
            int todayIndex = 0;
            int i = 0;
            do {
                File file = fileList.get(i);
                if (file.getName().startsWith(today)) {
                    todayIndex++;
                }
                file.delete();
            } while (++i < deleteSum);
            if (todayIndex > 0)
                return new File(mDir,
                        today + "-" + fixFileList(today, todayIndex) + ".log");

        }
        return null;
    }

    @SuppressLint("SimpleDateFormat")
    private static class FormatDate {

        static String getFormatDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            return sdf.format(System.currentTimeMillis());
        }

        static String getYesterdayFormatDate() {
            Calendar ca = Calendar.getInstance();
            ca.setTime(new Date());
            ca.add(Calendar.DATE, -1);
            Date lastDay = ca.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            return sdf.format(lastDay);
        }

        static String getBeforeYesterdayFormatDate() {
            Calendar ca = Calendar.getInstance();
            ca.setTime(new Date());
            ca.add(Calendar.DATE, -2);
            Date lastDay = ca.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            return sdf.format(lastDay);
        }

        static String getFormatTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            return sdf.format(System.currentTimeMillis());
        }
    }
}
