package com.doit.net.Model;

import android.os.Environment;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;

/**
 * Created by wiker on 2016/4/
 * Modeified by zxc
 */
public class UCSIDBManager {

    public static DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
            .setDbName("ucsi.db")
            // 不设置dbDir时, 默认存储在app的私有目录.
            //.setDbDir(new File("/sdcard/.doit")) // "sdcard"的写法并非最佳实践, 这里为了简单, 先这样写了.
            //.setDbDir(new File(EXPORT_FILE_PATH)) // "sdcard"的写法并非最佳实践, 这里为了简单, 先这样写了.
            .setDbVersion(4)
            .setDbOpenListener(new DbManager.DbOpenListener() {
                @Override
                public void onDbOpened(DbManager db) {
                    // 开启WAL, 对写入加速提升巨大
                    db.getDatabase().enableWriteAheadLogging();
                }
            })
            .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                @Override
                public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                    // db.addColumn(...);
                    // db.dropTable(...);
                    // ...
                    // or
                    // db.dropDb();
                }
            });


    public static DbManager DB = x.getDb(daoConfig);
    public static DbManager getDbManager(){
        return DB;
    }

    public static void saveUeidToDB(String imsi,String msisdn, String tmsi, long createDate, String longitude, String latitude){
        try {
            DB.save(new DBUeidInfo(imsi, msisdn, tmsi, createDate, longitude, latitude));
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

}
