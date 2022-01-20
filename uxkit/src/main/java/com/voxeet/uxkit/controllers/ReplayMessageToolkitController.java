package com.voxeet.uxkit.controllers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.VoxeetSDK;
import com.voxeet.promise.Promise;
import com.voxeet.sdk.events.sdk.ConferenceHistoryResult;
import com.voxeet.sdk.json.ConferenceEnded;
import com.voxeet.sdk.media.audio.AudioRoute;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.v1.HistoryConference;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.utils.Map;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.configuration.Configuration;
import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.uxkit.providers.containers.DefaultReplayMessageProvider;
import com.voxeet.uxkit.providers.logics.DefaultReplayMessageSubViewProvider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class ReplayMessageToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

    private final static ShortLogger Log = UXKitLogger.createLogger(ReplayMessageToolkitController.class);
    public final Configuration Configuration = new Configuration();

    private boolean _wait_for_history;
    private String _last_conference;
    private long _wait_for_history_offset;
    private long _last_conference_duration;


    public ReplayMessageToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus);

        _wait_for_history = false;
        _last_conference_duration = 0;
        _last_conference = null;

        //disable by default
        enable(false);
        setDefaultOverlayState(overlay);
        setVoxeetOverlayViewProvider(new DefaultReplayMessageProvider(this));
        setVoxeetSubViewProvider(new DefaultReplayMessageSubViewProvider());
    }

    @Override
    protected boolean validFilter(String conference) {
        return isEnabled();
    }

    /**
     * Ask for replay a given video
     * <p>
     * TODO this method does not make the replay specific to the current object for now
     * it will in the future if mandatory
     * <p>
     * The implementation would have to make a specific EventBus for the VoxeetSDK Instance
     * and add multiple instance management
     *
     * @param conferenceId the conference id to replay
     * @param offset       the offset in seconds from the start
     */
    public final Promise<Conference> replay(@NonNull String conferenceId, long offset) {
        VoxeetToolkit.instance().enable(this);

        _wait_for_history = true;
        _last_conference = conferenceId;
        _last_conference_duration = 0;
        _wait_for_history_offset = offset;


        ConferenceService service = VoxeetSDK.conference();
        VoxeetSDK.audio().setAudioRoute(AudioRoute.ROUTE_SPEAKER);
        service.conferenceHistory(conferenceId)
                .then((event, solver) -> {
                    //possibility to manage the conference history event right here
                    HistoryConference history_conference = findFirstMatch(event);

                    if (history_conference != null) {
                        _last_conference_duration = history_conference.getConferenceRecordingDuration();
                    }

                    _wait_for_history = false;
                })
                .error(throwable -> {
                    Log.e("onHistoryError: ", throwable);

                    _wait_for_history = false;
                });

        //TODO here, do a Promise.all with the two different method !
        //and resolve a returned promise with the relevant information
        return service.replay(_last_conference, _wait_for_history_offset);
    }

    /**
     * Will leave the current replay if any action is called on this method
     * <p>
     * to be used internally
     */
    @Override
    public void onActionButtonClicked() {
        //leave the current conference
        VoxeetSDK.conference()
                .leave()
                .then((result, solver) -> {
                    //do something here ?
                })
                .error(Log::e);
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEnded event) {
        //to allow replay - prevent super call()
        if (getMainView() != null) getMainView().onConferenceDestroyed();
    }

    public boolean isShowing() {
        return null != getMainView();
    }

    @Nullable
    public String getLastConferenceCalled() {
        return _last_conference;
    }

    public long getLastConferenceCalledDuration() {
        return _last_conference_duration;
    }

    /**
     * Retrieve the first relevant information about this history
     *
     * @param event the event to manage
     * @return a nullable object corresponding to the description
     */
    @Nullable
    private HistoryConference findFirstMatch(@NonNull ConferenceHistoryResult event) {
        return Map.find(Opt.of(event.items).or(new ArrayList<>()),
                item -> _last_conference.equalsIgnoreCase(item.getConferenceId()) && item.getConferenceRecordingDuration() > 0);
    }
}
