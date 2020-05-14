package com.voxeet.uxkit.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.VoxeetSDK;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.utils.Annotate;

import java.util.HashMap;
import java.util.List;

/**
 * Simple Timer made to schedule interactions accross the Speakers in a conference
 * <p>
 * This class can be started, stopped and get the current active speaker
 */
@Annotate
public final class VoxeetActiveSpeakerTimer {

    private ActiveSpeakerListener listener;
    private String currentActiveSpeaker;

    private Handler handler;
    private Runnable refreshActiveSpeaker = null;

    private HashMap<String, Double> audioLevels = new HashMap<>();

    private VoxeetActiveSpeakerTimer() {
        refreshActiveSpeaker = () -> {
            try {
                if (null != handler && null != listener && null != VoxeetSDK.instance()) {
                    String fromSdk = VoxeetSDK.conference().currentSpeaker();
                    Conference conference = VoxeetSDK.conference().getConference();

                    if (null != conference) {
                        List<Participant> participants = VoxeetSDK.conference().getParticipants();

                        for (Participant participant : participants) {
                            if (null == participant || null == participant.getId()) continue;

                            double audioLevel = VoxeetSDK.conference().audioLevel(participant);
                            audioLevels.put(participant.getId(), audioLevel);
                        }
                    }
                    if (null == currentActiveSpeaker || !currentActiveSpeaker.equals(fromSdk)) {
                        currentActiveSpeaker = fromSdk;
                        listener.onActiveSpeakerUpdated(currentActiveSpeaker);
                    }
                }
                handler.postDelayed(refreshActiveSpeaker, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public setActiveSpeakerListener(@NonNull ActiveSpeakerListener listener) {
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
