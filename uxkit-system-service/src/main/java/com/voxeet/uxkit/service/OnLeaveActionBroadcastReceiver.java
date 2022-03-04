package com.voxeet.uxkit.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.voxeet.VoxeetSDK;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.services.conference.information.ConferenceStatus;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

public class OnLeaveActionBroadcastReceiver extends BroadcastReceiver {

    private final static ShortLogger Log = UXKitLogger.createLogger(OnLeaveActionBroadcastReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        ConferenceStatus status = Opt.of(VoxeetSDK.conference().getConference())
                .then(Conference::getState).or(ConferenceStatus.ERROR);

        if(!ConferenceStatus.JOINED.equals(status)) return;

        VoxeetSDK.conference().leave().then(success -> {
            Log.d("left conference ? " + success);
        }).error(error -> Log.e("error while leaving", error));
    }
}
