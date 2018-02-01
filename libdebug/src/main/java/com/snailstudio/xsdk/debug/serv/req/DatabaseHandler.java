/*
 *
 *  *    Copyright (C) 2016 Snailstudio
 *  *    Copyright (C) 2011 Android Open Source Project
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.snailstudio.xsdk.debug.serv.req;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.snailstudio.xsdk.debug.Constants;
import com.snailstudio.xsdk.debug.R;
import com.snailstudio.xsdk.debug.serv.req.database.DatabaseFileProvider;
import com.snailstudio.xsdk.debug.serv.req.database.DatabaseHelper;
import com.snailstudio.xsdk.debug.serv.req.database.FileHelper;
import com.snailstudio.xsdk.debug.serv.req.database.PrefHelper;
import com.snailstudio.xsdk.debug.serv.req.objs.FileRow;
import com.snailstudio.xsdk.debug.serv.req.response.AddDBResponse;
import com.snailstudio.xsdk.debug.serv.req.response.FileListResponse;
import com.snailstudio.xsdk.debug.serv.req.response.Response;
import com.snailstudio.xsdk.debug.serv.req.response.RowDataRequest;
import com.snailstudio.xsdk.debug.serv.req.response.TableDataResponse;
import com.snailstudio.xsdk.debug.serv.req.response.TerminalResponse;
import com.snailstudio.xsdk.debug.serv.req.response.UpdateRowResponse;
import com.snailstudio.xsdk.debug.serv.terminal.TermWebSocket;
import com.snailstudio.xsdk.debug.serv.terminal.TerminalHelper;
import com.snailstudio.xsdk.debug.utils.CommonUtil;
import com.snailstudio.xsdk.debug.utils.LogUtils;
import com.snailstudio.xsdk.debug.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class DatabaseHandler implements TerminalHelper.OnReaderListener {
    private static final String TAG = DatabaseHandler.class.getSimpleName();
    private static DatabaseHandler instance;
    private final Context mContext;
    private final Gson mGson;
    private final AssetManager mAssets;
    private boolean isDbOpened;
    private SQLiteDatabase mDatabase;
    private String mDirPath;
    private HashMap<String, File> mDatabaseFiles;
    private HashMap<String, File> mCustomDatabaseFiles;
    private String mSelectedDatabase = null;
    private TermWebSocket mTermWebSocket;
    private TerminalHelper mTerminalHelper;
    private CommonUtil mCommonUtil = CommonUtil.getSingleton();
    private SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd ahh:mm");

    private DatabaseHandler(Context context) {
        mContext = context;
        mAssets = context.getResources().getAssets();
        mGson = new GsonBuilder().serializeNulls().create();
        mTermWebSocket = new TermWebSocket();
        mTerminalHelper = new TerminalHelper();
        mTerminalHelper.setOnReaderListener(this);
    }

    public static DatabaseHandler getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHandler(context);
        return instance;
    }

    @Override
    public void onReader(byte[] buffer, int size) {
        mTermWebSocket.send(buffer, size);
    }

    @Deprecated
    public boolean handle(Socket socket) throws IOException {

        BufferedReader reader = null;
        PrintStream output = null;
        try {
            String route = null;

            // Read HTTP headers and parse out the route.
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while (!TextUtils.isEmpty(line = reader.readLine())) {
                if (line.startsWith("GET /")) {
                    int start = line.indexOf('/') + 1;
                    int end = line.indexOf(' ', start);
                    route = line.substring(start, end);
                    break;
                }
            }
            // Output stream that we send the response to
            output = new PrintStream(socket.getOutputStream());

            if (route == null || route.isEmpty()) {
                route = "index.html";
            }

            byte[] bytes = null;
            LogUtils.d("route:" + route);
            route = Utils.decodeUrl(route);
            LogUtils.d("decodeUrl route:" + route);
            if (route.startsWith("getDbList")) {
                final String response = getDBListResponse();
                bytes = response.getBytes();
            } else if (route.startsWith("getAllDataFromTheTable")) {
                final String response = getAllDataFromTheTableResponse(route);
                bytes = response.getBytes();
            } else if (route.startsWith("getAllDataFromTheFile")) {
                final String response = getAllDataFromTheFileResponse(route);
                bytes = response.getBytes();
            } else if (route.startsWith("getTableList")) {
                final String response = getTableListResponse(route);
                bytes = response.getBytes();
            } else if (route.startsWith("backDirPath")) {
                final String response = backDirPathResponse();
                bytes = response.getBytes();
            } else if (route.startsWith("addTableData")) {
                final String response = addTableDataAndGetResponse(route);
                bytes = response.getBytes();
            } else if (route.startsWith("updateTableData")) {
                final String response = updateTableDataAndGetResponse(route);
                bytes = response.getBytes();
            } else if (route.startsWith("deleteTableData")) {
                final String response = deleteTableDataAndGetResponse(route);
                bytes = response.getBytes();
            } else if (route.startsWith("query")) {
                final String response = executeQueryAndGetResponse0(route);
                bytes = response.getBytes();
            } else if (route.startsWith("downloadDb")) {
                bytes = Utils.getDatabase(mSelectedDatabase, getDBList());
            } else if (route.startsWith("openTerminal")) {
                final String response = openTerminalResponse();
                bytes = response.getBytes();
            } else {
                int index = route.indexOf("?");
                if (index != -1) {
                    route = route.substring(0, index);
                }
                bytes = Utils.loadContent(route, mAssets);
            }

            if (null == bytes) {
                writeServerError(output);
                return true;
            }

            // Send out the content.
            output.println("HTTP/1.0 200 OK");
            output.println("Content-Type: " + Utils.detectMimeType(route));

            if (route.startsWith("downloadDb")) {
                String filename = mSelectedDatabase;
                if (TextUtils.equals(mSelectedDatabase, Constants.STORAGE)) {
                    File file = getDBList().get(mSelectedDatabase);
                    if (file != null && file.exists())
                        filename = file.getName();
                }
                output.println("Content-Disposition: attachment; filename=" + filename);
            } else {
                output.println("Content-Length: " + bytes.length);
            }
            output.println();
            output.write(bytes);
            output.flush();
        } finally {
            try {
                if (null != output) {
                    output.close();
                }
                if (null != reader) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void setCustomDatabaseFiles(HashMap<String, File> customDatabaseFiles) {
        mCustomDatabaseFiles = customDatabaseFiles;
    }

    private void writeServerError(PrintStream output) {
        output.println("HTTP/1.0 500 Internal Server Error");
        output.flush();
    }

    private void openDatabase(String database) {
        closeDatabase();
        File databaseFile = getDBList().get(database);
        mDatabase = SQLiteDatabase.openOrCreateDatabase(databaseFile.getAbsolutePath(), null);
        isDbOpened = true;
    }

    private void closeDatabase() {
        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
        }
        mDatabase = null;
        isDbOpened = false;
    }

    private HashMap<String, File> getDBList() {
        if (mDatabaseFiles == null)
            readDBList();
        return mDatabaseFiles;
    }

    private void readDBList() {
        mDatabaseFiles = DatabaseFileProvider.getDatabaseFiles(mContext);
        if (mCustomDatabaseFiles != null) {
            mDatabaseFiles.putAll(mCustomDatabaseFiles);
        }
    }

    String getDBListResponse() {
        Response response = new Response();
        for (HashMap.Entry<String, File> entry : getDBList().entrySet()) {
            response.rows.add(entry.getKey());
        }
        response.rows.add(Constants.APP_SHARED_PREFERENCES);
        response.rows.add(Constants.STORAGE);
        response.rows.add(Constants.STORAGE_ANR);
        response.isSuccessful = true;
        return mGson.toJson(response);
    }

    public String addDBResponse(String dbPath) {
        AddDBResponse response = new AddDBResponse();

        File dbFile = new File(dbPath);
        if (!dbFile.exists() || !dbFile.isFile()) {
            response.errorMessage = mContext.getString(R.string.info_not_exist);
            return mGson.toJson(response);
        }

        HashMap<String, File> databaseFiles = getDBList();
        if (databaseFiles == null) {
            response.errorMessage = mContext.getString(R.string.info_unknown_error);
            return mGson.toJson(response);
        }

        String dbName = dbFile.getName();

        for (HashMap.Entry<String, File> entry : databaseFiles.entrySet()) {
            if (dbFile.equals(entry.getValue())) {
                response.errorMessage = mContext.getString(R.string.info_db_exist);
                return mGson.toJson(response);
            }
        }
        if (databaseFiles.containsKey(dbName)) {
            int index = 1;
            String newName;
            do {
                newName = dbName + "-" + index++;
            } while (databaseFiles.containsKey(newName));
            dbName = newName;
        }
        LogUtils.d("dbName:" + dbName);
        databaseFiles.put(dbName, dbFile);
        response.dbName = dbName;
        response.isSuccessful = true;
        return mGson.toJson(response);
    }

    public String getAllDataFromTheTableResponse(String tableName) {

        TableDataResponse response;

        if (isDbOpened) {
            String sql = "SELECT * FROM " + tableName;
            response = DatabaseHelper.getTableData(mDatabase, sql, tableName);
        } else {
            response = PrefHelper.getAllPrefData(mContext, tableName);
        }

        return mGson.toJson(response);

    }

    private String getAllDataFromTheFileResponse(String route) {
        mSelectedDatabase = Constants.STORAGE;
        String fileName = null;

        if (route.contains("?fileName=")) {
            fileName = route.substring(route.indexOf("=") + 1, route.length());
        }

        String filePath = mDirPath + File.separator + fileName;
        File file = new File(filePath);
        Response response;
        if (file.isFile()) {
            LogUtils.d("file.isFile");
            getDBList().put(mSelectedDatabase, file);
            response = FileHelper.getTableData(filePath);
        } else {
            LogUtils.d("file.isDict");
            mDirPath = filePath;
            response = PrefHelper.getAllFileName(filePath);
        }

        return mGson.toJson(response);

    }

    public String executeQueryAndGetResponse(String dbName, String query) {
        String data = null;
        String first;
        try {
            if (query != null) {
                openDatabase(dbName);
                first = query.split(" ")[0].toLowerCase();
                if (first.equals("select") || first.equals("pragma")) {
                    TableDataResponse response = DatabaseHelper.getTableData(mDatabase, query, null);
                    data = mGson.toJson(response);
                } else {
                    TableDataResponse response = DatabaseHelper.exec(mDatabase, query);
                    data = mGson.toJson(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (data == null) {
            Response response = new Response();
            response.isSuccessful = false;
            data = mGson.toJson(response);
        }

        return data;
    }

    public String executeQueryAndGetResponse(String query) {
        String data = null;
        String first;
        try {
            if (query != null) {
                first = query.split(" ")[0].toLowerCase();
                if (first.equals("select") || first.equals("pragma")) {
                    TableDataResponse response = DatabaseHelper.getTableData(mDatabase, query, null);
                    data = mGson.toJson(response);
                } else {
                    TableDataResponse response = DatabaseHelper.exec(mDatabase, query);
                    data = mGson.toJson(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (data == null) {
            Response response = new Response();
            response.isSuccessful = false;
            data = mGson.toJson(response);
        }

        return data;
    }

    private String executeQueryAndGetResponse0(String route) {
        String query = null;
        String data = null;
        String first;
        try {
            if (route.contains("?query=")) {
                query = route.substring(route.indexOf("=") + 1, route.length());
            }
            try {
                query = URLDecoder.decode(query, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (query != null) {
                first = query.split(" ")[0].toLowerCase();
                if (first.equals("select") || first.equals("pragma")) {
                    TableDataResponse response = DatabaseHelper.getTableData(mDatabase, query, null);
                    data = mGson.toJson(response);
                } else {
                    TableDataResponse response = DatabaseHelper.exec(mDatabase, query);
                    data = mGson.toJson(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (data == null) {
            Response response = new Response();
            response.isSuccessful = false;
            data = mGson.toJson(response);
        }

        return data;
    }

    public String openTerminalResponse() {
        TerminalResponse response = new TerminalResponse();
        int port = mTermWebSocket.socket();
        if (port == -999) {
            response.isSuccessful = false;
            response.error = "Unknown error";
        } else {
            response.isSuccessful = true;
            response.port = port;
            new WebSocketThread().start();
        }
        return mGson.toJson(response);
    }

    private String backDirPathResponse() {
        mSelectedDatabase = Constants.STORAGE;
        Response response;
        if (TextUtils.isEmpty(mDirPath)
//                || mDirPath.length() <= Constants.ROOT_PATH.length()
                || Constants.ROOT_PATH.equals(mDirPath)
                || Constants.ANR_PATH.equals(mDirPath)) {
            response = new Response();
            response.isSuccessful = false;
        } else {
            mDirPath = new File(mDirPath).getParentFile().getPath();
            response = PrefHelper.getAllFileName(mDirPath);
        }

        return mGson.toJson(response);
    }

    public String getTableListResponse(String database) {
        if (TextUtils.isEmpty(database)) {
            return mGson.toJson(new Response());
        }
        Response response;

        if (Constants.APP_SHARED_PREFERENCES.equals(database)) {
            response = PrefHelper.getAllPrefTableName(mContext);
            closeDatabase();
            mSelectedDatabase = Constants.APP_SHARED_PREFERENCES;
        } else if (Constants.STORAGE.equals(database)
                || Constants.STORAGE_ANR.equals(database)) {
            closeDatabase();
            if (Constants.STORAGE.equals(database)) {
                database = Constants.ROOT_PATH;
            } else if (Constants.STORAGE_ANR.equals(database)) {
                database = Constants.ANR_PATH;
            }
            mDirPath = database;
            response = PrefHelper.getAllFileName(database);
            mSelectedDatabase = Constants.STORAGE;
        } else {
            openDatabase(database);
            response = DatabaseHelper.getAllTableName(mDatabase);
            mSelectedDatabase = database;
        }
        return mGson.toJson(response);
    }

    public String addTableDataAndGetResponse(String dbName, String tableName, String updatedData) {
        UpdateRowResponse response;
        try {
            List<RowDataRequest> rowDataRequests = mGson.fromJson(updatedData, new TypeToken<List<RowDataRequest>>() {
            }.getType());
            if (Constants.APP_SHARED_PREFERENCES.equals(mSelectedDatabase)) {
                response = PrefHelper.addOrUpdateRow(mContext, tableName, rowDataRequests);
            } else {
                openDatabase(dbName);
                response = DatabaseHelper.addRow(mDatabase, tableName, rowDataRequests);
            }
            return mGson.toJson(response);
        } catch (Exception e) {
            e.printStackTrace();
            response = new UpdateRowResponse();
            response.isSuccessful = false;
            return mGson.toJson(response);
        }
    }

    private String addTableDataAndGetResponse(String route) {
        UpdateRowResponse response;
        try {
            Uri uri = Uri.parse(URLDecoder.decode(route, "UTF-8"));
            String tableName = uri.getQueryParameter("tableName");
            String updatedData = uri.getQueryParameter("addData");
            List<RowDataRequest> rowDataRequests = mGson.fromJson(updatedData, new TypeToken<List<RowDataRequest>>() {
            }.getType());
            if (Constants.APP_SHARED_PREFERENCES.equals(mSelectedDatabase)) {
                response = PrefHelper.addOrUpdateRow(mContext, tableName, rowDataRequests);
            } else {
                response = DatabaseHelper.addRow(mDatabase, tableName, rowDataRequests);
            }
            return mGson.toJson(response);
        } catch (Exception e) {
            e.printStackTrace();
            response = new UpdateRowResponse();
            response.isSuccessful = false;
            return mGson.toJson(response);
        }
    }

    public String updateTableDataAndGetResponse(String dbName, String tableName, String updatedData) {
        UpdateRowResponse response;
        try {
            List<RowDataRequest> rowDataRequests = mGson.fromJson(updatedData, new TypeToken<List<RowDataRequest>>() {
            }.getType());
            if (Constants.APP_SHARED_PREFERENCES.equals(mSelectedDatabase)) {
                response = PrefHelper.addOrUpdateRow(mContext, tableName, rowDataRequests);
            } else {
                openDatabase(dbName);
                response = DatabaseHelper.updateRow(mDatabase, tableName, rowDataRequests);
            }
            return mGson.toJson(response);
        } catch (Exception e) {
            e.printStackTrace();
            response = new UpdateRowResponse();
            response.isSuccessful = false;
            return mGson.toJson(response);
        }
    }

    private String updateTableDataAndGetResponse(String route) {
        UpdateRowResponse response;
        try {
            Uri uri = Uri.parse(URLDecoder.decode(route, "UTF-8"));
            String tableName = uri.getQueryParameter("tableName");
            String updatedData = uri.getQueryParameter("updatedData");
            List<RowDataRequest> rowDataRequests = mGson.fromJson(updatedData, new TypeToken<List<RowDataRequest>>() {
            }.getType());
            if (Constants.APP_SHARED_PREFERENCES.equals(mSelectedDatabase)) {
                response = PrefHelper.addOrUpdateRow(mContext, tableName, rowDataRequests);
            } else {
                response = DatabaseHelper.updateRow(mDatabase, tableName, rowDataRequests);
            }
            return mGson.toJson(response);
        } catch (Exception e) {
            e.printStackTrace();
            response = new UpdateRowResponse();
            response.isSuccessful = false;
            return mGson.toJson(response);
        }
    }

    public String deleteTableDataAndGetResponse(String dbName, String tableName, String updatedData) {
        UpdateRowResponse response;
        try {
            List<RowDataRequest> rowDataRequests = mGson.fromJson(updatedData, new TypeToken<List<RowDataRequest>>() {
            }.getType());
            if (Constants.APP_SHARED_PREFERENCES.equals(mSelectedDatabase)) {
                response = PrefHelper.deleteRow(mContext, tableName, rowDataRequests);
            } else {
                openDatabase(dbName);
                response = DatabaseHelper.deleteRow(mDatabase, tableName, rowDataRequests);
            }
            return mGson.toJson(response);
        } catch (Exception e) {
            e.printStackTrace();
            response = new UpdateRowResponse();
            response.isSuccessful = false;
            return mGson.toJson(response);
        }
    }

    private String deleteTableDataAndGetResponse(String route) {
        UpdateRowResponse response;
        try {
            Uri uri = Uri.parse(URLDecoder.decode(route, "UTF-8"));
            String tableName = uri.getQueryParameter("tableName");
            String updatedData = uri.getQueryParameter("deleteData");
            List<RowDataRequest> rowDataRequests = mGson.fromJson(updatedData, new TypeToken<List<RowDataRequest>>() {
            }.getType());
            if (Constants.APP_SHARED_PREFERENCES.equals(mSelectedDatabase)) {
                response = PrefHelper.deleteRow(mContext, tableName, rowDataRequests);
            } else {
                response = DatabaseHelper.deleteRow(mDatabase, tableName, rowDataRequests);
            }
            return mGson.toJson(response);
        } catch (Exception e) {
            e.printStackTrace();
            response = new UpdateRowResponse();
            response.isSuccessful = false;
            return mGson.toJson(response);
        }
    }

    public File getSelectedDBFile() {
        return getDBList().get(mSelectedDatabase);
    }

    public void onDestroy() {
        mTerminalHelper.onExit(0);
    }

    public String getFileListResponse(String path) {
        FileListResponse response = new FileListResponse();
        File dir = new File(path);
        response.path = dir.getPath();
        if (!"/".equals(response.path))
            response.path += "/";
        if (dir.exists() && dir.isDirectory()) {
            response.colNames = new String[]{
                    mContext.getString(R.string.v_name),
                    mContext.getString(R.string.v_size),
                    mContext.getString(R.string.v_modi),
                    mContext.getString(R.string.v_oper)};
            response.fileRows = buildFileRows(dir);
            response.isSuccessful = true;
        }

        return mGson.toJson(response);
    }

    private List<FileRow> buildFileRows(File dir) {
        File[] files = dir.listFiles(); // 目录列表
        if (files != null) {
            sort(files); // 排序
            ArrayList<FileRow> fileRows = new ArrayList<FileRow>();
            for (File file : files) {
                fileRows.add(buildFileRow(file));
            }
            return fileRows;
        }
        return null;
    }

    private FileRow buildFileRow(File f) {
        boolean isDir = f.isDirectory();
        String clazz, name, link, size;
        if (isDir) {
            clazz = "icon dir";
            name = f.getName() + "/";
            link = f.getPath() + "/";
            size = "";
        } else {
            clazz = "icon file";
            name = f.getName();
            link = f.getPath();
            size = mCommonUtil.readableFileSize(f.length());
        }
        FileRow row = new FileRow(clazz, name, link, size);
        row.time = sdf.format(new Date(f.lastModified()));
        if (f.canRead()) {
            row.can_browse = true;
            if (Constants.Config.ALLOW_DOWNLOAD) {
                row.can_download = true;
            }
            if (f.canWrite() && !hasWsDir(f)) {
                if (Constants.Config.ALLOW_DELETE) {
                    row.can_delete = true;
                }
                if (Constants.Config.ALLOW_UPLOAD && isDir) {
                    row.can_upload = true;
                }
            }
        }
        row.is_file = f.isFile();
        return row;
    }

    private boolean hasWsDir(File f) {
        return HttpDelHandler.hasWsDir(f);
    }

    /**
     * 排序：文件夹、文件，再各安字符顺序
     */
    private void sort(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.toString().compareToIgnoreCase(f2.toString());
                }
            }
        });
    }

    private class WebSocketThread extends Thread {
        public void run() {
            int conn = mTermWebSocket.accept();
            LogUtils.dLog(TAG, "mWebSocketThread　conn:" + conn);
            try {
                if (!mTerminalHelper.isRunning())
                    mTerminalHelper.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mTermWebSocket.run();

        }
    }
}
