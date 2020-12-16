package com.voxeet.uxkit.implementation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.promise.solve.ThenVoid;
import com.voxeet.sdk.events.sdk.CameraSwitchSuccessEvent;
import com.voxeet.sdk.exceptions.ExceptionManager;
import com.voxeet.sdk.json.VideoPresentationPaused;
import com.voxeet.sdk.json.VideoPresentationPlay;
import com.voxeet.sdk.json.VideoPresentationSeek;
import com.voxeet.sdk.json.VideoPresentationStarted;
import com.voxeet.sdk.json.VideoPresentationStopped;
import com.voxeet.sdk.media.audio.SoundManager;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.sdk.models.v2.ParticipantType;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.services.conference.information.ConferenceParticipantType;
import com.voxeet.sdk.services.conference.information.ConferenceStatus;
import com.voxeet.sdk.utils.Filter;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.sdk.views.VideoView;
import com.voxeet.uxkit.R;
import com.voxeet.uxkit.configuration.ActionBar;
import com.voxeet.uxkit.configuration.Configuration;
import com.voxeet.uxkit.controllers.VoxeetToolkit;
import com.voxeet.uxkit.implementation.devices.VoxeetMediaRoutePickerView;
import com.voxeet.uxkit.implementation.overlays.abs.AbstractVoxeetExpandableView;
import com.voxeet.uxkit.utils.ConferenceViewRendererControl;
import com.voxeet.uxkit.utils.IParticipantViewListener;
import com.voxeet.uxkit.utils.ToolkitUtils;
import com.voxeet.uxkit.utils.VoxeetSpeakersTimerInstance;
import com.voxeet.uxkit.views.NotchAvoidView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.RendererCommon;

import java.util.ArrayList;
import java.util.List;

public class VoxeetConferenceView extends AbstractVoxeetExpandableView implements IParticipantViewListener, VoxeetSpeakersTimerInstance.ActiveSpeakerListener {
    private final String TAG = VoxeetConferenceView.class.getSimpleName();

    @Nullable
    private VoxeetVideoStreamView videoStream;

    private VoxeetParticipantsView participantView;

    private VoxeetActionBarView conferenceBarView;

    private VoxeetSpeakerView currentSpeakerView;

    private ViewGroup layoutTimer;

    @Nullable
    private VideoView videoView;

    private VideoView selfVideoView;
    private ViewGroup layoutParticipant;

    private VoxeetSpeakersTimerInstance voxeetActiveSpeakerTimer;
    private VoxeetTimer voxeetTimer;

    private NotchAvoidView notchView;

    @Nullable
    private String mPreviouslyAttachedPeerId;
    private boolean mPreviouslyScreenShare;
    private TextView conferenceState;
    private ConferenceStatus mState = ConferenceStatus.DEFAULT;
    private boolean isExpanded = false;
    private ScaleGestureDetector mScaleOnPinchDetector;

    private ConferenceViewRendererControl mConferenceViewRendererControl;

    private VoxeetMediaRoutePickerView mediaRoutePicker;
    private SoundManager.Call<List<MediaDevice>> onDevices = this::refreshConnectedDevice;

    private boolean resumed = false;

    @Nullable
    private MediaDevice connectedDevice;

    @Nullable
    private Participant participantSelected;

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param context the context
     */
    public VoxeetConferenceView(Context context) {
        super(context);

        internalInit();
    }

    /**
     * Check if audio mode or video mode is activated. For now, always video mode enabled
     *
     * @return
     */
    private boolean isVideoActivated() {
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void internalInit() {
        if (null == voxeetActiveSpeakerTimer)
            voxeetActiveSpeakerTimer = VoxeetSpeakersTimerInstance.instance;

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

                if (null != videoView) {
                    if (up > down) {
                        if (!RendererCommon.ScalingType.SCALE_ASPECT_FILL.equals(videoView.getScalingType())) {
                            videoView.setVideoFill();
                        }
                    } else {
                        if (!RendererCommon.ScalingType.SCALE_ASPECT_FIT.equals(videoView.getScalingType())) {
                            videoView.setVideoFit();
                        }
                    }
                }

                super.onScaleEnd(detector);
            }
        });

        this.setOnTouchListener((v, event) -> {
            if (isExpanded && null != mScaleOnPinchDetector) {
                return mScaleOnPinchDetector.onTouchEvent(event);
            }
            return false;
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
            voxeetActiveSpeakerTimer = VoxeetSpeakersTimerInstance.instance;

        voxeetActiveSpeakerTimer.setActiveSpeakerListener(this);
        voxeetActiveSpeakerTimer.start();

        VoxeetSDK.audio().registerUpdateDevices(onDevices);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // update the UI buttons for listener mode
        ConferenceInformation information = Opt.of(VoxeetSDK.conference().getConference())
                .then(Conference::getId)
                .then(id -> VoxeetSDK.conference().getConferenceInformation(id))
                .orNull();
        boolean isListener = Opt.of(information).then(ConferenceInformation::isListener).or(false);

        conferenceBarView.setDisplayMute(!isListener);
        conferenceBarView.setDisplayCamera(!isListener);


        resumed = true;
        voxeetActiveSpeakerTimer.start();
        connectedDevice = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        VoxeetSDK.audio().unregisterUpdateDevices(onDevices);
        resumed = false;
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

        refreshUI();
    }

    private void refreshUI() {
        if (null == getContext()) {
            return;
        }

        currentSpeakerView.onConferenceUsersListUpdate(getParticipants());

        refreshConnectedDevice();
        refreshVideoActivated();
        //streams get refreshed in the above function
        //refreshMediaStreams();

        conferenceBarView.onResume();

        if (!isExpanded) {
            if (null != participantView) participantView.setVisibility(View.GONE);
            if (selfVideoView.isAttached())
                selfVideoView.setVisibility(View.GONE);
        } else {
            if (null != participantView) participantView.setVisibility(View.VISIBLE);
            if (selfVideoView.isAttached())
                selfVideoView.setVisibility(View.VISIBLE);
        }

        selfVideoView.requestLayout();

        ConferenceInformation information = VoxeetSDK.conference().getCurrentConference();
        if (null != information) {
            switch (information.getConferenceState()) {
                case DEFAULT:
                case CREATING:
                    onConferenceCreating();
                    break;
                case CREATED:
                    onConferenceCreation(information.getConference());
                    break;
                case JOINING:
                    onConferenceJoining(information.getConference());
                    break;
                case JOINED:
                    onConferenceJoined(information.getConference());
                    break;
                case FIRST_PARTICIPANT:
                    onConferenceFromNoOneToOneUser();
                    break;
                case NO_MORE_PARTICIPANT:
                    onConferenceNoMoreUser();
                    break;
                case LEAVING:
                case LEFT:
                    onConferenceLeaving();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (null != selfVideoView) {
            selfVideoView.unAttach();
        }

        if (null != videoView) {
            mPreviouslyAttachedPeerId = videoView.getPeerId();
            mPreviouslyScreenShare = videoView.isScreenShare();
            videoView.unAttach();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != selfVideoView) {
            selfVideoView.unAttach();
        }

        if (null != videoView) {
            //reset the "saved" information"
            mPreviouslyAttachedPeerId = null;
            mPreviouslyScreenShare = false;

            videoView.unAttach();
        }
    }

    @Override
    public void onConferenceCreating() {
        super.onConferenceCreating();

        //expanded and minimized
        conferenceState.setVisibility(View.VISIBLE);
        currentSpeakerView.setVisibility(View.GONE);
        selfVideoView.setVisibility(View.GONE);
        participantView.setVisibility(View.VISIBLE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        conferenceBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceBarView.onConferenceCreating();
        Log.d(TAG, "onConferenceCreating: " + View.VISIBLE + " " + conferenceBarView.getVisibility());

        Conference conference = VoxeetSDK.conference().getConference();
        if (null != conference) participantView.update(conference);
    }

    @Override
    public void onConferenceCreation(@NonNull Conference conference) {
        super.onConferenceCreation(conference);

        //expanded and minimized
        updateTextState(R.string.voxeet_call);
        conferenceState.setVisibility(View.VISIBLE);
        currentSpeakerView.setVisibility(View.GONE);
        selfVideoView.setVisibility(View.GONE);
        participantView.setVisibility(View.VISIBLE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        conferenceBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceBarView.onConferenceCreation(conference);
        Log.d(TAG, "onConferenceCreation: " + View.VISIBLE + " " + conferenceBarView.getVisibility());

        if (null != conference) participantView.update(conference);
    }

    @Override
    public void onConferenceJoining(@NonNull Conference conference) {
        super.onConferenceJoining(conference);

        //expanded and minimized
        updateTextState(R.string.voxeet_call);
        conferenceState.setVisibility(View.VISIBLE);
        currentSpeakerView.setVisibility(View.GONE);
        selfVideoView.setVisibility(View.GONE);
        participantView.setVisibility(View.VISIBLE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        Log.d(TAG, "onConferenceJoining: " + View.VISIBLE + " " + conferenceBarView.getVisibility());

        conferenceBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceBarView.onConferenceJoining(conference);

        if (null != conference) participantView.update(conference);
    }

    @Override
    public void onConferenceJoined(@NonNull Conference conference) {
        super.onConferenceJoined(conference);

        updateTextState(R.string.voxeet_call);
        conferenceState.setVisibility(View.VISIBLE);
        if (isExpanded) {
            if (null != videoView) videoView.setVisibility(View.GONE);
            currentSpeakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.VISIBLE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.VISIBLE);
        } else {
            if (null != videoView) videoView.setVisibility(View.GONE);
            currentSpeakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.VISIBLE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.GONE);
        }

        Participant user = VoxeetSDK.conference().findParticipantById(VoxeetSDK.session().getParticipantId());
        if (null != user) {
            MediaStream stream = user.streamsHandler().getFirst(MediaStreamType.Camera);
            if (!ToolkitUtils.hasParticipants() && null != stream && stream.videoTracks().size() > 0) {
                videoView.setVisibility(View.VISIBLE);
                mConferenceViewRendererControl.attachStreamToSelf(stream);
                if (!isExpanded) selfVideoView.setVisibility(View.GONE);
            }
        }

        conferenceBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceBarView.onConferenceJoined(conference);

        if (null != conference) participantView.update(conference);
    }

    @Override
    public void onConferenceFromNoOneToOneUser() {
        super.onConferenceFromNoOneToOneUser();

        conferenceState.setVisibility(View.GONE);
        if (isExpanded) {
            conferenceState.setVisibility(View.GONE);
            if (null != videoView && videoView.isAttached())
                videoView.setVisibility(View.VISIBLE);
            else showSpeakerView();
            participantView.setVisibility(View.VISIBLE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.VISIBLE);
        } else {
            conferenceState.setVisibility(View.GONE);
            participantView.setVisibility(View.VISIBLE);
            voxeetTimer.setVisibility(View.VISIBLE);
            notchView.setVisibility(View.GONE);

            if (null != videoView && videoView.isAttached())
                videoView.setVisibility(View.VISIBLE);
            else showSpeakerView();
        }


        conferenceBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceBarView.onConferenceFromNoOneToOneUser();

        //just in case
        updateSpeakerViewVisibility();
        Log.d(TAG, "onConferenceFromNoOneToOneUser: " + View.VISIBLE + " " + conferenceBarView.getVisibility());

        Conference conference = VoxeetSDK.conference().getConference();
        if (null != conference) participantView.update(conference);
    }

    @Override
    public void onConferenceNoMoreUser() {
        super.onConferenceNoMoreUser();

        String ownUserId = VoxeetSDK.session().getParticipantId();
        if (null == ownUserId) ownUserId = "";

        updateTextState(R.string.voxeet_waiting_for_users);
        conferenceState.setVisibility(View.VISIBLE);
        if (isExpanded) {
            if (null != videoView && (!videoView.isAttached() || !ownUserId.equals(videoView.getPeerId()))) {
                videoView.setVisibility(View.GONE);
            }
            currentSpeakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.VISIBLE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.VISIBLE);
        } else {
            if (null != videoView && (!videoView.isAttached() || !ownUserId.equals(videoView.getPeerId()))) {
                videoView.setVisibility(View.GONE);
            }
            currentSpeakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.VISIBLE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.GONE);
        }

        Conference conference = VoxeetSDK.conference().getConference();
        if (null != conference) participantView.update(conference);

        conferenceBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceBarView.onConferenceNoMoreUser();
        Log.d(TAG, "onConferenceNoMoreUser: " + View.VISIBLE + " " + conferenceBarView.getVisibility());
    }

    @Override
    public void onConferenceLeaving() {
        super.onConferenceLeaving();

        //don't call updateUi here

        //expanded and minimized
        updateTextState(R.string.voxeet_leaving);
        conferenceState.setVisibility(View.VISIBLE);
        showSpeakerView();
        selfVideoView.setVisibility(View.GONE);
        participantView.setVisibility(View.GONE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        Conference conference = VoxeetSDK.conference().getConference();
        if (null != conference) participantView.update(conference);

        conferenceBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceBarView.onConferenceLeaving();
        Log.d(TAG, "onConferenceLeaving: " + View.VISIBLE + " " + conferenceBarView.getVisibility());
    }

    @Override
    public void onConferenceDestroyed() {
        super.onConferenceDestroyed();

        if (null != selfVideoView) {
            selfVideoView.unAttach();
        }

        if (null != videoView) {
            videoView.unAttach();
        }
    }

    @Override
    public void onConferenceLeft() {
        super.onConferenceLeft();

        if (null != selfVideoView) {
            selfVideoView.unAttach();
        }

        if (null != videoView) {
            videoView.unAttach();
        }
    }

    private void refreshVideoActivated() {
        boolean isVideoActivated = isVideoActivated();

        if (null != participantView)
            participantView.setVideoActivable(isVideoActivated);

        refreshMediaStreams();
    }

    private void refreshMediaStreams() {
        String localUserId = VoxeetSDK.session().getParticipantId();
        if (null == localUserId) localUserId = "";
        ConferenceService service = VoxeetSDK.conference();
        List<Participant> users = service.getParticipants();

        String currentUserAttached = videoView.getPeerId();
        Participant localUser = service.findParticipantById(localUserId);
        Participant user = service.findParticipantById(currentUserAttached);
        MediaStream localUserMediaStream = Opt.of(localUser).then(Participant::streamsHandler).then(s -> s.getFirst(MediaStreamType.Camera)).orNull();

        if (hasParticipants() && localUserId.equalsIgnoreCase(currentUserAttached)) {
            currentUserAttached = null;
            videoView.unAttach();
            videoView.setMirror(false);
        }

        if (null != currentUserAttached) {
            MediaStream stream = Opt.of(user).then(Participant::streamsHandler).then(s -> s.getFirst(MediaStreamType.Camera)).orNull();
            MediaStream screenShareStream = Opt.of(user).then(Participant::streamsHandler).then(s -> s.getFirst(MediaStreamType.ScreenShare)).orNull();
            boolean unAttach = true;
            boolean hasScreenShare = null != screenShareStream && screenShareStream.videoTracks().size() > 0;
            boolean hasVideo = null != stream && stream.videoTracks().size() > 0;
            if (hasScreenShare) {
                unAttach = false;
            } else if (hasVideo) {
                unAttach = false;
            }

            if (unAttach) videoView.unAttach();
        }

        String currentActiveSpeaker = getCurrentActiveSpeaker();

        if (!isVideoActivated()) {
            Log.d(TAG, "refreshMediaStreams: audio mode only, no video");
            videoView.unAttach();
        } else if (null != currentActiveSpeaker) { //else, do we have an active speaker ?
            if (null != videoView.getPeerId() || videoView.isAttached()) {
                //should we unattach ?
                if (!currentActiveSpeaker.equals(videoView.getPeerId())) {
                    videoView.unAttach();
                }
            }

            loopUserForStreamInVideoViewIfUnattached(currentActiveSpeaker, users, MediaStreamType.ScreenShare);
            loopUserForStreamInVideoViewIfUnattached(currentActiveSpeaker, users, MediaStreamType.Camera);
        }

        if (null != localUserMediaStream) {
            if (localUserMediaStream.videoTracks().size() > 0) {
                selfVideoView.attach(localUserId, localUserMediaStream);

                selfVideoView.setMirror(VoxeetSDK.mediaDevice().getCameraContext().isDefaultFrontFacing());
                if (isExpanded) selfVideoView.setVisibility(View.VISIBLE);
                else selfVideoView.setVisibility(View.GONE);
            } else {
                selfVideoView.unAttach();
                selfVideoView.setVisibility(View.GONE);
            }
        }

        if (!hasParticipants()) {
            currentSpeakerView.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
        } else if (!videoView.isAttached()) {
            currentSpeakerView.onResume();
            currentSpeakerView.setVisibility(View.VISIBLE);
            videoView.unAttach();
            videoView.setVisibility(View.GONE);
        } else {
            //participants are here so we don't have our own video on top
            //force unmirrored view with participants
            videoView.setMirror(false);
            currentSpeakerView.onPause();
            currentSpeakerView.setVisibility(View.GONE);
        }

        conferenceBarView.invalidateOwnStreams();

        ConferenceInformation information = VoxeetSDK.conference().getCurrentConference();

        boolean enableInConfiguration = VoxeetToolkit.instance().getConferenceToolkit().Configuration.ActionBar.displayScreenShare;
        if (enableInConfiguration && null != information && !information.isListener()) {
            conferenceBarView.setDisplayScreenShare(true);
        } else {
            conferenceBarView.setDisplayScreenShare(false);
        }

    }

    private void loopUserForStreamInVideoViewIfUnattached(@Nullable String currentSelectedUserId,
                                                          List<Participant> users,
                                                          MediaStreamType mediaStreamType) {

        MediaStream foundToAttach = null;
        String userIdFoundToAttach = null;

        if (null != currentSelectedUserId) {
            Participant user = VoxeetSDK.conference().findParticipantById(currentSelectedUserId);
            if (null != user) {
                userIdFoundToAttach = user.getId();
                foundToAttach = user.streamsHandler().getFirst(mediaStreamType);
            }
        }

        if (null == foundToAttach) {
            for (Participant user : users) {
                String userId = user.getId();
                if (null != userId && !userId.equals(VoxeetSDK.session().getParticipantId()) && !userId.equals(currentSelectedUserId) && null == foundToAttach) {
                    userIdFoundToAttach = userId;
                    foundToAttach = user.streamsHandler().getFirst(mediaStreamType);
                    if (null != foundToAttach && foundToAttach.videoTracks().size() <= 0)
                        foundToAttach = null;
                }
            }
        }

        if ((!videoView.isAttached() || MediaStreamType.ScreenShare.equals(mediaStreamType)) && null != foundToAttach && foundToAttach.videoTracks().size() > 0) {
            videoView.setVisibility(View.VISIBLE);
            videoView.attach(userIdFoundToAttach, foundToAttach);
        }

        if (null == foundToAttach) {
            MediaStreamType stream = videoView.current();
            if (null != stream && stream.equals(mediaStreamType)) {
                videoView.setVisibility(View.GONE);
                videoView.unAttach();
            }
        }
    }

    private void refreshConnectedDevice() {
        VoxeetSDK.audio().enumerateDevices().then((ThenVoid<List<MediaDevice>>) this::refreshConnectedDevice)
                .error(Throwable::printStackTrace);
    }

    private void refreshConnectedDevice(List<MediaDevice> devices) {
        if (!resumed || null == devices) return;

        List<MediaDevice> connected = Filter.filter(Opt.of(devices).or(new ArrayList<>()), item -> item.platformConnectionState() == ConnectionState.CONNECTED && item.connectionState() == ConnectionState.CONNECTED);

        if (connected.size() > 0) {
            MediaDevice mediaDevice = connected.get(0);
            String lastId = Opt.of(connectedDevice).then(MediaDevice::id).or("_default_");
            if (null == connectedDevice || !lastId.equals(mediaDevice.id())) {
                connectedDevice = mediaDevice;
                //TODO add information about which device is now connected ?
            }
        }
    }

    private void checkForLocalUserStreamVideo() {
        Participant user = VoxeetSDK.conference().findParticipantById(VoxeetSDK.session().getParticipantId());
        if (null != user) {
            MediaStream stream = user.streamsHandler().getFirst(MediaStreamType.Camera);
            if (null != stream && stream.videoTracks().size() > 0) {
                mConferenceViewRendererControl.attachStreamToSelf(stream);
                if (!isExpanded) selfVideoView.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void init() {

    }

    @Override
    public void onPreExpandedView() {
        if (selfVideoView.isAttached()) {
            selfVideoView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onExpandedView() {
        isExpanded = true;
        layoutTimer.setVisibility(View.GONE);

        layoutParticipant.setVisibility(View.VISIBLE);
        participantView.notifyDatasetChanged();

        conferenceBarView.onToggleSize(true);

        refreshUIVisibility();
        if (videoView != null) {
            videoView.setCornerRadius(0);
        }

        mConferenceViewRendererControl.enableClick(true);

        checkForLocalUserStreamVideo();
    }

    @Override
    public void onPreMinizedView() {
        selfVideoView.setVisibility(View.GONE);
    }

    @Override
    public void onMinizedView() {

        mConferenceViewRendererControl.enableClick(false);

        isExpanded = false;
        layoutTimer.setVisibility(View.VISIBLE);

        participantView.notifyDatasetChanged();
        layoutParticipant.setVisibility(View.GONE);

        conferenceBarView.onToggleSize(false);

        refreshUIVisibility();
        refreshUI();
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

            currentSpeakerView = view.findViewById(R.id.current_speaker_view);

            videoView = view.findViewById(R.id.selected_video_view);

            selfVideoView = view.findViewById(R.id.self_video_view);

            mConferenceViewRendererControl = new ConferenceViewRendererControl(this, selfVideoView, videoView);

            selfVideoView.setOnClickListener(view1 -> mConferenceViewRendererControl.switchCamera());

            layoutTimer = view.findViewById(R.id.layout_timer);

            conferenceBarView = view.findViewById(R.id.conference_bar_view);

            participantView = view.findViewById(R.id.participant_view);
            participantView.setParticipantListener(this);

            voxeetTimer = view.findViewById(R.id.voxeet_timer);

            notchView = view.findViewById(R.id.notch);

            mediaRoutePicker = view.findViewById(R.id.media_route_picker);

            if (null != conferenceBarView)
                conferenceBarView.setMediaDeviceControl(mediaRoutePicker);

            Configuration configuration = VoxeetToolkit.getInstance().getConferenceToolkit().Configuration;
            ActionBar actionBarConfiguration = configuration.ActionBar;
            conferenceBarView.setDisplayCamera(actionBarConfiguration.displayCamera);
            conferenceBarView.setDisplayLeave(actionBarConfiguration.displayLeave);
            conferenceBarView.setDisplayMute(actionBarConfiguration.displayMute);
            conferenceBarView.setDisplayScreenShare(actionBarConfiguration.displayScreenShare);
            conferenceBarView.setDisplaySpeaker(actionBarConfiguration.displaySpeaker);

            //addListeners for voxeet dispatch events
            addListener(currentSpeakerView);
            addListener(conferenceBarView);
            addListener(participantView);
            addListener(voxeetTimer);

            refreshUI();
        } catch (Exception e) {
            ExceptionManager.sendException(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onParticipantSelected(@NonNull Participant participant) {
        if (null != participant) {
            participantSelected = participant;
            currentSpeakerView.lockScreen(participant);
        }

        refreshUI();
    }

    @Override
    public void onUserAddedEvent(@NonNull Conference conference, @NonNull Participant user) {
        super.onUserAddedEvent(conference, user);
        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.onUserAddedEvent(conference, user);

        refreshUI();
    }

    @Override
    public void onUserUpdatedEvent(@NonNull Conference conference, @NonNull Participant user) {
        super.onUserUpdatedEvent(conference, user);
        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.onUserUpdatedEvent(conference, user);

        refreshUI();
    }

    @Override
    public void onStreamAddedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamAddedEvent(conference, user, mediaStream);
        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.onStreamAddedEvent(conference, user, mediaStream);

        refreshUI();
    }

    @Override
    public void onStreamUpdatedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamUpdatedEvent(conference, user, mediaStream);
        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.onStreamUpdatedEvent(conference, user, mediaStream);

        refreshUI();
    }

    @Override
    public void onStreamRemovedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamRemovedEvent(conference, user, mediaStream);

        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.onStreamRemovedEvent(conference, user, mediaStream);

        refreshUI();
    }

    @Override
    public void onParticipantUnselected(@NonNull Participant user) {
        participantSelected = null;
        currentSpeakerView.unlockScreen();

        refreshUI();
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
        ConferenceService service = VoxeetSDK.conference();

        mState = ConferenceStatus.DEFAULT;
        boolean isInConference = service.isInConference();
        if (isInConference && null != service.getConferenceId()) {
            ConferenceInformation information = service.getCurrentConference();
            if (information != null) {
                mState = information.getConferenceState();
            } else {
                mState = ConferenceStatus.LEFT;
            }
        } else if (isInConference) {
            mState = ConferenceStatus.CREATING;
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
        ConferenceService service = VoxeetSDK.conference();
        if (service.isInConference() && null != service.getConferenceId()) {
            ConferenceInformation information = service.getCurrentConference();
            if (information != null) {
                conferenceId = information.getConference().getId();
            }
        }

        ConferenceStatus state = mState;
        if (null == conferenceId) {
            conferenceId = "";
            state = ConferenceStatus.LEFT;
        }
        ConferenceInformation conferenceInformation = service.getCurrentConference();

        boolean enableInConfiguration = VoxeetToolkit.getInstance().getConferenceToolkit().Configuration.ActionBar.displayScreenShare;
        conferenceBarView.setDisplayScreenShare(enableInConfiguration && VoxeetToolkit.getInstance().getConferenceToolkit().isScreenShareEnabled());

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
        if (null != currentSpeakerView) {
            String selectedUser = Opt.of(participantSelected).then(Participant::getId).orNull();
            if (null != selectedUser) {
                Participant user = VoxeetSDK.conference().findParticipantById(selectedUser);
                if (null == user) {
                    participantSelected = null;
                    currentSpeakerView.unlockScreen();
                }
            }
        }

        if (null != videoView) {
            String selectedUser = videoView.getPeerId();
            if (null != selectedUser) {
                Participant user = VoxeetSDK.conference().findParticipantById(selectedUser);
                if (null == user) {
                    videoView.unAttach();
                }
            }
        }

        if (null != videoView && videoView.isAttached()) {
            hideSpeakerView();
        } else if (getParticipants().size() > 0) {
            if (null != videoView) videoView.setVisibility(View.GONE);
            showSpeakerView();
        } else {
            hideSpeakerView();
            if (null != videoView) videoView.setVisibility(View.GONE);
        }
    }

    public void hideSpeakerView() {
        currentSpeakerView.onPause();
        currentSpeakerView.setVisibility(View.GONE);
    }

    public void showSpeakerView() {
        if (hasParticipants()) {
            currentSpeakerView.setVisibility(View.VISIBLE);
            currentSpeakerView.onResume();
        } else {
            onConferenceNoMoreUser();
        }
    }

    private void updateConferenceBarViewVisibility() {
        ConferenceInformation information = VoxeetSDK.conference().getCurrentConference();

        boolean hide = null == information || ConferenceParticipantType.LISTENER.equals(information.getConferenceParticipantType());

        conferenceBarView.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CameraSwitchSuccessEvent event) {
        mConferenceViewRendererControl.updateMirror(event.isFront);
    }

    @Nullable
    private String getCurrentActiveSpeaker() {
        //get the selected user OR the "refreshed"/"cached" active speaker
        String activeSpeaker = Opt.of(participantSelected).then(Participant::getId).orNull();
        if (null != activeSpeaker && activeSpeaker.equals(VoxeetSDK.session().getParticipantId())) {
            activeSpeaker = null;
        }

        Participant participant = VoxeetSDK.conference().findParticipantById(Opt.of(activeSpeaker).or(""));
        if (null != participant && participant.isLocallyActive()) {
            return activeSpeaker;
        }

        return VoxeetSpeakersTimerInstance.instance.getCurrentActiveSpeakerOrDefault();
    }

    @Override
    public void onActiveSpeakerUpdated(@Nullable String activeSpeakerUserId) {
        if (null != participantSelected && participantSelected.isLocallyActive()) {
            activeSpeakerUserId = participantSelected.getId();
        }

        if (null == activeSpeakerUserId || !activeSpeakerUserId.equals(VoxeetSDK.session().getParticipantId())) {
            currentSpeakerView.onActiveSpeakerUpdated(activeSpeakerUserId);
        }

        refreshUI();
    }

    //TODO optimize by collecting the participants on the beginning of the loop only
    private List<Participant> getParticipants() {
        return Filter.filter(VoxeetSDK.conference().getParticipants(), participant -> {
            ParticipantType type = Opt.of(participant.participantType()).or(ParticipantType.NONE);

            if ("00000000-0000-0000-0000-000000000000".equals(participant.getId())) return false;
            if (!(type.equals(ParticipantType.DVC) || type.equals(ParticipantType.USER) || type.equals(ParticipantType.PSTN))) {
                return false;
            }

            if (Opt.of(participant.getId()).or("").equals(VoxeetSDK.session().getParticipantId())) {
                //prevent own user to be "active speaker"
                return false;
            }
            if (ConferenceParticipantStatus.ON_AIR == participant.getStatus()) return true;
            return ConferenceParticipantStatus.CONNECTING == participant.getStatus() && null != participant.streamsHandler().getFirst(MediaStreamType.Camera);
        });
    }

    //TODO optimize by collecting the participants on the beginning of the loop only
    private boolean hasParticipants() {
        return getParticipants().size() > 0;
    }
}