package com.doit.net.utils;

import com.doit.net.bean.BlackBoxBean;

import org.xutils.ex.DbException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Zxc on 2018/11/28.
 */

public class BlackBoxManger {
    public final static String LOGIN = "账户登录";
    public final static String FIRST_HEARTBEAT = "收到第一个心跳包";
    public final static String CHANGE_DETTECT_OPERATE = "切换采集制式为:";
    public final static String OPEN_ALL_RF = "打开所有射频...";
    public final static String CLOSE_ALL_RF = "关闭所有射频...";
    public final static String SET_CELL_CONFIG = "设置小区信息:";
    public final static String SET_CHANNEL_CONFIG = "设置通道信息:";
    public final static String CHANNEL_TAG = "更新了TAC...";
    public final static String CHANGE_BAND = "切换BAND到:";
    public final static String REBOOT_DEVICE = "软重启了设备...";
    public final static String SET_ALL_POWER = "设置了总功率为:";
    public final static String ADD_NAMELIST = "添加了一个黑名单:";
    public final static String DELETE_NAMELIST = "删除了一个黑名单:";
    public final static String MODIFY_NAMELIST = "修改了一个黑名单信息，";
    public final static String ADD_WHITE_LIST = "添加了一个白名单:";
    public final static String DELETE_WHITE_LIST = "删除了一个白名单:";
    public final static String MODIFY_WHITE_LIST = "修改了一个白名单:";
    public final static String TIME_PERIOD_COLLIDE = "进行了一次时间段碰撞...";
    public final static String TIME_POINT_COLLIDE = "进行了一次打点碰撞...";
    public final static String GET_PARTNER = "进行了一次伴随分析...";
    public final static String START_FOLLOWING = "开始跟踪...";
    public final static String PAUSE_FOLLOWING = "暂停跟踪...";
    public final static String RESTART_FOLLOWING = "重新开始跟踪...";
    public final static String CLEAN_HISTORY_DATA = "清除历史上号数据，时间段为：";
    public final static String START_LOCALTE_FROM_NAMELIST = "添加号码开始搜寻，号码为:";
    public final static String START_LOCALTE = "开始搜寻，号码为:";
    public final static String STOP_LOCALTE = "停止搜寻，号码为:";
    public final static String EXPORT_HISTORT_DATA = "导出历史数据,文件名为:";
    public final static String EXPORT_NAMELIST = "导出黑名单,文件名为:";
    public final static String EXPORT_WHITELIST = "导出白名单,文件名为:";
    public final static String CLEAR_NAMELIST = "清空黑名单";
    public final static String CLEAR_WHITELIST = "清空白名单";
    public final static String IMPORT_NAMELIST = "导入黑名单,文件名为:";
    public final static String IMPORT_WHITELIST = "导入白名单,文件名为:";
    public final static String ADD_USER = "添加了一个用户:";
    public final static String DELTE_USER = "删除了一个用户:";
    public final static String MODIFY_USER = "修改一个用户信息，";
    public final static String MODIFY_ADMIN_ACCOUNT = "修改了管理员账户为:";
    public final static String EXPORT_BLACKBOX = "导出黑匣子信息，文件为:";

    private static String currentAccount = "";
    public static final String LOCAL_FTP_BLX_PATH = FileUtils.ROOT_PATH+"FtpBlx/";
    public static String currentBlxFileName;

    public static void setCurrentAccount(String account){
        currentAccount = account;
    }

    public static void recordOperation(String operation){
        //不对超级账户做记录

        if (VersionManage.isArmyVer()){
            return;
        }

        if (AccountManage.getCurrentPerLevel() > AccountManage.PERMISSION_LEVEL2)
            return;

        recordOprToFile(currentAccount, operation);
        LogUtils.log("黑匣子:"+currentAccount+"，"+operation);
    }

    public static void saveOperationToDB(String operation){
        try {
            UCSIDBManager.getDbManager().save(new BlackBoxBean(operation.split(",")[0],
                    operation.split(",")[1],
                    DateUtils.convert2long(operation.substring(operation.lastIndexOf(",")+1), DateUtils.LOCAL_DATE)));
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private static void startUploadBlxLoop(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtils.log("黑匣子上传周期");
                uploadCurrentBlxFile();
            }
        },5000, 3*60*1000);
    }


    /**********  一下为黑匣子文件和设备上下载相关 *************/
    public static void initBlx(){
        try {
            //1.删除数据库里有的
            UCSIDBManager.getDbManager().delete(BlackBoxBean.class);

            //2.下载当天的（如果有的话），用于接着记录
            checkBlackBoxFile();
            downloadIntradayBlx();

            //3.开始循环上传
            startUploadBlxLoop();
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void recordOprToFile(String account, String content){
        try {
            if (!isTheSameDay(new Date(), new SimpleDateFormat("yyyy-MM-dd").parse(currentBlxFileName.split("\\.")[0]))){
                LogUtils.log("重新生成新的黑匣子文件");
                uploadCurrentBlxFile();
                checkBlackBoxFile();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String saveFileName = LOCAL_FTP_BLX_PATH +currentBlxFileName;
        LogUtils.log("写入黑匣子："+account+":"+content+","+saveFileName);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFileName,true)));
            bufferedWriter.write(account+","+content+","+ DateUtils.convert2String(new Date().getTime(), DateUtils.LOCAL_DATE)+"\n");
            bufferedWriter.flush();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(bufferedWriter != null){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {e.printStackTrace();}
            }
        }
    }

    private static void deleteFile(String path, String filename){
        File file=new File(path+filename);
        if(file.exists()&&file.isFile())
            file.delete();
    }

    private static boolean isTheSameDay(Date d1,Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                && (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH))
                && (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH));
    }

    public static void checkBlackBoxFile() {
        String logDir = LOCAL_FTP_BLX_PATH;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String fileName = df.format(new Date())+".blx";// new Date()为获取当前系统时间
        // 以第一次启动的日期做为文件名，如果没有则创建，否则追加
        File dir = new File(logDir);
        if (!dir.exists() && !dir.isDirectory()) {
            //UtilBaseLog.printLog("创建文件夹");
            dir.mkdir();
        }

        String filePath = logDir+fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        currentBlxFileName = fileName;
    }

    public static void uploadCurrentBlxFile() {
        if (!NetWorkUtils.getNetworkState()){
            return;
        }


        Thread uploadCurrentThread = new Thread() {
            public void run() {
                try {
                    FTPManager.getInstance().connect();
                    FTPManager.getInstance().uploadFile(true,LOCAL_FTP_BLX_PATH, currentBlxFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        uploadCurrentThread.start();
        try {
            uploadCurrentThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void downloadIntradayBlx() {
        if (!NetWorkUtils.getNetworkState()){
            return;
        }

        Thread downloadIntradayBlxThread = new Thread() {
            public void run() {
                try {
                    if (FTPManager.getInstance().connect()) {
                       FTPManager.getInstance().downloadFile(LOCAL_FTP_BLX_PATH, currentBlxFileName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.log("FTP异常："+e.toString());
                }
            }
        };

        downloadIntradayBlxThread.start();
    }


//    public static void clearBlxData(){
//        File file = new File(LOCAL_FTP_BLX_PATH);
//        if(!file.exists()){//判断是否待删除目录是否存在
//            return;
//        }
//
//        String[] content = file.list();//取得当前目录下所有文件和文件夹
//        for(String name : content){
//            File temp = new File(LOCAL_FTP_BLX_PATH, name);
//            temp.delete();
//        }
//    }
}
