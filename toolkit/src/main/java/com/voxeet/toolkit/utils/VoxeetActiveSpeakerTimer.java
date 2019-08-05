package com.voxeet.toolkit.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.utils.annotate;

@annotate
public class VoxeetActiveSpeakerTimer {

    private ActiveSpeakerListener listener;
    private String currentActiveSpeaker;

    private Handler handler;
    private Runnable refreshActiveSpeaker = new Runnable() {
        @Override
        public void run() {
            if (null != handler) {
                try {
                    if (null != listener && null != VoxeetSdk.instance()) {
                        String fromSdk = VoxeetSdk.conference().currentSpeaker();

                        if(null == currentActiveSpeaker || !currentActiveSpeaker.equals(fromSdk)) {
                            currentActiveSpeaker = fromSdk;
                            listener.onActiveSpeakerUpdated(currentActiveSpeaker);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (handler != null) {
                    handler.postDelayed(this, 1000);
                }
            }
        }
    };

    private VoxeetActiveSpeakerTimer() {

    }

    public VoxeetActiveSpeakerTimer(@NonNull ActiveSpeakerListener listener) {
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

    @Nullable
    public String getCurrentActiveSpeaker() {
        return currentActiveSpeaker;
    }

    public static interface ActiveSpeakerListener {
        void onActiveSpeakerUpdated(@Nullable String activeSpeakerUserId);
    }
}
