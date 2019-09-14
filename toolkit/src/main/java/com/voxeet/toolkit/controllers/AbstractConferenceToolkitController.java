package com.voxeet.toolkit.controllers;

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

import com.voxeet.audio.AudioRoute;
import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.core.preferences.VoxeetPreferences;
import com.voxeet.sdk.core.services.AudioService;
import com.voxeet.sdk.core.services.ConferenceService;
import com.voxeet.sdk.core.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.core.services.conference.information.ConferenceUserType;
import com.voxeet.sdk.events.error.ConferenceCreatedError;
import com.voxeet.sdk.events.error.ConferenceJoinedError;
import com.voxeet.sdk.events.error.ConferenceLeftError;
import com.voxeet.sdk.events.error.ReplayConferenceErrorEvent;
import com.voxeet.sdk.events.sdk.ConferenceCreatingEvent;
import com.voxeet.sdk.events.sdk.ConferenceCreationSuccess;
import com.voxeet.sdk.events.sdk.ConferenceJoinedSuccessEvent;
import com.voxeet.sdk.events.sdk.ConferenceLeftSuccessEvent;
import com.voxeet.sdk.events.sdk.ConferencePreJoinedEvent;
import com.voxeet.sdk.events.sdk.ConferenceUserCallDeclinedEvent;
import com.voxeet.sdk.events.sdk.IncomingCallEvent;
import com.voxeet.sdk.events.success.ConferenceUpdated;
import com.voxeet.sdk.events.v2.StreamAddedEvent;
import com.voxeet.sdk.events.v2.StreamRemovedEvent;
import com.voxeet.sdk.events.v2.StreamUpdatedEvent;
import com.voxeet.sdk.events.v2.UserAddedEvent;
import com.voxeet.sdk.events.v2.UserUpdatedEvent;
import com.voxeet.sdk.exceptions.ExceptionManager;
import com.voxeet.sdk.json.ConferenceDestroyedPush;
import com.voxeet.sdk.json.ConferenceEnded;
import com.voxeet.sdk.json.InvitationReceivedEvent;
import com.voxeet.sdk.json.RecordingStatusUpdateEvent;
import com.voxeet.sdk.json.UserInvited;
import com.voxeet.sdk.models.User;
import com.voxeet.sdk.models.v1.ConferenceUserStatus;
import com.voxeet.sdk.models.v1.RecordingStatus;
import com.voxeet.sdk.models.v1.UserProfile;
import com.voxeet.sdk.utils.AudioType;
import com.voxeet.sdk.utils.ScreenHelper;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.implementation.VoxeetConferenceView;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.toolkit.providers.containers.IVoxeetOverlayViewProvider;
import com.voxeet.toolkit.providers.logics.IVoxeetSubViewProvider;
import com.voxeet.toolkit.providers.rootview.AbstractRootViewProvider;
import com.voxeet.toolkit.utils.LoadLastSavedOverlayStateEvent;
import com.voxeet.toolkit.views.internal.VoxeetOverlayContainerFrameLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


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

public abstract class AbstractConferenceToolkitController implements VoxeetOverlayContainerFrameLayout.OnSizeChangedListener {

    private Context mContext;
    @NonNull
    private EventBus mEventBus = EventBus.getDefault();

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

    //private VoxeetOverlayContainerFrameLayout mMainViewParent;

    /**
     * Information about the mParams of the
     */
    @NonNull
    private FrameLayout.LayoutParams mParams;

    private IVoxeetOverlayViewProvider mVoxeetOverlayViewProvider;
    private IVoxeetSubViewProvider mVoxeetSubViewProvider;
    private OverlayState mDefaultOverlayState;
    private boolean mEnabled;
    private String TAG = VoxeetConferenceView.class.getSimpleName();
    private boolean mIsViewRetainedOnLeave;
    private AbstractRootViewProvider mRootViewProvider;
    private OverlayState mSavedOverlayState;
    private CopyOnWriteArrayList<Runnable> removeRunnables;

    private AbstractConferenceToolkitController() {

    }

    protected AbstractConferenceToolkitController(Context context, EventBus eventbus) {
        removeRunnables = new CopyOnWriteArrayList();
        mContext = context;
        mEventBus = eventbus;

        mHandler = new Handler(Looper.getMainLooper());

        mRootViewProvider = VoxeetToolkit.getInstance().getDefaultRootViewProvider();

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

        ConferenceService service = VoxeetSdk.conference();

        Log.d(TAG, "init saved ?" + mSavedOverlayState);
        if (null == mSavedOverlayState) mSavedOverlayState = getDefaultOverlayState();

        boolean is_new_conference = false; //TODO implement conference switch

        OverlayState state = mSavedOverlayState;
        mMainView = mVoxeetOverlayViewProvider.createView(activity,
                mVoxeetSubViewProvider,
                state);

        List<User> list = VoxeetSdk.conference().getLastInvitationUsers();
        mergeConferenceUsers(list);

        if (null != AudioService.getSoundManager()) {
            AudioService.getSoundManager().requestAudioFocus();
        }
    }

    /**
     * Register the controller to the instance of eventbus given in constructor
     * <p>
     * If the mainview is valid, we also call the interface's method to give possible new
     */
    public void register() {
        if (!mEventBus.isRegistered(this))
            mEventBus.register(this);
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

    public void setRootViewProvider(@NonNull AbstractRootViewProvider provider) {
        mRootViewProvider = provider;
    }

    private AbstractRootViewProvider getRootViewProvider() {
        return mRootViewProvider;
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

    private void flushRemoveRunnables() {
        for (Runnable runnable : removeRunnables) {
            mHandler.removeCallbacks(runnable);
        }
        removeRunnables.clear();
    }

    public void forceReattach() {

    }

    private void displayView() {
        flushRemoveRunnables();

        //display the view
        boolean in_conf = false;
        if (null != VoxeetSdk.instance()) {
            in_conf = VoxeetSdk.conference().isInConference()
                    || VoxeetSdk.conference().isLive();
        }


        Log.d(TAG, "displayView: " + mMainView + " " + in_conf + " " + isOverlayEnabled());

        boolean should_send_user_join = false;
        if (mMainView == null && in_conf) {
            init();
            should_send_user_join = true;
        }
        /*if (!isOverlayEnabled() || !in_conf) {
            try {
                throw new Exception("trying to load view when not in proper state");
            } catch (Exception e) {
                //ExceptionManager.sendException(e);
            }
        }*/

        if (isOverlayEnabled() && in_conf) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        //request audio focus and set in voice call
                        if (null != VoxeetSdk.instance()) {
                            AudioService service = VoxeetSdk.audio();
                            service.requestAudioFocus();
                            service.checkOutputRoute();
                        }

                        log("run: add view" + mMainView);
                        if (mMainView != null) {
                            boolean added = false;
                            Activity activity = getRootViewProvider().getCurrentActivity();
                            ViewGroup root = getRootViewProvider().getRootView();

                            if (!getRootViewProvider().isSameActivity()) {
                                getRootViewProvider().detachRootViewFromParent();
                            }

                            ViewGroup viewHolder = (ViewGroup) mMainView.getParent();
                            if (null != viewHolder && null != root && root != viewHolder) {
                                Log.d(TAG, "run: REMOVING MAIN VIEW FROM HOLDER" + root + " " + viewHolder);
                                //viewHolder.removeView(mMainView);
                                viewHolder = (ViewGroup) mMainView.getParent();
                                if (viewHolder != null)
                                    viewHolder.removeView(mMainView);
                            }

                            if (null != root && null != activity && !activity.isFinishing()) {

                                getRootViewProvider().addRootView(AbstractConferenceToolkitController.this);

                                if (null == mMainView.getParent()) {
                                    added = true;
                                    getRootViewProvider().getRootView().addView(mMainView, mParams);
                                }

                                mMainView.requestLayout();
                                getRootViewProvider().getRootView().requestLayout();
                                mMainView.onResume();

                                if (added) mEventBus.post(new LoadLastSavedOverlayStateEvent());
                            }
                        }
                    } catch (Exception e) {
                        ExceptionManager.sendException(e);
                    }
                }
            }, 1000);
        }
    }

    public void removeView(final boolean should_release, final RemoveViewType from_type) {
        removeView(should_release, from_type, false, -1);
    }

    public void removeView(final boolean should_release, final RemoveViewType from_type, boolean keepOverlayState) {
        removeView(should_release, from_type, keepOverlayState, -1);
    }

    public void removeView(final boolean should_release, final RemoveViewType from_type, boolean keepOverlayState, int timeout /* < 0 now*/) {
        final AbstractVoxeetOverlayView view = mMainView;
        final FrameLayout viewParent = getRootViewProvider().getRootView();
        final boolean release = !isEnabled() || (!RemoveViewType.FROM_HUD_BUT_KEEP_TIMEOUT.equals(from_type) && !isViewRetainedOnLeave());

        final boolean statement_release = should_release && release;

        Log.d(TAG, "removeView: statement_release_1 " + statement_release);

        Runnable removeHold = new Runnable() {
            @Override
            public void run() {

                //releasing the hold on the view
                if (statement_release) {
                    Log.d(TAG, "run: killing the saved overlay state 1");
                    if (!keepOverlayState) mSavedOverlayState = null;

                    mMainView = null;

                    getRootViewProvider().onReleaseRootView();
                }
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (view != null && release) {
                    ViewGroup viewHolder = (ViewGroup) view.getParent();
                    //if (viewHolder != null)
                    //    viewHolder.removeView(view);

                    if (view == mMainView || statement_release) {
                        if (viewParent.getParent() instanceof ViewGroup) {

                            Log.d(TAG, "removeView: statement_release_2 true ");

                            viewHolder = (ViewGroup) viewParent.getParent();
                            if (viewHolder != null)
                                viewHolder.removeView(viewParent);
                        } else {

                            Log.d(TAG, "removeView: statement_release_3 true ");

                            getRootViewProvider().onReleaseRootView();
                        }
                    }

                    view.onStop();

                    if (statement_release) {
                        //restore the saved state
                        Log.d(TAG, "run: killing the saved overlay state");
                        if (!keepOverlayState) mSavedOverlayState = null;

                        Log.d(TAG, "run: AbstractConferenceToolkitController should release view " + view.getClass().getSimpleName());
                        view.onDestroy();
                        //if we still have the main view displayed
                        //but wanted to clear it
                        if (view == mMainView) {
                            mMainView = null;

                            getRootViewProvider().onReleaseRootView();
                        }
                    }
                }
            }
        };


        //long after = should_release && mMainView != null ? mMainView.getCloseTimeoutInMilliseconds() : 0;

        //remove right now
        if (0 > timeout) {
            removeHold.run();
            removeRunnables.add(runnable);
            mHandler.post(runnable);
        } else {
            //after a delay
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    removeHold.run();
                    runnable.run();
                }
            };
            removeRunnables.add(run);
            mHandler.postDelayed(run, timeout);
        }
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
            //removeView(false, RemoveViewType.FROM_HUD_BUT_KEEP_TIMEOUT, 300);
            removeView(false, RemoveViewType.FROM_HUD, true);
        }
    }

    /**
     * Reset the state of the streams and conference users of this controller
     */
    private void reset() {
        //mMediaStreams = new HashMap<>();
        //mConferenceUsers = new ArrayList<>();
    }

    /**
     * @param overlay as the new default
     */
    public void setDefaultOverlayState(@NonNull OverlayState overlay) {
        mDefaultOverlayState = overlay;

        Log.d(TAG, "setDefaultOverlayState: overlay := " + overlay);
        //set the new state to the view
        if (mMainView != null) {
            if (OverlayState.EXPANDED.equals(overlay)) {
                expand();
            } else {
                minimize();
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

    private void minimize() {
        if (null != mMainView) mMainView.minimize();
        mSavedOverlayState = OverlayState.MINIMIZED;
    }

    private void expand() {
        if (null != mMainView) mMainView.expand();
        mSavedOverlayState = OverlayState.EXPANDED;
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
     * TODO check for retain state switch : quit in correct cases
     *
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

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Event Management - see EventBus field
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Display the conference view when the user is creating a conference
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ConferenceCreatingEvent event) {
        //TODO check for call ?
        //VoxeetSdk.audio().playSoundType(AudioType.RING);
        Activity activity = VoxeetToolkit.getInstance().getCurrentActivity();

        log("onEvent: " + event.getClass().getSimpleName()
                + " " + activity);
        if (activity != null) {
            if (mMainView == null) init();

            setParams();

            displayView();

            if (mMainView != null) {
                mMainView.onConferenceCreating();
            }
        }
    }

    /**
     * Display the conference view when the user is creating/joining a conference.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ConferencePreJoinedEvent event) {
        ConferenceInformation information = VoxeetSdk.conference().getCurrentConferenceInformation();

        if (null != information && ConferenceUserType.NORMAL.equals(information.getConferenceUserType())) {
            VoxeetSdk.audio().playSoundType(AudioType.RING);
        } else {
            VoxeetSdk.audio().stopSoundType(AudioType.RING);
            Log.d(TAG, "onEvent: your current conference type is not compatible with ringing");
        }

        Activity activity = VoxeetToolkit.getInstance().getCurrentActivity();

        if (activity != null && validFilter(event.conference.getId())) {
            if (mMainView == null) init();

            setParams();

            displayView();

            if (mMainView != null) {
                mMainView.onConferenceJoining(event.conference);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserInvited invited) {
        List<UserProfile> profiles = invited.participants;
        List<User> users = getConferenceUsers();
        if (mMainView != null) {
            mMainView.onConferenceUsersListUpdate(users);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final InvitationReceivedEvent event) {
        if (null != event && null != event.getInvitations()) {
            List<User> users = getConferenceUsers();
            if (mMainView != null) {
                mMainView.onConferenceUsersListUpdate(users);
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
        if (validFilter(event.conferenceId) || validFilter(event.conferenceAlias)) {
            VoxeetSdk.audio().setAudioRoute(AudioRoute.ROUTE_SPEAKER);

            displayView();

            List<User> users = VoxeetSdk.conference().getConferenceUsers();
            log("onEvent: ConferenceJoinedSuccessEvent");
            if (mMainView != null) {
                mMainView.onConferenceUsersListUpdate(users);
            }

            if (mMainView != null) {
                mMainView.onConferenceJoined(event.conferenceId);
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
        ConferenceInformation information = VoxeetSdk.conference().getCurrentConferenceInformation();

        if (null != information && ConferenceUserType.NORMAL.equals(information.getConferenceUserType())) {
            VoxeetSdk.audio().playSoundType(AudioType.RING);
        } else {
            VoxeetSdk.audio().stopSoundType(AudioType.RING);
            Log.d(TAG, "onEvent: your current conference type is not compatible with ringing");
        }

        if (mMainView == null) init();

        if (validFilter(event.conferenceId) || validFilter(event.conferenceAlias)) {
            mMainView.onConferenceCreation(event.conferenceId);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull UserAddedEvent event) {
        log("onEvent: UserAddedEvent " + event.user);
        User user = event.user;

        if (mMainView != null) mMainView.onUserAddedEvent(event.conference, user);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final UserUpdatedEvent event) {
        checkStopOutgoingCall();

        log("onEvent: UserUpdatedEvent " + event);
        User user = event.user;

        if (mMainView != null) mMainView.onUserUpdatedEvent(event.conference, user);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StreamAddedEvent event) {
        if (null != mMainView) {
            mMainView.onStreamAddedEvent(event.conference, event.user, event.mediaStream);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StreamUpdatedEvent event) {
        if (null != mMainView) {
            mMainView.onStreamUpdatedEvent(event.conference, event.user, event.mediaStream);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StreamRemovedEvent event) {
        if (null != mMainView) {
            mMainView.onStreamRemovedEvent(event.conference, event.user, event.mediaStream);
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
        if (null != mMainView) {
            int i = 0;
            User user = null;
            List<User> users = getConferenceUsers();
            while (i < users.size()) {
                user = users.get(i);
                if (user.getId() != null && user.getId().equals(event.userId)) {
                    users.remove(i);
                    mMainView.onConferenceUsersListUpdate(users);
                } else {
                    i++;
                }
            }
            mMainView.onConferenceUserDeclined(event.userId);
        }
    }

    /**
     * On ConferenceLeftSuccessEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceLeftSuccessEvent event) {
        Log.d("SoundPool", "onEvent: " + event.getClass().getSimpleName());
        VoxeetSdk.audio().stop();

        if (null != mMainView) {
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
        Log.d("SoundPool", "onEvent: " + event.getClass().getSimpleName());
        VoxeetSdk.audio().stop();

        if (null != mMainView) {
            reset();
            mMainView.onConferenceLeft();

            removeView(true, RemoveViewType.FROM_EVENT);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceCreatedError event) {
        Log.d("SoundPool", "onEvent: " + event.getClass().getSimpleName());
        VoxeetSdk.audio().stop();

        if (null != mMainView) {
            reset();
            mMainView.onConferenceDestroyed();

            removeView(true, RemoveViewType.FROM_EVENT);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceJoinedError event) {
        Log.d("SoundPool", "onEvent: " + event.getClass().getSimpleName());
        VoxeetSdk.audio().stop();

        if (null != mMainView) {
            reset();
            mMainView.onConferenceDestroyed();

            removeView(true, RemoveViewType.FROM_EVENT);
        }
    }

    /**
     * On event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPush event) {
        Log.d("SoundPool", "onEvent: " + event.getClass().getSimpleName());

        //avoid leaving when received event for another conference
        //should not impact current flows since local events are already make the ui leave when the current user interacts in hi.er own conf
        if (!optConferenceId().equals(event.conferenceId)) {
            Log.d(TAG, "onEvent: ConferenceDestroyedPush received for another conf. current:=" + optConferenceId() + " other:=" + event.conferenceId);
            return;
        }

        if (null != VoxeetSdk.instance()) {
            VoxeetSdk.audio().stop();
        }

        reset();
        if (null != mMainView) {
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
    public void onEvent(ConferenceEnded event) {
        Log.d("SoundPool", "onEvent: " + event.getClass().getSimpleName());

        //avoid leaving when received event for another conference
        //should not impact current flows since local events are already make the ui leave when the current user interacts in hi.er own conf
        if (!optConferenceId().equals(event.conferenceId)) {
            Log.d(TAG, "onEvent: ConferenceDestroyedPush received for another conf. current:=" + optConferenceId() + " other:=" + event.conferenceId);
            return;
        }

        VoxeetSdk.audio().stop();

        reset();
        if (null != mMainView) {
            mMainView.onConferenceDestroyed();
        }

        removeView(true, RemoveViewType.FROM_EVENT);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ReplayConferenceErrorEvent event) {
        Log.d("SoundPool", "onEvent: " + event.getClass().getSimpleName());
        VoxeetSdk.audio().stop();

        reset();
        //TODO error message
        if (null != mMainView) {
            mMainView.onConferenceDestroyed();
        }

        removeView(true, RemoveViewType.FROM_EVENT);
    }

    /**
     * On RecordingStatusUpdateEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull RecordingStatusUpdateEvent event) {
        if (null != mMainView) {
            mMainView.onRecordingStatusUpdated(RecordingStatus.RECORDING.name().equalsIgnoreCase(event.recordingStatus));
        }
    }

    /**
     * On ConferenceUpdatedEvent event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ConferenceUpdated event) {
        if (null != mMainView) {
            ConferenceInformation currentConference = VoxeetSdk.conference().getCurrentConferenceInformation();
            if (null != currentConference) {
                mMainView.onConferenceUpdated(currentConference.getConference().getUsers());
            }
            //mMainView.onConferenceUpdated(event.getEvent().getParticipants());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull IncomingCallEvent event) {
        if (null != mMainView) {
            mMainView.minimize();
            mSavedOverlayState = OverlayState.MINIMIZED;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull LoadLastSavedOverlayStateEvent event) {
        Log.d(TAG, "onEvent: LoadLastSavedOverlayStateEvent");
        if (null != mMainView) {
            OverlayState state = mSavedOverlayState;
            if (null == state) state = getDefaultOverlayState();

            if (OverlayState.EXPANDED.equals(state)) {
                expand();
            } else {
                minimize();
            }
        }
    }

    private void log(@NonNull String value) {
        Log.d(TAG, value);
    }

    private void mergeConferenceUsers(@NonNull List<User> users) {
        List<User> current_users = getConferenceUsers();
        if (users != current_users) {
            for (User user : users) {
                if (null != user && !current_users.contains(user)) {
                    log("init: adding " + user + " " + user.getUserInfo());
                    current_users.add(user);
                }
            }
        }
    }

    private void checkStopOutgoingCall() {
        boolean found = false;

        List<User> users = VoxeetSdk.conference().getConferenceUsers();
        for (User user : users) {
            if (null != user.getId() && !user.getId().equals(VoxeetPreferences.id())
                    && ConferenceUserStatus.ON_AIR.equals(user.getStatus())) {
                found = true;
            }
        }

        if (found) {
            Log.d("SoundPool", " checkOutgoingCall");
            VoxeetSdk.audio().stop();
        }
    }


    private List<User> getConferenceUsers() {
        if (null != VoxeetSdk.instance()) {
            return VoxeetSdk.conference().getConferenceUsers();
        }
        return new ArrayList<>();
    }

    @Override
    public void onSizedChangedListener(@NonNull VoxeetOverlayContainerFrameLayout view) {
        Log.d(TAG, "onSizedChangedListener: " + mSavedOverlayState);
        if (null != mMainView) {
            switch (mSavedOverlayState) {
                case MINIMIZED:
                    minimize();
                case EXPANDED:
                    expand();
                default:
            }
        }
    }

    @NonNull
    private String optConferenceId() {
        String conferenceId = VoxeetSdk.conference().getConferenceId();
        return null != conferenceId ? conferenceId : "";
    }
}
