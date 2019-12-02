package com.ilab.testysy.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.ilab.testysy.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler INSTANCE;
    private Context mContext;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (CrashHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CrashHandler();
                }
            }
        }

        return INSTANCE;
    }

    public static String[] getCrashReportFiles(Context ctx) {
        File filesDir = new File(getCrashFilePath(ctx));
        String[] fileNames = filesDir.list();
        int length = fileNames.length;
        String[] filePaths = new String[length];

        for (int i = 0; i < length; ++i) {
            filePaths[i] = getCrashFilePath(ctx) + fileNames[i];
        }

        return filePaths;
    }

    private static String getCrashFilePath(Context context) {
        String path = null;

        try {
            path = Environment.getExternalStorageDirectory().getCanonicalPath() + "/" + context.getResources().getString(R.string.app_name) + "/Crash/";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (IOException var3) {
            var3.printStackTrace();
        }
        return path;
    }

    public void init(Context ctx) {
        this.mContext = ctx;
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        this.handleException(ex);
        if (this.mDefaultHandler != null) {
            this.mDefaultHandler.uncaughtException(thread, ex);
        }

    }

    private void handleException(Throwable ex) {
        if (ex == null) {
            Log.w("NewCrashHandler", "handleException--- ex==null");
        } else {
            String msg = ex.getLocalizedMessage();
            if (msg != null) {
                this.saveCrashInfoToFile(ex);
            }
            //使用Toast来显示异常信息
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();

                    Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }.start();
        }
    }

    private void saveCrashInfoToFile(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);

        for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
            cause.printStackTrace(printWriter);
        }

        String result = info.toString();
        printWriter.close();
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String now = sdf.format(new Date());
        sb.append("TIME:").append(now);
        sb.append("\nAPPLICATION_ID:").append("com.ilab.checkysy");
        sb.append("\nVERSION_CODE:").append(1);
        sb.append("\nVERSION_NAME:").append("1.0");
        sb.append("\nBUILD_TYPE:").append("release");
        sb.append("\nMODEL:").append(Build.MODEL);
        sb.append("\nRELEASE:").append(Build.VERSION.RELEASE);
        sb.append("\nSDK:").append(Build.VERSION.SDK_INT);
        sb.append("\nEXCEPTION:").append(ex.getLocalizedMessage());
        sb.append("\nSTACK_TRACE:").append(result);

        try {
            FileWriter writer = new FileWriter(getCrashFilePath(this.mContext) + now + ".txt");
            writer.write(sb.toString());
            writer.flush();
            writer.close();
        } catch (Exception var10) {
            var10.printStackTrace();
        }
    }
}