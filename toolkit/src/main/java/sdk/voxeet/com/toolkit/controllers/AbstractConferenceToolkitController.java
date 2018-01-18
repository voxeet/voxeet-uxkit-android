package sdk.voxeet.com.toolkit.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.voxeet.android.media.MediaStream;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetView;
import voxeet.com.sdk.events.success.ConferenceCreationSuccess;
import voxeet.com.sdk.events.success.ConferenceDestroyedPushEvent;
import voxeet.com.sdk.events.success.ConferenceEndedEvent;
import voxeet.com.sdk.events.success.ConferenceJoinedSuccessEvent;
import voxeet.com.sdk.events.success.ConferenceLeftSuccessEvent;
import voxeet.com.sdk.events.success.ConferenceUpdatedEvent;
import voxeet.com.sdk.events.success.ConferenceUserJoinedEvent;
import voxeet.com.sdk.events.success.ConferenceUserLeftEvent;
import voxeet.com.sdk.events.success.ConferenceUserUpdatedEvent;
import voxeet.com.sdk.json.RecordingStatusUpdateEvent;
import voxeet.com.sdk.models.RecordingStatus;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public abstract class ConferenceToolkitController {

    @NonNull
    private EventBus eventBus = EventBus.getDefault();

    /**
     * The IConference users.
     */
    @NonNull
    protected List<DefaultConferenceUser> conferenceUsers = new ArrayList<>();

    /**
     * The Media streams.
     */
    @NonNull
    protected Map<String, MediaStream> mediaStreams = new HashMap<>();

    /**
     * The Handler.
     */
    @NonNull
    protected Handler handler = new Handler(Looper.getMainLooper());
    private VoxeetView mMainView;

    public ConferenceToolkitController(EventBus eventbus) {
        this.eventBus = eventbus;
    }

    /**
     * Init.
     */
    protected void init() {
        mMainView = createMainView();
    }

    public void enable() {
        eventBus.register(this);

        //set the relevant streams info
        //TODO transform those setters to listeners onto this element ?
        mMainView.onMediaStreamsListUpdated(mediaStreams);
        mMainView.onConferenceUsersListUpdate(conferenceUsers);
    }

    public void disable() {
        try {
            eventBus.unregister(this);
        } catch (Exception e){

        }
    }

    protected abstract VoxeetView createMainView();

    /**
     * Release.
     */
    protected void onDestroy() {
        mMainView.onDestroy();
    }

    /**
     * On ConferenceJoinedSuccessEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceJoinedSuccessEvent event) {
        mMainView.onConferenceJoined(event.getConferenceId());
    }

    /**
     * On ConferenceCreationSuccess event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceCreationSuccess event) {
        mMainView.onConferenceCreation(event.getConfId());
    }

    /**
     * On ConferenceUserUpdatedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserUpdatedEvent event) {
        DefaultConferenceUser user = event.getUser();

        mediaStreams.put(user.getUserId(), event.getMediaStream());

        mMainView.onMediaStreamUpdated(user.getUserId(), mediaStreams);

        mMainView.onConferenceUserUpdated(user);
    }

    /**
     * On ConferenceUserJoinedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserJoinedEvent event) {
        DefaultConferenceUser user = event.getUser();
        if (!conferenceUsers.contains(user)) {
            conferenceUsers.add(user);

            mediaStreams.put(user.getUserId(), event.getMediaStream());

            mMainView.onMediaStreamUpdated(user.getUserId(), mediaStreams);

            mMainView.onConferenceUserJoined(user);

            mMainView.onConferenceUsersListUpdate(conferenceUsers);
        }
    }

    /**
     * On ConferenceUserLeftEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserLeftEvent event) {
        DefaultConferenceUser user = event.getUser();
        if (conferenceUsers.contains(user))
            conferenceUsers.remove(user);

        mMainView.onConferenceUserLeft(user);
    }

    /**
     * On ConferenceLeftSuccessEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceLeftSuccessEvent event) {
        mMainView.onConferenceLeft();
    }

    /**
     * On event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPushEvent event) {
        mMainView.onConferenceDestroyed();
    }

    /**
     * On ConferenceEndedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEndedEvent event) {
        mMainView.onConferenceDestroyed();
    }

    /**
     * On RecordingStatusUpdateEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RecordingStatusUpdateEvent event) {
        mMainView.onRecordingStatusUpdated(event.getRecordingStatus().equalsIgnoreCase(RecordingStatus.RECORDING.name()));
    }

    /**
     * On ConferenceUpdatedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceUpdatedEvent event) {
        mMainView.onConferenceUpdated(event.getEvent().getParticipants());
    }
}
