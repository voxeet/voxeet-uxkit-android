package com.voxeet.uxkit.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.voxeet.VoxeetSDK;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

public class OnMuteActionBroadcastReceiver extends BroadcastReceiver {

    private final static ShortLogger Log = UXKitLogger.createLogger(OnMuteActionBroadcastReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReceive :: muting");
        VoxeetSDK.conference().mute(true);
    }
}
