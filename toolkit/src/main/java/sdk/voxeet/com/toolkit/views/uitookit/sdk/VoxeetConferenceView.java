package sdk.voxeet.com.toolkit.views.uitookit.sdk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.voxeet.android.media.MediaStream;
import com.voxeet.toolkit.R;

import java.util.HashMap;
import java.util.Map;

import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;
import sdk.voxeet.com.toolkit.utils.IParticipantViewListener;
import sdk.voxeet.com.toolkit.views.uitookit.nologic.VideoView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetExpandableView;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.preferences.VoxeetPreferences;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Created by romainbenmansour on 11/08/16.
 */
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

    @Nullable
    private String mPreviouslyAttachedPeerId;
    private boolean mPreviouslyScreenShare;

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param context the context
     */
    public VoxeetConferenceView(Context context) {
        super(context);

        internalInit();
    }

    private void internalInit() {
        mPreviouslyScreenShare = false;
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
    public void onResume() {
        super.onResume();

        //UPDATE participant adapter consequently
        if (null != mPreviouslyAttachedPeerId) {
            if (mPreviouslyScreenShare) {
                onScreenShareMediaStreamUpdated(mPreviouslyAttachedPeerId,
                        VoxeetSdk.getInstance().getConferenceService().getMapOfScreenShareStreams());
            } else {
                onMediaStreamUpdated(mPreviouslyAttachedPeerId,
                        VoxeetSdk.getInstance().getConferenceService().getMapOfStreams());
            }
        }

        HashMap<String, MediaStream> streams = VoxeetSdk.getInstance()
                .getConferenceService().getMapOfStreams();

        if (streams.containsKey(VoxeetPreferences.id())) {
            onMediaStreamUpdated(VoxeetPreferences.id(), streams);
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
    public void onMediaStreamUpdated(String userId, Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamUpdated(userId, mediaStreams);

        MediaStream mediaStream = null != mediaStreams ? mediaStreams.get(userId) : null;
        if (null != mediaStream) {
            if (userId.equalsIgnoreCase(VoxeetPreferences.id())) {
                if (mediaStream.videoTracks().size() > 0) {
                    selfView.setVisibility(VISIBLE);
                    selfView.attach(userId, mediaStream);
                } else {
                    selfView.setVisibility(GONE);
                    selfView.unAttach();
                }
            } else if (null != selectedView && (null == selectedView.getPeerId() || userId.equalsIgnoreCase(selectedView.getPeerId()))) {
                if (mediaStream.videoTracks().size() > 0) {
                    selectedView.setVisibility(View.VISIBLE);
                    selectedView.attach(userId, mediaStream);
                    speakerView.setVisibility(View.GONE);
                } else {
                    selectedView.setVisibility(GONE);
                    selectedView.unAttach();
                    speakerView.setVisibility(View.VISIBLE);
                }
            }
        }

        Log.d(TAG, "onMediaStreamUpdated: " + userId + " " + mediaStream);
    }

    @Override
    public void onScreenShareMediaStreamUpdated(@NonNull String userId, @NonNull Map<String, MediaStream> screen_share_media_streams) {
        super.onScreenShareMediaStreamUpdated(userId, screen_share_media_streams);

        MediaStream mediaStream = screen_share_media_streams.get(userId);
        if (null != mediaStream) {
            if (!userId.equalsIgnoreCase(VoxeetPreferences.id())) {
                if (mediaStream.videoTracks().size() > 0) {
                    selectedView.setVisibility(View.VISIBLE);
                    selectedView.attach(userId, mediaStream);

                    speakerView.setVisibility(View.GONE);
                } else {
                    speakerView.setVisibility(View.VISIBLE);

                    selectedView.setVisibility(GONE);
                    selectedView.unAttach();
                }
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
        layoutTimer.setVisibility(GONE);

        layoutParticipant.setVisibility(VISIBLE);

        conferenceBarView.onToggleSize(true);
    }

    @Override
    public void onPreMinizedView() {
        selfView.setVisibility(View.GONE);
    }

    @Override
    public void onMinizedView() {
        layoutTimer.setVisibility(VISIBLE);

        layoutParticipant.setVisibility(GONE);

        conferenceBarView.onToggleSize(false);
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_conference_view;
    }

    @Override
    protected void bindView(View view) {
        layoutParticipant = (ViewGroup) view.findViewById(R.id.layout_participant);

        speakerView = (VoxeetCurrentSpeakerView) view.findViewById(R.id.current_speaker_view);

        selectedView = (VideoView) view.findViewById(R.id.selected_video_view);
        selectedView.setAutoUnAttach(true);

        selfView = (VideoView) view.findViewById(R.id.self_video_view);

        selfView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VoxeetSdk.getInstance() != null) {
                    //switchCamera should not trigger crash since it is only possible
                    //to click when already capturing and ... rendering the camera
                    VoxeetSdk.getInstance()
                            .getConferenceService().switchCamera()
                            .then(new PromiseExec<Boolean, Object>() {
                                @Override
                                public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {

                                }
                            })
                            .error(new ErrorPromise() {
                                @Override
                                public void onError(Throwable error) {

                                }
                            });
                }
            }
        });

        layoutTimer = (ViewGroup) view.findViewById(R.id.layout_timer);

        conferenceBarView = (VoxeetConferenceBarView) view.findViewById(R.id.conference_bar_view);

        participantView = (VoxeetParticipantView) view.findViewById(R.id.participant_view);
        participantView.setParticipantListener(this);

        voxeetTimer = view.findViewById(R.id.voxeet_timer);

        //addListeners for voxeet dispatch events
        addListener(speakerView);
        addListener(conferenceBarView);
        addListener(participantView);
        addListener(voxeetTimer);
    }

    @Override
    public void onParticipantSelected(DefaultConferenceUser user, MediaStream mediaStream) {
        speakerView.lockScreen(user.getUserId());

        Log.d(TAG, "onParticipantSelected: onParticipantSelected");
        if (mediaStream != null && (mediaStream.videoTracks().size() > 0 || mediaStream.isScreenShare())) {
            selectedView.setVisibility(VISIBLE);
            selectedView.setAutoUnAttach(true);
            selectedView.attach(user.getUserId(), mediaStream, true);

            speakerView.setVisibility(GONE);
            speakerView.onPause();
        } else {
            selectedView.setVisibility(View.GONE);
            selectedView.unAttach();

            speakerView.setVisibility(View.VISIBLE);
            speakerView.onResume();
        }
    }

    @Override
    public void onParticipantUnselected(DefaultConferenceUser user) {
        if (null != selectedView) {
            selectedView.setVisibility(GONE);
            selectedView.unAttach();
        }

        speakerView.unlockScreen();
        speakerView.setVisibility(VISIBLE);
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }
}