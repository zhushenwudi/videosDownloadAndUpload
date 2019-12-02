package com.ilab.testysy.entity;

/**
 * Created by loveMeeko on 2018/1/15.
 */

public class AccessToken {

    /**
     * data : {"accessToken":"at.clikzeer4nqqb0n84gzk4ghz101wmmx3-1l9ps18rwx-1d4m77p-uk8tajyph","expireTime":1564656902225}
     * code : 200
     * msg : 操作成功!
     */

    private DataBean data;
    private String code;
    private String msg;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static class DataBean {
        /**
         * accessToken : at.clikzeer4nqqb0n84gzk4ghz101wmmx3-1l9ps18rwx-1d4m77p-uk8tajyph
         * expireTime : 1564656902225
         */

        private String accessToken;
        private long expireTime;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }
    }
}
