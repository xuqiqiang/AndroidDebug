package com.snailstudio.xsdk.debug.serv.req.database;

import java.io.UnsupportedEncodingException;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class ConverterUtils {

    private static final int MAX_BLOB_LENGTH = 512;

    private static final String UNKNOWN_BLOB_LABEL = "{blob}";

    private ConverterUtils() {
        // This class in not publicly instantiable
    }

    public static String blobToString(byte[] blob) {
        if (blob.length <= MAX_BLOB_LENGTH) {
            if (fastIsAscii(blob)) {
                try {
                    return new String(blob, "US-ASCII");
                } catch (UnsupportedEncodingException ignored) {

                }
            }
        }
        return UNKNOWN_BLOB_LABEL;
    }

    public static boolean fastIsAscii(byte[] blob) {
        for (byte b : blob) {
            if ((b & ~0x7f) != 0) {
                return false;
            }
        }
        return true;
    }

}
