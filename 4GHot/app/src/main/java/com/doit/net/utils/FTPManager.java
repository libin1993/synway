package com.doit.net.utils;

import com.doit.net.model.CacheManager;
import com.doit.net.sockets.ServerSocketUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Created by Zxc on 2019/4/24.
 */

public class FTPManager {
    private static final int PORT = 21;
    private static final String USERNAME = "nodeb";
    private static final String PASSWORD = "nodeb";

    private static final String REMOTE_PATH = "/log/synway";
    private static final String REMOTE_PATH_BACKUP = "/log/etc";


    private static FTPManager mInstance = null;
    private FTPClient ftpClient;

    private FTPManager() {
        ftpClient = new FTPClient();
    }

    public static FTPManager getInstance() {
        if (mInstance == null) {
            mInstance = new FTPManager();
        }
        return mInstance;
    }


    // 连接到ftp服务器
    public synchronized boolean connect() throws Exception {
        boolean bool = false;

        try {
            if (ftpClient.isConnected()) {//判断是否已登陆
                ftpClient.disconnect();
            }
            ftpClient.setDataTimeout(5000);//设置连接超时时间
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.connect(CacheManager.DEVICE_IP, PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                bool = true;
                boolean changeDirectory = checkRemoteDir();
                LogUtils.log("FTP连接成功，切换目录结果：" + changeDirectory);
            }else {
                LogUtils.log("FTP连接失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.log("FTP连接失败："+e.getMessage());
        }

        return bool;
    }


    /**
     * @return
     * @throws Exception
     * FTPClient无创建文件夹权限，只能使用设备已存在目录；由于有的设备不存在/log/synway目录，
     * 可先切换到/log/synway目录，若文件夹不存在，再切换到/log/etc目录
     */
    // 创建文件夹
    public boolean checkRemoteDir() throws Exception {
        if (ftpClient.changeWorkingDirectory("/")) {
            LogUtils.log("切换到根目录");

        }

        if (ftpClient.changeWorkingDirectory(REMOTE_PATH)) {
            LogUtils.log("切换到工作目录");
            return true;
        }

        if (ftpClient.changeWorkingDirectory(REMOTE_PATH_BACKUP)){
            LogUtils.log("切换到备用目录");
            return true;
        }

        return false;
    }


    // 实现上传文件的功能
    public synchronized boolean uploadFile(boolean isAppend,String path, String updateFileName)
            throws Exception {
        String localPath = path + updateFileName;
        // 上传文件之前，先判断本地文件是否存在
        File localFile = new File(localPath);
        if (!localFile.exists()) {
            LogUtils.log("上传文件，本地文件不存在");
            return false;
        }

        String fileName = localFile.getName();

        // 如果本地文件存在，服务器文件也在，上传文件，这个方法中也包括了断点上传
        long localSize = localFile.length(); // 本地文件的长度
        FTPFile[] files = ftpClient.listFiles(fileName);


        long serverSize = 0;
        if (files.length != 0) {
            if (isAppend){
                serverSize = files[0].getSize(); // 服务器文件的长度
            }else {
                LogUtils.log("非断点续传，删除文件");
                ftpClient.deleteFile(fileName);

            }
        }

        if (localSize <= serverSize) {
            if (ftpClient.deleteFile(fileName)) {
                LogUtils.log("服务器文件存在,删除文件,开始重新上传");
                serverSize = 0;
            }
        }
        RandomAccessFile raf = new RandomAccessFile(localFile, "r");

        // 好了，正式开始上传文件
        ftpClient.enterLocalPassiveMode();
        ftpClient.setRestartOffset(serverSize);
        raf.seek(serverSize);

        OutputStream output = ftpClient.appendFileStream(fileName);
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = raf.read(b)) != -1) {
            output.write(b, 0, length);

        }
        output.flush();
        output.close();
        raf.close();

        if (ftpClient.completePendingCommand()) {
            LogUtils.log("文件上传成功");
            return true;
        } else {
            LogUtils.log("文件上传失败");
            return false;
        }
    }


    // 实现下载文件功能，可实现断点下载
    public synchronized boolean downloadFile(String localPath, String remoteFileName) {

        // 先判断服务器文件是否存在
        try {
            ftpClient.enterLocalPassiveMode();
            FTPFile[] files = ftpClient.listFiles(remoteFileName);
            if (files.length == 0) {
                LogUtils.log("服务器文件不存在");
                return false;
            }

            LogUtils.log("远程文件存在,名字为：" + files[0].getName());
            localPath = localPath + remoteFileName;
            // 接着判断下载的文件是否能断点下载
            long serverSize = files[0].getSize(); // 获取远程文件的长度
            LogUtils.log("服务器文件大小为：" + serverSize);
            if (serverSize <= 0) {
                return false;
            }

            File localFile = new File(localPath);
            if (localFile.exists()) {
                localFile.delete();
                LogUtils.log("本地文件存在，删除成功，开始重新下载");
            }

            // 开始准备下载文件
            OutputStream out = new FileOutputStream(new File(localPath));
            InputStream input = ftpClient.retrieveFileStream(remoteFileName);
            byte[] b = new byte[1024];
            int length = 0;
            while ((length = input.read(b)) != -1) {
                out.write(b, 0, length);
            }
            out.flush();
            out.close();
            input.close();
            // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
            if (ftpClient.completePendingCommand()) {
                LogUtils.log("文件下载成功");
                return true;
            } else {
                LogUtils.log("文件下载失败");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.log("文件下载失败"+e.getMessage());
        }
        return false;
    }

    public FTPFile[] listFiles() {
        ftpClient.enterLocalPassiveMode();
        FTPFile[] files = new FTPFile[0];
        try {
            files = ftpClient.listFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files == null || files.length == 0) {
            return null;
        }

        return files;
    }

    // 如果ftp上传打开，就关闭掉
    public void closeFTP() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
