package com.ilab.testysy.utils;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class ScreenOffAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {

    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {

    }
}