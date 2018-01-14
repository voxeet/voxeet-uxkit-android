package sdk.voxeet.com.toolkit.views.uitookit.sdk;

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
 * Created by romainbenmansour on 20/02/2017.
 */
public abstract class VoxeetView extends FrameLayout {

    private final String TAG = VoxeetView.class.getSimpleName();

    protected boolean builderMode = false;

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

    /**
     * Instantiates a new Voxeet view.
     *
     * @param context the context
     */
    public VoxeetView(Context context) {
        super(context);

        onInit();
    }

    /**
     * Instantiates a new Voxeet view.
     *
     * @param context     the context
     * @param builderMode inflating the layout will differ depending on the value
     */
    public VoxeetView(Context context, boolean builderMode) {
        super(context);

        this.builderMode = builderMode;

        onInit();
    }

    /**
     * Instantiates a new Voxeet view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetView(Context context, AttributeSet attrs) {
        super(context, attrs);

        onInit();
    }

    /**
     * Instantiates a new Voxeet view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public VoxeetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        onInit();
    }

    /**
     * On conference joined.
     *
     * @param conferenceId the conference id
     */
    protected abstract void onConferenceJoined(String conferenceId);

    /**
     * On conference updated.
     *
     * @param conferenceId the conference id
     */
    protected abstract void onConferenceUpdated(List<DefaultConferenceUser> conferenceId);

    /**
     * On conference creation.
     *
     * @param conferenceId the conference id
     */
    protected abstract void onConferenceCreation(String conferenceId);

    /**
     * On conference user joined.
     *
     * @param conferenceUser the conference user
     */
    protected abstract void onConferenceUserJoined(DefaultConferenceUser conferenceUser);

    /**
     * On conference user updated.
     *
     * @param conferenceUser the conference user
     */
    protected abstract void onConferenceUserUpdated(DefaultConferenceUser conferenceUser);

    /**
     * On conference user left.
     *
     * @param conferenceUser the conference user
     */
    protected abstract void onConferenceUserLeft(DefaultConferenceUser conferenceUser);

    /**
     * On recording status updated.
     *
     * @param recording the recording
     */
    protected abstract void onRecordingStatusUpdated(boolean recording);

    /**
     * On media stream updated.
     *
     * @param userId the user id
     */
    protected abstract void onMediaStreamUpdated(String userId);

    /**
     * On conference destroyed.
     */
    protected abstract void onConferenceDestroyed();

    /**
     * On conference left.
     */
    protected abstract void onConferenceLeft();

    /**
     * Init.
     */
    protected abstract void init();

    /**
     * Inflate layout.
     */
    protected abstract void inflateLayout();

    /**
     * Bind view.
     *
     * @param view the view
     */
    protected abstract void bindView(View view);

    /**
     * On init.
     */
    protected void onInit() {
        eventBus.register(this);

        inflateLayout();

        bindView(this);

        init();
    }

    /**
     * Release.
     */
    protected void release() {
        onDestroy();
    }

    /**
     * On destroy.
     */
    public void onDestroy() {
        eventBus.unregister(this);

        handler.removeCallbacks(null);
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * On ConferenceJoinedSuccessEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceJoinedSuccessEvent event) {
        onConferenceJoined(event.getConferenceId());
    }

    /**
     * On ConferenceCreationSuccess event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceCreationSuccess event) {
        onConferenceCreation(event.getConfId());
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

        onMediaStreamUpdated(user.getUserId());

        onConferenceUserUpdated(user);
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

            onMediaStreamUpdated(user.getUserId());

            onConferenceUserJoined(user);
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

        onConferenceUserLeft(user);
    }

    /**
     * On ConferenceLeftSuccessEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceLeftSuccessEvent event) {
        onConferenceLeft();
    }

    /**
     * On event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPushEvent event) {
        onConferenceDestroyed();
    }

    /**
     * On ConferenceEndedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEndedEvent event) {
        onConferenceDestroyed();
    }

    /**
     * On RecordingStatusUpdateEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RecordingStatusUpdateEvent event) {
        onRecordingStatusUpdated(event.getRecordingStatus().equalsIgnoreCase(RecordingStatus.RECORDING.name()));
    }

    /**
     * On ConferenceUpdatedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceUpdatedEvent event) {
        onConferenceUpdated(event.getEvent().getParticipants());
    }
}
