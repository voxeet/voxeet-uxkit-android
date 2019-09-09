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
import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.core.preferences.VoxeetPreferences;
import com.voxeet.sdk.core.services.ConferenceService;
import com.voxeet.sdk.core.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.core.services.conference.information.ConferenceState;
import com.voxeet.sdk.core.services.conference.information.ConferenceUserType;
import com.voxeet.sdk.events.sdk.CameraSwitchSuccessEvent;
import com.voxeet.sdk.exceptions.ExceptionManager;
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
import java.util.Map;

public class VoxeetConferenceView extends AbstractVoxeetExpandableView implements IParticipantViewListener, VoxeetActiveSpeakerTimer.ActiveSpeakerListener {
    private final String TAG = VoxeetConferenceView.class.getSimpleName();

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
    private VoxeetTimer voxeetTimerExpand;

    private NotchAvoidView notchView;

    @Nullable
    private String mPreviouslyAttachedPeerId;
    private boolean mPreviouslyScreenShare;
    private TextView conferenceState;
    private TextView conferenceName;
    private ConferenceState mState = ConferenceState.DEFAULT;
    private boolean isExpanded = false;
    private ScaleGestureDetector mScaleOnPinchDetector;

    private ConferenceViewRendererControl mConferenceViewRendererControl;
    private String currentActiveSpeaker;

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

        voxeetActiveSpeakerTimer.start();
        updateUi();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        voxeetActiveSpeakerTimer.stop();
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
        Log.d(TAG, "updateUi: ");

        //check for the conference state
        checkStateValue();

        updateConferenceBarViewVisibility();

        refreshMediaStreams();
        refreshUIVisibility();
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
        conferenceName.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        voxeetTimerExpand.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceCreating();
        Log.d(TAG, "onConferenceCreating: " + View.VISIBLE + " " + conferenceActionBarView.getVisibility());
    }

    @Override
    public void onConferenceCreation(@NonNull String conferenceId) {
        super.onConferenceCreation(conferenceId);

        //expanded and minimized
        updateTextState(R.string.voxeet_call);
        conferenceState.setVisibility(View.VISIBLE);
        speakerView.setVisibility(View.GONE);
        selfView.setVisibility(View.GONE);
        participantView.setVisibility(View.GONE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        conferenceName.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        voxeetTimerExpand.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceCreation(conferenceId);
        Log.d(TAG, "onConferenceCreation: " + View.VISIBLE + " " + conferenceActionBarView.getVisibility());
    }

    @Override
    public void onConferenceJoining(@NonNull String conference_id) {
        super.onConferenceJoining(conference_id);

        //expanded and minimized
        updateTextState(R.string.voxeet_call);
        conferenceState.setVisibility(View.VISIBLE);
        speakerView.setVisibility(View.GONE);
        selfView.setVisibility(View.GONE);
        participantView.setVisibility(View.GONE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        conferenceName.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        voxeetTimerExpand.setVisibility(View.GONE);
        Log.d(TAG, "onConferenceJoining: " + View.VISIBLE + " " + conferenceActionBarView.getVisibility());

        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceJoining(conference_id);
    }

    @Override
    public void onConferenceJoined(@NonNull String conference_id) {
        super.onConferenceJoined(conference_id);

        updateTextState(R.string.voxeet_call);
        conferenceState.setVisibility(View.VISIBLE);
        if (isExpanded) {
            if (null != selectedView) selectedView.setVisibility(View.GONE);
            speakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.GONE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.VISIBLE);
            conferenceName.setVisibility(View.VISIBLE);
            voxeetTimerExpand.setVisibility(View.VISIBLE);
        } else {
            if (null != selectedView) selectedView.setVisibility(View.GONE);
            speakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.GONE);
            voxeetTimer.setVisibility(View.VISIBLE);
            notchView.setVisibility(View.GONE);
            conferenceName.setVisibility(View.GONE);
            voxeetTimerExpand.setVisibility(View.GONE);
        }


        MediaStream stream = VoxeetSdk.conference().getMapOfStreams().get(VoxeetPreferences.id());
        if (!ToolkitUtils.hasParticipants() && null != stream && stream.videoTracks().size() > 0) {
            selectedView.setVisibility(View.VISIBLE);
            mConferenceViewRendererControl.attachStreamToSelf(stream);
            if (!isExpanded) selfView.setVisibility(View.GONE);
        }

        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceJoined(conference_id);
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
            conferenceName.setVisibility(View.VISIBLE);
            voxeetTimerExpand.setVisibility(View.VISIBLE);
        } else {
            conferenceState.setVisibility(View.GONE);
            participantView.setVisibility(View.GONE);
            voxeetTimer.setVisibility(View.VISIBLE);
            notchView.setVisibility(View.GONE);
            conferenceName.setVisibility(View.GONE);
            voxeetTimerExpand.setVisibility(View.GONE);

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
            conferenceName.setVisibility(View.VISIBLE);
            voxeetTimerExpand.setVisibility(View.VISIBLE);
        } else {
            if (null != selectedView && (!selectedView.isAttached() || !ownUserId.equals(selectedView.getPeerId()))) {
                selectedView.setVisibility(View.GONE);
            }
            speakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.GONE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.GONE);
            conferenceName.setVisibility(View.GONE);
            voxeetTimerExpand.setVisibility(View.GONE);
        }

        conferenceActionBarView.setVisibility(!isExpanded ? View.GONE : View.VISIBLE);
        conferenceActionBarView.onConferenceNoMoreUser();
        Log.d(TAG, "onConferenceNoMoreUser: " + View.VISIBLE + " " + conferenceActionBarView.getVisibility());
    }

    @Override
    public void onConferenceLeaving() {
        super.onConferenceLeaving();

        //expanded and minimized
        updateTextState(R.string.voxeet_leaving);
        conferenceState.setVisibility(View.VISIBLE);
        showSpeakerView();
        selfView.setVisibility(View.GONE);
        participantView.setVisibility(View.GONE);
        voxeetTimer.setVisibility(View.GONE);
        notchView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        conferenceName.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        voxeetTimerExpand.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

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

    @Override
    public void onMediaStreamUpdated(@NonNull String userId,
                                     @NonNull Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamUpdated(userId, mediaStreams);

        updateUi();
    }

    @Override
    public void onScreenShareMediaStreamUpdated(@NonNull String userId, @NonNull Map<String, MediaStream> screen_share_media_streams) {
        super.onScreenShareMediaStreamUpdated(userId, screen_share_media_streams);

        updateUi();
    }

    private void refreshMediaStreams() {
        ConferenceService service = VoxeetSdk.conference();
        Map<String, MediaStream> streams = service.getMapOfStreams();
        Map<String, MediaStream> screenShareStreams = service.getMapOfScreenShareStreams();
        List<User> users = service.getConferenceUsers();

        if (service.getConference() != null && service.getConference().getAlias() != null) {
            String aliasName = service.getConference().getAlias();
//            if (aliasName.contains(":")) {
                Log.d("if name: ", aliasName);
                String[] confName = aliasName.split(":");
                Log.d("conf name: ", confName[0]);
                String spConfName = confName[0].replace("*"," ");
                conferenceName.setText(spConfName);
             /* } else {
                Log.d("else name: ", aliasName);
                conferenceName.setText(service.getConference().getAlias());
            }*/
        }
        String currentUserAttached = selectedView.getPeerId();
        MediaStream currentUser = streams.get(VoxeetPreferences.id());


        if (ToolkitUtils.hasParticipants() && VoxeetPreferences.id().equalsIgnoreCase(currentUserAttached)) {
            selectedView.unAttach();
            selectedView.setMirror(false);
        }

        if (null != currentUserAttached) {
            MediaStream stream = streams.get(currentUserAttached);
            MediaStream screenShareStream = screenShareStreams.get(currentUserAttached);
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
        loopUserForStreamInVideoViewIfUnattached(currentActiveSpeaker, users, streams, screenShareStreams);


        Log.d("VideoView", "refreshMediaStreams: " + currentActiveSpeaker + " " + selectedView.getPeerId());

        if (null != currentUser) {
            if (currentUser.videoTracks().size() > 0) {
                selfView.attach(VoxeetPreferences.id(), currentUser);

                selfView.setMirror(VoxeetSdk.mediaDevice().getCameraInformationProvider().isDefaultFrontFacing());
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

        conferenceActionBarView.onMediaStreamUpdated(VoxeetPreferences.id(), streams);
        conferenceActionBarView.onScreenShareMediaStreamUpdated(VoxeetPreferences.id(), screenShareStreams);

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
                                                          Map<String, MediaStream> streams,
                                                          Map<String, MediaStream> screenshares) {

        MediaStream foundToAttach = null;
        String userIdFoundToAttach = null;

        if (null != currentSelectedUserId) {
            User user = VoxeetSdk.conference().getUser(currentSelectedUserId);
            if (null != user && !user.getId().equals(VoxeetPreferences.id())) {
                userIdFoundToAttach = user.getId();
                foundToAttach = streams.get(userIdFoundToAttach);
            }
        }

        if (null == foundToAttach) {
            for (User user : users) {
                String userId = user.getId();
                if (null != userId && !userId.equals(VoxeetPreferences.id()) && !userId.equals(currentSelectedUserId) && null == foundToAttach) {
                    userIdFoundToAttach = userId;
                    foundToAttach = screenshares.get(user.getId());
                    if(null != foundToAttach && foundToAttach.videoTracks().size() <= 0) foundToAttach = null;
                }
            }
        }

        if (null != foundToAttach) {
            Log.d(TAG, "loopUserForStreamInVideoViewIfUnattached: " + foundToAttach.peerId() + " " + foundToAttach.label());
        }

        if (null != foundToAttach && foundToAttach.videoTracks().size() > 0) {
            selectedView.setVisibility(View.VISIBLE);
            selectedView.attach(userIdFoundToAttach, foundToAttach);
        } else {
            selectedView.setVisibility(View.GONE);
            selectedView.unAttach();
        }
        Log.d(TAG, " " + selectedView);
    }

    @Override
    public void onConferenceUserLeft(@NonNull User conference_user) {
        super.onConferenceUserLeft(conference_user);

        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.notifyDatasetChanged();

        updateUi();
    }

    private void checkForLocalUserStreamVideo() {
        MediaStream stream = VoxeetSdk.conference().getMapOfStreams().get(VoxeetPreferences.id());
        if (null != stream && stream.videoTracks().size() > 0) {
            mConferenceViewRendererControl.attachStreamToSelf(stream);
            if (!isExpanded) selfView.setVisibility(View.GONE);
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
            conferenceState = view.findViewById(R.id.conference_state);
            conferenceName = view.findViewById(R.id.conference_name);
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
            voxeetTimerExpand = view.findViewById(R.id.voxeet_timer_expand);

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
            addListener(voxeetTimerExpand);

            Log.d(TAG, "bindView: ");

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
    public void onParticipantUnselected(User user) {
        speakerView.unlockScreen();
        showSpeakerView();

        updateSpeakerViewVisibility();
        updateUi();
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

        boolean enableInConfiguration = VoxeetToolkit.getInstance().getConferenceToolkit().Configuration.ActionBar.displayScreenShare;
        conferenceActionBarView.setDisplayScreenShare(enableInConfiguration && VoxeetToolkit.getInstance().getConferenceToolkit().isScreenShareEnabled());

        Log.d(TAG, "refreshUIVisibility: " + state);
        switch (state) {
            case CREATING:
                onConferenceCreating();
                break;
            case CREATED:
            case JOINING:
                onConferenceJoining(conferenceId);
                break;
            case JOINED:
                onConferenceJoined(conferenceId);
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
                User user = VoxeetSdk.conference().getUser(selectedUser);
                if (null == user) {
                    speakerView.unlockScreen();
                }
            }
        }

        if (null != selectedView) {
            String selectedUser = selectedView.getPeerId();
            if (null != selectedUser) {
                User user = VoxeetSdk.conference().getUser(selectedUser);
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
        if (null == activeSpeaker) activeSpeaker = currentActiveSpeaker;
        return activeSpeaker;
    }

    @Override
    public void onActiveSpeakerUpdated(@Nullable String activeSpeakerUserId) {
        currentActiveSpeaker = activeSpeakerUserId;

        updateUi();
    }
}