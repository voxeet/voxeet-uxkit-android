package sdk.voxeet.com.toolkit.controllers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.voxeet.android.media.Media;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.providers.containers.DefaultReplayMessageProvider;
import sdk.voxeet.com.toolkit.providers.logics.DefaultReplayMessageSubViewProvider;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.services.SdkConferenceService;
import voxeet.com.sdk.events.error.GetConferenceHistoryErrorEvent;
import voxeet.com.sdk.events.success.ConferenceDestroyedPushEvent;
import voxeet.com.sdk.events.success.ConferenceEndedEvent;
import voxeet.com.sdk.events.success.GetConferenceHistoryEvent;
import voxeet.com.sdk.models.HistoryConference;
import voxeet.com.sdk.promise.ErrorPromise;
import voxeet.com.sdk.promise.Promise;
import voxeet.com.sdk.promise.SuccessPromise;

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
    public final Promise<Boolean> replay(@NonNull String conferenceId, long offset) {
        VoxeetToolkit.getInstance().getConferenceToolkit().enable(false);
        enable(true);

        _wait_for_history = true;
        _last_conference = conferenceId;
        _last_conference_duration = 0;
        _wait_for_history_offset = offset;

        VoxeetToolkit.getInstance().getConferenceToolkit().enable(false);
        enable(true);

        SdkConferenceService service = VoxeetSdk.getInstance().getConferenceService();
        service.setAudioRoute(Media.AudioRoute.ROUTE_SPEAKER);
        service.conferenceHistory(conferenceId);

        //TODO here, do a Promise.all with the two different method !
        //and resolve a returned promise with the relevant information
        return service.replay(_last_conference, _wait_for_history_offset);
    }

    @Override
    public void onActionButtonClicked() {
        //leave the current conference
        VoxeetSdk.getInstance().getConferenceService()
                .leave()
                .then(new SuccessPromise<Boolean, Object>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        //do something here ?
                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
    }

    public void onEvent(GetConferenceHistoryErrorEvent event) {
        Log.d(TAG, "onEvent: " + event.getClass().getSimpleName() + " " + event.message());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GetConferenceHistoryEvent event) {
        HistoryConference history_conference = findFirstMatch(event);

        if (history_conference != null) {
            _last_conference_duration = history_conference.getConferenceRecordingDuration();
        } else {
            //must be because it is not the current conference which returned something !
        }
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEndedEvent event) {
        //to allow replay - prevent super call()
        if (getMainView() != null) getMainView().onConferenceDestroyed();
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPushEvent event) {
        //to allow replay - prevent super call()
        if (getMainView() != null) getMainView().onConferenceDestroyed();
    }

    public boolean isShowing() {
        return null != getMainView();
    }


    public String getLastConferenceCalled() {
        return _last_conference;
    }

    public long getLastConferenceCalledDuration() {
        return _last_conference_duration;
    }

    private HistoryConference findFirstMatch(GetConferenceHistoryEvent event) {
        for (HistoryConference item : event.getItems()) {
            if (_last_conference.equalsIgnoreCase(item.getConferenceId())
                    && item.getConferenceRecordingDuration() > 0) {
                return item;
            }
        }

        return null;
    }
}
