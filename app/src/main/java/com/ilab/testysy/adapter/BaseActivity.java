package com.ilab.testysy.adapter;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ilab.checkupdatebypatch.CheckUpdateByPatch;
import com.ilab.testysy.Constants;
import com.ilab.testysy.R;
import com.ilab.testysy.utils.ScreenOffAdminReceiver;
import com.race604.drawable.wave.WaveDrawable;

import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity {
    private Unbinder mUnbinder;
    private DevicePolicyManager policyManager;
    private ComponentName adminReceiver;
    String md5Url = "http://139.217.81.11:8066/downandup/videoServlet";
    //String md5Url = "http://172.31.0.59:8080/videoServlet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewResId());
        //保持屏幕常亮 Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mUnbinder = ButterKnife.bind(this);
        init(savedInstanceState);
        //获取app 资源管理器
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = Objects.requireNonNull(km).newKeyguardLock("name");
        kl.disableKeyguard();
        adminReceiver = new ComponentName(this, ScreenOffAdminReceiver.class);
        policyManager = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        checkAndTurnOnDeviceManager(null);
        checkPermissioin();
    }

    public abstract int getContentViewResId();

    public abstract void init(Bundle savedInstanceState);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    void checkPermissioin() {
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (storage == PackageManager.PERMISSION_GRANTED) {
            checkUpdateByPatch();
        } else {//没有权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.STORAGE_PERMISSION);//申请授权
        }
    }

    private void checkUpdateByPatch() {
        ImageView imageView = findViewById(R.id.imageView);
        ScrollView scrollView = findViewById(R.id.scrollView2);
        Drawable mWaveDrawable = new WaveDrawable(this, R.drawable.wave_pic);
        imageView.setImageDrawable(mWaveDrawable);
        CheckUpdateByPatch checkUpdateByPatch = new CheckUpdateByPatch(this, getApplicationInfo().packageName, md5Url, imageView, scrollView, mWaveDrawable);
        checkUpdateByPatch.setCallBack(new CheckUpdateByPatch.CallBack() {
            @Override
            public void onOverVersionSuccess(String msg) {
                judgeTask(checkUpdateByPatch, msg);
            }

            @Override
            public void onStepVersionSuccess(String msg) {
                judgeTask(checkUpdateByPatch, msg);
            }

            @Override
            public void onOldMd5Uncorrect() {
                myToast("旧版本apk的md5不正确，下载完整安装包");
                checkUpdateByPatch.motifyPackageInfo(false);
            }

            @Override
            public void onNewMd5Uncorrect() {
                myToast("新版本apk的md5不正确，下载完整安装包");
                checkUpdateByPatch.motifyPackageInfo(false);
            }

            @Override
            public void onPackageNotFound() {
                myToast("包不存在，下载完整安装包");
                checkUpdateByPatch.motifyPackageInfo(false);
            }

            @Override
            public void onError() {
                myToast("未知错误，下载完整安装包");
                checkUpdateByPatch.motifyPackageInfo(false);
            }

            @Override
            public void onInstallApkError() {
                myToast("安装apk失败，下载完整安装包");
                checkUpdateByPatch.motifyPackageInfo(false);
            }

            @Override
            public void onCheckFinish() {
                judgeTask(null, null);
            }

            @Override
            public void onCheckUnknownError() {
                myToast("服务器判断数值时发生空值异常，请联系管理员");
                judgeTask(null, null);
            }

            @Override
            public void onNetError() {
                myToast("网络异常，请稍候再试");
                judgeTask(null, null);
            }

            @Override
            public void onDownloadError() {
                myToast("升级失败，请检查完整包是否存在");
                judgeTask(null, null);
            }

            @Override
            public void onFullInstallSuccess() {
                checkUpdateByPatch.downloadFile();
            }
        });
        //连缀表达式，不写为默认值
        checkUpdateByPatch.execute();
    }

    /**
     * 熄屏
     */
    public void screenOff() {
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            policyManager.lockNow();
        } else {
            myToast("没有设备管理权限");
        }
    }

    /**
     * @param view 检测并去激活设备管理器权限
     */
    public void checkAndTurnOnDeviceManager(View view) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getResources().getString(R.string.message));
        startActivityForResult(intent, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkUpdateByPatch();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public abstract void judgeTask(CheckUpdateByPatch checkUpdateByPatch, String msg);

    //自定义Toast
    private void myToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }
}
