package com.snailstudio.xsdk.debug.serv.req.database;

import com.snailstudio.xsdk.debug.serv.req.response.Response;
import com.snailstudio.xsdk.debug.utils.LogUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class FileHelper {

    public static Response getTableData(String filePath) {
        Response fileData = new Response();
        fileData.isSuccessful = true;
        fileData.info = "Error when getting the text";
        fileData.isFile = true;
        fileData.path = filePath;
        if (filePath.endsWith(".txt")
                || filePath.endsWith(".log")
                || filePath.endsWith(".info")
                || filePath.endsWith(".ini")
                || filePath.endsWith(".java")
                || filePath.endsWith(".c")
                || filePath.endsWith(".cpp")
                || filePath.endsWith(".js")
                || filePath.endsWith(".xml")
                || filePath.endsWith(".html")
                || filePath.endsWith(".css")) {
            try {
                StringBuilder sb = new StringBuilder();
                File file = new File(filePath);
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                fileData.info = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            fileData.error = "Not text";
        }
        LogUtils.d("fileData.info:" + fileData.info);
        return fileData;
    }
}
