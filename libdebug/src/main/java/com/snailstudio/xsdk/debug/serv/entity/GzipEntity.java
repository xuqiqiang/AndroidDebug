package com.snailstudio.xsdk.debug.serv.entity;

import com.snailstudio.xsdk.debug.Constants.Config;
import com.snailstudio.xsdk.debug.utils.LogUtils;

import org.apache.http.Header;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 基础Gzip实体
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public abstract class GzipEntity extends AbstractHttpEntity implements Cloneable {

    /**
     * @param instream  输入流
     * @param outstream
     * @throws IOException 输出流
     * @brief 输入流拷贝进输出流
     * @warning When outstream is GZIPOutputStream, it will call finish(). But won't close any stream.
     */
    protected void copy(InputStream instream, OutputStream outstream) throws IOException {
        byte[] tmp = new byte[Config.BUFFER_LENGTH];
        int l;
        while ((l = instream.read(tmp)) != -1) {
            outstream.write(tmp, 0, l);
        }
        // Fix: java.util.zip.DataFormatException: stream error
        try {
            if (outstream instanceof GZIPOutputStream) {
                ((GZIPOutputStream) outstream).finish();
            }
            outstream.flush();
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public InputStream getContent() throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        writeTo(buf);
        return new ByteArrayInputStream(buf.toByteArray());
    }

    @Override
    public Header getContentEncoding() {
        return new BasicHeader("Content-Encoding", "gzip");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
