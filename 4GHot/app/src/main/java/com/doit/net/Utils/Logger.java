package com.doit.net.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import android.os.Environment;
import android.util.Log;


/**
 * 日志类
 * 
 * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
 * @version 2012-5-23 下午3:57:25
 */
public class Logger {
    
    // 是否开启日志
    public static boolean logFlag = true;
    
    // 日志级别
    private final static int logLevel = Log.VERBOSE;
    private static Hashtable<String, Logger> sLoggerTable = new Hashtable<String, Logger>();
    private String mClassName;
    
    private Logger(String name) {
        mClassName = name;
    }
    
    /**
     * @param className
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Logger getLogger(Class c) {
        Logger classLogger = (Logger) sLoggerTable.get(c.getName());
        if (classLogger == null) {
            classLogger = new Logger(c.getName());
            sLoggerTable.put(c.getName(), classLogger);
        }

        return classLogger;
    }
    
    /**
     * Get The Current Function Name
     * 
     * @return
     */
    private String getFunctionName() {
        if(true){
            return "";
        }
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            return "[" + Thread.currentThread().getName() + "] - [" + mClassName + "." + st.getMethodName() + "("
                    + st.getFileName() + ":" + st.getLineNumber() + ")]";
        }
        return null;
    }
    
    /**
     * The Log Level:v
     * 
     * @param str
     */
    public void trace(String msg) {
        if (logFlag) {
            if (logLevel <= Log.VERBOSE) {
                String name = getFunctionName();
                String curlog = msg;
                if (name != null) {
                    Log.v(TAG.TAG_DSHINE, name + " - [" + msg + "]");
                    saveLog(TAG.TAG_DSHINE,name + " - [" + msg + "]");
                    curlog = name + " - [" + msg + "]"; 
                } else {
                    Log.i(TAG.TAG_DSHINE, msg);
                    saveLog(TAG.TAG_DSHINE,msg);
                    curlog = msg;
                }
            }
        }
    }
    
    /**
     * The Log Level:i
     * 
     * @param str
     */
    public void trace(String myTag, String msg) {
        if (logFlag) {
            if (logLevel <= Log.VERBOSE) {
                String name = getFunctionName();
                if (name != null) {
                    Log.v(myTag,msg);
                } else {
                    Log.v(myTag, msg);
                }
                saveLog(myTag,msg);
            }
        }
    }
    
    /**
     * The Log Level:i
     * 
     * @param str
     */
    public void info(String msg) {
        if (logFlag) {
            if (logLevel <= Log.INFO) {
                String name = getFunctionName();
                String curlog = msg;
                if (name != null) {
                    Log.i(TAG.TAG_DSHINE, name + " - [" + msg + "]");
                    saveLog(TAG.TAG_DSHINE,name + " - [" + msg + "]");
                    curlog = name + " - [" + msg + "]";
                } else {
                    Log.i(TAG.TAG_DSHINE, msg);
                    saveLog(TAG.TAG_DSHINE,msg);
                    curlog = msg;
                }
                
            }
        }
    }
    
    /**
     * The Log Level:i
     * 
     * @param str
     */
    public void info(String myTag, String msg) {
        if (logFlag) {
            if (logLevel <= Log.INFO) {
                String name = getFunctionName();
                if (name != null) {
                    Log.i(myTag,msg);
                } else {
                    Log.i(myTag, msg);
                }
                //saveLog(myTag,msg);
            }
        }
    }
    
    /**
     * The Log Level:d
     * 
     * @param str
     */
    public void debug(String msg) {
        if (logFlag) {
            if (logLevel <= Log.DEBUG) {
                String name = getFunctionName();
                String curlog = msg;
                if (name != null) {
                    Log.d(TAG.TAG_DSHINE, name + " - [" + msg + "]");
                    saveLog(TAG.TAG_DSHINE,name + " - [" + msg + "]");
                    curlog = name + " - [" + msg + "]";
                } else {
                    Log.d(TAG.TAG_DSHINE, msg);
                    saveLog(TAG.TAG_DSHINE,msg);
                    curlog = msg;
                }
                
            }
        }
    }
    
    /**
     * The Log Level:d
     * 
     * @param str
     */
    public void debug(String mTag,String msg) {
        if (logFlag) {
            if (logLevel <= Log.DEBUG) {
                String name = getFunctionName();
                if (name != null) {
                    Log.d(mTag,msg);
                } else {
                    Log.d(mTag, msg);
                }
                saveLog(mTag,msg);
            }
        }
    }
    
    /**
     * The Log Level:w
     * 
     * @param str
     */
    public void warn(String msg) {
        if (logFlag) {
            if (logLevel <= Log.WARN) {
                String name = getFunctionName();
                String curlog = msg;
                if (name != null) {
                    Log.w(TAG.TAG_DSHINE, name + " - [" + msg + "]");
                    saveLog(TAG.TAG_DSHINE,name + " - [" + msg + "]");
                    curlog = name + " - [" + msg + "]";
                } else {
                    Log.w(TAG.TAG_DSHINE, msg);
                    saveLog(TAG.TAG_DSHINE,msg);
                    curlog = msg;
                }
                
            }
        }
    }
    /**
     * The Log Level:d
     * 
     * @param str
     */
    public void warn(String msg,Throwable tr) {
        if (logFlag) {
            if (logLevel <= Log.WARN) {
                String name = getFunctionName();
                String curlog = msg;
                if (name != null) {
                    Log.w(TAG.TAG_DSHINE,name);
                    saveLog(TAG.TAG_DSHINE,name);
                    curlog = name;
                } else {
                    Log.w(TAG.TAG_DSHINE, msg);
                    saveLog(TAG.TAG_DSHINE,msg);
                    curlog = msg;
                }
                
            }
        }
    }
    /**
     * The Log Level:d
     * 
     * @param str
     */
    public void warn(String mTag,String msg) {
        if (logFlag) {
            if (logLevel <= Log.WARN) {
                String name = getFunctionName();
                if (name != null) {
                    Log.w(mTag,msg);
                } else {
                    Log.w(mTag, msg);
                }
                saveLog(mTag,msg);
            }
        }
    }
    
    /**
     * The Log Level:e
     * 
     * @param str
     */
    public void error(String msg) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                String name = getFunctionName();
                
                String curlog;
                if (name != null) {
                    Log.e(TAG.TAG_DSHINE, name + " - [" + msg + "]");
                    saveLog(TAG.TAG_DSHINE,name + " - [" + msg + "]");
                    curlog = name + " - [" + msg + "]";
                } else {
                    curlog = msg;
                    Log.e(TAG.TAG_DSHINE, msg);
                    saveLog(TAG.TAG_DSHINE,msg);
                }
                
            }
        }
    }
    
    /**
     * The Log Level:e
     * 
     * @param str
     */
    public void error(String myTag, String str) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                String name = getFunctionName();
                if (name != null) {
                    Log.e(myTag, str);
                } else {
                    Log.e(myTag, str);
                }
                saveLog(myTag,str);
                
            }
        }
    }
    
    /**
     * The Log Level:e
     * 
     * @param tr
     */
    public void error(String message, Throwable tr) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                String line = getFunctionName();
                Log.e(TAG.TAG_DSHINE, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + mClassName + line + ":] "
                        + message == null ? "" : message + "\n", tr);
                saveLog(TAG.TAG_DSHINE,"{Thread:" + Thread.currentThread().getName() + "}" + "[" + mClassName + line + ":] "
                        + message == null ? "" : message + "\n");
            }
        }
    }
    
    /**
     * The Log Level:e
     *
     * @param tr
     */
    public void error(Throwable tr) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                String line = getFunctionName();
                Log.e(TAG.TAG_DSHINE, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + mClassName + line + ":] "
                        + "" + "\n", tr);
                saveLog(TAG.TAG_DSHINE,"{Thread:" + Thread.currentThread().getName() + "}" + "[" + mClassName + line + ":] "
                        + "" + "\n");
            }
        }
    }
    @SuppressWarnings("unused")
    public static void saveLog(String tag,String msg) {
        if(true){
            return;
        }
        String errorlog = "log.txt";
        String savePath = "";
        String logFilePath = "";
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            // 判断是否挂载了SD卡
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                savePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                logFilePath = savePath + errorlog;
            }
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
            pw.println("[PID:"+android.os.Process.myPid()+"] - [TID:"+android.os.Process.myTid()+"] - TAG:["+tag+"] - "+msg);
            pw.close();
            fw.close();
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
    public final class TAG{
        public final static String TAG_DSHINE = "UCSI";
    }

    private static void printfLog(int level, String tag,String log) {
        String curTag = tag == null?TAG.TAG_DSHINE : tag;
        
        switch(level){
            case 'v':
                Log.v(curTag, log);
            break;
            case 'i':
                Log.i(curTag, log);
            break;
            case 'd':
                Log.d(curTag, log);
            break;
            case 'w':
                Log.w(curTag, log);
            break;
            case 'e':
                Log.e(curTag, log);
            break;
            default:
                Log.d(curTag, log);;
        }
        
        saveLog(curTag,log);
    }  
    
}
