package com.ilab.testysy;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.heima.easysp.SharedPreferencesUtils;
import com.ilab.testysy.database.DaoMaster;
import com.ilab.testysy.database.DaoSession;
import com.ilab.testysy.network.NetWork;
import com.ilab.testysy.utils.CrashHandler;
import com.videogo.openapi.EZOpenSDK;

public class MainApplication extends Application {
    private SQLiteDatabase db;
    private DaoSession mDaoSession;
    private static MainApplication instances;
    private SharedPreferencesUtils sp;

    @Override
    public void onCreate() {
        super.onCreate();
        instances=this;
        setDatabase();
        NetWork.getInstance().init();

        EZOpenSDK.showSDKLog(false);
        EZOpenSDK.enableP2P(true);
        sp = SharedPreferencesUtils.init(this);
        CrashHandler.getInstance().init(getApplicationContext());
    }

    public SharedPreferencesUtils getSp() {
        return sp;
    }
    public static MainApplication getInstances(){
        return instances;
    }

    /**
     * 设置greenDao
     */
    private void setDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        DaoMaster.DevOpenHelper mHelper = new DaoMaster.DevOpenHelper(this, "success-db", null);
        db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        DaoMaster mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }
    public DaoSession getDaoSession() {
        return mDaoSession;
    }
    public SQLiteDatabase getDb() {
        return db;
    }
    public void initYSY(int type){
        if(type==1){
            EZOpenSDK.initLib(this, Constants.mAppKeyZheda);
        }
        if(type==0){
            EZOpenSDK.initLib(this, Constants.mAppKeyQuzhou);
        }
    }
}
