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

    public static final int REFRESH_METER = 100;
    private final static int INTERVALS_BEFORE_NEXT_SPEAKER_UPDATED = 50; //
    public final static VoxeetSpeakersTimerInstance instance = new VoxeetSpeakersTimerInstance();

    private CopyOnWriteArrayList<SpeakersUpdated> speakers_listeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<ActiveSpeakerListener> activespeakers_listeners = new CopyOnWriteArrayList<>();
    private String currentActiveSpeaker;
    private String lastActiveSpeaker;

    private Handler handler;
    private Runnable refreshActiveSpeaker = null;

    private HashMap<String, Double> audioLevels = new HashMap<>();

    private int current_loop_state = 0;

    private VoxeetSpeakersTimerInstance() {
        refreshActiveSpeaker = () -> {
            try {
                //TODO since using the active speaker's O(n) loop and doing same here, mutualize code and remove the call to the SDK alltogether
                if (null != handler && null != VoxeetSDK.instance()) {
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

                    //check if it's time to update the current active speaker (every 5 times - current value of INTERVALS_BEFORE_NEXT_SPEAKER_UPDATED)
                    boolean new_loop_activespeaker = false;
                    current_loop_state++;
                    if (current_loop_state >= INTERVALS_BEFORE_NEXT_SPEAKER_UPDATED) {
                        current_loop_state = 0;
                        new_loop_activespeaker = true;
                    }

                    //we save the last active speaker known
                    if (null != fromSdk) {
                        lastActiveSpeaker = fromSdk;
                    }

                    if (new_loop_activespeaker && (null == currentActiveSpeaker || !currentActiveSpeaker.equals(fromSdk))) {
                        currentActiveSpeaker = lastActiveSpeaker;
                        sendActiveSpeakersUpdated();
                        lastActiveSpeaker = null;
                    }

                    //also warn the listeners
                    sendSpeakersUpdated();
                }
                handler.postDelayed(refreshActiveSpeaker, VoxeetSpeakersTimerInstance.REFRESH_METER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    @Deprecated
    public void setActiveSpeakerListener(@NonNull ActiveSpeakerListener listener) {
        registerActiveSpeakerListener(listener);
    }

    /**
     * Optional listener to set to receive events when a new active speaker loop has finished
     * <p>
     * Only one is available in memory. A getter for each audio level is available and will get refreshed every 1s
     *
     * @param listener
     */
    public void registerActiveSpeakerListener(@NonNull ActiveSpeakerListener listener) {
        if (!activespeakers_listeners.contains(listener)) {
            activespeakers_listeners.add(listener);
        }
    }

    public void unregisterActiveSpeakerListener(@NonNull ActiveSpeakerListener listener) {
        activespeakers_listeners.remove(listener);
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

    private void sendActiveSpeakersUpdated() {
        for (ActiveSpeakerListener speaker : activespeakers_listeners) {
            try {
                speaker.onActiveSpeakerUpdated(currentActiveSpeaker);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
