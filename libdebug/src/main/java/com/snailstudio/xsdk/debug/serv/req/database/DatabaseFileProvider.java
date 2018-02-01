package com.snailstudio.xsdk.debug.serv.req.database;

import android.content.Context;

import java.io.File;
import java.util.HashMap;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class DatabaseFileProvider {

    private DatabaseFileProvider() {
        // This class in not publicly instantiable
    }

    public static HashMap<String, File> getDatabaseFiles(Context context) {
        HashMap<String, File> databaseFiles = new HashMap<>();
        try {
            for (String databaseName : context.databaseList()) {
                databaseFiles.put(databaseName, context.getDatabasePath(databaseName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return databaseFiles;
    }

}
