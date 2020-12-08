package com.doit.net.utils;

import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Author：Libin on 2020/6/18 16:17
 * Email：1993911441@qq.com
 * Describe：
 */
public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final static String LOG_PATH = FileUtils.ROOT_PATH+"CrashLog/";
    private Thread.UncaughtExceptionHandler mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String stackTraceInfo = DateUtils.getCurrentDate()+":"+getStackTraceInfo(e)+"\r\n\r\n\r\n";
        LogUtils.log("stackTraceInfo:"+ stackTraceInfo);
        saveThrowableMessage(stackTraceInfo);
        mDefaultHandler.uncaughtException(t, e);
    }
    /**
     * 获取错误的信息
     *
     * @param throwable
     * @return
     */
    private String getStackTraceInfo(final Throwable throwable) {
        PrintWriter pw = null;
        Writer writer = new StringWriter();
        try {
            pw = new PrintWriter(writer);
            throwable.printStackTrace(pw);
        } catch (Exception e) {
            return "";
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        return writer.toString();
    }



    private void saveThrowableMessage(String errorMessage) {
        if (TextUtils.isEmpty(errorMessage)) {
            return;
        }
        File file = new File(LOG_PATH);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (mkdirs) {
                writeStringToFile(errorMessage, file);
            }
        } else {
            writeStringToFile(errorMessage, file);
        }
    }

    private void writeStringToFile(final String errorMessage, File parentFile) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream outputStream = null;
                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(errorMessage.getBytes());
                    File file = new File(parentFile, DateUtils.getCurrentDay() + ".txt");
                    outputStream = new FileOutputStream(file,true);
                    int len = 0;
                    byte[] bytes = new byte[1024];
                    while ((len = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, len);
                    }
                    outputStream.flush();
                    outputStream.close();
                    LogUtils.log("程序出异常了,写入本地文件成功：" + file.getAbsolutePath());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
