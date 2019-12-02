package com.ilab.testysy.helpers;

import android.content.Context;
import android.util.Log;

import com.ilab.testysy.Constants;
import com.ilab.testysy.MainApplication;

import java.io.File;
import java.util.Objects;

import static com.ilab.testysy.utils.Util.restartApp;

public class DeleteFileThread extends Thread {
    private boolean restart;//是否重启
    private Context context;

    public DeleteFileThread(Context context, boolean shouldRestart) {
        restart = shouldRestart;
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        Log.e("aaa", "------------删除全部文件开始------------");
        MainApplication.getInstances().getDaoSession().getSuccessEntyDao().deleteAll();
        File file = new File(Constants.path);
        File[] files = file.listFiles();
        for (File value : Objects.requireNonNull(files)) {
            value.delete();
        }
        Log.e("aaa", "------------删除全部文件结束------------");
        if (restart) {
            Log.e("aaa", "------------删除完毕重启，准备下一个任务------------");
            restartApp(context, 5000);
        }
    }
}
