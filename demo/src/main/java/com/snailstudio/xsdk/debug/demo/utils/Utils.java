package com.snailstudio.xsdk.debug.demo.utils;

import android.content.Context;
import android.widget.Toast;

import com.snailstudio.xsdk.debug.demo.BuildConfig;
import com.snailstudio.xsdk.debug.demo.database.ExtTestDBHelper;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Utils {

    private Utils() {
        // This class is not publicly instantiable
    }

    public static void showDebugDBAddressLogToast(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.snailstudio.xsdk.debug.DebugConfig");
                Class[] argTypes = new Class[]{Context.class};
                Method getAddressLog = debugDB.getMethod("getAddressLog", argTypes);
                Object value = getAddressLog.invoke(null, context);
                Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setCustomDatabaseFiles(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.snailstudio.xsdk.debug.DebugConfig");
                Class[] argTypes = new Class[]{Context.class, HashMap.class};
                Method setCustomDatabaseFiles = debugDB.getMethod("setCustomDatabaseFiles", argTypes);
                HashMap<String, File> customDatabaseFiles = new HashMap<>();
                // set your custom database files
                customDatabaseFiles.put(ExtTestDBHelper.DATABASE_NAME,
                        new File(context.getFilesDir() + "/" + ExtTestDBHelper.DIR_NAME +
                                "/" + ExtTestDBHelper.DATABASE_NAME));
                setCustomDatabaseFiles.invoke(null, context, customDatabaseFiles);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
