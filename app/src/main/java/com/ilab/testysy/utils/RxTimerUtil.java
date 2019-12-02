package com.ilab.testysy.utils;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;


public class RxTimerUtil {

    private static RxTimerUtil rxTimerUtil;
    private static Disposable mDisposable;

    private RxTimerUtil() {
    }

    public static RxTimerUtil getInstance() {
        if (rxTimerUtil == null) {
            synchronized (RxTimerUtil.class) {
                if (rxTimerUtil == null) {
                    rxTimerUtil = new RxTimerUtil();
                }
            }
        }
        return rxTimerUtil;
    }

    /**
     * 延时器
     *
     * @param milliseconds 毫秒后
     * @param next         下一个操作
     */
    public void timer(long milliseconds, final IRxNext next) {
        Observable.timer(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        if (next != null) {
                            next.doNext(number);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        //取消订阅
                        cancel();
                    }

                    @Override
                    public void onComplete() {
                        //取消订阅
                        cancel();
                    }
                });
    }


    /**
     * 定时器
     *
     * @param initialDelay 第一次执行延时
     * @param period       第n次执行延时(n>1)
     * @param next         下一个操作
     */
    public void interval(long initialDelay, long period, final IRxNext next) {
        Observable.interval(initialDelay, period, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        if (next != null) {
                            next.doNext(number);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 取消订阅
     */
    public void cancel() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
            Log.e("aaa", "====定时器取消======");
        }

    }

    public boolean status() {
        return mDisposable != null && !mDisposable.isDisposed();
    }

    public interface IRxNext {
        void doNext(long number);
    }
}