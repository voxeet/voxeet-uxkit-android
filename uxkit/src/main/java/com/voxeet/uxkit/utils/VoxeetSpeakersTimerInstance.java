package com.voxeet.uxkit.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.VoxeetSDK;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.implementation.VoxeetSpeakerView;
import com.voxeet.uxkit.views.internal.VoxeetVuMeter;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple Timer made to schedule interactions accross the Speakers in a conference
 * <p>
 * This class can be started, stopped and get the current active speaker
 */
@Annotate
public final class VoxeetSpeakersTimerInstance {

    public final static VoxeetSpeakersTimerInstance instance = new VoxeetSpeakersTimerInstance();

    private CopyOnWriteArrayList<SpeakersUpdated> speakers_listeners = new CopyOnWriteArrayList<>();
    private ActiveSpeakerListener listener;
    private String currentActiveSpeaker;

    private Handler handler;
    private Runnable refreshActiveSpeaker = null;

    private HashMap<String, Double> audioLevels = new HashMap<>();

    private VoxeetSpeakersTimerInstance() {
        refreshActiveSpeaker = () -> {
            try {
                //TODO since using the active speaker's O(n) loop and doing same here, mutualize code and remove the call to the SDK alltogether
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
                        try {
                            listener.onActiveSpeakerUpdated(currentActiveSpeaker);
                        } catch (Exception e) {

                        }
                    }

                    //also warn the listeners
                    sendSpeakersUpdated();
                }
                handler.postDelayed(refreshActiveSpeaker, VoxeetSpeakerView.REFRESH_METER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * Optional listener to set to receive events when a new active speaker loop has finished
     * <p>
     * Only one is available in memory. A getter for each audio level is available and will get refreshed every 1s
     *
     * @param listener
     */
    public void setActiveSpeakerListener(@NonNull ActiveSpeakerListener listener) {

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
     * Optional method for fast and possibly spammy behaviour from apps where views can be rendered multiple times.
     * The value returned is a cached one and refreshed every 1s
     *
     * @param participant
     * @return the audio level for the given participant or null
     */
    public double audioLevel(@NonNull Participant participant) {
        String id = Opt.of(participant).then(Participant::getId).or("");
        if (audioLevels.containsKey(id)) {
            return Opt.of(audioLevels.get(id)).or(0d);
        }
        return 0d;
    }

    public void register(@NonNull SpeakersUpdated listener) {
        if (!speakers_listeners.contains(listener)) {
            speakers_listeners.add(listener);
        }
    }

    public void unregister(@NonNull SpeakersUpdated listener) {
        speakers_listeners.remove(listener);
    }

    private void sendSpeakersUpdated() {
        for (SpeakersUpdated speaker : speakers_listeners) {
            try {
                speaker.onSpeakersUpdated();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Listener of the active speaker of a conference
     * <p>
     * Any call this method will be catch for errors and printed in the error logs
     */
    public static interface ActiveSpeakerListener {
        void onActiveSpeakerUpdated(@Nullable String activeSpeakerUserId);
    }

    public static interface SpeakersUpdated {
        void onSpeakersUpdated();
    }
}
