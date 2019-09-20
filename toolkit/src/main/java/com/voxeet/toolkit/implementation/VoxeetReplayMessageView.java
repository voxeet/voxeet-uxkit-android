package com.voxeet.toolkit.implementation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.views.VideoView;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetExpandableView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VoxeetReplayMessageView extends AbstractVoxeetExpandableView {
    private final String TAG = VoxeetConferenceView.class.getSimpleName();

    //private ViewGroup layoutTimer;

    private VideoView selectedView;

    private Map<String, MediaStream> mMediaStreams;
    //private VoxeetTimer voxeetTimer;

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param context the context
     */
    public VoxeetReplayMessageView(Context context) {
        super(context);
    }

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetReplayMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMediaStreamsUpdated(Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamsUpdated(mediaStreams);

        updateStreams(mediaStreams);

    }

    @Override
    public void onConferenceMute(Boolean isMuted) {
    }

    @Override
    public void onConferenceVideo(Boolean isVideoEnabled) {

    }

    @Override
    public void onConferenceCallEnded() {

    }

    @Override
    public void onConferenceMinimized() {

    }

    @Override
    public void onConferenceSpeakerOn(Boolean isSpeakerOn) {

    }

    @Override
    public void onMediaStreamUpdated(String userId, Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamUpdated(userId, mediaStreams);

        updateStreams(mediaStreams);
    }

    @Override
    public void onMediaStreamsListUpdated(Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamsListUpdated(mediaStreams);

        updateStreams(mediaStreams);
    }

    private void updateStreams(Map<String, MediaStream> mediaStreams) {
        Set<String> set = mediaStreams.keySet();
        boolean found = false;
        for (String key : set) {
            if (!found && mediaStreams.get(key) != null) {
                selectedView.setVisibility(View.VISIBLE);
                selectedView.attach(key, mediaStreams.get(key));
                found = true;
            }
        }

        if (!found) {
            selectedView.setVisibility(View.GONE);
        } else {
            selectedView.setVisibility(View.VISIBLE);
        }

        mMediaStreams = mediaStreams;
    }

    @Override
    public void init() {
        mMediaStreams = new HashMap<>();
    }

    @Override
    public void onPreExpandedView() {

    }

    @Override
    public void onExpandedView() {
        //layoutTimer.setVisibility(GONE);
    }

    @Override
    public void onPreMinizedView() {

    }

    @Override
    public void onMinizedView() {
        //layoutTimer.setVisibility(VISIBLE);
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_replay_message_view;
    }

    @Override
    protected void bindView(View view) {
        selectedView = view.findViewById(R.id.selected_video_view);

        //layoutTimer = view.findViewById(R.id.layout_timer);

        //voxeetTimer = view.findViewById(R.id.voxeet_timer);

        //addListeners for voxeet dispatch events
        //addListener(voxeetTimer);
    }
}