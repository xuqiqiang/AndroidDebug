package com.snailstudio.xsdk.debug.serv.terminal;

import android.os.ParcelFileDescriptor;

import com.snailstudio.xsdk.debug.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 终端模拟器
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class TerminalHelper {

    private OnReaderListener mOnReaderListener;
    private ParcelFileDescriptor mTermFd;
    private OutputStream mTermOut;
    private InputStream mTermIn;
    private int mProcId = -999;
    private boolean isRunning;

    public void setOnReaderListener(OnReaderListener listner) {
        mOnReaderListener = listner;
    }

    public synchronized void start() throws IOException {
        mTermFd = ParcelFileDescriptor.open(new File("/dev/ptmx"), ParcelFileDescriptor.MODE_READ_WRITE);
        mTermOut = new ParcelFileDescriptor.AutoCloseOutputStream(mTermFd);
        mTermIn = new ParcelFileDescriptor.AutoCloseInputStream(mTermFd);

        TermWebSocket.mTermOut = mTermOut;

        mProcId = TermExec.createSubprocess(mTermFd, "/system/bin/sh",
                new String[]{"/system/bin/sh", "-"},
                new String[]{"TERM=screen",
                        "PATH=/vendor/bin:/system/bin:/system/xbin",
                        "HOME=/data/data/com.dftc.phddns/app_HOME"});

        Thread mWatcherThread = new Thread() {
            @Override
            public void run() {
                LogUtils.i("waiting for: " + mProcId);
                int result = TermExec.waitFor(mProcId);
                LogUtils.i("Subprocess exited: " + result);
                onExit(result);
            }
        };
        mWatcherThread.setName("Process watcher");
        mWatcherThread.start();

        // For test
        final Thread mWriterThread = new Thread() {
            private byte[] mBuffer = new byte[4096];

            @Override
            public void run() {
                LogUtils.d("mWriterThread start");
                writeToOutput();
            }

            private void writeToOutput() {

                String cmd = "su\t";

                byte[] buffer = {108, 115, 13};
//                buffer = cmd.getBytes();
                buffer[buffer.length - 1] = 13;
                for (int i = 0; i < buffer.length; i++) {
                    LogUtils.d("write b : " + buffer[i]);
                }
                int bytesToWrite = buffer.length;
                try {
//                    writeQueue.read(buffer, 0, bytesToWrite);
                    mTermOut.write(buffer, 0, bytesToWrite);
                    mTermOut.flush();
                } catch (IOException e) {
                    // Ignore exception
                    // We don't really care if the receiver isn't listening.
                    // We just make a best effort to answer the query.
                    e.printStackTrace();
                }
                LogUtils.d("mWriterThread finish");
            }
        };

        final Thread mReaderThread = new Thread() {
            private byte[] mBuffer = new byte[4096];

            @Override
            public void run() {
                try {
                    LogUtils.d("mReaderThread start");
                    while (true) {
                        int read = mTermIn.read(mBuffer);
                        if (read == -1) {
                            // EOF -- process exited
                            LogUtils.e("mReaderThread EOF -- process exited");
                            break;
                        }
                        LogUtils.d("read : " + read);
                        if (mOnReaderListener != null)
                            mOnReaderListener.onReader(mBuffer, read);

                        LogUtils.d("read:" + new String(mBuffer, "UTF-8"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                onExit(0);
//                if (exitOnEOF) mMsgHandler.sendMessage(mMsgHandler.obtainMessage(EOF));
            }
        };

        mReaderThread.start();
        isRunning = true;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    public synchronized void onExit(int result) {
        isRunning = false;
        if (mProcId != -999) {
            TermExec.sendSignal(-mProcId, 1);
            mProcId = -999;
        }

        try {
            if (mTermFd != null) {
                mTermFd.close();
                mTermFd = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (mTermIn != null) {
                mTermIn.close();
                mTermIn = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (mTermOut != null) {
                mTermOut.close();
                mTermOut = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public interface OnReaderListener {
        void onReader(byte[] buffer, int size);
    }
}
