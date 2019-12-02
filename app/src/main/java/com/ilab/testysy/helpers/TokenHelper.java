package com.ilab.testysy.helpers;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ilab.testysy.Constants;
import com.ilab.testysy.MainApplication;
import com.ilab.testysy.entity.AccessToken;
import com.ilab.testysy.entity.AccountList;
import com.ilab.testysy.entity.Login;
import com.ilab.testysy.entity.SubAccessToken;
import com.ilab.testysy.network.NetWork;
import com.ilab.testysy.network.RequestBaseObserver;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EzvizAPI;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.ilab.testysy.Constants.LOGIN_SUCCESS;
import static com.ilab.testysy.Constants.mAppKeyQuzhou;
import static com.ilab.testysy.Constants.mAppKeyZheda;
import static com.ilab.testysy.Constants.mAppSecretQuzhou;
import static com.ilab.testysy.Constants.mAppSecretZheda;
import static com.ilab.testysy.Constants.mOpenApiServer;
import static com.ilab.testysy.Constants.mOpenAuthApiServer;

public class TokenHelper {
    private Handler mHandler;
    public TokenHelper(Handler mHandler) {
        this.mHandler = mHandler;
    }
    private int type;
    public void initToken(int typ) {
        this.type = typ;
        initYSY();
    }

    void initYSY() {
        MainApplication.getInstances().initYSY(type);
        if(type==Constants.PROJECT_QUZHOU){
            //quzhou
            NetWork.getRequest().getAccessToken(mAppKeyQuzhou, mAppSecretQuzhou)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new RequestBaseObserver<AccessToken>() {
                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);

                            Message msg = new Message();
                            msg.what = Constants.TOKEN_RESULT;
                            mHandler.sendMessage(msg);
                            Log.i("csdn", "---------------------获取accesstoken失败-----------------------");
                        }

                        @Override
                        protected void onSuccess(AccessToken t) {
                            if ("200".equals(t.getCode())) {
                                Log.i("csdn", "---------------------获取accesstoken成功----------------------");
                                String token = t.getData().getAccessToken();
                                Log.i("aaa", token);
                                Log.i("csdn", "---------------------获取accesstoken成功----------------------token==" + token);
                                EZOpenSDK.getInstance().setAccessToken(token);
                                EzvizAPI.getInstance().setServerUrl(mOpenApiServer, mOpenAuthApiServer);
                                Message msg = Message.obtain();
                                msg.what = LOGIN_SUCCESS;
                                msg.arg1 = 0;
                                mHandler.sendMessage(msg);
                            }
                        }
                    });
        }

        if(type==Constants.PROJECT_ZHEDA){
            //qzheda
            NetWork.getRequest().getAccessToken(mAppKeyZheda, mAppSecretZheda)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new RequestBaseObserver<AccessToken>() {
                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            Message msg = new Message();
                            msg.what = Constants.TOKEN_RESULT;
                            mHandler.sendMessage(msg);
                            Log.i("csdn", "---------------------获取accesstoken失败-----------------------");
                        }

                        @Override
                        protected void onSuccess(AccessToken t) {
                            if ("200".equals(t.getCode())) {
                                Log.i("csdn", "---------------------获取accesstoken成功----------------------");
                                String token = t.getData().getAccessToken();
                                getAccountList(token);
                                Message msg = Message.obtain();
                                msg.what = LOGIN_SUCCESS;
                                msg.arg1 = 1;
                                mHandler.sendMessage(msg);
                            }
                        }
                    });
        }


    }

    void getAccountList(String token) {
        NetWork.getRequest().getAccountList(token,0,100)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestBaseObserver<AccountList>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.i("csdn","---------------------获取AccountList失败-----------------------");
                    }

                    @Override
                    protected void onSuccess(AccountList t) {
                        if ("200".equals(t.getCode())) {
                            Log.i("csdn","---------------------获取AccountList成功----------------------");

                            List<AccountList.DataBean> data = t.getData();
                            AccountList.DataBean beran = data.get(0);
                            getSubAccessToken(token,beran.getAccountId());
                        }
                    }
                });
    }

    void getSubAccessToken(String token,String accountId) {
        NetWork.getRequest().getSubAccessToken(token,accountId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestBaseObserver<SubAccessToken>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.i("csdn","---------------------获取SubAccessToken失败-----------------------");
                    }

                    @Override
                    protected void onSuccess(SubAccessToken t) {
                        if ("200".equals(t.getCode())) {
                            //获取token
                            String subAccessToken = t.getData().getAccessToken();
                            Log.i("csdn", "---------------------获取SubAccessToken成功---------------------subAccessToken=" + subAccessToken);
                            if(subAccessToken!=null){
                                EZOpenSDK.getInstance().setAccessToken(subAccessToken);
                                EzvizAPI.getInstance().setServerUrl(mOpenApiServer,mOpenAuthApiServer);
                                Log.i("csdn","---------------------SDK设置SubAccessToken成功----------------------");
                            }
                        }
                    }
                });
    }
}
