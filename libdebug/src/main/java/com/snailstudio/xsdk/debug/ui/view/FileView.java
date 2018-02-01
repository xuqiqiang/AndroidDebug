package com.snailstudio.xsdk.debug.ui.view;

import com.snailstudio.xsdk.debug.Constants.Config;
import com.snailstudio.xsdk.debug.serv.GzipFilter;
import com.snailstudio.xsdk.debug.serv.entity.GzipFileEntity;
import com.snailstudio.xsdk.debug.serv.support.GzipUtil;
import com.snailstudio.xsdk.debug.serv.support.MIME;
import com.snailstudio.xsdk.debug.utils.LogUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.entity.FileEntity;

import java.io.File;
import java.io.IOException;

/**
 * 文件视图渲染
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class FileView extends BaseView<File, String> {

    static final String TAG = "FileView";

    /**
     * @param contentType 文件响应类型
     * @details contentType为null时，默认通过{@link MIME#getMimeType(File)}获取且charset为{@link Config#ENCODING}
     * @see BaseView#render(Object, Object)
     */
    @Override
    public HttpEntity render(HttpRequest request, final File file, String contentType)
            throws IOException {
        if (contentType == null) {
            String mine = MIME.getFromFile(file);
            contentType = null == mine ? "charset=" + Config.ENCODING : mine + ";charset="
                    + Config.ENCODING;
        }
        if (Config.USE_GZIP && GzipUtil.getSingleton().isGZipSupported(request)
                && GzipFilter.isGzipFile(file)) {
            if (Config.USE_FILE_CACHE) {
                File cacheFile = new File(Config.FILE_CACHE_DIR, file.getName() + Config.EXT_GZIP);
                if (cacheFile.exists()) {
                    LogUtils.dLog(TAG, "Read from cache " + cacheFile);
                } else {
                    GzipUtil.getSingleton().gzip(file, cacheFile);
                    LogUtils.dLog(TAG, "Cache to " + cacheFile + " and read it.");
                }
                return new GzipFileEntity(cacheFile, contentType, true);
            } else {
                LogUtils.dLog(TAG, "Directly return gzip stream for " + file);
                return new GzipFileEntity(file, contentType, false);
            }
        }
        return new FileEntity(file, contentType);
    }

}
