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
import com.voxeet.sdk.core.services.conferences.information.ConferenceInformation;
import com.voxeet.sdk.core.services.conferences.information.ConferenceState;
import com.voxeet.sdk.core.services.conferences.information.ConferenceUserType;
import com.voxeet.sdk.events.success.CameraSwitchSuccessEvent;
import com.voxeet.sdk.exceptions.ExceptionManager;
import com.voxeet.sdk.models.abs.ConferenceUser;
import com.voxeet.sdk.views.VideoView;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.controllers.VoxeetToolkit;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetExpandableView;
import com.voxeet.toolkit.utils.ConferenceViewRendererControl;
import com.voxeet.toolkit.utils.IParticipantViewListener;
import com.voxeet.toolkit.utils.ToolkitUtils;
import com.voxeet.toolkit.views.NotchAvoidView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.RendererCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VoxeetConferenceView extends AbstractVoxeetExpandableView implements IParticipantViewListener {
    private final String TAG = VoxeetConferenceView.class.getSimpleName();

    private VoxeetParticipantView participantView;

    private VoxeetConferenceBarView conferenceBarView;

    private VoxeetCurrentSpeakerView speakerView;

    private ViewGroup layoutTimer;

    @Nullable
    private VideoView selectedView;

    private VideoView selfView;
    private ViewGroup layoutParticipant;

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

        updateUi();

        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if(EventBus.getDefault().isRegistered(this)) {
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

        refreshUIVisibility();

        //UPDATE participant adapter consequently
        if (null != mPreviouslyAttachedPeerId) {
            if (mPreviouslyScreenShare) {
                onScreenShareMediaStreamUpdated(mPreviouslyAttachedPeerId,
                        VoxeetSdk.conference().getMapOfScreenShareStreams());
            } else {
                onMediaStreamUpdated(mPreviouslyAttachedPeerId,
                        VoxeetSdk.conference().getMapOfStreams());
            }
        }

        HashMap<String, MediaStream> streams = VoxeetSdk.conference().getMapOfStreams();
        HashMap<String, MediaStream> screenShareStreams = VoxeetSdk.conference().getMapOfScreenShareStreams();

        if (streams.containsKey(VoxeetPreferences.id())) {
            onMediaStreamUpdated(VoxeetPreferences.id(), streams);
        }

        if (screenShareStreams.containsKey(VoxeetPreferences.id())) {
            onScreenShareMediaStreamUpdated(VoxeetPreferences.id(), screenShareStreams);
        }

        updateConferenceBarViewVisibility();
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
        Log.d(TAG, "onConferenceCreating: " + View.VISIBLE + " " + conferenceBarView.getVisibility());
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
        Log.d(TAG, "onConferenceCreation: " + View.VISIBLE + " " + conferenceBarView.getVisibility());
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
        Log.d(TAG, "onConferenceJoining: " + View.VISIBLE + " " + conferenceBarView.getVisibility());
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
        } else {
            if (null != selectedView) selectedView.setVisibility(View.GONE);
            speakerView.setVisibility(View.GONE);
            participantView.setVisibility(View.GONE);
            voxeetTimer.setVisibility(View.GONE);
            notchView.setVisibility(View.GONE);
        }


        MediaStream stream = VoxeetSdk.conference().getMapOfStreams().get(VoxeetPreferences.id());
        if (!ToolkitUtils.hasParticipants() && null != stream && stream.videoTracks().size() > 0) {
            selectedView.setVisibility(View.VISIBLE);
            mConferenceViewRendererControl.attachStreamToSelf(stream);
        }
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

        //just in case
        updateSpeakerViewVisibility();
        Log.d(TAG, "onConferenceFromNoOneToOneUser: " + View.VISIBLE + " " + conferenceBarView.getVisibility());
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
        Log.d(TAG, "onConferenceNoMoreUser: " + View.VISIBLE + " " + conferenceBarView.getVisibility());
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
        Log.d(TAG, "onConferenceLeaving: " + View.VISIBLE + " " + conferenceBarView.getVisibility());
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

        refreshUIVisibility();

        boolean show = false;
        MediaStream mediaStream = mediaStreams.get(userId);
        if (null != mediaStream) {
            if (userId.equalsIgnoreCase(VoxeetPreferences.id())) {
                if (mediaStream.videoTracks().size() > 0) {
                    mConferenceViewRendererControl.attachStreamToSelf(mediaStream);
                    show = true;
                } else {
                    mConferenceViewRendererControl.detachStreamFromSelf();
                    show = true; //prevent modification from the center view
                }
            } else if (null != selectedView) {
                if (mediaStream.videoTracks().size() > 0) {
                    mConferenceViewRendererControl.attachStreamToSelected(userId, mediaStream);
                    show = true;
                } else if (!selectedView.isAttached() || !selectedView.isScreenShare()) {
                    //if we are not showing any stream or ...
                    //is not showing a screenshare, we do hide it...
                    mConferenceViewRendererControl.detachStreamFromSelected();
                }
            }
        }

        if (!show) {
            String selectedUserId = speakerView.getSelectedUserId();
            if (null != selectedUserId && selectedUserId.equals(userId)) {
                show = tryLoadScreenshare(userId);
            }

            if (!show && VoxeetSdk.conference().hasParticipants())
                showSpeakerView();
            //well.. does not modify the view if no users to show
        }
        checkForLocalUserStreamVideo();

        updateSpeakerViewVisibility();

        participantView.notifyDatasetChanged();
        Log.d(TAG, "onMediaStreamUpdated: " + userId + " " + mediaStream);
    }

    @Override
    public void onScreenShareMediaStreamUpdated(@NonNull String userId, @NonNull Map<String, MediaStream> screen_share_media_streams) {
        super.onScreenShareMediaStreamUpdated(userId, screen_share_media_streams);

        refreshUIVisibility();

        boolean show = false;

        MediaStream mediaStream = screen_share_media_streams.get(userId);
        Log.d(TAG, "onScreenShareMediaStreamUpdated: " + mediaStream + " " + userId);

        if (null != mediaStream) {
            Log.d(TAG, "onScreenShareMediaStreamUpdated: tracks ? " + (mediaStream.videoTracks().size() > 0));
            if (!userId.equalsIgnoreCase(VoxeetPreferences.id())) {
                if (mediaStream.videoTracks().size() > 0) {
                    mConferenceViewRendererControl.attachStreamToSelected(userId, mediaStream);
                    show = true;
                }
            }
        }

        if (!show) {
            String selectedUserId = speakerView.getSelectedUserId();
            if (null != selectedUserId && selectedUserId.equals(userId)) {
                show = tryLoadCamera(userId);
            }
        }

        Log.d(TAG, "onScreenShareMediaStreamUpdated: show := " + show);

        HashMap<String, MediaStream> streams = VoxeetSdk.conference().getMapOfStreams();
        if (!show) {
            if (streams.containsKey(userId) && null != streams.get(userId)) {
                onMediaStreamUpdated(userId, streams);
            } else {
                mConferenceViewRendererControl.detachStreamFromSelected();
            }
        }
        checkForLocalUserStreamVideo();

        updateSpeakerViewVisibility();
        participantView.notifyDatasetChanged();
    }

    private boolean tryLoadScreenshare(String userId) {
        HashMap<String, MediaStream> streams = VoxeetSdk.conference().getMapOfScreenShareStreams();

        return tryLoadStream(streams, userId);
    }

    private boolean tryLoadCamera(String userId) {
        HashMap<String, MediaStream> streams = VoxeetSdk.conference().getMapOfStreams();

        return tryLoadStream(streams, userId);
    }

    private boolean tryLoadStream(HashMap<String, MediaStream> streams, String userId) {
        Log.d(TAG, "tryLoadStream: loading " + userId);
        if (streams.containsKey(userId) && null != streams.get(userId)) {
            MediaStream mediaStream = streams.get(userId);
            Log.d(TAG, "tryLoadStream: userId:=" + userId + " prefs:=" + VoxeetPreferences.id());
            if (userId.equalsIgnoreCase(VoxeetPreferences.id())) {
                if (ToolkitUtils.hasVideo(mediaStream)) {
                    mConferenceViewRendererControl.attachStreamToSelf(mediaStream);
                    return true;
                } else {
                    mConferenceViewRendererControl.detachStreamFromSelf();
                }
            } else if (null != selectedView/* && (null == selectedView.getPeerId() || userId.equalsIgnoreCase(selectedView.getPeerId()))*/) {
                //above comments was preventing switch when left
                Log.d(TAG, "tryLoadStream: screenshare ? " + selectedView.isScreenShare());
                if (ToolkitUtils.hasVideo(mediaStream)) {
                    Log.d(TAG, "tryLoadStream: this user has a video stream");
                    mConferenceViewRendererControl.attachStreamToSelected(userId, mediaStream);

                    updateSpeakerViewVisibility();
                    return true;
                } else if (!selectedView.isAttached() || !selectedView.isScreenShare()) {
                    Log.d(TAG, "tryLoadStream: this user does not have any video stream");
                    //if we are already showing a stream which is a screenshare, we do not hide it...
                    mConferenceViewRendererControl.detachStreamFromSelected();
                }
            }
        } else {
            Log.d(TAG, "tryLoadStream: ");
            showSpeakerView();

            if (selectedView != null) {
                selectedView.setVisibility(View.GONE);
                selectedView.unAttach();
            }
        }
        checkForLocalUserStreamVideo();

        updateSpeakerViewVisibility();
        return false;
    }


    @Override
    public void onConferenceUserLeft(@NonNull ConferenceUser conference_user) {
        super.onConferenceUserLeft(conference_user);
        Log.d(TAG, "onConferenceUserLeft: user " + conference_user.getUserId() + " left");

        HashMap<String, MediaStream> mediaStreamMap = VoxeetSdk.conference().getMapOfStreams();
        HashMap<String, MediaStream> mediaScreenStreamMap = VoxeetSdk.conference().getMapOfScreenShareStreams();

        String userId = conference_user.getUserId();

        if (null != userId && null != selectedView && userId.equals(selectedView.getPeerId())) {
            if (!checkForReplacingStream(mediaScreenStreamMap, userId)) {
                boolean fallback_success = checkForReplacingStream(mediaStreamMap, userId);
                Log.d(TAG, "onConferenceUserLeft: new stream found ? " + fallback_success + " " + userId);

                if (!fallback_success) {

                    mConferenceViewRendererControl.detachStreamFromSelected();

                    Log.d(TAG, "onConferenceUserLeft: hiding....");
                }
            } else {
                Log.d(TAG, "onConferenceUserLeft: new screenshare found");
            }
        } else {
            Log.d(TAG, "onConferenceUserLeft: no need to remove...");
        }

        checkForLocalUserStreamVideo();
        updateSpeakerViewVisibility();
        participantView.notifyDatasetChanged();

        refreshUIVisibility();
    }

    private void checkForLocalUserStreamVideo() {
        MediaStream stream = VoxeetSdk.conference().getMapOfStreams().get(VoxeetPreferences.id());
        if (null != stream && stream.videoTracks().size() > 0) {
            mConferenceViewRendererControl.attachStreamToSelf(stream);
        }
    }

    private boolean checkForReplacingStream(HashMap<String, MediaStream> mediaMap, String userId) {
        Set<String> set = mediaMap.keySet();

        for (String key : set) {
            if (null != key && !key.equals(userId)) {
                MediaStream stream = mediaMap.get(key);
                if (null != stream && stream.videoTracks().size() > 0) {
                    Log.d(TAG, "checkForReplacingStream: replacing " + key + " " + userId);
                    tryLoadStream(mediaMap, key);
                    return true;
                }
            }
        }
        //no new stream found
        return false;
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

        conferenceBarView.onToggleSize(true);

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

        conferenceBarView.onToggleSize(false);

        refreshUIVisibility();

        MediaStream stream = VoxeetSdk.conference().getMapOfStreams().get(VoxeetPreferences.id());
        if (!ToolkitUtils.hasParticipants() && null != stream && stream.videoTracks().size() > 0) {
            mConferenceViewRendererControl.attachStreamToSelf(stream);
        }
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_conference_view;
    }

    @Override
    protected void bindView(View view) {
        try {
            conferenceState = view.findViewById(R.id.conference_state);
            layoutParticipant = view.findViewById(R.id.layout_participant);

            speakerView = view.findViewById(R.id.current_speaker_view);

            selectedView = view.findViewById(R.id.selected_video_view);
            selectedView.setAutoUnAttach(true);

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

            conferenceBarView = view.findViewById(R.id.conference_bar_view);

            participantView = view.findViewById(R.id.participant_view);
            participantView.setParticipantListener(this);

            voxeetTimer = view.findViewById(R.id.voxeet_timer);

            notchView = view.findViewById(R.id.notch);

            //addListeners for voxeet dispatch events
            addListener(speakerView);
            addListener(conferenceBarView);
            addListener(participantView);
            addListener(voxeetTimer);

            Log.d(TAG, "bindView: ");

            updateUi();
        } catch (Exception e) {
            ExceptionManager.sendException(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onParticipantSelected(ConferenceUser user, MediaStream mediaStream) {
        if (null != user && null != VoxeetSdk.conference().getUser(user.getUserId())) {
            speakerView.lockScreen(user.getUserId());

            Log.d(TAG, "onParticipantSelected: onParticipantSelected");
            if (mediaStream != null && (mediaStream.videoTracks().size() > 0 || mediaStream.isScreenShare())) {
                mConferenceViewRendererControl.attachStreamToSelected(user.getUserId(), mediaStream);
            } else {
                mConferenceViewRendererControl.detachStreamFromSelected();
            }
        }

        updateSpeakerViewVisibility();
    }

    @Override
    public void onParticipantUnselected(ConferenceUser user) {
        if (null != selectedView) {
            selectedView.setVisibility(View.GONE);
            selectedView.unAttach();
        }

        speakerView.unlockScreen();
        showSpeakerView();

        updateSpeakerViewVisibility();
    }

    private void checkStateValue() {
        ConferenceService service = VoxeetSdk.conference();

        mState = ConferenceState.DEFAULT;
        boolean isInConference = service.isInConference();
        if (isInConference && null != service.getConferenceId()) {
            ConferenceInformation information = service.getCurrentConferenceInformation();
            if (information != null) {
                mState = information.getState();
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
                conferenceId = information.getConference().getConferenceId();
            }
        }

        ConferenceState state = mState;
        if (null == conferenceId) {
            conferenceId = "";
            state = ConferenceState.LEFT;
        }

        conferenceBarView.setDisplayScreenshare(VoxeetToolkit.getInstance().getConferenceToolkit().isScreenShareEnabled());

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
                ConferenceUser user = VoxeetSdk.conference().getUser(selectedUser);
                if (null == user) {
                    speakerView.unlockScreen();
                }
            }
        }

        if (null != selectedView) {
            String selectedUser = selectedView.getPeerId();
            if (null != selectedUser) {
                ConferenceUser user = VoxeetSdk.conference().getUser(selectedUser);
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

        conferenceBarView.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CameraSwitchSuccessEvent event) {
        mConferenceViewRendererControl.updateMirror(event.isFront());
    }
}