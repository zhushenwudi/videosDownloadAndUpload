package com.ilab.testysy;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import static com.ilab.testysy.utils.Util.restartApp;

public class LocalService extends Service {

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                Log.e("aaa", "connected with " + iMyAidlInterface.getServiceName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(LocalService.this, "链接断开，重新启动 RemoteService", Toast.LENGTH_LONG).show();
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(new Intent(LocalService.this, RemoteService.class));
            else startService(new Intent(LocalService.this, RemoteService.class));
            bindService(new Intent(LocalService.this, RemoteService.class), connection, Context.BIND_IMPORTANT);
            restartApp(LocalService.this, 0);
        }
    };

    public LocalService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this,"LocalService 启动",Toast.LENGTH_LONG).show();
        startService(new Intent(LocalService.this, RemoteService.class));
        bindService(new Intent(this, RemoteService.class), connection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private class MyBinder extends IMyAidlInterface.Stub {

        @Override
        public String getServiceName() {
            return LocalService.class.getName();
        }
    }
}