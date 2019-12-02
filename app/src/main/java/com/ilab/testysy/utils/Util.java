package com.ilab.testysy.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.ilab.testysy.Constants;
import com.ilab.testysy.MainActivity;
import com.ilab.testysy.MainApplication;
import com.ilab.testysy.R;
import com.ilab.testysy.cloud.EZCloudRecordFile;
import com.ilab.testysy.database.ErrorFile;
import com.ilab.testysy.database.ErrorFileDao;
import com.ilab.testysy.database.UploadEnty;
import com.ilab.testysy.database.UploadEntyDao;
import com.ilab.testysy.database.UsefulEnty;
import com.ilab.testysy.database.UsefulEntyDao;
import com.ilab.testysy.entity.FileEntity;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.util.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Util {
    public static <T> List<List<T>> splitList(List<T> source, int n) {
        if (null == source || source.size() == 0 || n <= 0)
            return null;
        List<List<T>> result = new ArrayList<>();
        int remainder = source.size() % n;
        int size = (source.size() / n);
        for (int i = 0; i < size; i++) {
            List<T> subset;
            subset = source.subList(i * n, (i + 1) * n);
            result.add(subset);
        }
        if (remainder > 0) {
            List<T> subset;
            subset = source.subList(size * n, size * n + remainder);
            result.add(subset);
        }
        return result;
    }

    public static Map<String, EZCloudRecordFile> convert(List<CloudPartInfoFile> dsts) {
        Map<String, EZCloudRecordFile> filesMap = new HashMap<>();
        for (int i = 0; i < dsts.size(); i++) {
            EZCloudRecordFile dst = new EZCloudRecordFile();
            CloudPartInfoFile cloud = dsts.get(i);
            dst.setCoverPic(cloud.getPicUrl());
            dst.setDownloadPath(cloud.getDownloadPath());
            dst.setFileId(cloud.getFileId().trim());
            dst.setEncryption(cloud.getKeyCheckSum());
            dst.setStartTime(Utils.convert14Calender(cloud.getStartTime()));
            dst.setStopTime(Utils.convert14Calender(cloud.getEndTime()));
            dst.setDeviceSerial(cloud.getDeviceSerial());
            dst.setCameraNo(cloud.getCameraNo());
            dst.setRetry(0);
            filesMap.put(cloud.getFileId(), dst);
        }
        return filesMap;
    }

    public static Map<String, FileEntity> scanFiles(int type) {
        Map<String, FileEntity> res = new HashMap<>();
        ErrorFileDao greenErrorDao = MainApplication.getInstances().getDaoSession().getErrorFileDao();
        UsefulEntyDao greenUsefulDao = MainApplication.getInstances().getDaoSession().getUsefulEntyDao();
        UploadEntyDao greenUploadDao = MainApplication.getInstances().getDaoSession().getUploadEntyDao();
        String path = Constants.path;
        File file = new File(path);
        //是文件夹，便遍历出里面所有的文件（文件，文件夹）
        File[] files = file.listFiles();
        if (files != null) {
            for (File value : files) {
                String name = value.getName();
                if ((name.endsWith(".MP4") && type == 1) || (name.endsWith(".PNG") && type == 2)) {
                    if (value.length() == 0) {
                        List<ErrorFile> errorFiles = greenErrorDao.loadAll();
                        boolean hasKey = false;
                        for (ErrorFile errorFile : errorFiles) {
                            if (errorFile.getFileName().equals(path + name)) {
                                if (errorFile.getCount() < 3) {
                                    errorFile.setCount(errorFile.getCount() + 1);
                                    greenErrorDao.update(errorFile);
                                } else {
                                    UsefulEnty usefulEnty = new UsefulEnty();
                                    usefulEnty.setUseful(errorFile.getFileName());
                                    greenUsefulDao.insert(usefulEnty);
                                    try {
                                        UploadEnty uploadEnty = new UploadEnty();
                                        uploadEnty.setCause("视频下载3次均失败");
                                        uploadEnty.setFileName(errorFile.getFileName());
                                        greenUploadDao.insert(uploadEnty);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                hasKey = true;
                                break;
                            }
                        }
                        if (!hasKey) {
                            ErrorFile errorFile = new ErrorFile();
                            errorFile.setFileName(path + name);
                            errorFile.setCount(1);
                        }
                        value.delete();
                        Log.i("aaa", "-----文件删除，文件字节数==0-----name==" + name);
                    }
                    String[] arr = name.split("_");
                    String serial = "";
                    if (arr.length >= 4) {
                        int index = arr[3].indexOf(".");
                        serial = arr[3].substring(0, index);
                    }
                    res.put(arr[0], new FileEntity(path + name, serial, arr[1], arr[2], arr[0], 0));

                } else {
                    value.delete();
                    Log.i("aaa", "-----文件删除，后缀名不是mp4-----name==" + name);

                }
            }
        }
        return res;
    }

    /**
     * 判断视频是否全部有效
     *
     * @param flag      下载完成检索标志 true:已全部下载完毕 false:定时器检索
     * @param laterTime 索引视频修改时间(flag为true时无效)
     * @return 是否已经全部有效 true:全部有效 false:存在无效视频
     */
    public static boolean scanSD_ValidVideo(boolean flag, long laterTime) {
        boolean isCanContinueWork = true;
        List<String> sdCardList = new ArrayList<>();
        List<UsefulEnty> uncheckedList = new ArrayList<>();
        List<File> timeNotEnoughList = new ArrayList<>();
        List<UsefulEnty> dropedList = new ArrayList<>();
        boolean isContains = false;
        UsefulEntyDao greenUsefulDao = MainApplication.getInstances().getDaoSession().getUsefulEntyDao();
        ErrorFileDao greenErrorDao = MainApplication.getInstances().getDaoSession().getErrorFileDao();
        //拿到已经检测完的视频
        List<String> checkedList = new ArrayList<>();
        //取出Path文件夹中全部视频
        File file = new File(Constants.path);
        File[] files = file.listFiles();
        Log.e("aaa", "#################开始处理...################");
        if (files != null) {
            //数组转list
            for (File sdfile : files) {
                sdCardList.add(sdfile.getAbsolutePath());
            }
            //去除检查过的
            for (UsefulEnty ufl : greenUsefulDao.loadAll()) {
                checkedList.add(ufl.getUseful());
            }
            sdCardList.removeAll(checkedList);
            //判断要检查的视频数量
            if (sdCardList.size() > 0) {
                for (String s : sdCardList) {
                    File f = new File(s);

                    //无需检测修改日期，全部检测
                    if (flag) {
                        isContains = true;
                    } else {
                        //判断视频是否为lastTime时间前的视频
                        isContains = (new Date().getTime() - f.lastModified()) > laterTime;
                    }
                    if (f.isFile() && f.exists() && f.getName().endsWith(".MP4") && f.length() != 0 && isContains) {
                        //视频时长是否足够
                        if (f.length() < Long.valueOf(f.getAbsolutePath().split("_")[2])) {
                            timeNotEnoughList.add(f);
                        } else {
                            UsefulEnty usefulEnty = new UsefulEnty();
                            usefulEnty.setUseful(f.getAbsolutePath());
                            uncheckedList.add(usefulEnty);
                        }
                    }
                }
            }
        }
        Log.e("aaa", "##########检查有效性...需检测长度为:" + uncheckedList.size() + "##########");
        //检查视频有效性
        List<ErrorFile> errorFiles = greenErrorDao.loadAll();
        //Log.e("aaa", "errorfiles.size():" + errorFiles.size());
        for (UsefulEnty list : uncheckedList) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(list.getUseful(), MediaStore.Video.Thumbnails.MINI_KIND);
            if (bitmap == null) {
                //将失效视频记录下来，等下删除
                dropedList.add(list);
                //如果连续检测视频失效就记录为成功并存入数据库
                boolean hasflag = false;
                for (ErrorFile errorFile : errorFiles) {
                    if (errorFile.getFileName().equals(list.getUseful())) {
                        if (errorFile.getCount() < 3) {
                            Log.i("aaa", "连续错误计数器++");
                            errorFile.setCount(errorFile.getCount() + 1);
                            greenErrorDao.update(errorFile);
                        } else {
                            Log.e("aaa", "记录连续错误的视频，放过它吧...");
                            greenUsefulDao.insert(list);

                        }
                        hasflag = true;
                        break;
                    }
                }
                if (!hasflag) {
                    ErrorFile errorFile = new ErrorFile();
                    errorFile.setCount(1);
                    errorFile.setFileName(list.getUseful());
                    greenErrorDao.insert(errorFile);
                }
            } else {
                //将已检测有效视频存入数据库
                greenUsefulDao.insert(list);
            }
        }
        Log.e("aaa", "#################删除阶段1...################");
        Log.e("aaa", "时长不足视频数量" + timeNotEnoughList.size());
        //删除时长不足的视频，等待重新下载
        if (timeNotEnoughList.size() > 0) {
            isCanContinueWork = false;
            for (File f : timeNotEnoughList) {
                Log.e("aaa", "视频时长不足，准备删除:" + f.getName() + "###检测到的长度:" + f.length());
                if (f.isFile() && f.exists())
                    f.delete();
            }
        }
        Log.e("aaa", "#################删除阶段2...################");
        //删除无效的视频，等待重新下载
        Log.e("aaa", "无效视频数量" + dropedList.size());
        if (dropedList.size() > 0) {
            isCanContinueWork = false;
            for (UsefulEnty a : dropedList) {
                Log.e("aaa", "要删除的无效视频，准备删除:" + a.getUseful());
                File file1 = new File(a.getUseful());
                if (file1.isFile() && file1.exists())
                    file1.delete();
            }
        }
        return isCanContinueWork;
    }

//    public static void showInfo(CloudPartInfoFile file) {
//        Log.e("aaa", "DeviceSerial:" + file.getDeviceSerial() + "\nDownloadPath:" + file.getDownloadPath() +
//                "\nFileId:" + file.getFileId() + "\nFileName:" + file.getFileName() + "\nPicUrl:" + file.getPicUrl() +
//                "\nCameraNo:" + file.getCameraNo() + "\nStartTime:" + file.getStartTime() + "Position:" + file.getPosition() +
//                "\nStartMillis:" + file.getStartMillis());
//    }

    public static String getYesterday() {
        Date d = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        //减去一天
        d.setTime(d.getTime() - 1000 * 60 * 60 * 24);
        return sdf.format(d);
    }

    //移除文件，获取文件时间与当前时间对比，删除btwTime小时前的文件
    public static void removeFileByTime(long btwTime, String dirPath) {
        //获取目录下所有文件
        List<File> allFile = getDirAllFile(new File(dirPath));
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (File file : allFile) {
            try {
                //文件时间减去当前时间
                Date start = dateFormat.parse(dateFormat.format(new Date(file.lastModified())));
                if (btwTime >= Objects.requireNonNull(start).getTime()) {
                    deleteFile(file);
                }
            } catch (Exception e) {
                Log.d("aaa", "dataformat exeption e " + e.toString());
            }
        }
    }

    //删除文件夹及文件夹下所有文件
    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : Objects.requireNonNull(files)) {
                deleteFile(f);
            }
            file.delete();
        } else if (file.exists()) {
            file.delete();
        }
    }

    //获取指定目录下一级文件
    public static List<File> getDirAllFile(File file) {
        List<File> fileList = new ArrayList<>();
        File[] fileArray = file.listFiles();
        if (fileArray == null)
            return fileList;
        fileList.addAll(Arrays.asList(fileArray));
        fileSortByTime(fileList);
        return fileList;
    }

    //对文件进行时间排序
    private static void fileSortByTime(List<File> fileList) {
        Collections.sort(fileList, (p1, p2) -> {
            if (p1.lastModified() < p2.lastModified()) {
                return -1;
            } else if (p1.lastModified() > p2.lastModified()) {
                return 1;
            }
            return 0;
        });
    }

    /**
     * 按文件修改时间排序
     *
     * @param filePath  filePath
     */
    public static ArrayList<File> orderByDate(String filePath) {
        File file = new File(filePath);
        File[] files = file.listFiles();
        Arrays.sort(Objects.requireNonNull(files), new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;// 如果 if 中修改为 返回-1 同时此处修改为返回 1 排序就会是递减
            }

            public boolean equals(Object obj) {
                return true;
            }

        });

        return new ArrayList<>(Arrays.asList(files));
    }

    public static void restartApp(Context context, long delayTime) {
        RxTimerUtil.getInstance().timer(delayTime, number -> {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent restartIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, 0);
            //退出程序
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Objects.requireNonNull(mgr).set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用

            //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
            android.os.Process.killProcess(android.os.Process.myPid());
        });
    }

    public static String writeToFile(Context context, String text, String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        String now = sdf.format(new Date());
        String rtnPath = "";
        try {
            String dirName = Environment.getExternalStorageDirectory().getCanonicalPath() + File.separator + context.getResources().getString(R.string.app_name) + File.separator + date + File.separator;
            String fileName = dirName + now + ".txt";

            File file = new File(dirName);
            if (!file.exists()) {
                file.mkdirs();
            }
            rtnPath = dirName;
            FileWriter writer = new FileWriter(fileName);
            writer.write(text);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rtnPath;
    }
}