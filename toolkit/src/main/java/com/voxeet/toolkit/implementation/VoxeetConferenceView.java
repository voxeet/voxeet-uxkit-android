package com.voxeet.toolkit.implementation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.MediaStreamType;
import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.core.preferences.VoxeetPreferences;
import com.voxeet.sdk.core.services.ConferenceService;
import com.voxeet.sdk.core.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.core.services.conference.information.ConferenceState;
import com.voxeet.sdk.core.services.conference.information.ConferenceUserType;
import com.voxeet.sdk.events.sdk.CameraSwitchSuccessEvent;
import com.voxeet.sdk.exceptions.ExceptionManager;
import com.voxeet.sdk.json.VideoPresentationPaused;
import com.voxeet.sdk.json.VideoPresentationPlay;
import com.voxeet.sdk.json.VideoPresentationSeek;
import com.voxeet.sdk.json.VideoPresentationStarted;
import com.voxeet.sdk.json.VideoPresentationStopped;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.User;
import com.voxeet.sdk.views.VideoView;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.configuration.ActionBar;
import com.voxeet.toolkit.configuration.Configuration;
import com.voxeet.toolkit.controllers.VoxeetToolkit;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetExpandableView;
import com.voxeet.toolkit.utils.ConferenceViewRendererControl;
import com.voxeet.toolkit.utils.IParticipantViewListener;
import com.voxeet.toolkit.utils.ToolkitUtils;
import com.voxeet.toolkit.utils.VoxeetActiveSpeakerTimer;
import com.voxeet.toolkit.views.NotchAvoidView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.RendererCommon;

import java.util.ArrayList;
import java.util.List;

public class VoxeetConferenceView extends AbstractVoxeetExpandableView implements IParticipantViewListener, VoxeetActiveSpeakerTimer.ActiveSpeakerListener {
    private final String TAG = VoxeetConferenceView.class.getSimpleName();

    @Nullable
    private VoxeetVideoStreamView videoStream;

    private VoxeetUsersView participantView;

    private VoxeetActionBarView conferenceActionBarView;

    private VoxeetSpeakerView speakerView;

    private ViewGroup layoutTimer;

    @Nullable
    private VideoView selectedView;

    private VideoView selfView;
    private ViewGroup layoutParticipant;

    private VoxeetActiveSpeakerTimer voxeetActiveSpeakerTimer;
    private VoxeetTimer voxeetTimer;

    private NotchAvoidView notchView;

    @Nullable
    private String mPreviouslyAttachedPeerId;
    private boolean mPreviouslyScreenShare;
    private TextView conferenceState;
    private ConferenceState mState = ConferenceState.DEFAULT;
    private boolean isExpanded = false;
    private ScaleGestureDetector mScaleOnPinchDetector;

    private ConferenceViewRendererControl mConferenceViewRendererControl;

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param context the context
     */
    public VoxeetConferenceView(Context context) {
        super(context);

        internalInit();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void internalInit() {
        if (null == voxeetActiveSpeakerTimer)
            voxeetActiveSpeakerTimer = new VoxeetActiveSpeakerTimer(this);

        mPreviouslyScreenShare = false;

        mScaleOnPinchDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            List<Float> mItems = new ArrayList<>();

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                Log.d(TAG, "onScale: " + scaleFactor);
                while (mItems.size() > 5) mItems.remove(0);
                mItems.add(scaleFactor);

                return super.onScale(detector);
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mItems.clear();
                return super.onScaleBegin(detector);
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                int up = 0;
                int down = 0;
                for (int i = 0, j = 1; i < mItems.size() && j < mItems.size(); i++, j++) {
                    if (mItems.get(i) < mItems.get(j)) up++;
                    else down++;
                }

                if (null != selectedView) {
                    if (up > down) {
                        if (!RendererCommon.ScalingType.SCALE_ASPECT_FILL.equals(selectedView.getScalingType())) {
                            selectedView.setVideoFill();
                        }
                    } else {
                        if (!RendererCommon.ScalingType.SCALE_ASPECT_FIT.equals(selectedView.getScalingType())) {
                            selectedView.setVideoFit();
                        }
                    }
                }

                super.onScaleEnd(detector);
            }
        });

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isExpanded && null != mScaleOnPinchDetector) {
                    return mScaleOnPinchDetector.onTouchEvent(event);
                }
                return false;
            }
        });
    }

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetConferenceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        internalInit();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (null == voxeetActiveSpeakerTimer)
            voxeetActiveSpeakerTimer = new VoxeetActiveSpeakerTimer(this);
        voxeetActiveSpeakerTimer.start();
        updateUi();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        voxeetActiveSpeakerTimer.stop();
        voxeetActiveSpeakerTimer = null;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onDetachedFromWindow();
    }

    @Override
    public void onResume() {
        super.onResume();

        //updateUi();
    }

    private void updateUi() {
        ConferenceInformation information = VoxeetSdk.conference().getCurrentConferenceInformation();

        if (null != information) {
            //check for the conference state
            checkStateValue();

            updateConferenceBarViewVisibility();

            refreshMediaStreams();
            refreshUIVisibility();
        } else {
            onConferenceLeaving(); //Left ? but left does not show anything
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (null != selfView) {
            selfView.unAttach();
        }

        if (null != selectedView) {
            mPreviouslyAttachedPeerId = selectedView.getPeerId();
            mPreviouslyScreenShare = selectedView.isScreenShare();
            selectedView.unAttach();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != selfView) {
            selfView.unAttach();
        }

        if (null != selectedView) {
            //reset the "saved" information"
            mPreviouslyAttachedPeerId = null;
            mPreviouslyScreenShare = false;

            selectedView.unAttach();
        }
    }

    @Override
    public void onConferenceCreating() {
        super.onConferenceCreating();

        //expanded and minimized
        conferenceState.setVisibility(View.VISIBLE);
        speakerView.setVisibility(View.GONE);
        selfView.setVisibility(View.GONE);
        participantView.setVisibility(View.GONE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceCreating();
        Log.d(TAG, "onConferenceCreating: " + View.VISIBLE + " " + conferenceActionBarView.getVisibility());
    }

    @Override
    public void onConferenceCreation(@NonNull Conference conference) {
        super.onConferenceCreation(conference);

        //expanded and minimized
        updateTextState(R.string.voxeet_call);
        conferenceState.setVisibility(View.VISIBLE);
        speakerView.setVisibility(View.GONE);
        selfView.setVisibility(View.GONE);
        participantView.setVisibility(View.GONE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceCreation(conference);
        Log.d(TAG, "onConferenceCreation: " + View.VISIBLE + " " + conferenceActionBarView.getVisibility());
    }

    @Override
    public void onConferenceJoining(@NonNull Conference conference) {
        super.onConferenceJoining(conference);

        //expanded and minimized
        updateTextState(R.string.voxeet_call);
        conferenceState.setVisibility(View.VISIBLE);
        speakerView.setVisibility(View.GONE);
        selfView.setVisibility(View.GONE);
        participantView.setVisibility(View.GONE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        Log.d(TAG, "onConferenceJoining: " + View.VISIBLE + " " + conferenceActionBarView.getVisibility());

        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceJoining(conference);
    }

    @Override
    public void onConferenceJoined(@NonNull Conference conference) {
        super.onConferenceJoined(conference);

        updateTextState(R.string.voxeet_call);
        conferenceState.setVisibility(View.VISIBLE);
        if (isExpanded) {
            if (null != selectedView) selectedView.setVisibility(View.GONE);
            speakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.GONE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.VISIBLE);
        } else {
            if (null != selectedView) selectedView.setVisibility(View.GONE);
            speakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.GONE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.GONE);
        }

        User user = VoxeetSdk.conference().findUserById(VoxeetSdk.session().getUserId());
        if (null != user) {
            MediaStream stream = user.streamsHandler().getFirst(MediaStreamType.Camera);
            if (!ToolkitUtils.hasParticipants() && null != stream && stream.videoTracks().size() > 0) {
                selectedView.setVisibility(View.VISIBLE);
                mConferenceViewRendererControl.attachStreamToSelf(stream);
                if (!isExpanded) selfView.setVisibility(View.GONE);
            }
        }

        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceJoined(conference);
    }

    @Override
    public void onConferenceFromNoOneToOneUser() {
        super.onConferenceFromNoOneToOneUser();

        conferenceState.setVisibility(View.GONE);
        if (isExpanded) {
            conferenceState.setVisibility(View.GONE);
            if (null != selectedView && selectedView.isAttached())
                selectedView.setVisibility(View.VISIBLE);
            else showSpeakerView();
            participantView.setVisibility(View.VISIBLE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.VISIBLE);
        } else {
            conferenceState.setVisibility(View.GONE);
            participantView.setVisibility(View.GONE);
            voxeetTimer.setVisibility(View.VISIBLE);
            notchView.setVisibility(View.GONE);

            if (null != selectedView && selectedView.isAttached())
                selectedView.setVisibility(View.VISIBLE);
            else showSpeakerView();
        }


        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceFromNoOneToOneUser();

        //just in case
        updateSpeakerViewVisibility();
        Log.d(TAG, "onConferenceFromNoOneToOneUser: " + View.VISIBLE + " " + conferenceActionBarView.getVisibility());
    }

    @Override
    public void onConferenceNoMoreUser() {
        super.onConferenceNoMoreUser();

        String ownUserId = VoxeetPreferences.id();
        if (null == ownUserId) ownUserId = "";

        updateTextState(R.string.voxeet_waiting_for_users);
        conferenceState.setVisibility(View.VISIBLE);
        if (isExpanded) {
            if (null != selectedView && (!selectedView.isAttached() || !ownUserId.equals(selectedView.getPeerId()))) {
                selectedView.setVisibility(View.GONE);
            }
            speakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.GONE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.VISIBLE);
        } else {
            if (null != selectedView && (!selectedView.isAttached() || !ownUserId.equals(selectedView.getPeerId()))) {
                selectedView.setVisibility(View.GONE);
            }
            speakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.GONE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.GONE);
        }

        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceNoMoreUser();
        Log.d(TAG, "onConferenceNoMoreUser: " + View.VISIBLE + " " + conferenceActionBarView.getVisibility());
    }

    @Override
    public void onConferenceLeaving() {
        super.onConferenceLeaving();

        //don't call updateUi here

        //expanded and minimized
        updateTextState(R.string.voxeet_leaving);
        conferenceState.setVisibility(View.VISIBLE);
        showSpeakerView();
        selfView.setVisibility(View.GONE);
        participantView.setVisibility(View.GONE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceLeaving();
        Log.d(TAG, "onConferenceLeaving: " + View.VISIBLE + " " + conferenceActionBarView.getVisibility());
    }

    @Override
    public void onConferenceDestroyed() {
        super.onConferenceDestroyed();

        if (null != selfView) {
            selfView.unAttach();
        }

        if (null != selectedView) {
            selectedView.unAttach();
        }
    }

    @Override
    public void onConferenceLeft() {
        super.onConferenceLeft();

        if (null != selfView) {
            selfView.unAttach();
        }

        if (null != selectedView) {
            selectedView.unAttach();
        }
    }

    private void refreshMediaStreams() {
        String localUserId = VoxeetSdk.session().getUserId();
        if (null == localUserId) localUserId = "";
        ConferenceService service = VoxeetSdk.conference();
        List<User> users = service.getConferenceUsers();

        String currentUserAttached = selectedView.getPeerId();
        User localUser = service.findUserById(localUserId);
        User user = service.findUserById(currentUserAttached);
        MediaStream localUserMediaStream = localUser.streamsHandler().getFirst(MediaStreamType.Camera);


        if (ToolkitUtils.hasParticipants() && localUserId.equalsIgnoreCase(currentUserAttached)) {
            selectedView.unAttach();
            selectedView.setMirror(false);
        }

        if (null != currentUserAttached) {
            MediaStream stream = user.streamsHandler().getFirst(MediaStreamType.Camera);
            MediaStream screenShareStream = user.streamsHandler().getFirst(MediaStreamType.ScreenShare);
            boolean unAttach = true;
            boolean hasScreenShare = null != screenShareStream && screenShareStream.videoTracks().size() > 0;
            boolean hasVideo = null != stream && stream.videoTracks().size() > 0;
            if (hasScreenShare) {
                unAttach = false;
            } else if (hasVideo) {
                unAttach = false;
            }

            if (unAttach) selectedView.unAttach();
        }

        String currentActiveSpeaker = getCurrentActiveSpeaker();
        loopUserForStreamInVideoViewIfUnattached(currentActiveSpeaker, users, MediaStreamType.ScreenShare);
        loopUserForStreamInVideoViewIfUnattached(currentActiveSpeaker, users, MediaStreamType.Camera);

        if (null != localUserMediaStream) {
            if (localUserMediaStream.videoTracks().size() > 0) {
                selfView.attach(localUserId, localUserMediaStream);

                selfView.setMirror(VoxeetSdk.mediaDevice().getCameraContext().isDefaultFrontFacing());
                if (isExpanded) selfView.setVisibility(View.VISIBLE);
                else selfView.setVisibility(View.GONE);
            } else {
                selfView.unAttach();
                selfView.setVisibility(View.GONE);
            }
        }

        if (!service.hasParticipants()) {
            speakerView.setVisibility(View.GONE);
            selectedView.setVisibility(View.GONE);
        } else if (!selectedView.isAttached()) {
            speakerView.onResume();
            speakerView.setVisibility(View.VISIBLE);
            selectedView.unAttach();
            selectedView.setVisibility(View.GONE);
        } else {
            speakerView.onPause();
            speakerView.setVisibility(View.GONE);
        }

        conferenceActionBarView.invalidateOwnStreams();

        ConferenceInformation information = VoxeetSdk.conference().getCurrentConferenceInformation();

        boolean enableInConfiguration = VoxeetToolkit.getInstance().getConferenceToolkit().Configuration.ActionBar.displayScreenShare;
        if (enableInConfiguration && null != information && !information.isListener()) {
            conferenceActionBarView.setDisplayScreenShare(true);
        } else {
            conferenceActionBarView.setDisplayScreenShare(false);
        }
    }

    private void loopUserForStreamInVideoViewIfUnattached(@Nullable String currentSelectedUserId,
                                                          List<User> users,
                                                          MediaStreamType mediaStreamType) {

        MediaStream foundToAttach = null;
        String userIdFoundToAttach = null;

        if (null != currentSelectedUserId) {
            User user = VoxeetSdk.conference().findUserById(currentSelectedUserId);
            if (null != user) {
                userIdFoundToAttach = user.getId();
                foundToAttach = user.streamsHandler().getFirst(mediaStreamType);
            }
        }

        if (null == foundToAttach) {
            for (User user : users) {
                String userId = user.getId();
                if (null != userId && !userId.equals(VoxeetPreferences.id()) && !userId.equals(currentSelectedUserId) && null == foundToAttach) {
                    userIdFoundToAttach = userId;
                    foundToAttach = user.streamsHandler().getFirst(mediaStreamType);
                    if (null != foundToAttach && foundToAttach.videoTracks().size() <= 0)
                        foundToAttach = null;
                }
            }
        }

        if ((!selectedView.isAttached() || MediaStreamType.ScreenShare.equals(mediaStreamType)) && null != foundToAttach && foundToAttach.videoTracks().size() > 0) {
            selectedView.setVisibility(View.VISIBLE);
            selectedView.attach(userIdFoundToAttach, foundToAttach);
        }

        if(null == foundToAttach) {
            MediaStreamType stream = selectedView.current();
            if(null != stream && stream.equals(mediaStreamType)) {
                selectedView.setVisibility(View.GONE);
                selectedView.unAttach();
            }
        }
    }

    private void checkForLocalUserStreamVideo() {
        User user = VoxeetSdk.conference().findUserById(VoxeetSdk.session().getUserId());
        if (null != user) {
            MediaStream stream = user.streamsHandler().getFirst(MediaStreamType.Camera);
            if (null != stream && stream.videoTracks().size() > 0) {
                mConferenceViewRendererControl.attachStreamToSelf(stream);
                if (!isExpanded) selfView.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void init() {

    }

    @Override
    public void onPreExpandedView() {
        if (selfView.isAttached()) {
            selfView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onExpandedView() {
        isExpanded = true;
        layoutTimer.setVisibility(View.GONE);

        layoutParticipant.setVisibility(View.VISIBLE);
        participantView.notifyDatasetChanged();

        conferenceActionBarView.onToggleSize(true);

        refreshUIVisibility();
        if (selectedView != null) {
            selectedView.setCornerRadius(0);
        }

        mConferenceViewRendererControl.enableClick(true);

        checkForLocalUserStreamVideo();
    }

    @Override
    public void onPreMinizedView() {
        selfView.setVisibility(View.GONE);
    }

    @Override
    public void onMinizedView() {

        mConferenceViewRendererControl.enableClick(false);

        isExpanded = false;
        layoutTimer.setVisibility(View.VISIBLE);

        participantView.notifyDatasetChanged();
        layoutParticipant.setVisibility(View.GONE);

        conferenceActionBarView.onToggleSize(false);

        refreshUIVisibility();
        updateUi();
        checkForLocalUserStreamVideo();
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_conference_view;
    }

    @Override
    protected void bindView(View view) {
        try {
            videoStream = view.findViewById(R.id.videoStream);
            conferenceState = view.findViewById(R.id.conference_state);
            layoutParticipant = view.findViewById(R.id.layout_participant);

            speakerView = view.findViewById(R.id.current_speaker_view);

            selectedView = view.findViewById(R.id.selected_video_view);

            selfView = view.findViewById(R.id.self_video_view);

            mConferenceViewRendererControl = new ConferenceViewRendererControl(this, selfView, selectedView);

            selfView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (VoxeetSdk.instance() != null) {
                        mConferenceViewRendererControl.switchCamera();
                    }
                }
            });

            layoutTimer = view.findViewById(R.id.layout_timer);

            conferenceActionBarView = view.findViewById(R.id.conference_bar_view);

            participantView = view.findViewById(R.id.participant_view);
            participantView.setParticipantListener(this);

            voxeetTimer = view.findViewById(R.id.voxeet_timer);

            notchView = view.findViewById(R.id.notch);


            Configuration configuration = VoxeetToolkit.getInstance().getConferenceToolkit().Configuration;
            ActionBar actionBarConfiguration = configuration.ActionBar;
            conferenceActionBarView.setDisplayCamera(actionBarConfiguration.displayCamera);
            conferenceActionBarView.setDisplayLeave(actionBarConfiguration.displayLeave);
            conferenceActionBarView.setDisplayMute(actionBarConfiguration.displayMute);
            conferenceActionBarView.setDisplayScreenShare(actionBarConfiguration.displayScreenShare);
            conferenceActionBarView.setDisplaySpeaker(actionBarConfiguration.displaySpeaker);

            //addListeners for voxeet dispatch events
            addListener(speakerView);
            addListener(conferenceActionBarView);
            addListener(participantView);
            addListener(voxeetTimer);

            updateUi();
        } catch (Exception e) {
            ExceptionManager.sendException(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onParticipantSelected(User user, MediaStream mediaStream) {
        speakerView.lockScreen(user);
        updateSpeakerViewVisibility();
        updateUi();
    }

    @Override
    public void onUserAddedEvent(@NonNull Conference conference, @NonNull User user) {
        super.onUserAddedEvent(conference, user);
        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.onUserAddedEvent(conference, user);

        updateUi();
    }

    @Override
    public void onUserUpdatedEvent(@NonNull Conference conference, @NonNull User user) {
        super.onUserUpdatedEvent(conference, user);
        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.onUserUpdatedEvent(conference, user);

        updateUi();
    }

    @Override
    public void onStreamAddedEvent(@NonNull Conference conference, @NonNull User user, @NonNull MediaStream mediaStream) {
        super.onStreamAddedEvent(conference, user, mediaStream);
        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.onStreamAddedEvent(conference, user, mediaStream);

        updateUi();
    }

    @Override
    public void onStreamUpdatedEvent(@NonNull Conference conference, @NonNull User user, @NonNull MediaStream mediaStream) {
        super.onStreamUpdatedEvent(conference, user, mediaStream);
        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.onStreamUpdatedEvent(conference, user, mediaStream);

        updateUi();
    }

    @Override
    public void onStreamRemovedEvent(@NonNull Conference conference, @NonNull User user, @NonNull MediaStream mediaStream) {
        super.onStreamRemovedEvent(conference, user, mediaStream);

        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.onStreamRemovedEvent(conference, user, mediaStream);

        updateUi();
    }

    @Override
    public void onParticipantUnselected(User user) {
        speakerView.unlockScreen();
        showSpeakerView();

        updateSpeakerViewVisibility();
        updateUi();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VideoPresentationStarted event) {
        if (null != videoStream) {
            videoStream.onEvent(event);
            videoStream.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VideoPresentationPlay event) {
        if (null != videoStream) videoStream.onEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VideoPresentationPaused event) {
        if (null != videoStream) videoStream.onEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VideoPresentationStopped event) {
        if (null != videoStream) {
            videoStream.onEvent(event);
            videoStream.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VideoPresentationSeek event) {
        if (null != videoStream) videoStream.onEvent(event);
    }

    private void checkStateValue() {
        ConferenceService service = VoxeetSdk.conference();

        mState = ConferenceState.DEFAULT;
        boolean isInConference = service.isInConference();
        if (isInConference && null != service.getConferenceId()) {
            ConferenceInformation information = service.getCurrentConferenceInformation();
            if (information != null) {
                mState = information.getConferenceState();
            } else {
                mState = ConferenceState.LEFT;
            }
        } else if (isInConference) {
            mState = ConferenceState.CREATING;
        }
    }

    private void updateTextState(@StringRes int string) {
        float size;
        if (isExpanded) {
            size = getResources().getDimension(R.dimen.voxeet_conference_state_expanded);
        } else {
            size = getResources().getDimension(R.dimen.voxeet_conference_state_minimized);
        }
        conferenceState.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        conferenceState.setText(string);
    }

    private void refreshUIVisibility() {
        checkStateValue();

        String conferenceId = null;
        ConferenceService service = VoxeetSdk.conference();
        if (service.isInConference() && null != service.getConferenceId()) {
            ConferenceInformation information = service.getCurrentConferenceInformation();
            if (information != null) {
                conferenceId = information.getConference().getId();
            }
        }

        ConferenceState state = mState;
        if (null == conferenceId) {
            conferenceId = "";
            state = ConferenceState.LEFT;
        }
        ConferenceInformation conferenceInformation = service.getCurrentConferenceInformation();

        boolean enableInConfiguration = VoxeetToolkit.getInstance().getConferenceToolkit().Configuration.ActionBar.displayScreenShare;
        conferenceActionBarView.setDisplayScreenShare(enableInConfiguration && VoxeetToolkit.getInstance().getConferenceToolkit().isScreenShareEnabled());

        switch (state) {
            case CREATING:
                onConferenceCreating();
                break;
            case CREATED:
            case JOINING:
                if (null != conferenceInformation) {
                    onConferenceJoining(conferenceInformation.getConference());
                }
                break;
            case JOINED:
                if (null != conferenceInformation) {
                    onConferenceJoined(conferenceInformation.getConference());
                }
                break;
            case FIRST_PARTICIPANT:
                onConferenceFromNoOneToOneUser();
                break;
            case NO_MORE_PARTICIPANT:
                onConferenceNoMoreUser();
                break;
            case LEAVING:
                onConferenceLeaving();
                break;
            case LEFT:
                onConferenceLeft();
                break;

            default:
                //snif
        }
    }

    private void updateSpeakerViewVisibility() {
        if (null != speakerView) {
            String selectedUser = speakerView.getSelectedUserId();
            if (null != selectedUser) {
                User user = VoxeetSdk.conference().findUserById(selectedUser);
                if (null == user) {
                    speakerView.unlockScreen();
                }
            }
        }

        if (null != selectedView) {
            String selectedUser = selectedView.getPeerId();
            if (null != selectedUser) {
                User user = VoxeetSdk.conference().findUserById(selectedUser);
                if (null == user) {
                    selectedView.unAttach();
                }
            }
        }

        if (null != selectedView && selectedView.isAttached()) {
            hideSpeakerView();
        } else if (VoxeetSdk.conference().getConferenceUsers().size() > 0) {
            if (null != selectedView) selectedView.setVisibility(View.GONE);
            showSpeakerView();
        } else {
            hideSpeakerView();
            if (null != selectedView) selectedView.setVisibility(View.GONE);
        }
    }

    public void hideSpeakerView() {
        speakerView.setVisibility(View.GONE);
        speakerView.onPause();
    }

    public void showSpeakerView() {
        if (VoxeetSdk.conference().hasParticipants()) {
            speakerView.setVisibility(View.VISIBLE);
            speakerView.onResume();
        } else {
            onConferenceNoMoreUser();
        }
    }

    private void updateConferenceBarViewVisibility() {
        ConferenceInformation information = VoxeetSdk.conference().getCurrentConferenceInformation();

        boolean hide = null == information || ConferenceUserType.LISTENER.equals(information.getConferenceUserType());

        conferenceActionBarView.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CameraSwitchSuccessEvent event) {
        mConferenceViewRendererControl.updateMirror(event.isFront);
    }

    @Nullable
    private String getCurrentActiveSpeaker() {
        //get the selected user OR the "refreshed"/"cached" active speaker
        String activeSpeaker = speakerView.getSelectedUserId();
        if (null == activeSpeaker && null != voxeetActiveSpeakerTimer)
            activeSpeaker = voxeetActiveSpeakerTimer.getCurrentActiveSpeaker();
        return activeSpeaker;
    }

    @Override
    public void onActiveSpeakerUpdated(@Nullable String activeSpeakerUserId) {
        updateUi();
    }
}