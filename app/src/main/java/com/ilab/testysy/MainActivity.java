package com.ilab.testysy;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.heima.easysp.SharedPreferencesUtils;
import com.ilab.checkupdatebypatch.CheckUpdateByPatch;
import com.ilab.testysy.adapter.BaseActivity;
import com.ilab.testysy.adapter.MyAdapter;
import com.ilab.testysy.cloud.EZCloudRecordFile;
import com.ilab.testysy.database.ErrorFileDao;
import com.ilab.testysy.database.SuccessEnty;
import com.ilab.testysy.database.SuccessEntyDao;
import com.ilab.testysy.database.SuccessPicEnty;
import com.ilab.testysy.database.SuccessPicEntyDao;
import com.ilab.testysy.database.UploadEnty;
import com.ilab.testysy.database.UploadEntyDao;
import com.ilab.testysy.database.UsefulEntyDao;
import com.ilab.testysy.entity.FileEntity;
import com.ilab.testysy.entity.TaskEnty;
import com.ilab.testysy.helpers.DeleteFileThread;
import com.ilab.testysy.helpers.DownloadHelper;
import com.ilab.testysy.helpers.QueryHelper;
import com.ilab.testysy.helpers.TokenHelper;
import com.ilab.testysy.helpers.UploadHelper;
import com.ilab.testysy.network.NetWork;
import com.ilab.testysy.utils.DoubleClickUtil;
import com.ilab.testysy.utils.RxTimerUtil;
import com.ilab.testysy.utils.SFTPUtils;
import com.ilab.testysy.utils.Util;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.ilab.testysy.Constants.DISSMISS_DIALOG;
import static com.ilab.testysy.Constants.DOWN_FAILED;
import static com.ilab.testysy.Constants.DOWN_SUCCESS;
import static com.ilab.testysy.Constants.LOGIN_SUCCESS;
import static com.ilab.testysy.Constants.PARTITION_SIZE;
import static com.ilab.testysy.Constants.QUERY_END;
import static com.ilab.testysy.Constants.QUERY_ING;
import static com.ilab.testysy.Constants.SHOW_STRING;
import static com.ilab.testysy.Constants.TOKEN_RESULT;
import static com.ilab.testysy.Constants.UPLOADPIC_FINISH;
import static com.ilab.testysy.Constants.UPLOADVideo_FINISH;
import static com.ilab.testysy.Constants.UPLOAD_SERVER_SUCCESS;
import static com.ilab.testysy.Constants.path;
import static com.ilab.testysy.utils.Util.getAppVersionName;
import static com.ilab.testysy.utils.Util.restartApp;
import static com.ilab.testysy.utils.Util.writeToFile;

public class MainActivity extends BaseActivity {
    @BindView(R.id.btn_one)
    Button btnOne;
    @BindView(R.id.tv_task)
    TextView tvTask;
    @BindView(R.id.tv_token)
    TextView tvToken;
    @BindView(R.id.btn_two)
    Button btnTwo;
    @BindView(R.id.btn_three)
    Button btnThree;
    @BindView(R.id.btn_date)
    Button btnDate;
    @BindView(R.id.tv_download)
    TextView tvDownload;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.tv_upload)
    TextView tvUpload;
    @BindView(R.id.recycleview)
    RecyclerView mRecycleview;
    @BindView(R.id.tv_phone)
    TextView tvPhone;
    @BindView(R.id.btn_zheda)
    Button btnZheda;
    @BindView(R.id.btn_quzhou)
    Button btnQuzhou;
    @BindView(R.id.ll_project)
    LinearLayout llProject;
    @BindView(R.id.ll_phone_select)
    LinearLayout llPhoneSelect;
    @BindView(R.id.bt_startwork)
    Button btStartwork;

    List<TaskEnty> tasks = new ArrayList<>();//任务列表
    private QueryHelper queryHelper;
    List<List<CloudPartInfoFile>> parts;//每天云视频的总的数量。
    MyAdapter adapater;
    private int downSize = 0;//待下载的视频数量
    private SuccessEntyDao greenEntyDao;
    private SuccessPicEntyDao greenPicDao;
    private UsefulEntyDao greenUsefulDao;
    private ErrorFileDao greenErrorDao;
    private SharedPreferencesUtils sp = MainApplication.getInstances().getSp();
    private Map<String, EZCloudRecordFile> maps;//剩余需要下载的视频map
    private DownloadHelper downloadHelper;
    private UploadHelper uploadHelper;
    private UploadEntyDao greenUploadDao;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AlertDialog globalAlertDialog;
    private Timer timer;

    @SuppressLint("SetTextI18n")
    private Handler mHandler = new Handler(msg -> {
        switch (msg.what) {
            case TOKEN_RESULT:
                Log.e("aaa", "-----------------登录错误，重启app------------------");
                restartApp(MainActivity.this, 2000);
                break;
            case LOGIN_SUCCESS:
                if (msg.arg1 == 0) {
                    tvToken.setText("衢州--登陆成功，萤石云初始化成功");
                }
                if (msg.arg1 == 1) {
                    tvToken.setText("浙大--登陆成功，萤石云初始化成功");
                }
                break;
            case QUERY_ING:
                int size = msg.arg1;
                tvDownload.setText("云视频列表.size()==" + size + "个");
                break;
            case QUERY_END:
                int count = msg.arg1;
                tvDownload.setText("查询完毕，共计" + count + "视频片段");
                int projectId = sp.getInt("projectId");
                if (projectId == 0) {
                    sp.putInt("quzhoucloudsize", count);
                } else if (projectId == 1) {
                    sp.putInt("zhedacloudsize", count);
                }
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //转化之后开始下载
                        convertFiles();
                    }
                }, 2000);
                break;
            case DOWN_SUCCESS:
            case DOWN_FAILED:
                tvDownload.setText("当前分任务还需下载视频:\ntotal=" + downSize + " , success=" + msg.arg1 + " , fail=" + msg.arg2);
                break;
            case SHOW_STRING:
                tvUpload.setText((String) msg.obj);
                break;
            case UPLOADPIC_FINISH:
                tvUpload.setText("");
                greenPicDao.deleteAll();
                downAndUploadVideo();
                break;
            case UPLOADVideo_FINISH:
                tvUpload.setText("");
                finishUploadVideo();
                break;
            case UPLOAD_SERVER_SUCCESS:
                clean_greenDao();
                new DeleteFileThread(this, true).start();
                break;
            case DISSMISS_DIALOG:
                if (globalAlertDialog != null) {
                    try {
                        Field field = Objects.requireNonNull(Objects.requireNonNull(globalAlertDialog.getClass().getSuperclass()).getSuperclass()).getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(globalAlertDialog, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    globalAlertDialog.dismiss();
                }
                break;
            default:
                break;
        }
        return false;
    });

    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            if (hour == 0 && min == 10) {
                Log.e("aaa", "----整点---整点----整点----");
                try {
                    if (downloadHelper != null) downloadHelper.shutdownNow();
                    if (uploadHelper != null) uploadHelper.shutdownNow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int phoneId = sp.getInt("phoneId", -1);
                boolean isNoManMode = sp.getBoolean("noManMode", false);
                sp.clear();
                sp.putInt("phoneId", phoneId);
                sp.putInt("currentTaskId", 0);
                sp.putInt("projectId", 0);
                sp.putBoolean("canwork", true);
                sp.putBoolean("noManMode", isNoManMode);
                clean_greenDao();
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        new DeleteFileThread(MainActivity.this, true).start();
                    }
                };
                timer.schedule(timerTask, 5 * 1000);
                try {
                    Thread.sleep(15 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    public int getContentViewResId() {
        return R.layout.activity_main;
    }

    private void finishUploadVideo() {
        //已经全部上传完毕，开启删除文件的操作，并分发下一个任务
        int next = taskId + 1;
        sp.putBoolean("taskFirstBegin", true);
        new Thread(() -> {
            sp.putInt("currentTaskId", next);
            ArrayList<File> files = Util.orderByDate(path);
            //记录下载并上传完成后的最后一条视频的修改时间，方便下个任务如果文件数目超过后删除的判定
            if (files.size() > 0) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date start = sdf.parse(sdf.format(new Date(files.get(0).lastModified() + 30 * 1000)));
                    sp.putLong("lastMovieTime", Objects.requireNonNull(start).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            Log.e("aaa", "---------------------上传完毕，准备删除文件，next-----------------------" + next);
            uploadInfoToServer();
        }).start();
    }

    private void uploadInfoToServer() {
        List<UploadEnty> uploadEntyList = greenUploadDao.loadAll();

        StringBuilder stringBuilder = new StringBuilder();
        for (UploadEnty uploadEnty : uploadEntyList) {
            stringBuilder.append(uploadEnty.getCause()).append("---").append(uploadEnty.getFileName()).append("\n");
        }
        StringBuilder sb = new StringBuilder(sp.getString("customDate"));
        sb.insert(4, "-").insert(7, "-");
        String titleString = (sp.getInt("projectId") == 0 ? "衢州项目" : "浙大项目") + "\n" +
                "手机id = " + sp.getInt("phoneId") + "\n" +
                "任务进度 = " + sp.getInt("currentTaskId") + "/" + parts.size() + "\n" +
                "上传成功统计 = " + (maps.size() - uploadEntyList.size()) + "/" + maps.size() + "\n" +
                "下载跳过统计 = " + uploadEntyList.size() + "/" + maps.size() + "\n\n";
        String errorPath = writeToFile(this, titleString + stringBuilder.toString(), sb.toString());
        String remote_errorPath = "/hub.devops.intelab.cloud/a_zhuguirui/" + sb.toString() + "/";

        SFTPUtils sftp = new SFTPUtils("40.73.40.129", "gr.zhu", "o1uNF7f5m0", 50022);
        sftp.connect();
        if (!sftp.isDirExist(remote_errorPath)) {
            sftp.myMkdirs(remote_errorPath);
        }
        boolean isUploadSuccess = true;
        while (isUploadSuccess) {
            isUploadSuccess = !sftp.bacthUploadFile(remote_errorPath, errorPath, false);
        }

        sftp.disconnect();
        Message message = Message.obtain();
        message.what = UPLOAD_SERVER_SUCCESS;
        mHandler.sendMessage(message);
    }

    private int phoneId = -1;//衢州的手机id
    private int taskId = 0;//当前任务id

    @Override
    public void init(Bundle savedInstanceState) {
        greenEntyDao = MainApplication.getInstances().getDaoSession().getSuccessEntyDao();
        greenPicDao = MainApplication.getInstances().getDaoSession().getSuccessPicEntyDao();
        greenUsefulDao = MainApplication.getInstances().getDaoSession().getUsefulEntyDao();
        greenErrorDao = MainApplication.getInstances().getDaoSession().getErrorFileDao();
        greenUploadDao = MainApplication.getInstances().getDaoSession().getUploadEntyDao();
        Log.e("aaa", "***********************************");
        List<UploadEnty> uploadEntyList = greenUploadDao.loadAll();
        Log.e("aaa", "size:" + uploadEntyList.size());
        for (UploadEnty uploadEnty : uploadEntyList) {
            Log.e("aaa", uploadEnty.getCause() + "---" + uploadEnty.getFileName());
        }
        Log.e("aaa", "***********************************");

        initRv();
        initTimePrompt();
        //启动双线程守护
        startService(new Intent(this, LocalService.class));
        timerUtil();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTimeReceiver);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void judgeTask(CheckUpdateByPatch checkUpdateByPatch, String msg) {
        if (checkUpdateByPatch != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("警告--软件有新版本");
            StringBuilder sb = new StringBuilder();
            sb.append("请勿在运行中的软件升级app，取消将继续执行任务");
            if (msg != null) {
                sb.append("\n\n更新内容：\n").append(msg);
            }
            builder.setMessage(sb.toString());
            builder.setPositiveButton("确定", (dialog, which) -> {
                RxTimerUtil.getInstance().cancel();
                checkUpdateByPatch.downloadFile();
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                if (!isRunning.get()) {
                    isRunning.set(true);
                    doTask();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            RxTimerUtil.getInstance().timer(10 * 1000, number -> {
                if (!isRunning.get()) {
                    isRunning.set(true);
                    dialog.dismiss();
                    doTask();
                }
            });
        } else {
            doTask();
        }
    }

    @SuppressLint("SetTextI18n")
    private void doTask() {
        Log.e("aaa", "---------------------judgeTask一次-----------------------");
        //浙大 1,衢州 0

        int projectId = sp.getInt("projectId", -1);
        if (projectId != -1) {

            //设置项目选择按钮不可见
            llProject.setVisibility(View.GONE);

            //初始化YSY
            new TokenHelper(mHandler).initToken(projectId);

            //判断手机id
            phoneId = sp.getInt("phoneId", -1);
            if (phoneId == -1) {
                llPhoneSelect.setVisibility(View.VISIBLE);
                Toast.makeText(this, "请选择手机id", Toast.LENGTH_SHORT).show();
                return;
            }

            //区分显示
            if (projectId == 0) {
                tvPhone.setText("正在执行 --衢州项目，手机id==" + phoneId);
            } else if (projectId == 1) {
                tvPhone.setText("正在执行 --浙大项目，手机id==" + phoneId);
            }


            //判断是否可以工作
            btStartwork.setVisibility(View.VISIBLE);
            boolean canwork = sp.getBoolean("canwork", false);
            if (!canwork) {
                btStartwork.setText("已关闭，点击开启");
                btStartwork.setBackgroundColor(getResources().getColor(R.color.red));
                return;
            }
            btStartwork.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            btStartwork.setText("已开启,不要乱点");
            btnDate.setVisibility(View.GONE);
            //判断当前任务
            String data = sp.getString("parts", null);
            if (data != null) {
                //获取今天的已扫描云文件
                parts = new Gson().fromJson(data, new TypeToken<List<List<CloudPartInfoFile>>>() {
                }.getType());
                for (int i = 0; i < parts.size(); i++) {
                    tasks.add(new TaskEnty(i, parts.get(i).size()));
                }
                adapater.notifyDataSetChanged();
                String day = sp.getString("customDate", Util.getYesterday());

                //TODO:获取当前的任务id
                //sp.putInt("currentTaskId",1);//设置当前part

                taskId = sp.getInt("currentTaskId");
                tvTask.setText("正在执行 --分任务" + (taskId + 1) + "，任务总数==" + parts.size() + "，时间:" + day);
                //开始分发任务
                if (taskId < parts.size()) {
                    maps = Util.convert(parts.get(taskId));
                    Log.e("aaa", "正在执行---分任务==" + (taskId + 1) + ",任务总数==" + parts.size());
                    boolean isFirst = sp.getBoolean("taskFirstBegin", false);
                    if (isFirst) {
                        sp.putBoolean("taskFirstBegin", false);
                    }
                    distributeTask(taskId, maps, isFirst);
                } else {
                    //如果是衢州项目执行完了，就接着执行浙大的
                    if (projectId == 0) {
                        sp.putBoolean("canwork", true);
                        sp.putInt("projectId", 1);
                        sp.remove("parts");
                        sp.putInt("currentTaskId", 0);
                        new DeleteFileThread(this, true).start();
                        return;
                    }
                    //浙大的也执行完了
                    int quzhouSize = sp.getInt("quzhoucloudsize");
                    int zhedaSize = sp.getInt("zhedacloudsize");
                    tvTask.setText(day + "所有任务(" + "衢州" + quzhouSize + "," + "浙大" + zhedaSize + "\n已经完成,等待第二天任务...");
                    new DeleteFileThread(this, false).start();
                    if (sp.getInt("screenOff", 0) == 0 && !sp.getBoolean("noManMode", false)) {
                        sp.putInt("screenOff", 1);
                        screenOff();
                    }

                }
            } else {
                tvDownload.setText("正在查询云视频列表。。。");
                //今天的未扫描云文件
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        queryHelper = new QueryHelper(mHandler);
                        queryHelper.startQueryCould(phoneId);
                    }
                }, 20000);
            }
            btnOne.setVisibility(View.INVISIBLE);
            btnTwo.setVisibility(View.INVISIBLE);
            btnThree.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    //确保只进入一次,主要做文件转化
    boolean flag = false;

    @SuppressLint("NewApi")
        //任务分发
    void distributeTask(int taskId, Map<String, EZCloudRecordFile> fileMap, boolean isFirst) {
        if (sp.getString("savePic" + taskId, "error").equals("ok")) {
            Log.e("aaa", "图片任务上次已完成，开始视频任务");
            if (isFirst) {
                new DeleteFileThread(this, false).start();
            }
            downAndUploadVideo();
        } else {
            Log.e("aaa", "开始图片任务");
            //获取图片url列表
            List<EZCloudRecordFile> imageList = new ArrayList<>(fileMap.values());
            Log.e("aaa", "size:" + imageList.size());
            List<SuccessPicEnty> successPicEnties = greenPicDao.loadAll();
            List<EZCloudRecordFile> dropList = new ArrayList<>();
            for (EZCloudRecordFile ezCloudRecordFile : imageList) {
                for (SuccessPicEnty successPicEnty : successPicEnties) {
                    if (ezCloudRecordFile.getFileId().equals(successPicEnty.getFileId())) {
                        dropList.add(ezCloudRecordFile);
                    }
                }
            }
            imageList.removeAll(dropList);
            //下载并上传图片
            //上下文，handler，类型（1、2），TextView（上传控件）、列表、任务序号
            uploadHelper = new UploadHelper(this, mHandler, 2, tvUpload, imageList, taskId);
            uploadHelper.execute();
        }
    }

    void convertFiles() {
        if (flag) {
            return;
        }
        flag = true;
        Glide.get(this).clearMemory();
        Glide.get(this).clearDiskCache();
        new Thread(() -> {
            List<CloudPartInfoFile> cloudPartInfoFiles = queryHelper.getCloudPartInfoFiles();
            //将文件分批保存
            List<List<CloudPartInfoFile>> parts = Util.splitList(cloudPartInfoFiles, PARTITION_SIZE);
            Gson gson = new Gson();
            String data = gson.toJson(parts);
            //将总的云文件等分保存
            sp.putString("parts", data);
            //保存当前任务id为 0
            sp.putInt("currentTaskId", 0);
            for (int i = 0; i < parts.size(); i++) {
                sp.putString("savePic" + i, "error");
            }
            runOnUiThread(() -> tvTask.setText("正在重启中..."));
            clean_greenDao();
            Log.e("aaa", "---------------------准备重启，开始干活了-----------------------");
            restartApp(this, 7000);
        }).start();
    }

    @SuppressLint("SetTextI18n")
    @OnClick({R.id.btn_one, R.id.btn_two, R.id.btn_three, R.id.btn_zheda, R.id.btn_quzhou, R.id.bt_startwork, R.id.btn_date})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_one:
                sp.putInt("phoneId", 0);
                tvPhone.setText((sp.getInt("projectId") == 0 ? "衢州任务---" : "浙大任务---") + "手机id==0");
                btnOne.setVisibility(View.INVISIBLE);
                btnTwo.setVisibility(View.INVISIBLE);
                btnThree.setVisibility(View.INVISIBLE);
                btnDate.setVisibility(View.VISIBLE);
                btStartwork.setVisibility(View.VISIBLE);
                Toast.makeText(this, "请选择工作状态", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_two:
                sp.putInt("phoneId", 1);
                tvPhone.setText((sp.getInt("projectId") == 0 ? "衢州任务---" : "浙大任务---") + "手机id==1");
                btnOne.setVisibility(View.INVISIBLE);
                btnTwo.setVisibility(View.INVISIBLE);
                btnThree.setVisibility(View.INVISIBLE);
                btnDate.setVisibility(View.VISIBLE);
                btStartwork.setVisibility(View.VISIBLE);
                Toast.makeText(this, "请选择工作状态", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_three:
                sp.putInt("phoneId", 2);
                tvPhone.setText((sp.getInt("projectId") == 0 ? "衢州任务---" : "浙大任务---") + "手机id==2");
                btnOne.setVisibility(View.INVISIBLE);
                btnTwo.setVisibility(View.INVISIBLE);
                btnThree.setVisibility(View.INVISIBLE);
                btnDate.setVisibility(View.VISIBLE);
                btStartwork.setVisibility(View.VISIBLE);
                Toast.makeText(this, "请选择工作状态", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_zheda:
                sp.putInt("projectId", 1);
                llProject.setVisibility(View.GONE);
                llPhoneSelect.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_quzhou:
                sp.putInt("projectId", 0);
                llProject.setVisibility(View.GONE);
                llPhoneSelect.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_startwork:
                boolean canwork = sp.getBoolean("canwork", false);
                if (canwork) {
                    sp.putBoolean("canwork", false);
                    btStartwork.setBackgroundColor(getResources().getColor(R.color.red));
                    btStartwork.setText("已关闭");
                } else {
                    sp.putBoolean("canwork", true);
                    btStartwork.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    btStartwork.setText("已开启");
                }
                Toast.makeText(this, "正在重启程序生效", Toast.LENGTH_SHORT).show();
                restartApp(this, 2000);
                break;

            case R.id.btn_date:
                Calendar d = Calendar.getInstance(Locale.CHINA);
                // 创建一个日历引用d，通过静态方法getInstance() 从指定时区 Locale.CHINA 获得一个日期实例
                Date myDate = new Date();
                // 创建一个Date实例
                d.setTime(myDate);
                // 设置日历的时间，把一个新建Date实例myDate传入
                int year = d.get(Calendar.YEAR);
                int month = d.get(Calendar.MONTH);
                int day = d.get(Calendar.DAY_OF_MONTH);
                //初始化默认日期year, month, day
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyDatePickerDialogTheme, (view1, year1, monthOfYear, dayOfMonth) -> {
                    String str_m = (monthOfYear + 1) > 9 ? (monthOfYear + 1) + "" : "0" + (monthOfYear + 1);
                    String str_d = dayOfMonth > 9 ? dayOfMonth + "" : "0" + dayOfMonth;
                    String str = year1 + str_m + str_d;
                    sp.putString("customDate", str);
                    Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                }, year, month, day);
                DatePicker dp = datePickerDialog.getDatePicker();
                dp.setMaxDate(new Date().getTime() - 1000 * 60 * 60 * 24);
                datePickerDialog.show();
                break;
        }
    }

    private void initTimePrompt() {
        IntentFilter timeFilter = new IntentFilter();
        timeFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, timeFilter);
    }

    void initRv() {
        adapater = new MyAdapter(tasks);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);//recycler垂直放置
        mRecycleview.setLayoutManager(layoutManager);
        mRecycleview.setAdapter(adapater);
    }

    //上传下载视频
    private void downAndUploadVideo() {
        //获取sd卡视频文件
        Map<String, FileEntity> existFiles = Util.scanFiles(1);
        List<SuccessEnty> success = greenEntyDao.loadAll();
        List<UploadEnty> uploadEntyList = greenUploadDao.loadAll();
        Log.e("aaa", "已经上传的共计==" + success.size());
        int count = maps.size() - uploadEntyList.size();
        Log.e("aaa", "云列表总数==" + maps.size());
        Log.e("aaa", "跳过文件总数==" + uploadEntyList.size());
        Log.e("aaa", "sd卡中视频数理论应为==" + count);
        Log.e("aaa", "sd卡文件数量==" + existFiles.size());
        tvPhone.append("，sd卡中有:" + existFiles.size());
        if (existFiles.size() == count) {
            //已经全部下载完毕，开始上传任务
            now_downAndUp(success, existFiles);
        } else if (existFiles.size() > count) {
            if (sp.getLong("lastMovieTime", 0) != 0) {
                Message message = Message.obtain();
                message.what = SHOW_STRING;
                message.obj = "文件数目异常，重拉未下载文件";
                mHandler.sendMessage(message);
                new Thread(() -> {
                    //删除上个任务最后一个文件修改时间以前的视频
                    Util.removeFileByTime(sp.getLong("lastMovieTime"), path);
                    Util.scanSD_ValidVideo(true, 0);
                    sp.remove("lastMovieTime");
                    restartApp(this, 2000);
                }).start();
            } else {
                now_downAndUp(success, existFiles);
            }
        } else {
            //说明还没有完全下载完，开启下载任务
            //去除掉已经下载成功的;
            Map<String, EZCloudRecordFile> clearMap = new HashMap<>(maps);
            for (String key : existFiles.keySet()) {
                clearMap.remove(key);
            }
            for (UploadEnty uploadEnty : uploadEntyList) {
                String[] strs = uploadEnty.getFileName().split("_")[0].split("/");
                String fieldId = strs[strs.length - 1];
                clearMap.remove(fieldId);
            }
            Log.e("aaa", "需要下载的数量==" + clearMap.size());
            downSize = clearMap.size();
            tvDownload.setText("准备下载中...");
            downloadHelper = new DownloadHelper(this, mHandler, clearMap);
            downloadHelper.execute();
        }
    }

    private void now_downAndUp(List<SuccessEnty> success, Map<String, FileEntity> existFiles) {
        if (success.size() > 0) {
            for (SuccessEnty enty : success) {
                existFiles.remove(enty.getFileId());
            }
        }
        //清理无效视频
        new Thread(() -> {
            Message msg = Message.obtain();
            msg.what = SHOW_STRING;
            msg.obj = "正在清理无效视频...";
            mHandler.sendMessage(msg);
            List<FileEntity> uploadVideoList = new ArrayList<>(existFiles.values());
            Message m = Message.obtain();
            msg.what = SHOW_STRING;
            if (Util.scanSD_ValidVideo(true, 0)) {
                m.obj = "清理成功，准备上传";
                //待上传的视频数量
                Log.e("aaa", "---------待上传共计--------" + uploadVideoList.size());
                new UploadHelper(this, 1, tvUpload, mHandler, uploadVideoList).execute();
            } else {
                m.obj = "仍存在无效视频，准备重启并重新下载";
                restartApp(this, 2000);
                Log.e("aaa", "重启中...");
            }
            mHandler.sendMessage(m);
        }).start();
    }

    private void clean_greenDao() {
        greenUsefulDao.deleteAll();
        greenEntyDao.deleteAll();
        greenPicDao.deleteAll();
        greenErrorDao.deleteAll();
        greenUploadDao.deleteAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        boolean isNoManMode = sp.getBoolean("noManMode", false);
        if (isNoManMode) {
            menu.getItem(2).setTitle("已打开值守");
        } else {
            menu.getItem(2).setTitle("未打开值守");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.recovery:
                String[] gender = {"删除视频(完全重置，类似重装)", "不删除视频(继续执行part1任务)"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("请选择要清空的类型");
                builder.setItems(gender, (dialog, which) -> {
                    sp.clear();
                    clean_greenDao();
                    restartApp(this, 2000);
                    if (which == 0) {
                        new DeleteFileThread(this, true).start();
                    }
                });
                builder.show();
                break;
            case R.id.about:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("APP版本号:" + getAppVersionName(this));
                builder1.show();
                break;
            case R.id.task:
                Call<ResponseBody> myCall = NetWork.getRequest().getCompleteTaskDateList();
                myCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String reg = "<a href=\"20(.*?)/</a>";
                            Pattern pattern = Pattern.compile(reg);
                            Matcher matcher = pattern.matcher(Objects.requireNonNull(response.body()).string());
                            List<String> dateList = new ArrayList<>();
                            while (matcher.find()) {
                                dateList.add(Objects.requireNonNull(matcher.group(1)).split("/\">")[1]);
                            }
                            showDatePickerDialog(dateList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "网络异常，请重试", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.noman:
                boolean isNoManMode = sp.getBoolean("noManMode", false);
                if (isNoManMode) {
                    sp.putBoolean("noManMode", false);
                    item.setTitle("未打开值守");
                } else {
                    sp.putBoolean("noManMode", true);
                    item.setTitle("已打开值守");
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void showDatePickerDialog(List<String> dateList) {
        Calendar d = Calendar.getInstance(Locale.CHINA);
        Date myDate = new Date();
        d.setTime(myDate);
        int year = d.get(Calendar.YEAR);
        int month = d.get(Calendar.MONTH);
        int day = d.get(Calendar.DAY_OF_MONTH);
        //初始化默认日期year, month, day
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyDatePickerDialogTheme, (view1, year1, monthOfYear, dayOfMonth) -> {
            String str_m = (monthOfYear + 1) > 9 ? (monthOfYear + 1) + "" : "0" + (monthOfYear + 1);
            String str_d = dayOfMonth > 9 ? dayOfMonth + "" : "0" + dayOfMonth;
            String date = year1 + "-" + str_m + "-" + str_d;
            if (dateList.contains(date)) {
                Call<ResponseBody> myCall = NetWork.getRequest().getDateTaskList(date);
                myCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String reg = "<a href=\"(.*?)</a>";
                            Pattern pattern = Pattern.compile(reg);
                            Matcher matcher = pattern.matcher(Objects.requireNonNull(response.body()).string());
                            List<String> taskList = new ArrayList<>();
                            while (matcher.find()) {
                                if (Objects.requireNonNull(matcher.group(1)).contains("txt")) {
                                    taskList.add(Objects.requireNonNull(matcher.group(1)).split(">")[1].replace(".txt", ""));
                                }
                            }
                            String[] task = taskList.toArray(new String[0]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("请选择要查看的时间");
                            builder.setItems(task, (dialog, which) -> {
                                try {
                                    Field field = Objects.requireNonNull(Objects.requireNonNull(dialog.getClass().getSuperclass()).getSuperclass()).getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                showTask(date, task[which]);
                            });
                            builder.setNegativeButton("关闭", (dialog, which) -> {
                                try {
                                    Field field = Objects.requireNonNull(Objects.requireNonNull(dialog.getClass().getSuperclass()).getSuperclass()).getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            globalAlertDialog = builder.create();
                            globalAlertDialog.setCanceledOnTouchOutside(false);
                            globalAlertDialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "网络异常，请重试", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "没有找到" + date + "的任务", Toast.LENGTH_LONG).show();
            }
        }, year, month, day);
        DatePicker dp = datePickerDialog.getDatePicker();
        dp.setMaxDate(new Date().getTime() - 1000 * 60 * 60 * 24);
        datePickerDialog.show();
    }

    private void showTask(String date, String task) {
        Call<ResponseBody> myCall = NetWork.getRequest().getTask(date, task + ".txt");
        myCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String body = Objects.requireNonNull(response.body()).string();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    String[] bodyspl = body.split("\n\n");
                    StringBuilder sb = new StringBuilder();
                    sb.append(bodyspl[0]);
                    if (bodyspl.length == 2) {
                        sb.append("\n\n").append(bodyspl[1].replace(" ", "")
                                .replace("/storage/emulated/0/aaa/CloudFile/", "")
                                .replace("\n", "\n\n"));
                    }
                    builder.setMessage(sb.toString());
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    WindowManager.LayoutParams lp = Objects.requireNonNull(alertDialog.getWindow()).getAttributes();
                    DisplayMetrics metric = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getRealMetrics(metric);
                    lp.height = Double.valueOf(metric.heightPixels * 0.8).intValue(); // 高度（PX）
                    alertDialog.getWindow().setAttributes(lp);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = DISSMISS_DIALOG;
                    mHandler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "网络异常，请重试", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void timerUtil() {
        try {
            timer.cancel();
            timer = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> screenLightDown(1));
                }
            }, 30 * 1000);
        }
    }

    private void screenLightDown(int light) {
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        localLayoutParams.screenBrightness = light / 255.0F;
        getWindow().setAttributes(localLayoutParams);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (!DoubleClickUtil.isDoubleClick(300)) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                screenLightDown(150);
                timerUtil();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}