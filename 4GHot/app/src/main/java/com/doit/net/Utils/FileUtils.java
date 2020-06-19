package com.doit.net.Utils;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

/**
 * Author：Libin on 2020/5/18 18:58
 * Email：1993911441@qq.com
 * Describe：文件管理
 */
public class FileUtils {
    public final static String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/4GHotspot/";
    public final static String ROOT_DIRECTORY = "4GHotspot";
    private static FileUtils mInstance;

    private FileUtils() {
    }

    public static FileUtils getInstance() {
        if (mInstance == null) {
            synchronized (FileUtils.class) {
                if (mInstance == null) {
                    mInstance = new FileUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * @param filePath
     * @return 文件转字符串
     */
    public String fileToString(String filePath) {

        File file = new File(filePath);

        if (!file.exists()) {
            return "";
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) != -1) {
                bos.write(bytes, 0, length);
            }
            bos.close();
            fis.close();
            return bos.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    /**
     * @param str
     * @param filePath
     * @return 字符串转文件
     */
    public boolean stringToFile(String str, String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }

            byte[] bytes = str.getBytes();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * @param filePath  删除文件
     */
    public void deleteFile(String filePath){
        File file = new File(filePath);
        if (file.exists()){
            file.delete();
        }
    }
}
