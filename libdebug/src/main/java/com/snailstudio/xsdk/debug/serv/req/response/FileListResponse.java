package com.snailstudio.xsdk.debug.serv.req.response;

import com.snailstudio.xsdk.debug.serv.req.objs.FileRow;

import java.util.List;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class FileListResponse {

    public String path;
    public boolean isSuccessful;
    public String errorMessage;
    public List<FileRow> fileRows;
    public String[] colNames;
}
