package sdk.voxeet.com.toolkit.views.uitookit.sdk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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

    private VideoView selectedView;

    private VideoView selfView;

    private ViewGroup layoutParticipant;

    private Map<String, MediaStream> mMediaStreams;
    private VoxeetTimer voxeetTimer;

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
        mMediaStreams = new HashMap<>();
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
    public void onMediaStreamUpdated(String userId, Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamUpdated(userId, mediaStreams);

        MediaStream mediaStream = null != mediaStreams ? mediaStreams.get(userId) : null;
        if (userId.equalsIgnoreCase(VoxeetPreferences.id()) && mediaStream != null) {
            if (mediaStream.hasVideo()) {
                selfView.setVisibility(VISIBLE);
                selfView.attach(userId, mediaStream);
            } else {
                selfView.setVisibility(GONE);
                selfView.unAttach();
            }
        }
    }

    @Override
    public void onScreenShareMediaStreamUpdated(@NonNull String userId, @NonNull Map<String, MediaStream> screen_share_media_streams) {
        super.onScreenShareMediaStreamUpdated(userId, screen_share_media_streams);


    }

    @Override
    public void onMediaStreamsListUpdated(Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamsListUpdated(mediaStreams);

        mMediaStreams = mediaStreams;
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

        if (mediaStream != null && (mediaStream.hasVideo() || mediaStream.isScreenShare())) {
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
        selectedView.setVisibility(GONE);
        selectedView.unAttach();

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