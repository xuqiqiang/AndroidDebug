package com.snailstudio.xsdk.debug.serv.req.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class Response {

    public List<Object> rows = new ArrayList<>();
    public List<String> columns = new ArrayList<>();
    public boolean isSuccessful;
    public String error;
    public int dbVersion;
    public String info;
    public String path;
    public boolean isFile;

    public Response() {

    }

}
