package sdk.voxeet.com.toolkit.controllers;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import sdk.voxeet.com.toolkit.providers.containers.IVoxeetOverlayViewProvider;
import sdk.voxeet.com.toolkit.providers.logics.IVoxeetSubViewProvider;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.abs.AbstractConferenceSdkService;
import voxeet.com.sdk.events.error.ConferenceLeftError;
import voxeet.com.sdk.events.error.ReplayConferenceErrorEvent;
import voxeet.com.sdk.events.success.ConferenceCreationSuccess;
import voxeet.com.sdk.events.success.ConferenceDestroyedPushEvent;
import voxeet.com.sdk.events.success.ConferenceEndedEvent;
import voxeet.com.sdk.events.success.ConferenceJoinedSuccessEvent;
import voxeet.com.sdk.events.success.ConferenceLeftSuccessEvent;
import voxeet.com.sdk.events.success.ConferencePreJoinedEvent;
import voxeet.com.sdk.events.success.ConferenceRefreshedEvent;
import voxeet.com.sdk.events.success.ConferenceUpdatedEvent;
import voxeet.com.sdk.events.success.ConferenceUserCallDeclinedEvent;
import voxeet.com.sdk.events.success.ConferenceUserJoinedEvent;
import voxeet.com.sdk.events.success.ConferenceUserLeftEvent;
import voxeet.com.sdk.events.success.ConferenceUserUpdatedEvent;
import voxeet.com.sdk.events.success.InvitationReceived;
import voxeet.com.sdk.events.success.UserInvitedEvent;
import voxeet.com.sdk.json.InvitationReceivedEvent;
import voxeet.com.sdk.json.RecordingStatusUpdateEvent;
import voxeet.com.sdk.json.UserInfo;
import voxeet.com.sdk.json.UserInvited;
import voxeet.com.sdk.models.RecordingStatus;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;
import voxeet.com.sdk.models.impl.DefaultInvitation;
import voxeet.com.sdk.models.impl.DefaultUserProfile;
import voxeet.com.sdk.utils.ScreenHelper;

/**
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
    private EventBus mEventBus = EventBus.getDefault();

    /**
     * The IConference users.
     */
    @NonNull
    protected List<DefaultConferenceUser> mConferenceUsers = new ArrayList<>();

    /**
     * The Media streams.
     * <p>
     * Empty by default
     */
    @NonNull
    protected Map<String, MediaStream> mMediaStreams = new HashMap<>();

    /**
     * The Handler.
     */
    @NonNull
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * MainView represent the view showed to the user
     */
    @Nullable
    private AbstractVoxeetOverlayView mMainView;

    private FrameLayout mMainViewParent;

    /**
     * Information about the mParams of the
     */
    @NonNull
    private FrameLayout.LayoutParams mParams;

    private IVoxeetOverlayViewProvider mVoxeetOverlayViewProvider;
    private IVoxeetSubViewProvider mVoxeetSubViewProvider;
    private OverlayState mDefaultOverlayState;
    private boolean mEnabled;
    private String TAG = AbstractConferenceSdkService.class.getSimpleName();
    private boolean mIsViewRetainedOnLeave;

    AbstractConferenceToolkitController(Context context, EventBus eventbus) {
        mContext = context;
        mEventBus = eventbus;

        mHandler = new Handler(Looper.getMainLooper());

        setViewRetainedOnLeave(false);
        setParams();

        register();
    }

    /**
     * Init the controller
     * <p>
     * ensures the main view is valid
     */
    protected void init() {
        Activity activity = VoxeetToolkit.getInstance().getCurrentActivity();

        mMainViewParent = new FrameLayout(activity);
        mMainViewParent.setLayoutParams(createMatchParams());

        mMainView = mVoxeetOverlayViewProvider.createView(activity,
                mVoxeetSubViewProvider,
                getDefaultOverlayState());

        List<DefaultConferenceUser> list = VoxeetSdk.getInstance().getConferenceService().getLastInvitationUsers();
        if (null != list) {
            mergeConferenceUsers(list);
        }

        mMainView.onMediaStreamsListUpdated(mMediaStreams);
        mMainView.onConferenceUsersListUpdate(mConferenceUsers);
    }

    /**
     * Register the controller to the instance of eventbus given in constructor
     * <p>
     * If the mainview is valid, we also call the interface's method to give possible new
     */
    public void register() {
        if (!mEventBus.isRegistered(this))
            mEventBus.register(this);

        //set the relevant streams info
        if (mMainView != null) {
            mMainView.onMediaStreamsListUpdated(mMediaStreams);
            mMainView.onConferenceUsersListUpdate(mConferenceUsers);
        }
    }

    /**
     * Unregister the controller from the EvenBus
     * <p>
     * In a typical workflow, this method is never called
     */
    public void unregister() {
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    /**
     * Inject an overlay view provider
     *
     * @param provider a non-null provider
     */
    public AbstractConferenceToolkitController setVoxeetOverlayViewProvider(@NonNull IVoxeetOverlayViewProvider provider) {
        mVoxeetOverlayViewProvider = provider;

        return this;
    }

    /**
     * @param provider
     */
    public AbstractConferenceToolkitController setVoxeetSubViewProvider(@NonNull IVoxeetSubViewProvider provider) {
        mVoxeetSubViewProvider = provider;

        return this;
    }

    /**
     * Release.
     */
    public void onDestroy() {
        mMainView.onDestroy();
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
            removeView(false, RemoveViewType.FROM_EVENT);
    }

    private void displayView() {
        //display the view
        boolean in_conf = false;
        if (null != VoxeetSdk.getInstance()) {
            in_conf = VoxeetSdk.getInstance().getConferenceService().isInConference()
                    || VoxeetSdk.getInstance().getConferenceService().isLive();
        }


        Log.d(TAG, "displayView: " + mMainView + " " + in_conf + " " + isOverlayEnabled());

        if (mMainView == null && in_conf) {
            init();
        }

        if (isOverlayEnabled()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    log("run: add view" + mMainView);
                    if (mMainView != null) {
                        Activity activity = VoxeetToolkit.getInstance().getCurrentActivity();
                        ViewGroup root = VoxeetToolkit.getInstance().getRootView();


                        ViewGroup viewHolder = (ViewGroup) mMainViewParent.getParent();
                        if (null != viewHolder && root != viewHolder) {
                            viewHolder.removeView(mMainViewParent);

                            viewHolder = (ViewGroup) mMainView.getParent();
                            if (viewHolder != null)
                                viewHolder.removeView(mMainView);
                        }


                        log("run: " + root + " " + activity + " " + !activity.isFinishing());
                        if (root != null && activity != null && !activity.isFinishing()) {
                            if (null == mMainViewParent.getParent()) {
                                root.addView(mMainViewParent, createMatchParams());
                            }

                            if (null == mMainView.getParent()) {
                                mMainViewParent.addView(mMainView, mParams);
                            }

                            mMainView.requestLayout();
                            mMainViewParent.requestLayout();
                            mMainView.onResume();
                        }
                    }
                }
            });
        }
    }

    public void removeView(final boolean should_release, final RemoveViewType from_type) {
        final AbstractVoxeetOverlayView view = mMainView;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (view != null) {
                    ViewGroup viewHolder = (ViewGroup) view.getParent();
                    if (viewHolder != null)
                        viewHolder.removeView(view);

                    if (view == mMainView) {
                        viewHolder = (ViewGroup) mMainViewParent.getParent();
                        if (viewHolder != null)
                            viewHolder.removeView(mMainViewParent);
                    }

                    view.onStop();

                    if (should_release && (RemoveViewType.FROM_HUD.equals(from_type) || !isEnabled() || !isViewRetainedOnLeave())) {
                        Log.d(TAG, "run: AbstractConferenceToolkitController should release view " + view.getClass().getSimpleName());
                        view.onDestroy();
                        //if we still have the main view displayed
                        //but wanted to clear it
                        if (view == mMainView) {
                            mMainView = null;
                        }
                    }
                }
            }
        };

        long after = should_release && mMainView != null ? mMainView.getCloseTimeoutInMilliseconds() : 0;

        mHandler.postDelayed(runnable, after);
    }

    /**
     * @param activity
     */
    public void onActivityResumed(Activity activity) {
        if (mMainView != null) {
            displayView();
        }
    }

    /**
     * When activity pause, remove the main view
     *
     * @param activity paused to
     */
    public void onActivityPaused(@NonNull Activity activity) {
        if (mMainView != null) {
            removeView(false, RemoveViewType.FROM_HUD);
        }
    }

    /**
     * Reset the state of the streams and conference users of this controller
     */
    private void reset() {
        mMediaStreams = new HashMap<>();
        mConferenceUsers = new ArrayList<>();
    }

    /**
     * @param overlay as the new default
     */
    public void setDefaultOverlayState(@NonNull OverlayState overlay) {
        mDefaultOverlayState = overlay;

        //set the new state to the view
        if (mMainView != null) {
            if (OverlayState.EXPANDED.equals(overlay)) {
                mMainView.expand();
            } else {
                mMainView.minimize();
            }
        }
    }

    /**
     * Access the overlay state showed by this controller
     *
     * @return the default state, expanded or minimized
     */
    public OverlayState getDefaultOverlayState() {
        return mDefaultOverlayState;
    }

    /**
     * Change the state of this controller
     *
     * @param state the new state of the controller
     */
    public void enable(boolean state) {
        mEnabled = state;

        //enable or disable depending
        if (mEnabled) register();
        else unregister();
    }

    /**
     *
     * TODO check for retain state switch : quit in correct cases
     * @param state the new state of the view
     */
    public void setViewRetainedOnLeave(boolean state) {
        mIsViewRetainedOnLeave = state;
    }
    /**
     * Check wether the view should still be up and running on quit conference
     */
    public boolean isViewRetainedOnLeave() {
        return mIsViewRetainedOnLeave;
    }


    /**
     * Check wether this controller can be called
     *
     * @return the activation state of this controller
     */
    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Getter of the main view
     *
     * @return the instance of the main view
     */
    @Nullable
    protected AbstractVoxeetOverlayView getMainView() {
        return mMainView;
    }


    protected Context getContext() {
        return mContext;
    }

    /**
     * Method set to filter specific conference from the given id
     *
     * @param conference the conference id to test against
     * @return return true if the given conference can be managed
     */
    protected abstract boolean validFilter(String conference);


    private void setParams() {
        mParams = new FrameLayout.LayoutParams(
                getContext().getResources().getDimensionPixelSize(R.dimen.dimen_100),
                getContext().getResources().getDimensionPixelSize(R.dimen.dimen_140));
        mParams.gravity = Gravity.END | Gravity.TOP;
        mParams.topMargin = ScreenHelper.actionBar(getContext()) + ScreenHelper.getStatusBarHeight(getContext());
    }


    private FrameLayout.LayoutParams createMatchParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        return params;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Event Management - see EventBus field
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Display the conference view when the user is creating/joining a conference.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ConferencePreJoinedEvent event) {
        Activity activity = VoxeetToolkit.getInstance().getCurrentActivity();

        log("onEvent: " + event.getClass().getSimpleName()
                + " " + validFilter(event.getConferenceId())
                + " " + activity);
        if (activity != null && validFilter(event.getConferenceId())) {
            if (mMainView == null) init();

            setParams();

            displayView();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserInvitedEvent event) {
        UserInvited invited = event.getEvent();
        List<DefaultUserProfile> profiles = invited.getParticipants();

        for (DefaultUserProfile profile : profiles) {
            DefaultConferenceUser user = new DefaultConferenceUser(profile);
            if (!mConferenceUsers.contains(user)) {
                mConferenceUsers.add(user);

                if (mMainView != null) {
                    mMainView.onConferenceUsersListUpdate(mConferenceUsers);
                }

                if (mMainView != null) {
                    mMainView.onConferenceUserUpdated(user);
                }
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final InvitationReceived invitation) {
        InvitationReceivedEvent event = invitation.getEvent();
        if (null != event && null != event.getInvitations()) {
            for (DefaultInvitation invite : event.getInvitations()) {
                DefaultConferenceUser temp = new DefaultConferenceUser(invite.getProfile());

                if (mMainView != null) {
                    mMainView.onConferenceUsersListUpdate(mConferenceUsers);
                }

                if (mMainView != null) {
                    mMainView.onConferenceUserUpdated(temp);
                }

                if (!mConferenceUsers.contains(temp)) {
                    mConferenceUsers.add(temp);
                }
            }
        }

    }

    /**
     * On ConferenceJoinedSuccessEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ConferenceJoinedSuccessEvent event) {
        if (validFilter(event.getConferenceId()) || validFilter(event.getAliasId())) {
            VoxeetSdk.getInstance().getConferenceService()
                    .setAudioRoute(Media.AudioRoute.ROUTE_SPEAKER);

            displayView();

            log("onEvent: ConferenceJoinedSuccessEvent");
            if (mMainView != null) {
                mMainView.onConferenceUsersListUpdate(mConferenceUsers);
            }

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
    public void onEvent(@NonNull ConferenceCreationSuccess event) {
        if (validFilter(event.getConfId()) || validFilter(event.getConfAlias())) {
            mMainView.onConferenceCreation(event.getConfId());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceRefreshedEvent event) {
        DefaultConferenceUser user = event.getUser();
        if (user == null) {
            UserInfo profile = VoxeetToolkit.getInstance().getConferenceToolkit()
                    .getInvitedUserFromCache(event.getUserId());

            if (profile != null) {
                user = new DefaultConferenceUser(event.getUserId(), null, profile);
            }
        }

        if (null != user) {
            if (!mConferenceUsers.contains(user)) {
                mConferenceUsers.add(user);
                if (mMainView != null) {
                    mMainView.onConferenceUsersListUpdate(mConferenceUsers);
                }
            }

            if (mMainView != null) {
                mMainView.onConferenceUserUpdated(user);
            }
        }
    }

    /**
     * On ConferenceUserUpdatedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ConferenceUserUpdatedEvent event) {
        log("onEvent: ConferenceUserUpdatedEvent " + event.getUser());
        DefaultConferenceUser user = event.getUser();

        if (!mConferenceUsers.contains(user)) {
            mConferenceUsers.add(user);
            if (mMainView != null) {
                mMainView.onConferenceUsersListUpdate(mConferenceUsers);
            }
        }

        mMediaStreams.put(user.getUserId(), event.getMediaStream());

        if (mMainView != null) {
            mMainView.onMediaStreamUpdated(user.getUserId(), mMediaStreams);

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
        log("onEvent: ConferenceUserJoinedEvent " + event);
        DefaultConferenceUser user = event.getUser();

        if (!mConferenceUsers.contains(user)) {
            mConferenceUsers.add(user);
            if (mMainView != null) {
                mMainView.onConferenceUsersListUpdate(mConferenceUsers);
            }
        }

        mMediaStreams.put(user.getUserId(), event.getMediaStream());

        if (mMainView != null) {
            mMainView.onMediaStreamUpdated(user.getUserId(), mMediaStreams);

            mMainView.onConferenceUserJoined(user);
        }

    }

    /**
     * On ConferenceUserLeftEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserLeftEvent event) {
        if (mMainView != null) {
            DefaultConferenceUser user = event.getUser();
            if (mConferenceUsers.contains(user)) {
                mConferenceUsers.remove(user);
                mMainView.onConferenceUsersListUpdate(mConferenceUsers);
            }

            mMainView.onConferenceUserLeft(user);
        }
    }

    /**
     * On User Declined call event
     * <p>
     * Logic is quite different from the onEvent(ConferenceUserLeftEvent)
     * since we do not have a direct object but the user's
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserCallDeclinedEvent event) {
        if (mMainView != null) {
            int i = 0;
            DefaultConferenceUser user = null;
            while (i < mConferenceUsers.size()) {
                user = mConferenceUsers.get(i);
                if (user.getUserId() != null && user.getUserId().equals(event.getUserId())) {
                    mConferenceUsers.remove(i);
                    mMainView.onConferenceUsersListUpdate(mConferenceUsers);
                } else {
                    i++;
                }
            }
            mMainView.onConferenceUserDeclined(event.getUserId());
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

            removeView(true, RemoveViewType.FROM_EVENT);
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

            removeView(true, RemoveViewType.FROM_EVENT);
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


        removeView(true, RemoveViewType.FROM_EVENT);
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

        removeView(true, RemoveViewType.FROM_EVENT);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ReplayConferenceErrorEvent event) {
        reset();
        //TODO error message
        if (mMainView != null) mMainView.onConferenceDestroyed();

        removeView(true, RemoveViewType.FROM_EVENT);
    }

    /**
     * On RecordingStatusUpdateEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull RecordingStatusUpdateEvent event) {
        mMainView.onRecordingStatusUpdated(RecordingStatus.RECORDING.name().equalsIgnoreCase(event.getRecordingStatus()));
    }

    /**
     * On ConferenceUpdatedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ConferenceUpdatedEvent event) {
        mMainView.onConferenceUpdated(event.getEvent().getParticipants());
    }

    private void log(@NonNull String value) {
        //Log.d(TAG, value);
    }

    private void mergeConferenceUsers(@NonNull List<DefaultConferenceUser> users) {
        if (users != mConferenceUsers) {
            for (DefaultConferenceUser user : users) {
                if (null != user && !mConferenceUsers.contains(user)) {
                    log("init: adding " + user + " " + user.getUserInfo());
                    mConferenceUsers.add(user);
                }
            }
        }
    }

    /**
     * Simple enum to manage the different ways to request for view removal, if any
     *
     * FROM_HUD = a graphical interaction occured : pause, kill etc...
     * FROM_EVENT = the conference emitted an/- event-s, management requires a removal from thi.o.se
     */
    public enum RemoveViewType {
        FROM_HUD,
        FROM_EVENT
    }
}
