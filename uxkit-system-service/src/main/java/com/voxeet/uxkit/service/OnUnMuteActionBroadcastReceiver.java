package com.voxeet.uxkit.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.voxeet.VoxeetSDK;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

public class OnUnMuteActionBroadcastReceiver extends BroadcastReceiver {

    private final static ShortLogger Log = UXKitLogger.createLogger(OnUnMuteActionBroadcastReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReceive :: unmuting");
        VoxeetSDK.conference().mute(false);
    }
}
