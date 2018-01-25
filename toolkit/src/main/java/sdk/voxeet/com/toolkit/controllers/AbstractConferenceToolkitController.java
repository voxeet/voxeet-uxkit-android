package sdk.voxeet.com.toolkit.controllers;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.voxeet.android.media.Media;
import com.voxeet.android.media.MediaStream;
import com.voxeet.toolkit.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.VoxeetOverlayToggleView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.events.error.ConferenceLeftError;
import voxeet.com.sdk.events.error.ReplayConferenceErrorEvent;
import voxeet.com.sdk.events.success.ConferenceCreationSuccess;
import voxeet.com.sdk.events.success.ConferenceDestroyedPushEvent;
import voxeet.com.sdk.events.success.ConferenceEndedEvent;
import voxeet.com.sdk.events.success.ConferenceJoinedSuccessEvent;
import voxeet.com.sdk.events.success.ConferenceLeftSuccessEvent;
import voxeet.com.sdk.events.success.ConferencePreJoinedEvent;
import voxeet.com.sdk.events.success.ConferenceUpdatedEvent;
import voxeet.com.sdk.events.success.ConferenceUserJoinedEvent;
import voxeet.com.sdk.events.success.ConferenceUserLeftEvent;
import voxeet.com.sdk.events.success.ConferenceUserUpdatedEvent;
import voxeet.com.sdk.json.RecordingStatusUpdateEvent;
import voxeet.com.sdk.models.RecordingStatus;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;
import voxeet.com.sdk.utils.ScreenHelper;

/**
 * Created by kevinleperf on 15/01/2018.
 * <p>
 * Implements the common logic to any controller this SDK provides
 * <p>
 * The general idea is that it will receive relevants events and dispatch
 * them to its children
 * <p>
 * children are subviews for overlay
 * <p>
 * implementations must provide the view which will be used later
 */

public abstract class AbstractConferenceToolkitController {

    private Context mContext;
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

    private AbstractVoxeetOverlayView mMainView;

    private FrameLayout.LayoutParams params;
    private OverlayState mOverlayState;
    private final static String TAG = AbstractConferenceToolkitController.class.getSimpleName();
    private boolean mEnabled;

    public AbstractConferenceToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        setDefaultOverlayState(overlay);
        mContext = context;
        this.eventBus = eventbus;

        handler = new Handler(Looper.getMainLooper());

        params = new FrameLayout.LayoutParams(
                context.getResources().getDimensionPixelSize(R.dimen.dimen_100),
                context.getResources().getDimensionPixelSize(R.dimen.dimen_140));
        params.gravity = Gravity.END | Gravity.TOP;
        params.topMargin = ScreenHelper.actionBar(context) + ScreenHelper.getStatusBarHeight(context);

        register();
    }

    /**
     * Init.
     */
    protected void init() {
        Activity activity = VoxeetToolkit.getInstance().getCurrentActivity();
        mMainView = createMainView(activity);

        mMainView.onMediaStreamsListUpdated(mediaStreams);
        mMainView.onConferenceUsersListUpdate(conferenceUsers);
    }

    public void register() {
        if (!eventBus.isRegistered(this))
            eventBus.register(this);

        //set the relevant streams info
        //TODO transform those setters to listeners onto this element ?
        if (mMainView != null) {
            mMainView.onMediaStreamsListUpdated(mediaStreams);
            mMainView.onConferenceUsersListUpdate(conferenceUsers);
        }
    }

    public void unregister() {
        if (eventBus.isRegistered(this))
            eventBus.unregister(this);
    }

    protected abstract AbstractVoxeetOverlayView createMainView(Activity activity);

    /**
     * Method set to filter specific conference from the given id
     *
     * @param conference the conference id to test against
     * @return return true if the given conference can be managed
     */
    protected abstract boolean validFilter(String conference);

    protected Context getContext() {
        return mContext;
    }

    /**
     * Release.
     */
    public void onDestroy() {
        mMainView.onDestroy();
    }

    /**
     * Display the conference view when the user is creating/joining a conference.
     *
     * @param event the event
     * @exclude
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferencePreJoinedEvent event) {
        Activity activity = VoxeetToolkit.getInstance().getCurrentActivity();
        if (activity != null && validFilter(event.getConferenceId())) {
            if (mMainView == null)
                init();

            params = new FrameLayout.LayoutParams(
                    activity.getResources().getDimensionPixelSize(R.dimen.dimen_100),
                    activity.getResources().getDimensionPixelSize(R.dimen.dimen_140));
            params.gravity = Gravity.END | Gravity.TOP;
            params.topMargin = ScreenHelper.actionBar(activity) + ScreenHelper.getStatusBarHeight(activity);

            displayView();
        }
    }

    /**
     * On ConferenceJoinedSuccessEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceJoinedSuccessEvent event) {
        if (validFilter(event.getConferenceId()) || validFilter(event.getAliasId())) {
            VoxeetSdk.getInstance().getConferenceService().setAudioRoute(Media.AudioRoute.ROUTE_SPEAKER);

            if (mMainView != null) {
                mMainView.onConferenceJoined(event.getConferenceId());
            }
        }
    }

    /**
     * On ConferenceCreationSuccess event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceCreationSuccess event) {
        if (validFilter(event.getConfId()) || validFilter(event.getConfAlias())) {
            mMainView.onConferenceCreation(event.getConfId());
        }
    }

    /**
     * On ConferenceUserUpdatedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserUpdatedEvent event) {
        if (conferenceUsers != null && mMainView != null && mediaStreams != null) {
            Log.d("VoxeetSDK", "onEvent: ConferenceUserUpdatedEvent " + event.getMediaStream().hasVideo());
            DefaultConferenceUser user = event.getUser();
            if (!conferenceUsers.contains(user)) {
                conferenceUsers.add(user);
                mMainView.onConferenceUsersListUpdate(conferenceUsers);
            }

            mediaStreams.put(user.getUserId(), event.getMediaStream());

            mMainView.onMediaStreamUpdated(user.getUserId(), mediaStreams);

            mMainView.onConferenceUserUpdated(user);
        }
    }

    /**
     * On ConferenceUserJoinedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserJoinedEvent event) {
        if (conferenceUsers != null && mMainView != null) {
            Log.d("VoxeetSDK", "onEvent: ConferenceUserJoinedEvent " + event.getUser().getUserId() + " " + event.getMediaStream().hasVideo() + " " + mMainView);
            DefaultConferenceUser user = event.getUser();
            if (!conferenceUsers.contains(user)) {
                conferenceUsers.add(user);
                if (mMainView != null) {
                    mMainView.onConferenceUsersListUpdate(conferenceUsers);
                }
            }

            mediaStreams.put(user.getUserId(), event.getMediaStream());

            if (mMainView != null) {
                mMainView.onMediaStreamUpdated(user.getUserId(), mediaStreams);

                mMainView.onConferenceUserJoined(user);
            }
        }
    }

    /**
     * On ConferenceUserLeftEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserLeftEvent event) {
        if (conferenceUsers != null && mMainView != null) {
            DefaultConferenceUser user = event.getUser();
            if (conferenceUsers.contains(user))
                conferenceUsers.remove(user);

            mMainView.onConferenceUserLeft(user);
        }
    }

    /**
     * On ConferenceLeftSuccessEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceLeftSuccessEvent event) {
        if (mMainView != null) {
            reset();
            mMainView.onConferenceLeft();


            removeView(true);
        }
    }

    /**
     * On ConferenceLeftSuccessEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceLeftError event) {
        if (mMainView != null) {
            reset();
            mMainView.onConferenceLeft();


            removeView(true);
        }
    }

    /**
     * On event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPushEvent event) {
        reset();
        if (mMainView != null) {
            mMainView.onConferenceDestroyed();
        }

        removeView(true);
    }

    /**
     * On ConferenceEndedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEndedEvent event) {
        reset();
        if (mMainView != null) mMainView.onConferenceDestroyed();

        removeView(true);
    }

    //TODO event replay ok

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ReplayConferenceErrorEvent event) {
        reset();
        //TODO error message
        Log.d("VoxeetSDK", "onEvent: " + event.toString());
        if (mMainView != null) mMainView.onConferenceDestroyed();

        removeView(true);
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


    public boolean isOverlayEnabled() {
        return VoxeetToolkit.getInstance().isEnabled();
    }


    /**
     * Toggles overlay visibility.
     */
    public void onOverlayEnabled(boolean enabled) {
        if (enabled)
            displayView();
        else
            removeView(false);
    }

    private synchronized void displayView() {
        if (isOverlayEnabled()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mMainView != null) {
                        ViewGroup viewHolder = (ViewGroup) mMainView.getParent();
                        if (viewHolder != null)
                            viewHolder.removeView(mMainView);

                        Activity activity = VoxeetToolkit.getInstance().getCurrentActivity();
                        ViewGroup root = VoxeetToolkit.getInstance().getRootView();

                        if (root != null && activity != null && !activity.isFinishing()) {
                            root.addView(mMainView, params);
                            mMainView.onResume();
                        }
                    }
                }
            });
        }
    }

    public synchronized void removeView(final boolean shouldRelease) {
        Log.d("VoxeetSDK", "removeView: " + shouldRelease);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mMainView != null) {
                    ViewGroup viewHolder = (ViewGroup) mMainView.getParent();
                    if (viewHolder != null)
                        viewHolder.removeView(mMainView);

                    if (shouldRelease) {
                        mMainView.onDestroy();
                        mMainView = null;
                    }
                }
            }
        });
    }


    public void onActivityResumed(Activity activity) {
        if (mMainView != null) { // conf is live
            displayView();
        }
    }

    public void onActivityPaused(Activity activity) {
        if (mMainView != null)
            removeView(false);
    }

    private void reset() {
        mediaStreams = new HashMap<>();
        conferenceUsers = new ArrayList<>();
    }

    public void setDefaultOverlayState(OverlayState overlay) {
        Log.d(TAG, "setDefaultOverlayState: ");
        mOverlayState = overlay;

        if(mMainView != null) {
            if(OverlayState.EXPANDED.equals(overlay)) {
                mMainView.expand();
            } else {
                mMainView.minimize();
            }
        }
    }

    public OverlayState getDefaultOverlayState() {
        Log.d(TAG, "getDefaultOverlayState: " + mOverlayState);
        return mOverlayState;
    }

    public void enable(boolean state) {
        mEnabled = state;
    }

    public boolean isEnabled() {
        return mEnabled;
    }
}
