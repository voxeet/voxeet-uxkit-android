package sdk.voxeet.com.toolkit.controllers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.voxeet.android.media.Media;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.providers.containers.DefaultReplayMessageProvider;
import sdk.voxeet.com.toolkit.providers.logics.DefaultReplayMessageSubViewProvider;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.events.error.GetConferenceHistoryErrorEvent;
import voxeet.com.sdk.events.success.ConferenceDestroyedPushEvent;
import voxeet.com.sdk.events.success.ConferenceEndedEvent;
import voxeet.com.sdk.events.success.GetConferenceHistoryEvent;
import voxeet.com.sdk.models.HistoryConference;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class ReplayMessageToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

    private final static String TAG = ReplayMessageToolkitController.class.getSimpleName();

    private boolean _wait_for_history;
    private String _last_conference;
    private long _wait_for_history_offset;
    private long _last_conference_duration;

    public ReplayMessageToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus);

        _wait_for_history = false;
        _last_conference_duration = 0;
        _last_conference = null;

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
    public final void replay(@NonNull String conferenceId, long offset) {
        VoxeetToolkit.getInstance().getConferenceToolkit().enable(false);
        enable(true);

        _wait_for_history = true;
        _last_conference = conferenceId;
        _last_conference_duration = 0;
        _wait_for_history_offset = offset;

        VoxeetSdk.getInstance().getConferenceService().conferenceHistory(conferenceId);
    }

    @Override
    public void onActionButtonClicked() {
        //leave the current conference
        VoxeetSdk.getInstance().getConferenceService().leave();
    }

    public void onEvent(GetConferenceHistoryErrorEvent event) {
        Log.d(TAG, "onEvent: " + event.getClass().getSimpleName() + " " + event.message());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GetConferenceHistoryEvent event) {


        HistoryConference history_conference = findFirstMatch(event);

        if (history_conference != null) {
            _last_conference_duration = history_conference.getConferenceDuration();
            Log.d(TAG, "onEvent: " + Arrays.toString(conferenceHistoryToString(event).toArray()));
            VoxeetSdk.getInstance().getConferenceService().setAudioRoute(Media.AudioRoute.ROUTE_SPEAKER);
            VoxeetSdk.getInstance().getConferenceService().replay(_last_conference, _wait_for_history_offset);
        } else {
            //must be because it is not the current conference which returned something !
        }
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEndedEvent event) {
        //to allow replay
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPushEvent event) {
        //to allow replay
    }

    public String getLastConferenceCalled() {
        return _last_conference;
    }

    public long getLastConferenceCalledDuration() {
        return _last_conference_duration;
    }

    private HistoryConference findFirstMatch(GetConferenceHistoryEvent event) {
        for (HistoryConference item : event.getItems()) {
            if (_last_conference.equalsIgnoreCase(item.getConferenceId())) {
                return item;
            }
        }

        return null;
    }

    private List<String> conferenceHistoryToString(GetConferenceHistoryEvent event) {
        List<String> result = new ArrayList<>();

        for (HistoryConference item : event.getItems()) {
            result.add(item.getConferenceId() + " " + item.getConferenceType() + " " + item.getOwnerId()
                    + " " + item.getUserId() + " " + item.getConferenceDuration() + " " + item.getConferenceTimestamp()
                    + " " + item.getMetadata().getExternalId());
        }

        return result;
    }
}
