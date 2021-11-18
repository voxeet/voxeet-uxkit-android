package com.voxeet.uxkit.controllers;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.promise.solve.ThenPromise;
import com.voxeet.sdk.events.sdk.ConferenceStatusUpdatedEvent;
import com.voxeet.sdk.events.sdk.IncomingCallEvent;
import com.voxeet.sdk.events.success.ConferenceUpdated;
import com.voxeet.sdk.events.v2.ParticipantAddedEvent;
import com.voxeet.sdk.events.v2.ParticipantUpdatedEvent;
import com.voxeet.sdk.events.v2.StreamAddedEvent;
import com.voxeet.sdk.events.v2.StreamRemovedEvent;
import com.voxeet.sdk.events.v2.StreamUpdatedEvent;
import com.voxeet.sdk.exceptions.ExceptionManager;
import com.voxeet.sdk.json.ConferenceDestroyedPush;
import com.voxeet.sdk.json.ConferenceEnded;
import com.voxeet.sdk.json.RecordingStatusUpdatedEvent;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.sdk.models.v1.RecordingStatus;
import com.voxeet.sdk.models.v2.ParticipantType;
import com.voxeet.sdk.services.AudioService;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.services.conference.information.ConferenceParticipantType;
import com.voxeet.sdk.utils.AudioType;
import com.voxeet.sdk.utils.Map;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.sdk.utils.ScreenHelper;
import com.voxeet.uxkit.R;
import com.voxeet.uxkit.configuration.Configuration;
import com.voxeet.uxkit.events.UXKitNotInConferenceEvent;
import com.voxeet.uxkit.implementation.VoxeetConferenceView;
import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.uxkit.providers.containers.IVoxeetOverlayViewProvider;
import com.voxeet.uxkit.providers.logics.IVoxeetSubViewProvider;
import com.voxeet.uxkit.providers.rootview.AbstractRootViewProvider;
import com.voxeet.uxkit.utils.LoadLastSavedOverlayStateEvent;
import com.voxeet.uxkit.utils.ToolkitUtils;
import com.voxeet.uxkit.views.internal.VoxeetOverlayContainerFrameLayout;

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
    //TODO put this static variable into each controller with an abstract method to make sure of no collision with various impl
    private static OverlayState SAVED_OVERLAY_STATE = null;

    public final Configuration Configuration = new Configuration();

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
    private CopyOnWriteArrayList<Runnable> removeRunnables;
    private boolean showOnCreations = true; //it will make the overlay to be displayed on creating/created

    private AbstractConferenceToolkitController() {

    }

    protected AbstractConferenceToolkitController(Context context, EventBus eventbus) {
        removeRunnables = new CopyOnWriteArrayList();
        mContext = context;
        mEventBus = eventbus;

        mHandler = new Handler(Looper.getMainLooper());

        mRootViewProvider = VoxeetToolkit.instance().getDefaultRootViewProvider();

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
        Activity activity = VoxeetToolkit.instance().getCurrentActivity();

        Log.d(TAG, "init saved ?" + SAVED_OVERLAY_STATE);
        if (null == SAVED_OVERLAY_STATE) {
            SAVED_OVERLAY_STATE = getDefaultOverlayState();
        }

        OverlayState state = SAVED_OVERLAY_STATE;
        mMainView = mVoxeetOverlayViewProvider.createView(activity,
                mVoxeetSubViewProvider,
                state);

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
        return VoxeetToolkit.instance().isEnabled();
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

    private boolean isInConference() {
        ConferenceService service = VoxeetSDK.conference();
        if (null != service) return service.isInConference() || service.isLive();

        return false;
    }

    private void displayView() {
        flushRemoveRunnables();

        if (mMainView == null && isInConference()) {
            init();
        }

        if (isOverlayEnabled() && isInConference()) {
            mHandler.postDelayed(() -> {
                try {
                    //request audio focus and set in voice call
                    AudioService service = VoxeetSDK.audio();
                    service.requestAudioFocus();
                    service.checkOutputRoute();

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

        Runnable removeHold = () -> {
            //releasing the hold on the view
            if (statement_release) {
                Log.d(TAG, "run: killing the saved overlay state 1");
                if (!keepOverlayState) SAVED_OVERLAY_STATE = null;

                mMainView = null;

                getRootViewProvider().onReleaseRootView();
            }
        };

        Runnable runnable = () -> {
            if (view != null && release) {
                if (view == mMainView || statement_release) {
                    if (viewParent.getParent() instanceof ViewGroup) {

                        Log.d(TAG, "removeView: statement_release_2 true ");

                        ViewGroup viewHolder = (ViewGroup) viewParent.getParent();
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
                    Log.d("DefaultRootViewProvider", "run: killing the saved overlay state " + keepOverlayState);
                    if (!keepOverlayState) SAVED_OVERLAY_STATE = null;

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
        };

        //remove right now
        if (0 > timeout) {
            removeHold.run();
            removeRunnables.add(runnable);
            mHandler.post(runnable);
        } else {
            //after a delay
            Runnable run = () -> {
                removeHold.run();
                runnable.run();
            };
            removeRunnables.add(run);
            mHandler.postDelayed(run, timeout);
        }
    }

    /**
     * @param activity
     */
    public void onActivityResumed(Activity activity) {
        if (isEnabled() && isInConference() && null == mMainView) init();

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
            removeView(false, RemoveViewType.FROM_HUD, true);
        }
    }

    /**
     * @param overlay as the new default
     */
    public void setDefaultOverlayState(@NonNull OverlayState overlay) {
        mDefaultOverlayState = overlay;

        //set the new state to the view
        if (mMainView != null) {
            if (OverlayState.EXPANDED.equals(overlay)) {
                maximize();
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

    /**
     * Minimize the overlay
     */
    public void minimize() {
        Log.d("DefaultRootViewProvider", "minimize");
        if (null != mMainView) mMainView.minimize();
        SAVED_OVERLAY_STATE = OverlayState.MINIMIZED;
    }

    /**
     * Maximize the overlay
     */
    public void maximize() {
        Log.d("DefaultRootViewProvider", "maximize");
        if (null != mMainView) mMainView.expand();
        SAVED_OVERLAY_STATE = OverlayState.EXPANDED;
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ConferenceStatusUpdatedEvent event) {
        Log.d("DefaultRootViewProvider", "onEvent: state " + event.state + " " + mMainView);
        switch (event.state) {
            case CREATING:
                onConferenceCreatingEvent(event);
                break;
            case CREATED:
                onConferenceCreatedEvent(event);
                break;
            case ERROR:
                onConferenceError(event);
                break;
            case JOINING:
                onConferenceJoiningEvent(event);
                break;
            case JOINED:
                onConferenceJoinedEvent(event);
                break;
            case LEAVING:
                break;
            case LEFT:
                onConferenceLeftEvent(event);
                break;
            case DEFAULT:
            default:

        }
    }

    private void onConferenceCreatingEvent(ConferenceStatusUpdatedEvent event) {
        //TODO check for call ?
        //VoxeetSDK.audio().playSoundType(AudioType.RING);
        Activity activity = VoxeetToolkit.instance().getCurrentActivity();

        log("onEvent: " + event.getClass().getSimpleName() + " " + activity);
        if (activity != null) {
            if (isEnabled() && isInConference() && null == mMainView) init();

            setParams();

            displayView();

            if (mMainView != null) {
                mMainView.onConferenceCreating();
            }
        }
    }

    private void onConferenceJoiningEvent(ConferenceStatusUpdatedEvent event) {
        ConferenceInformation information = VoxeetSDK.conference().getCurrentConference();

        if (null != information && ConferenceParticipantType.NORMAL.equals(information.getConferenceParticipantType())) {
            VoxeetSDK.audio().playSoundType(AudioType.RING);
        } else {
            VoxeetSDK.audio().stopSoundType(AudioType.RING);
            Log.d(TAG, "onEvent: your current conference type is not compatible with ringing");
        }

        Activity activity = VoxeetToolkit.instance().getCurrentActivity();

        if (activity != null && validFilter(Opt.of(event.conference).then(Conference::getId).or(""))) {
            if (null == mMainView) init();

            setParams();

            displayView();

            if (mMainView != null) {
                mMainView.onConferenceJoining(event.conference);
            }
        }
    }

    private void onConferenceJoinedEvent(ConferenceStatusUpdatedEvent event) {
        if (validFilter(event.conference.getAlias()) || validFilter(event.conference.getId())) {

            if (isEnabled() && Configuration.Contextual.default_speaker_on) {
                Log.d(TAG, "onConferenceJoinedEvent: switching to speaker");
                VoxeetSDK.audio().enumerateDevices().then((ThenPromise<List<MediaDevice>, Boolean>) mediaDevices -> {
                    MediaDevice speaker = Map.find(mediaDevices,
                            mediaDevice -> mediaDevice.deviceType() == DeviceType.EXTERNAL_SPEAKER
                                    && mediaDevice.platformConnectionState() == ConnectionState.CONNECTED);

                    if (null == speaker)
                        throw new IllegalStateException("Impossible to make output to speaker");
                    return VoxeetSDK.audio().connect(speaker);
                }).error(error -> Log.e(TAG, "onError: ", error));
            }

            displayView();

            log("onEvent: ConferenceJoinedSuccessEvent");
            if (mMainView != null) {
                mMainView.onConferenceUsersListUpdate(getParticipants());
                mMainView.onConferenceJoined(event.conference);
            }
        }
    }

    private void onConferenceCreatedEvent(ConferenceStatusUpdatedEvent event) {
        ConferenceInformation information = VoxeetSDK.conference().getCurrentConference();

        if (null != information && ConferenceParticipantType.NORMAL.equals(information.getConferenceParticipantType())) {
            VoxeetSDK.audio().playSoundType(AudioType.RING);
        } else {
            VoxeetSDK.audio().stopSoundType(AudioType.RING);
            Log.d(TAG, "onEvent: your current conference type is not compatible with ringing");
        }

        if (showOnCreations)
            displayView();

        if (validFilter(event.conference.getId()) && mMainView == null) init();

        if (validFilter(event.conference.getId()) || validFilter(event.conference.getAlias())) {
            mMainView.onConferenceCreation(event.conference);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ParticipantAddedEvent event) {
        log("onEvent: UserAddedEvent " + event.participant);
        if (mMainView != null) mMainView.onUserAddedEvent(event.conference, event.participant);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ParticipantUpdatedEvent event) {
        checkStopOutgoingCall();

        log("onEvent: UserUpdatedEvent " + event);
        if (mMainView != null) mMainView.onUserUpdatedEvent(event.conference, event.participant);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StreamAddedEvent event) {
        if (null != mMainView) {
            mMainView.onStreamAddedEvent(event.conference, event.participant, event.mediaStream);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StreamUpdatedEvent event) {
        if (null != mMainView) {
            mMainView.onStreamUpdatedEvent(event.conference, event.participant, event.mediaStream);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StreamRemovedEvent event) {
        if (null != mMainView) {
            mMainView.onStreamRemovedEvent(event.conference, event.participant, event.mediaStream);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UXKitNotInConferenceEvent event) {
        onConferenceLeftEvent(null);
    }

    private void onConferenceLeftEvent(@Nullable ConferenceStatusUpdatedEvent event) {
        VoxeetSDK.audio().stop();

        if (null != mMainView) {
            mMainView.onConferenceLeft();

            removeView(true, RemoveViewType.FROM_EVENT);
        }
    }

    private void onConferenceError(ConferenceStatusUpdatedEvent event) {
        Log.d("SoundPool", "onEvent: " + event.getClass().getSimpleName());
        VoxeetSDK.audio().stop();

        if (null != mMainView) {
            mMainView.onConferenceError(event.error);

            removeView(true, RemoveViewType.FROM_EVENT, false, 3000);
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

        VoxeetSDK.audio().stop();

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

        VoxeetSDK.audio().stop();

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
    public void onEvent(@NonNull RecordingStatusUpdatedEvent event) {
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
            ConferenceInformation currentConference = VoxeetSDK.conference().getCurrentConference();
            if (null != currentConference) {
                mMainView.onConferenceUpdated(ToolkitUtils.filterParticipants(currentConference.getConference().getParticipants()));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull IncomingCallEvent event) {
        if (null != mMainView) {
            mMainView.onConferenceUsersListUpdate(getParticipants());
            mMainView.minimize();
            SAVED_OVERLAY_STATE = OverlayState.MINIMIZED;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull LoadLastSavedOverlayStateEvent event) {
        Log.d("DefaultRootViewProvider", "onEvent: LoadLastSavedOverlayStateEvent " + SAVED_OVERLAY_STATE);
        if (null != mMainView) {
            OverlayState state = SAVED_OVERLAY_STATE;
            if (null == state) state = getDefaultOverlayState();

            if (OverlayState.EXPANDED.equals(state)) {
                maximize();
            } else {
                minimize();
            }
        }
    }

    private void log(@NonNull String value) {
        Log.d(TAG, value);
    }

    private void checkStopOutgoingCall() {
        boolean found = null != Map.find(Opt.of(VoxeetSDK.conference())
                        .then(ConferenceService::getParticipants).or(new ArrayList<>()),
                participant -> {
                    // if invalid or ourselves
                    if (null == participant.getId() || participant.getId().equals(VoxeetSDK.session().getParticipantId()))
                        return false;
                    // if known as on air
                    if (ConferenceParticipantStatus.ON_AIR.equals(participant.getStatus()))
                        return true;
                    // if not a user
                    if (!ParticipantType.USER.equals(participant.participantType())) return false;
                    // if connecting but with already a stream
                    if (ConferenceParticipantStatus.CONNECTING.equals(participant.getStatus())) {
                        MediaStream stream = participant.streamsHandler().getFirst(MediaStreamType.Camera);
                        if (null == stream) return false;
                        return stream.audioTracks().size() > 0 || stream.videoTracks().size() > 0;
                    }
                    return false;
                });

        if (found) {
            VoxeetSDK.audio().stop();
        }
    }


    private List<Participant> getParticipants() {
        return Opt.of(VoxeetSDK.conference()).then(ConferenceService::getParticipants).or(new ArrayList<>());
    }

    @Override
    public void onSizedChangedListener(@NonNull VoxeetOverlayContainerFrameLayout view) {
        Log.d(TAG, "onSizedChangedListener: " + SAVED_OVERLAY_STATE);
        if (null != mMainView) {
            switch (SAVED_OVERLAY_STATE) {
                case MINIMIZED:
                    minimize();
                    break;
                case EXPANDED:
                    maximize();
                    break;
                default:
            }
        }
    }

    @NonNull
    private String optConferenceId() {
        ConferenceService service = VoxeetSDK.conference();
        return Opt.of(service.getConferenceId()).or("");
    }
}
