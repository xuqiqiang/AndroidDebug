package com.snailstudio.xsdk.debug.serv.req.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.snailstudio.xsdk.debug.Constants;
import com.snailstudio.xsdk.debug.serv.req.response.Response;
import com.snailstudio.xsdk.debug.serv.req.response.RowDataRequest;
import com.snailstudio.xsdk.debug.serv.req.response.TableDataResponse;
import com.snailstudio.xsdk.debug.serv.req.response.UpdateRowResponse;
import com.snailstudio.xsdk.debug.utils.LogUtils;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class PrefHelper {

    private static final String PREFS_SUFFIX = ".xml";

    private PrefHelper() {
        // This class in not publicly instantiable
    }

    public static List<String> getSharedPreferenceTags(Context context) {

        ArrayList<String> tags = new ArrayList<>();

        String rootPath = context.getApplicationInfo().dataDir + "/shared_prefs";
        File root = new File(rootPath);
        if (root.exists()) {
            for (File file : root.listFiles()) {
                String fileName = file.getName();
                if (fileName.endsWith(PREFS_SUFFIX)) {
                    tags.add(fileName.substring(0, fileName.length() - PREFS_SUFFIX.length()));
                }
            }
        }

        Collections.sort(tags);

        return tags;
    }

    public static Response getAllPrefTableName(Context context) {

        Response response = new Response();

        List<String> prefTags = getSharedPreferenceTags(context);

        for (String tag : prefTags) {
            response.rows.add(tag);
        }

        response.isSuccessful = true;

        return response;
    }

    public static Response getAllFileName(String path) {

        Response response = new Response();
        File dir = new File(path);
        File[] files = dir.listFiles();
        LogUtils.d("getAllFileName path:" + path);
        if (files != null) {
            LogUtils.d("getAllFileName:" + files.length);
            List<File> fileList = Arrays.asList(files);
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile())
                        return -1;
                    if (o1.isFile() && o2.isDirectory())
                        return 1;
                    return o1.getName().compareTo(o2.getName());
                }
            });
            for (File f : fileList) {
                response.rows.add(f.getName());
            }
        } else {
            LogUtils.e("dir.listFiles == null");
        }

        response.path = path;
        response.isSuccessful = true;
        return response;
    }

    public static TableDataResponse getAllPrefData(Context context, String tag) {

        TableDataResponse response = new TableDataResponse();
        response.isEditable = true;
        response.isSuccessful = true;
        response.isSelectQuery = true;

        TableDataResponse.TableInfo keyInfo = new TableDataResponse.TableInfo();
        keyInfo.isPrimary = true;
        keyInfo.title = "Key";

        TableDataResponse.TableInfo valueInfo = new TableDataResponse.TableInfo();
        valueInfo.isPrimary = false;
        valueInfo.title = "Value";

        response.tableInfos = new ArrayList<>();
        response.tableInfos.add(keyInfo);
        response.tableInfos.add(valueInfo);

        response.rows = new ArrayList<>();

        SharedPreferences preferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            List<TableDataResponse.ColumnData> row = new ArrayList<>();
            TableDataResponse.ColumnData keyColumnData = new TableDataResponse.ColumnData();
            keyColumnData.dataType = DataType.TEXT;
            keyColumnData.value = entry.getKey();

            row.add(keyColumnData);

            TableDataResponse.ColumnData valueColumnData = new TableDataResponse.ColumnData();
            valueColumnData.value = entry.getValue().toString();
            if (entry.getValue() != null) {
                if (entry.getValue() instanceof String) {
                    valueColumnData.dataType = DataType.TEXT;
                } else if (entry.getValue() instanceof Integer) {
                    valueColumnData.dataType = DataType.INTEGER;
                } else if (entry.getValue() instanceof Long) {
                    valueColumnData.dataType = DataType.LONG;
                } else if (entry.getValue() instanceof Float) {
                    valueColumnData.dataType = DataType.FLOAT;
                } else if (entry.getValue() instanceof Boolean) {
                    valueColumnData.dataType = DataType.BOOLEAN;
                } else if (entry.getValue() instanceof Set) {
                    valueColumnData.dataType = DataType.STRING_SET;
                }
            } else {
                valueColumnData.dataType = DataType.TEXT;
            }
            row.add(valueColumnData);
            response.rows.add(row);
        }

        return response;

    }

    public static UpdateRowResponse addOrUpdateRow(Context context, String tableName,
                                                   List<RowDataRequest> rowDataRequests) {
        UpdateRowResponse updateRowResponse = new UpdateRowResponse();

        if (tableName == null) {
            return updateRowResponse;
        }

        RowDataRequest rowDataKey = rowDataRequests.get(0);
        RowDataRequest rowDataValue = rowDataRequests.get(1);

        String key = rowDataKey.value;
        String value = rowDataValue.value;
        String dataType = rowDataValue.dataType;

        if (Constants.NULL.equals(value)) {
            value = null;
        }

        SharedPreferences preferences = context.getSharedPreferences(tableName, Context.MODE_PRIVATE);

        try {
            switch (dataType) {
                case DataType.TEXT:
                    preferences.edit().putString(key, value).apply();
                    updateRowResponse.isSuccessful = true;
                    break;
                case DataType.INTEGER:
                    preferences.edit().putInt(key, Integer.valueOf(value)).apply();
                    updateRowResponse.isSuccessful = true;
                    break;
                case DataType.LONG:
                    preferences.edit().putLong(key, Long.valueOf(value)).apply();
                    updateRowResponse.isSuccessful = true;
                    break;
                case DataType.FLOAT:
                    preferences.edit().putFloat(key, Float.valueOf(value)).apply();
                    updateRowResponse.isSuccessful = true;
                    break;
                case DataType.BOOLEAN:
                    preferences.edit().putBoolean(key, Boolean.valueOf(value)).apply();
                    updateRowResponse.isSuccessful = true;
                    break;
                case DataType.STRING_SET:
                    JSONArray jsonArray = new JSONArray(value);
                    Set<String> stringSet = new HashSet<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        stringSet.add(jsonArray.getString(i));
                    }
                    preferences.edit().putStringSet(key, stringSet).apply();
                    updateRowResponse.isSuccessful = true;
                    break;
                default:
                    preferences.edit().putString(key, value).apply();
                    updateRowResponse.isSuccessful = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return updateRowResponse;
    }


    public static UpdateRowResponse deleteRow(Context context, String tableName,
                                              List<RowDataRequest> rowDataRequests) {
        UpdateRowResponse updateRowResponse = new UpdateRowResponse();

        if (tableName == null) {
            return updateRowResponse;
        }

        RowDataRequest rowDataKey = rowDataRequests.get(0);

        String key = rowDataKey.value;


        SharedPreferences preferences = context.getSharedPreferences(tableName, Context.MODE_PRIVATE);

        try {
            preferences.edit()
                    .remove(key).apply();
            updateRowResponse.isSuccessful = true;
        } catch (Exception ex) {
            updateRowResponse.isSuccessful = false;
        }

        return updateRowResponse;
    }
}
