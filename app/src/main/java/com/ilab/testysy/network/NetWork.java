package com.ilab.testysy.network;

import android.util.Log;

import com.ilab.testysy.ApiService;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetWork {

    private static NetWork mInstance;
    private static Retrofit retrofit;
    private static volatile ApiService request = null;
    private static String token="";

    private static final String YSY = "https://open.ys7.com/";

    private static final String ILAB = "https://qz.qzwjtest.ilabservice.cloud/";

    public static NetWork getInstance() {
        if (mInstance == null) {
            synchronized (NetWork.class) {
                if (mInstance == null) {
                    mInstance = new NetWork();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化必要对象和参数
     */
    public void init() {
        // 初始化okhttp
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        Response response = null;
//                        if(token==""){
//                            token = SpTool.obtainValue("token");
//                        }
//                        Request request = chain.request()
//                                .newBuilder()
//                                .addHeader("X-Authorization", "Bearer " + token)
//                                .build();
//                        Request.Builder requestBuilder = request.newBuilder();
//                        Request newRequest = requestBuilder.build();
//                        response = chain.proceed(newRequest);
//                        return response;
//                    }
//                })
                .addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        Log.i("net", message);
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY))
                .hostnameVerifier((hostname, session) -> {
                    return true;//强行返回true 即验证成功
                })
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();

        // 初始化Retrofit
        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(ILAB)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiService getRequest() {
        if (request == null) {
            synchronized (ApiService.class) {
                request = retrofit.create(ApiService.class);
            }
        }
        return request;
    }


}
