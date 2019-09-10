package com.voxeet.toolkit.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.utils.Annotate;

/**
 * Simple Timer made to schedule interactions accross the Speakers in a conference
 * <p>
 * This class can be started, stopped and get the current active speaker
 */
@Annotate
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

                        if (null == currentActiveSpeaker || !currentActiveSpeaker.equals(fromSdk)) {
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

    /**
     * Create an instance of the timer
     *
     * @param listener a valid instance of a listener to receive the callback
     */
    public VoxeetActiveSpeakerTimer(@NonNull ActiveSpeakerListener listener) {
        this();

        this.listener = listener;
    }

    /**
     * Start the timer if it was not previously started
     * <p>
     * Any attempt to call the start method when it's already started will have no effect
     */
    public void start() {
        if (null == handler) {
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(refreshActiveSpeaker, 1000);
        }
    }

    /**
     * Stop the timer if it was already started
     * Any call when the timer has already been stopped will have no effects
     */
    public void stop() {
        if (null != handler) {
            handler.removeCallbacks(refreshActiveSpeaker);
            handler = null;
        }
    }

    /**
     * Get the current active speaker
     * <p>
     * The result will be null if start has not been used or no users are in the conference
     *
     * @return the current active speaker
     */
    @Nullable
    public String getCurrentActiveSpeaker() {
        return currentActiveSpeaker;
    }

    /**
     * Listener of the active speaker of a conference
     * <p>
     * Any call this method will be catch for errors and printed in the error logs
     */
    public static interface ActiveSpeakerListener {
        void onActiveSpeakerUpdated(@Nullable String activeSpeakerUserId);
    }
}
