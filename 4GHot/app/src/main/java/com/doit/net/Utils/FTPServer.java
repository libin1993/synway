package com.doit.net.Utils;

import android.content.Context;
import android.os.Environment;
//
import com.doit.net.Model.CacheManager;
import com.doit.net.Protocol.LTE_PT_PARAM;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
//
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
//
//
/**
 * Created by Zxc on 2018/10/22.
 */
public class FTPServer extends DefaultFtplet {
    private final int FTP_PORT = 2221;
    private final String DEFAULT_LISTENER = "default";
    // private final Logger LOG = LoggerFactory.getLogger(FTPServer.class);
    private static final List<Authority> ADMIN_AUTHORITIES;
    private static final int BYTES_PER_KB = 1024;
    public static String FTP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+"/4GHotspot/";
    private static final String DEFAULT_USER_DIR = FTP_DIR;

    public final static int MAX_CONCURRENT_LOGINS = 3;
    public final static int MAX_CONCURRENT_LOGINS_PER_IP = 3;

    private static FtpServer mFTPServer;
    private static UserManager mUserManager;
    private static FtpServerFactory mFTPServerFactory;
    private ListenerFactory mListenerFactor;

    static {
        // Admin Authorities
        ADMIN_AUTHORITIES = new ArrayList<Authority>();
        ADMIN_AUTHORITIES.add(new WritePermission());
        ADMIN_AUTHORITIES.add(new ConcurrentLoginPermission(MAX_CONCURRENT_LOGINS, MAX_CONCURRENT_LOGINS_PER_IP));
        ADMIN_AUTHORITIES.add(new TransferRatePermission(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    public void init() {
        mFTPServerFactory = new FtpServerFactory();
        mListenerFactor = new ListenerFactory();
        mListenerFactor.setPort(FTP_PORT);

        //anonymous login
        //seems no need
        //ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        //connectionConfigFactory.setAnonymousLoginEnabled(true);
        //mFTPServerFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());

        mFTPServerFactory.addListener(DEFAULT_LISTENER, mListenerFactor.createListener());
        mFTPServerFactory.getFtplets().put(DefaultFtplet.class.getName(), this);
        //mFTPServerFactory.getFtplets().put(FTPLetImpl.class.getAccount(), new FTPLetImpl());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(new File(FTP_DIR + "users.properties"));
        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
        mUserManager = userManagerFactory.createUserManager();
        mFTPServerFactory.setUserManager(mUserManager);

        try {
            this.createAdminUser();
            FTPServer.addUser("sanhui", "123456", 100000, 100000);
        } catch (FtpException e) {
            e.printStackTrace();
        }

        mFTPServer = mFTPServerFactory.createServer();
    }

    public void startFTPServer(){
        try {
            mFTPServer.start();
        } catch (FtpException e) {
            e.printStackTrace();
        }
    }

    private UserManager createAdminUser() throws FtpException {
        UserManager userManager = mFTPServerFactory.getUserManager();
        String adminName = userManager.getAdminName();

        if (!userManager.doesExist(adminName)) {
            BaseUser adminUser = new BaseUser();
            adminUser.setName(adminName);
            adminUser.setPassword(adminName);
            adminUser.setEnabled(true);
            adminUser.setAuthorities(ADMIN_AUTHORITIES);
            adminUser.setHomeDirectory(DEFAULT_USER_DIR);
            adminUser.setMaxIdleTime(0);
            userManager.save(adminUser);
        }

        return userManager;
    }

    public static void addUser(String username, String password, int uploadRateKB, int downloadRateKB) throws FtpException {

        BaseUser user = new BaseUser();
        user.setName(username);
        user.setPassword(password);
        user.setHomeDirectory(DEFAULT_USER_DIR);
        user.setEnabled(true);

        //BaseUser anonuser = new BaseUser();
        //anonuser = new BaseUser(user);
        //anonuser.setAccount("anonymous");

        List<Authority> list = new ArrayList<Authority>();
        list.add(new TransferRatePermission(downloadRateKB * BYTES_PER_KB, uploadRateKB * BYTES_PER_KB)); // 20KB
        list.add(new ConcurrentLoginPermission(MAX_CONCURRENT_LOGINS, MAX_CONCURRENT_LOGINS_PER_IP));
        list.add(new WritePermission());
        user.setAuthorities(list);

        mFTPServerFactory.getUserManager().save(user);
    }


    public static void restartFTP() throws FtpException {
        if (mFTPServer != null) {
            mFTPServer.stop();
            try {
                Thread.sleep(1000 * 3);
            } catch (InterruptedException e) {}
            mFTPServer.start();
        }
    }

    public void stopFTP(){
        if (mFTPServer != null) {
            mFTPServer.stop();
        }
    }

    public static void pauseFTP() throws FtpException {
        if (mFTPServer != null) {
            mFTPServer.suspend();
        }
    }

    public static void resumeFTP() throws FtpException {
        if (mFTPServer != null) {
            mFTPServer.resume();
        }
    }

    @Override
    public FtpletResult onLogin(FtpSession session, FtpRequest request) throws FtpException, IOException {
        //UtilBaseLog.printLog("FTP客户端登录");
        return super.onLogin(session, request);
    }

    @Override
    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        //request.getArgument()得到的文件名为./xxxxxxxxxxx.tmp,后面上传完成会自动修改为./xxxxxxxxxxx.txt，回调onRenameEnd
        //UtilBaseLog.printLog("一个文件上传:" + request.getArgument());
        return super.onUploadEnd(session, request);
    }

    @Override
    public FtpletResult onRenameEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        UtilBaseLog.printLog("一个文件名更改:" + request.getArgument());
        if (!CacheManager.currentWorkMode.equals("2")){  //管控模式忽略ftp的上报
            String uploadFileName = request.getArgument().replaceAll("./", "");
            LTE_PT_PARAM.processUeidRpt(FTP_DIR+uploadFileName);
        }

        return super.onRenameEnd(session, request);
    }

    public void copyConfigFile(int rid, String targetFile, Context context) {
        InputStream fin = context.getResources().openRawResource(rid);
        FileOutputStream fos = null;
        int length;
        try {
            fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            while ((length = fin.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}