package com.ilab.checkupdatebypatch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.ilab.checkupdatebypatch.bean.OverInstall;
import com.ilab.checkupdatebypatch.bean.StepInstall;
import com.ilab.checkupdatebypatch.utils.ApkUtils;
import com.ilab.checkupdatebypatch.utils.PatchUtils;
import com.ilab.checkupdatebypatch.utils.SignUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class CheckUpdateByPatch {
    private static final int SUCCESS = 1;
    private static final int OLD_MD5_UNCORRECT = 2;
    private static final int NEW_MD5_UNCORRECT = 3;
    private static final int ERROR = 4;
    private static final int PACKAGE_NOT_FOUND = 5;

    private static String path = Environment.getExternalStorageDirectory().getPath() + File.separator;
    private static Context context;
    private static String mCurentRealMD5, mNewRealMD5;
    private static PackageInfo packageInfo;
    private static String md5PostApiUrl;
    private static String patchFileName = "patchfile.patch";
    private static String newApkName = "new.apk";
    private static String packageName;
    private static String latestVersionFlag = "new";
    private static String unknownError = "error";
    private static CallBack callBack;
    private static String FILEURL;
    //true: Patch更新  false: APK更新
    private static boolean flag = false;
    private ImageView imageView;
    private ScrollView scrollView;
    private Drawable mWaveDrawable;

    static {
        System.loadLibrary("ApkPatchLibrary");
    }

    /**
     * 构造函数
     *
     * @param context       上下文
     * @param packageName   程序包名
     * @param md5PostApiUrl 获取新旧版本MD5值的接口URL
     * @param scrollView
     * @param mWaveDrawable
     */
    public CheckUpdateByPatch(Context context, String packageName, String md5PostApiUrl, ImageView imageView, ScrollView scrollView, Drawable mWaveDrawable) {
        CheckUpdateByPatch.context = context.getApplicationContext();
        CheckUpdateByPatch.packageName = packageName;
        CheckUpdateByPatch.md5PostApiUrl = md5PostApiUrl;
        this.imageView = imageView;
        this.scrollView = scrollView;
        this.mWaveDrawable = mWaveDrawable;
    }

    //设置Patch文件名称(默认为 : patchfile.patch)
    public CheckUpdateByPatch setPatchName(String patchFileName) {
        CheckUpdateByPatch.patchFileName = patchFileName;
        return this;
    }

    //设置文件缓存路径(默认为 : 根目录)
    public CheckUpdateByPatch setPath(String path) {
        CheckUpdateByPatch.path = path;
        return this;
    }

    //设置融合后的APK名称(默认为 : new.apk)
    public CheckUpdateByPatch setApkName(String newApkName) {
        CheckUpdateByPatch.newApkName = newApkName;
        return this;
    }

    //设置当前版本为最新版APK时接口返回的值(默认为 : new)
    public CheckUpdateByPatch setLatestVersionFlag(String latestVersionFlag) {
        CheckUpdateByPatch.latestVersionFlag = latestVersionFlag;
        return this;
    }

    //设置回调接口
    public void setCallBack(CallBack callBack) {
        CheckUpdateByPatch.callBack = callBack;
    }

    public void execute() {
        try {
            File file;
            file = new File(path + newApkName);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            file = new File(path + patchFileName);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //是否按照真实值选择差量还是完整更新
        motifyPackageInfo(true);
    }

    //安装新的APK
    private static void installApk() {
        if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(newApkName)) {
            File file = new File(path + newApkName);
            if (file.exists() && file.isFile()) {
                ApkUtils.installApk(context, path + newApkName, packageName);
            } else {
                flag = false;
                callBack.onInstallApkError();
            }
        } else {
            flag = false;
            callBack.onInstallApkError();
        }
    }

    /**
     *
     * @param isTrue 是否传入真实数据，如果为假，则可从服务器返回最新的安装包的地址
     */
    public void motifyPackageInfo(boolean isTrue) {
        if (isTrue) {
            packageInfo = ApkUtils.getInstalledApkPackageInfo(context, packageName);
            if (packageInfo != null) {
                //检查MD5，判断是否是最新版本
                requestOldMD5(packageInfo.versionCode + "", packageInfo.versionName, true);
            }
        } else {
            //获取完整包在服务器上的路径
            requestOldMD5("1", "1.0.0", false);
        }
    }

    /**
     * 请求服务器，根据当前安装客户端的versionCode、versionName，来获取其文件的正确MD5，防止本地安装的是被篡改的版本
     *
     * @param versionCode 程序包名
     */
    private void requestOldMD5(String versionCode, String versionName, final boolean isTrue) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("versionCode", versionCode + "");
            obj.put("versionName", versionName);
            RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), obj.toString());
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build();
            OkGo.getInstance().setOkHttpClient(okHttpClient);
            OkGo.<String>post(md5PostApiUrl)
                    .tag(context)
                    .upRequestBody(body)
                    .retryCount(3)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            if (latestVersionFlag.equals(response.body())) {
                                scrollView.setVisibility(View.VISIBLE);
                                callBack.onCheckFinish();
                            } else if (unknownError.equals(response.body())) {
                                scrollView.setVisibility(View.VISIBLE);
                                callBack.onCheckUnknownError();
                            } else if (!response.body().contains("{")) {
                                //跨版本升级，直接下载最新的APK
                                OverInstall overInstall = new Gson().fromJson(response.body(), OverInstall.class);
                                FILEURL = overInstall.getPath();
                                if (isTrue) {
                                    //默认回调
                                    callBack.onOverVersionSuccess(overInstall.getMessage());
                                } else {
                                    //已经得到授权直接下载并安装
                                    callBack.onFullInstallSuccess();
                                }
                            } else {
                                StepInstall stepInstall = new Gson().fromJson(response.body(), StepInstall.class);
                                //解析json后拿到对应版本apk的md5和最新版本apk的md5
                                mCurentRealMD5 = stepInstall.getOldMd5();
                                mNewRealMD5 = stepInstall.getNewMd5();
                                FILEURL = stepInstall.getPatchFile();
                                flag = true;
                                callBack.onStepVersionSuccess(stepInstall.getMessage());
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            callBack.onNetError();
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void downloadApk(Context context, String url, String destFileDir, String destFileName) {
        OkGo.<File>get(url)
                .tag(context)
                .execute(new FileCallback(destFileDir, destFileName) {
                    @Override
                    public void onStart(Request<File, ? extends Request> request) {
                        super.onStart(request);
                        imageView.setVisibility(View.VISIBLE);
                        scrollView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onSuccess(Response<File> response) {
                        imageView.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        installApk();
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                        mWaveDrawable.setLevel((int) (progress.fraction * 10000));
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        imageView.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        callBack.onDownloadError();
                    }
                });
    }

    /**
     * 下载patch差量包
     *
     * @param context      上下文
     * @param url          下载完整url
     * @param destFileDir  下载目标文件夹名
     * @param destFileName 下载目标文件名
     */
    private void downloadPatch(Context context, String url, String destFileDir, String destFileName) {
        OkGo.<File>get(url)
                .tag(context)
                .execute(new FileCallback(destFileDir, destFileName) {
                    @Override
                    public void onStart(Request<File, ? extends Request> request) {
                        super.onStart(request);
                        imageView.setVisibility(View.VISIBLE);
                        scrollView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onSuccess(Response<File> response) {
                        imageView.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        new PatchApkTask().execute();
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                        mWaveDrawable.setLevel((int) (progress.fraction * 10000));
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        imageView.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        callBack.onDownloadError();
                    }
                });
    }

    public void downloadFile() {
        if (flag) {
            //下载patch
            downloadPatch(context, FILEURL, path, patchFileName);
        } else {
            //下载APK
            downloadApk(context, FILEURL, path, newApkName);
        }
    }

    public interface CallBack {
        void onOverVersionSuccess(String message);

        void onStepVersionSuccess(String msg);

        void onOldMd5Uncorrect();

        void onNewMd5Uncorrect();

        void onPackageNotFound();

        void onError();

        void onInstallApkError();

        void onCheckFinish();

        void onCheckUnknownError();

        void onNetError();

        void onDownloadError();

        void onFullInstallSuccess();
    }

    private static class PatchApkTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            //获取本地安装APK的路径
            String oldApkSource = ApkUtils.getSourceApkPath(context, packageInfo.packageName);
            if (!TextUtils.isEmpty(oldApkSource)) {
                // 校验一下本地安装APK的MD5是不是和真实的MD5一致
                if (SignUtils.checkMd5(oldApkSource, mCurentRealMD5)) {
                    //融合本地APK和Patch差量包
                    int patchResult = PatchUtils.patch(oldApkSource, path + newApkName, path + patchFileName);
                    //是否融合成功
                    if (patchResult == 0) {
                        //检查融合后的新APK的MD5和真实的MD5一致
                        if (SignUtils.checkMd5(path + newApkName, mNewRealMD5)) {
                            return SUCCESS;
                        } else {
                            return NEW_MD5_UNCORRECT;
                        }
                    } else {
                        return ERROR;
                    }
                } else {
                    return OLD_MD5_UNCORRECT;
                }
            } else {
                return PACKAGE_NOT_FOUND;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            switch (result) {
                case SUCCESS: {
                    //尝试删除缓存文件
                    try {
                        File file = new File(path + patchFileName);
                        if (file.exists() && file.isFile()) {
                            file.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    installApk();
                    break;
                }
                case OLD_MD5_UNCORRECT: {
                    flag = false;
                    callBack.onOldMd5Uncorrect();
                    break;
                }
                case NEW_MD5_UNCORRECT: {
                    flag = false;
                    callBack.onNewMd5Uncorrect();
                    break;
                }
                case PACKAGE_NOT_FOUND: {
                    flag = false;
                    callBack.onPackageNotFound();
                    break;
                }
                case ERROR: {
                    flag = false;
                    callBack.onError();
                    break;
                }
            }
        }
    }
}
