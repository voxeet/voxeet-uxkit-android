package com.voxeet.uxkit.implementation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.MediaStreamType;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.views.VideoView;
import com.voxeet.uxkit.R;
import com.voxeet.uxkit.implementation.overlays.abs.AbstractVoxeetExpandableView;
import com.voxeet.uxkit.implementation.overlays.abs.IExpandableViewListener;

import java.util.List;

public class VoxeetReplayMessageView extends AbstractVoxeetExpandableView implements IExpandableViewListener {
    private final String TAG = VoxeetConferenceView.class.getSimpleName();

    private VideoView selectedView;

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
    public void onUserAddedEvent(@NonNull Conference conference, @NonNull Participant user) {
        super.onUserAddedEvent(conference, user);
        updateStreams();
    }

    @Override
    public void onUserUpdatedEvent(@NonNull Conference conference, @NonNull Participant user) {
        super.onUserUpdatedEvent(conference, user);
        updateStreams();
    }

    @Override
    public void onStreamAddedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamAddedEvent(conference, user, mediaStream);
        updateStreams();
    }

    @Override
    public void onStreamUpdatedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamUpdatedEvent(conference, user, mediaStream);
        updateStreams();
    }

    @Override
    public void onStreamRemovedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamRemovedEvent(conference, user, mediaStream);
        updateStreams();
    }

    private void updateStreams() {

        List<Participant> users = VoxeetSDK.conference().getParticipants();

        MediaStream stream = null;
        Participant attach = null;
        for (Participant user : users) {
            stream = user.streamsHandler().getFirst(MediaStreamType.Camera);
            attach = user;
            if (null != stream) break;
        }

        if (null != stream) {
            selectedView.setVisibility(View.VISIBLE);
            selectedView.attach(attach.getId(), stream);
        } else {
            selectedView.unAttach();
            selectedView.setVisibility(View.GONE);
        }

    }

    @Override
    public void init() {
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
    }
}