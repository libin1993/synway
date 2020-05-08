package com.doit.net.Activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ConnectException;
import java.net.UnknownHostException;
import android.content.pm.PackageInfo;

import com.doit.net.Constant.SDConstant;
import com.doit.net.Constant.TestConstant;
import com.doit.net.Utils.DateUtil;
import com.doit.net.Utils.Logger;


/**
 * 应用程序异常类：用于捕获异常和提示错误信息
 * 
 * @author wiker
 */
public class GameException
        extends Exception
        implements UncaughtExceptionHandler {
    
    
    private static Logger log = Logger.getLogger(GameException.class);
    private static final long serialVersionUID = -5212543361506480888L;
    
    /** 定义异常类型 */
    public final static byte TYPE_NETWORK = 0x01;
    public final static byte TYPE_SOCKET = 0x02;
    public final static byte TYPE_HTTP_CODE = 0x03;
    public final static byte TYPE_HTTP_ERROR = 0x04;
    public final static byte TYPE_XML = 0x05;
    public final static byte TYPE_IO = 0x06;
    public final static byte TYPE_RUN = 0x07;
    public final static byte TYPE_JSON = 0x08;
    
    private static GameApplication gameApplication;
    
    private byte type;
    private int code;
    
    /** 系统默认的UncaughtException处理类 */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    
    private GameException() {
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    
    public static void setGameApplication(GameApplication context){
        gameApplication = context;
    }
    
    private GameException(byte type, int code, Exception excp) {
        super(excp);
        this.type = type;
        this.code = code;
        log.error("Excep Type:"+type+",Code:"+code, excp);
        excp.printStackTrace();
        postLog(excp);
    }
    
    public int getCode() {
        return this.code;
    }
    
    public int getType() {
        return this.type;
    }
    
    public void postLog(Throwable excp){
        if(!TestConstant.OPEN_LOG){
            return;
        }
        String errorlog = "errorlog.txt";
        String savePath = "";
        String logFilePath = "";
        FileWriter fw = null;
        PrintWriter pw = null;
        FileReader fr = null;
        try {
            // 判断是否挂载了SD卡
            if(SDConstant.getSDPath() == null){
                return;
            }
            savePath = SDConstant.getSDPath()+SDConstant.LOG_PATH;
            File file = new File(savePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            logFilePath = savePath + errorlog;
            // 没有挂载SD卡，无法写文件
            if (logFilePath == "") {
                return;
            }
            File logFile = new File(logFilePath);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            fw = new FileWriter(logFile, true);
            pw = new PrintWriter(fw);
            pw.println("--------------------" + DateUtil.getStrOfMs() + "---------------------");

            PackageInfo pinfo = gameApplication.getPackageInfo();
            pw.println("Version: " + pinfo.versionName + "(" + pinfo.versionCode + ")\n");
            pw.println("Android: " + android.os.Build.VERSION.RELEASE + "(" + android.os.Build.MODEL + ")\n");
            excp.printStackTrace(pw);
            pw.close();
            fw.close();
            
            if(gameApplication == null){
                return;
            }
            //if(GlobalVar.getGameBean().isUploadException() && gameApplication.isNetworkConnected()){

//                if(savePath.startsWith(gameApplication.getApplicationInfo().dataDir)){
//                    try {
//                        Runtime.getRuntime().exec("chmod 444 " + logFile);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                fr = new FileReader(logFile);
                BufferedReader br = new BufferedReader(fr);
                String s;
                StringBuffer sb = new StringBuffer();

                while ((s = br.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
                fr.close();
                //ApiClient.postException(gameApplication, sb.toString());
            //}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    public static GameException http(int code) {
        return new GameException(TYPE_HTTP_CODE, code, null);
    }
    
    public static GameException http(Exception e) {
        return new GameException(TYPE_HTTP_ERROR, 0, e);
    }
    
    public static GameException socket(Exception e) {
        return new GameException(TYPE_SOCKET, 0, e);
    }
    
    public static GameException json(Exception e) {
        return new GameException(TYPE_JSON, 0, e);
    }
    
    public static GameException io(Exception e) {
        if (e instanceof UnknownHostException || e instanceof ConnectException) {
            return new GameException(TYPE_NETWORK, 0, e);
        } else if (e instanceof IOException) {
            return new GameException(TYPE_IO, 0, e);
        }
        return run(e);
    }
    
    public static GameException xml(Exception e) {
        return new GameException(TYPE_XML, 0, e);
    }
    

    
    public static GameException run(Exception e) {
        return new GameException(TYPE_RUN, 0, e);
    }
    
    /**
     * 获取APP异常崩溃处理对象
     * 
     * @return
     */
    public static GameException getAppExceptionHandler() {
        return new GameException();
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        }
        
    }
    
    /**
     * 自定义异常处理:收集错误信息&发送错误报告
     * 
     * @param ex
     * @return true:处理了该异常信息;否则返回false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        ex.printStackTrace();
        postLog(ex);
        /**
        final Context context = GameManager.getAppManager().currentActivity();
        
        if (context == null) {
            return false;
        }
        
        
        final String crashReport = getCrashReport(context, ex);
        // 显示异常信息&发送报告
        new Thread() {
            public void run() {
                Looper.prepare();
                UIHelper.sendAppCrashReport(context, crashReport);
                Looper.loop();
            }
            
        }.start();
        **/
        return true;
    }
    
    /**
     * 获取APP崩溃异常报告
     * 
     * @param ex
     * @return
     */
//    private String getCrashReport(Throwable ex) {
//        PackageInfo pinfo = gameApplication.getPackageInfo();
//        StringBuffer exceptionStr = new StringBuffer();
//        exceptionStr.append("Version: " + pinfo.versionName + "(" + pinfo.versionCode + ")\n");
//        exceptionStr.append("Android: " + android.os.Build.VERSION.RELEASE + "(" + android.os.Build.MODEL + ")\n");
//        exceptionStr.append("Exception: " + ex.getMessage() + "\n");
//        StackTraceElement[] elements = ex.getStackTrace();
//        for (int i = 0; i < elements.length; i++) {
//            exceptionStr.append(elements[i].toString() + "\n");
//        }
//        return exceptionStr.toString();
//    }
}
