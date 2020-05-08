package com.doit.net.Activity;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.doit.net.Constant.SDConstant;
import com.doit.net.Constant.TestConstant;
import com.doit.net.Event.UIEventManager;
import com.doit.net.Model.PrefManage;
import com.doit.net.Utils.Logger;
import com.doit.net.Utils.StringUtils;
import com.doit.net.Utils.UtilCipher;

import org.xutils.x;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class  GameApplication
        extends Application {
    
    
    private static Logger log = Logger.getLogger(GameApplication.class);
    
    /** 中国移动 */
    public static final int OP_CMCC = 1;
    /** 中国联通 */
    public static final int OP_CU = 2;
    /** 中国电信 */
    public static final int OP_CT = 3;
    
    public static final String NETTYPE_WIFI = "WIFI";
    public static final String NETTYPE_CMWAP = "CMWAP";
    public static final String NETTYPE_CMNET = "CMNET";
    
    /** 没有网络 */
    public static final int NETWORKTYPE_INVALID = 0;
    /** wap网络 */
    public static final int NETWORKTYPE_WAP = 1;
    /** 2G网络 */
    public static final int NETWORKTYPE_2G = 2;
    /** 3G和3G以上网络，或统称为快速网络 */
    public static final int NETWORKTYPE_3G = 3;
    /** wifi网络 */
    public static final int NETWORKTYPE_WIFI = 4;
    
    public static final int NETWORKTYPE_4G = 5;
    
    public static final int CACHE_TIME = 60 * 60000;// 缓存失效时间
    
    public Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();
    
    public static boolean GAME_SPLASH = false;

    public static Context appContext;
    
    @Override
    public void onCreate() {
        super.onCreate();
        configLog4j();
        appContext = this;
        // 注册App异常崩溃处理器
        x.Ext.init(this);
        log.info("Application start");
//        PayInf.initNoAct(this);
//        GlobalVar.init(this);
        if(TestConstant.OPEN_LOG){
            GameException.setGameApplication(this);
            Thread.setDefaultUncaughtExceptionHandler(GameException.getAppExceptionHandler());
        }
        PrefManage.init(this);
        UtilCipher.init("");

        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            GAME_SPLASH = appInfo.metaData.getBoolean("GAME_SPLASH");

            UIEventManager.appInit(getApplicationContext());

            TypefaceProvider.registerDefaultIconSets();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        
//        try {
////          PayInf.initNoAct(this);
//            Util.setGlobalContext(this);
//            String mcc_mnc=Env.getInstance(this).imsi.substring(0, 5);
//            if("46001".equals(mcc_mnc) || "46006".equals(mcc_mnc)){
//                Utils.getInstances().initSDK (this,  
//                        new UnipayPayResultListener() {
//                            @Override
//                            public void PayResult(String arg0, int arg1, int arg2, String arg3) {
//                            }
//                        });
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //Env.getInstance(GameApplication.this);
        //initNetData();
    }


    private void configLog4j(){

    }

    
    /**
     * 检测当前系统声音是否为正常模式
     * 
     * @return
     */
    public boolean isAudioNormal() {
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }
    
    /**
     * 检测网络是否可用
     * 
     * @return
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }
    
    /**
     * 获取应用名称
     * 
     * @return
     */
    public String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }
    
    /**
     * 获取当前网络类型
     * 
     * @return 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
     */
    public String getNetWorkTypeStr() {
        String netType = "";
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!StringUtils.isEmpty(extraInfo)) {
                if ("cmnet".equalsIgnoreCase(extraInfo)) {
                    netType = NETTYPE_CMNET;
                } else {
                    netType = NETTYPE_CMWAP;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NETTYPE_WIFI;
        }
        return netType;
    }
    
    /**
     * 获取meta-data中的数据
     * 
     * @param key
     * @return
     * @throws GameException
     */
    public String getMetaDataStr(String key)
            throws GameException {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            return StringUtils.defaultString(appInfo.metaData.getString(key));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * 获取DeviceId
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public String getDeviceId() {
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return TelephonyMgr.getDeviceId();
    }
    
    /**
     * 获取SVID,PARAM_SVID
     * 
     * @return
     */
    @SuppressLint("DefaultLocale")
    public String getSvid() {
        String svid = null;
        int nval = 0;
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            nval = ai.metaData.getInt("PARAM_SVID");
            svid = String.format("%02d", nval);
        } catch (Exception e) {
            svid = "01";
        }
        return svid;
    }
    
    /**
     * 获取VTID，PARAM_VTID
     * 
     * @return
     */
    public String getVtid() {
        String vtid = null;
        int nval = 0;
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            nval = ai.metaData.getInt("PARAM_VTID");
            vtid = String.valueOf(nval);
        } catch (Exception e) {
            vtid = "9516";
        }
        return vtid;
    }
    
    /**
     * 获取meta-data中的数据
     * 
     * @param key
     * @return
     * @throws GameException
     */
    public int getMetaDataInt(String key)
            throws GameException {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            return appInfo.metaData.getInt(key);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 获取运营商类型
     * 
     * @return
     */
    @SuppressLint("MissingPermission")
    public int getOpType() {
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        /**
         * 获取SIM卡的IMSI码
         * SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber Identification Number）是区别移动用户的标志，
         * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成，
         * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成，
         * 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。
         * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可
         */
         String imsi = telManager.getSubscriberId();
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007")) {// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
                // 中国移动
                return OP_CMCC;
            } else if (imsi.startsWith("46001") || imsi.startsWith("46006")) {
                // 中国联通
                return OP_CU;
            } else if (imsi.startsWith("46003") || imsi.startsWith("46005")) {
                // 中国电信
                return OP_CT;
            }
        }
        return -1;
    }

    public static String getOperatorName(String plmn){
        if(plmn == null){
            return "";
        }
        if (plmn.startsWith("46000") || plmn.startsWith("46002") || plmn.startsWith("46007")) {// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
            // 中国移动
            return "中国移动";
        } else if (plmn.startsWith("46001") || plmn.startsWith("46006")) {
            // 中国联通
            return "中国联通";
        } else if (plmn.startsWith("46003") || plmn.startsWith("46005") || plmn.startsWith("46011")) {
            // 中国电信
            return "中国电信";
        }
        return "";
    }
    
    /**
     * 是否是高速网络
     * 
     * @param context
     * @return
     */
    public boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
        }
    }
    
    /**
     * 获取网络类型
     *
     * @param context
     * @return
     */
    public static int getNetworkClass(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
        return NETWORKTYPE_2G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
        return NETWORKTYPE_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
        return NETWORKTYPE_4G;
            default:
        return NETWORKTYPE_INVALID;
        }
    }
    
    /**
     * 获取网络状态，wifi,wap,2g,3g.
     * 
     *            上下文
     * @return int 网络状态 {@link #NETWORKTYPE_2G},{@link #NETWORKTYPE_3G}, {@link #NETWORKTYPE_INVALID},
     *         {@link #NETWORKTYPE_WAP}*
     *         <p>
     *         {@link #NETWORKTYPE_WIFI}
     */
    public int getNetWorkType() {
        int mNetWorkType = 0;
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            
            if (type.equalsIgnoreCase("WIFI")) {
                mNetWorkType = NETWORKTYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                String proxyHost = android.net.Proxy.getDefaultHost();
                
                mNetWorkType = TextUtils.isEmpty(proxyHost) ? getNetworkClass(this) : NETWORKTYPE_WAP;
            }
        } else {
            mNetWorkType = NETWORKTYPE_INVALID;
        }
        
        return mNetWorkType;
        //return NETWORKTYPE_2G;
    }
    
    /**
     * 判断当前版本是否兼容目标版本的方法
     * 
     * @param VersionCode
     * @return
     */
    public boolean isMethodsCompat(int VersionCode) {
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        return currentVersion >= VersionCode;
    }
    
    /**
     * 获取App安装包信息
     * 
     * @return
     */
    public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
        }
        return versionName;
    }

    /**
     * 判断缓存是否存在
     * 
     * @param cachefile
     * @return
     */
    private boolean isExistDataCache(String cachefile) {
        boolean exist = false;
        File data = getFileStreamPath(cachefile);
        if (data.exists())
            exist = true;
        return exist;
    }
    
    /**
     * 判断缓存是否失效
     * 
     * @param cachefile
     * @return
     */
    public boolean isCacheDataFailure(String cachefile) {
        boolean failure = false;
        File data = getFileStreamPath(cachefile);
        if (data.exists() && (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
            failure = true;
        else if (!data.exists())
            failure = true;
        return failure;
    }
    
    /**
     * 清除app缓存
     */
    public void clearAppCache() {
        // 清除webview缓存
        // File file = CacheManager.getCacheFileBaseDir();
        // if (file != null && file.exists() && file.isDirectory()) {
        // for (File item : file.listFiles()) {
        // item.delete();
        // }
        // file.delete();
        // }
        deleteDatabase("webview.db");
        deleteDatabase("webview.db-shm");
        deleteDatabase("webview.db-wal");
        deleteDatabase("webviewCache.db");
        deleteDatabase("webviewCache.db-shm");
        deleteDatabase("webviewCache.db-wal");
        // 清除数据缓存
        clearCacheFolder(getFilesDir(), System.currentTimeMillis());
        clearCacheFolder(getCacheDir(), System.currentTimeMillis());
        // 2.2版本才有将应用缓存转移到sd卡的功能
    }
    
    /**
     * 清除缓存目录
     * 
     * @param dir
     *            目录
     * @param curTime
     *            当前系统时间
     * @return
     */
    private int clearCacheFolder(File dir, long curTime) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, curTime);
                    }
                    if (child.lastModified() < curTime) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }
    
    /**
     * 将对象保存到内存缓存中
     * 
     * @param key
     * @param value
     */
    public void setMemCache(String key, Object value) {
        memCacheRegion.put(key, value);
    }
    
    /**
     * 从内存缓存中获取对象
     * 
     * @param key
     * @return
     */
    public Object getMemCache(String key) {
        return memCacheRegion.get(key);
    }
    
    /**
     * 保存磁盘缓存
     * 
     * @param key
     * @param value
     * @throws IOException
     */
    public void setDiskCache(String key, String value)
            throws IOException {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(SDConstant.CACHE_PATH + "cache_" + key + ".data", Context.MODE_PRIVATE);
            fos.write(value.getBytes());
            fos.flush();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * 获取磁盘缓存数据
     * 
     * @param key
     * @return
     * @throws IOException
     */
    public String getDiskCache(String key)
            throws IOException {
        FileInputStream fis = null;
        try {
            fis = openFileInput(SDConstant.CACHE_PATH + "cache_" + key + ".data");
            byte[] datas = new byte[fis.available()];
            fis.read(datas);
            return new String(datas);
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * 保存对象
     * 
     * @param ser
     * @param file
     * @throws IOException
     */
    public boolean saveObject(Serializable ser, String file) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = openFileOutput(file, MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * 读取对象
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public Serializable readObject(String file) {
        if (!isExistDataCache(file))
            return null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = openFileInput(file);
            ois = new ObjectInputStream(fis);
            return (Serializable) ois.readObject();
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
            // 反序列化失败 - 删除缓存文件
            if (e instanceof InvalidClassException) {
                File data = getFileStreamPath(file);
                data.delete();
            }
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
            }
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return null;
    }
    
}
