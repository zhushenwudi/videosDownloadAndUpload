package com.ilab.testysy;

import com.ilab.testysy.entity.AccessToken;
import com.ilab.testysy.entity.AccountList;
import com.ilab.testysy.entity.SubAccessToken;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 通用的 上传文件 下载文件 api
 * Created by meeko on 2018/11/12.
 */
public interface ApiService {

    @Headers({"Content-Type:application/x-www-form-urlencoded; charset=utf-8"})
    @POST("https://open.ys7.com/api/lapp/token/get")
    Observable<AccessToken> getAccessToken(@Query("appKey") String appKey, @Query("appSecret") String appSecret);

    //zheda
    @Headers({"Content-Type:application/x-www-form-urlencoded; charset=utf-8"})
    @POST("https://open.ys7.com/api/lapp/ram/token/get")
    Observable<SubAccessToken> getSubAccessToken(@Query("accessToken") String accessToken, @Query("accountId") String accountId);

    @Headers({"Content-Type:application/x-www-form-urlencoded; charset=utf-8"})
    @POST("https://open.ys7.com/api/lapp/ram/account/list")
    Observable<AccountList> getAccountList(@Query("accessToken") String accessToken, @Query("pageStart") int pageStart, @Query("pageSize") int pageSize);

    @Headers({"Content-Type:application/x-www-form-urlencoded; charset=utf-8"})
    @GET("https://hub.devops.intelab.cloud/download/a_zhuguirui/")
    Call<ResponseBody> getCompleteTaskDateList();

    @Headers({"Content-Type:application/x-www-form-urlencoded; charset=utf-8"})
    @GET("https://hub.devops.intelab.cloud/download/a_zhuguirui/{date}/")
    Call<ResponseBody> getDateTaskList(@Path("date") String date);

    @Headers({"Content-Type:application/x-www-form-urlencoded; charset=utf-8"})
    @GET("https://hub.devops.intelab.cloud/download/a_zhuguirui/{date}/{task}")
    Call<ResponseBody> getTask(@Path("date") String date, @Path("task") String task);
}
