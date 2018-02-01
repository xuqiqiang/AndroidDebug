package com.snailstudio.xsdk.debug.serv.req.objs;

/**
 * 渲染的行信息
 *
 * @note not obfuscated in this package
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class FileRow {

    public String clazz;
    public String name;
    public String link;
    public String size;
    public String time;

    public boolean is_file = false;
    public boolean can_browse = false;
    public boolean can_download = false;
    public boolean can_delete = false;
    public boolean can_upload = false;

    public FileRow() {
    }

    public FileRow(String clazz, String name, String link, String size) {
        this.clazz = clazz;
        this.name = name;
        this.link = link;
        this.size = size;
    }

}
