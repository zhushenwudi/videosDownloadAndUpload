package com.ilab.testysy.utils;

import android.view.View;

/**
 * Created by Admin on 2017/5/18.
 * 防止按钮连击工具类
 */

public class DoubleClickUtil {

    private static long mLastClick;

    public static boolean isDoubleClick(long milliseconds) {
        //大于milliseconds秒方可通过
        if (System.currentTimeMillis() - mLastClick <= milliseconds) {
            return true;
        }
        mLastClick = System.currentTimeMillis();
        return false;
    }

    public static void shakeClick(final View v, long milliseconds) {
        v.setClickable(false);
        v.postDelayed(() -> v.setClickable(true), milliseconds);
    }
}