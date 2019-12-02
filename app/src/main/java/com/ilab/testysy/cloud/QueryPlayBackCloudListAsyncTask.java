package com.ilab.testysy.cloud;

import com.ilab.testysy.entity.CloudPartInfoFileEx;
import com.ilab.testysy.utils.RemoteListContant;
import com.videogo.device.DeviceReportInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.util.CollectionUtil;
import com.videogo.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class QueryPlayBackCloudListAsyncTask extends HikAsyncTask<String, Void, Integer> {
    private final String MINUTE;
    // 设备序列号
    private String deviceSerial;
    // 通道号
    private int channelNo;
    // 搜索日期（格式为：yyyy-MM-dd）
    private Date searchDate;


    private volatile boolean abort = false;

    private int cloudErrorCode = 0;

    private QueryPlayBackListTaskCallback queryPlayBackListTaskCallback;

    private List<CloudPartInfoFile> cloudPartFiles;

    List<CloudPartInfoFileEx> cloudPartInfoFileExList = new ArrayList<CloudPartInfoFileEx>();


    public QueryPlayBackCloudListAsyncTask(String deviceSerial, int channelNo, QueryPlayBackListTaskCallback queryPlayBackListTaskCallback) {
        MINUTE = "点";
        this.deviceSerial = deviceSerial;
        this.channelNo = channelNo;
        this.queryPlayBackListTaskCallback = queryPlayBackListTaskCallback;
    }

    @Override
    protected Integer doInBackground(String... params) {
        boolean cloudException = false;
        boolean localException = false;

        int queryCloudFiles = 2;
        queryCloudFiles = queryCloudFile();
        localException = true;
        if (cloudException && localException) {
            return RemoteListContant.QUERY_EXCEPTION;
        }

        if (cloudException && !localException) {
            return RemoteListContant.QUERY_EXCEPTION;
        }

        if (!cloudException && localException) {
            if (queryCloudFiles == RemoteListContant.QUERY_NO_DATA) {
                return RemoteListContant.QUERY_EXCEPTION;
            } else {
                return RemoteListContant.QUERY_CLOUD_SUCCESSFUL_LOCAL_EX;
            }
        }

        if (!cloudException && !localException) {
            if (queryCloudFiles == RemoteListContant.QUERY_NO_DATA) {
                return RemoteListContant.QUERY_NO_DATA;
            } else {
                return RemoteListContant.QUERY_CLOUD_SUCCESSFUL_NOLOACL;
            }
        }
        return RemoteListContant.QUERY_NO_DATA;

    }

    @Override
    protected void onPostExecute(Integer result) {
        if (!abort) {
            queryPlayBackListTaskCallback.queryTaskOver(RemoteListContant.TYPE_CLOUD,
                    DeviceReportInfo.REPOERT_QUERY_CLOUD, cloudErrorCode, "");
            // 云和本地都没有数据
            if (result == RemoteListContant.QUERY_NO_DATA) {
                queryPlayBackListTaskCallback.queryHasNoData();
            }
            // 云有数据，本地没有数据
            else if (result == RemoteListContant.QUERY_CLOUD_SUCCESSFUL_NOLOACL) {
                queryPlayBackListTaskCallback.queryCloudSucess(cloudPartInfoFileExList, RemoteListContant.NO_LOCAL,
                        cloudPartFiles);
            }
            // 云有数据，本地也有数据
            else if (result == RemoteListContant.QUERY_CLOUD_SUCCESSFUL_HASLOCAL) {
                queryPlayBackListTaskCallback.queryCloudSucess(cloudPartInfoFileExList, RemoteListContant.HAS_LOCAL,
                        cloudPartFiles);
            }
            // 云有数据，本地异常
            else if (result == RemoteListContant.QUERY_CLOUD_SUCCESSFUL_LOCAL_EX) {
                queryPlayBackListTaskCallback.queryCloudSucess(cloudPartInfoFileExList,
                        RemoteListContant.EXCEPTION_LOCAL, cloudPartFiles);
            }
            // 云没数据，本地有数据
            else if (result == RemoteListContant.QUERY_ONLY_LOCAL) {
                queryPlayBackListTaskCallback.queryOnlyHasLocalFile();
            }
            // 云和本地查询都异常
            else if (result == RemoteListContant.QUERY_EXCEPTION) {
                queryPlayBackListTaskCallback.queryException();
            }
        }
    }

    private void convertEZCloudRecordFile2CloudPartInfoFile(CloudPartInfoFile dst, EZCloudRecordFile src, int pos) {
        String startT = new SimpleDateFormat("yyyyMMddHHmmss").format(src.getStartTime().getTime());
        String endT = new SimpleDateFormat("yyyyMMddHHmmss").format(src.getStopTime().getTime());

        dst.setCloud(true);
        dst.setDownloadPath(src.getDownloadPath());
        //dst.setEndMillis(src.get);
        dst.setEndTime(endT);
        dst.setFileId(src.getFileId());
        //dst.setFileName(src.getFileName());
//    	dst.setFileSize(Integer.parseInt(src.getFileSize()));
//    	dst.setFileType(src.getFileType());
        dst.setKeyCheckSum(src.getEncryption());
        dst.setPicUrl(src.getCoverPic());
        dst.setPosition(pos);
        //dst.setStartMillis(startMillis);
        dst.setStartTime(startT);
        dst.setDeviceSerial(src.getDeviceSerial());
        dst.setCameraNo(src.getCameraNo());
        //dst.setStartMillis(System.currentTimeMillis()-1000000l);
    }

    public void setCalendar(Date date) {
        searchDate = date;
    }

    private int queryCloudFile() /*throws VideoGoNetSDKException */ {
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        startTime.setTime(searchDate);
        endTime.setTime(searchDate);
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE, 59);
        endTime.set(Calendar.SECOND, 59);

        List<EZCloudRecordFile> tmpList = null;
        try {
            tmpList = EZOpenSDK.getInstance().searchRecordFileFromCloud(deviceSerial, channelNo, startTime, endTime);
        } catch (BaseException e) {
            e.printStackTrace();
        }

        cloudPartFiles = new ArrayList<>();
        if (tmpList != null && tmpList.size() > 0) {
            for (int i = 0; i < tmpList.size(); i++) {
                EZCloudRecordFile file = tmpList.get(i);
                CloudPartInfoFile cpif = new CloudPartInfoFile();
                file.setDeviceSerial(deviceSerial);
                convertEZCloudRecordFile2CloudPartInfoFile(cpif, file, i);
                cloudPartFiles.add(cpif);
            }
        }

        if (CollectionUtil.isNotEmpty(cloudPartFiles)) {
            Collections.sort(cloudPartFiles);
        }

        int length = cloudPartFiles.size();
        int i = 0;
        while (i < length) {
            CloudPartInfoFileEx cloudPartInfoFileEx = new CloudPartInfoFileEx();
            CloudPartInfoFile dataOne = cloudPartFiles.get(i);
            dataOne.setPosition(i);
            Calendar beginCalender = Utils.convert14Calender(dataOne.getStartTime());
            String hour = getHour(beginCalender.get(Calendar.HOUR_OF_DAY));
            cloudPartInfoFileEx.setHeadHour(hour);
            cloudPartInfoFileEx.setDataOne(dataOne);
            i++;
            if (i > length - 1) {
                cloudPartInfoFileExList.add(cloudPartInfoFileEx);
                continue;
            }
            CloudPartInfoFile dataTwo = cloudPartFiles.get(i);
            if (hour.equals(getHour(Utils.convert14Calender(dataTwo.getStartTime()).get(Calendar.HOUR_OF_DAY)))) {
                dataTwo.setPosition(i);
                cloudPartInfoFileEx.setDataTwo(dataTwo);
                i++;
                if (i > length - 1) {
                    cloudPartInfoFileExList.add(cloudPartInfoFileEx);
                    continue;
                }
                CloudPartInfoFile dataThree = cloudPartFiles.get(i);
                if (hour.equals(getHour(Utils.convert14Calender(dataThree.getStartTime()).get(Calendar.HOUR_OF_DAY)))) {
                    dataThree.setPosition(i);
                    cloudPartInfoFileEx.setDataThree(dataThree);
                    i++;
                }
            }
            cloudPartInfoFileExList.add(cloudPartInfoFileEx);
        }
        if (CollectionUtil.isNotEmpty(cloudPartInfoFileExList)) {
            return RemoteListContant.QUERY_CLOUD_SUCCESSFUL_NOLOACL;
        }
        return RemoteListContant.QUERY_NO_DATA;
    }

    private String getHour(int hourOfDay) {
        return hourOfDay + MINUTE;
    }
}
