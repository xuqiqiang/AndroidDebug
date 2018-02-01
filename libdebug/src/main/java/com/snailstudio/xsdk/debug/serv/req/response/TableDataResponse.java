package com.snailstudio.xsdk.debug.serv.req.response;

import java.util.List;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class TableDataResponse {

    public List<TableInfo> tableInfos;
    public boolean isSuccessful;
    public List<Object> rows;
    public String errorMessage;
    public boolean isEditable;
    public boolean isSelectQuery;

    public static class TableInfo {
        public String title;
        public boolean isPrimary;
    }

    public static class ColumnData {
        public String dataType;
        public Object value;
    }

}
