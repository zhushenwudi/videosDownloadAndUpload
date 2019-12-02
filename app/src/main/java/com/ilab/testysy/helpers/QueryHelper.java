package com.ilab.testysy.helpers;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.heima.easysp.SharedPreferencesUtils;
import com.ilab.testysy.Constants;
import com.ilab.testysy.MainApplication;
import com.ilab.testysy.cloud.QueryPlayBackCloudListAsyncTask;
import com.ilab.testysy.cloud.QueryPlayBackListTaskCallback;
import com.ilab.testysy.entity.CloudPartInfoFileEx;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class QueryHelper {
    private Timer timer;
    private QueryPlayBackCloudListAsyncTask queryPlayBackCloudListAsyncTask;
    private Handler mHandler;

    private int currentIndex = 0;
    private int addCount = 0;
    private int phoneId = -1;
    private List<CloudPartInfoFile> cloudPartInfoFiles = new ArrayList<>();
    private SharedPreferencesUtils sp = MainApplication.getInstances().getSp();
    public QueryHelper(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public List<CloudPartInfoFile> getCloudPartInfoFiles() {
        return cloudPartInfoFiles;
    }

    public void startQueryCould(int phoneId) {
        this.phoneId = phoneId;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                new GetCamersInfoListTask().execute();
            }
        }, 2000);
    }

    private void startQueryCouldList(List<EZDeviceInfo> result, Date date) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentIndex < result.size()) {
                    String serial = result.get(currentIndex).getDeviceSerial();
                    queryPlayBackCloudListAsyncTask = new QueryPlayBackCloudListAsyncTask(serial, 1,
                            new QueryPlayBackListTaskCallback() {
                                @Override
                                public void queryHasNoData() {

                                }

                                @Override
                                public void queryOnlyHasLocalFile() {

                                }

                                @Override
                                public void queryOnlyLocalNoData() {

                                }

                                @Override
                                public void queryLocalException() {

                                }

                                @Override
                                public void queryCloudSucess(List<CloudPartInfoFileEx> cloudPartInfoFileEx, int queryMLocalStatus, List<CloudPartInfoFile> cloudPartInfoFile) {
                                    cloudPartInfoFiles.addAll(cloudPartInfoFile);
                                    Message msg = new Message();
                                    msg.what = Constants.QUERY_ING;
                                    msg.arg1 = cloudPartInfoFiles.size();
                                    mHandler.sendMessage(msg);
                                    addCount++;
                                }

                                @Override
                                public void queryLocalSucess(List<CloudPartInfoFileEx> cloudPartInfoFileEx, int position, List<CloudPartInfoFile> cloudPartInfoFile) {
                                    int a = 100;
                                }

                                @Override
                                public void queryLocalNoData() {
                                    int a = 100;
                                }

                                @Override
                                public void queryException() {
                                    int a = 100;
                                    addCount++;
                                }

                                @Override
                                public void queryTaskOver(int type, int queryMode, int queryErrorCode, String detail) {
                                }
                            });
                    queryPlayBackCloudListAsyncTask.setCalendar(date);
                    queryPlayBackCloudListAsyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    currentIndex++;
                } else {
                    if (addCount == result.size()) {
                        timer.cancel();
                        Log.i("csdn", "查询完毕结束了");
                        Message msg = new Message();
                        msg.what = Constants.QUERY_END;
                        msg.arg1 = cloudPartInfoFiles.size();
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }, 500, 500);
    }

    @SuppressLint("StaticFieldLeak")
    class GetCamersInfoListTask extends AsyncTask<Void, Void, List<EZDeviceInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<EZDeviceInfo> doInBackground(Void... params) {
            List<EZDeviceInfo> arr = new ArrayList<>();
            List<EZDeviceInfo> devices = new ArrayList<>();
            try {
                devices = EZOpenSDK.getInstance().getDeviceList(0, 1000);
            } catch (BaseException e) {
                Log.i("csdn", "BaseException");
            }
            Log.i("csdn", "-------查询摄像头完毕，共计====" + devices.size());
            if (devices.size() > 0) {
                List<EZDeviceInfo> list = new ArrayList<>();
                int a = (int) Math.floor(devices.size() / 3);

                if (phoneId == 0) {
                    for (int i = 0; i < a; i++) {
                        list.add(devices.get(i));
                    }
                    arr = list;
                } else if (phoneId == 1) {
                    for (int i = a; i < (2 * a); i++) {
                        list.add(devices.get(i));
                    }
                    arr = list;
                } else if (phoneId == 2) {
                    for (int i = (2 * a); i < devices.size(); i++) {
                        list.add(devices.get(i));
                    }
                    arr = list;
                }
            }
            return arr;
        }

        @Override
        protected void onPostExecute(List<EZDeviceInfo> result) {
            super.onPostExecute(result);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String customDate;
            if (result.size() > 0) {
                customDate = sp.getString("customDate", null);
                if (customDate != null) {
                    try {
                        Date newDate = sdf.parse(customDate);
                        startQueryCouldList(result, newDate);
                    } catch (ParseException ignored) {
                    }
                } else {
                    Date date = new Date();
                    //减去一天
                    long ss = date.getTime() - 1000 * 60 * 60 * 24;
                    date.setTime(ss);
                    customDate = sdf.format(date);
                    sp.putString("customDate", customDate);
                    startQueryCouldList(result, date);
                }
            }
        }
    }
}
