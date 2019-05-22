package com.voxeet.toolkit.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.sdk.core.VoxeetSdk;

public class ActiveSpeakerTimer {

    private ActiveSpeakerListener listener;

    private Handler handler;
    private Runnable refreshActiveSpeaker = new Runnable() {
        @Override
        public void run() {
            if (null != handler) {
                try {
                    if (null != listener && null != VoxeetSdk.getInstance()) {
                        listener.onActiveSpeaker(VoxeetSdk.getInstance().getConferenceService().currentSpeaker());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (handler != null) {
                    handler.postDelayed(this, 5000);
                }
            }
        }
    };

    private ActiveSpeakerTimer() {

    }

    public ActiveSpeakerTimer(@NonNull ActiveSpeakerListener listener) {
        this();

        this.listener = listener;
    }

    public void start() {
        if (null == handler) {
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(refreshActiveSpeaker, 1000);
        }
    }

    public void stop() {
        handler.removeCallbacks(refreshActiveSpeaker);
        handler = null;
    }

    public static interface ActiveSpeakerListener {
        void onActiveSpeaker(@Nullable String activeSpeakerUserId);
    }
}
