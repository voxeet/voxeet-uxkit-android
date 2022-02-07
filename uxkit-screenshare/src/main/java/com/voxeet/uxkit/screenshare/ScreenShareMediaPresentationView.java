package com.voxeet.uxkit.screenshare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.sdk.events.v2.StreamAddedEvent;
import com.voxeet.sdk.events.v2.StreamRemovedEvent;
import com.voxeet.sdk.events.v2.StreamUpdatedEvent;
import com.voxeet.sdk.json.VideoPresentationPaused;
import com.voxeet.sdk.json.VideoPresentationPlay;
import com.voxeet.sdk.json.VideoPresentationSeek;
import com.voxeet.sdk.json.VideoPresentationStarted;
import com.voxeet.sdk.json.VideoPresentationStopped;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.utils.Map;
import com.voxeet.sdk.views.VideoView;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.presentation.view.AbstractMediaPlayerView;
import com.voxeet.uxkit.screenshare.utils.PinchGestureProvider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Simple implementation to help integrate screenshare playback into Apps
 */
public class ScreenShareMediaPresentationView extends AbstractMediaPlayerView {

    private final static ShortLogger Log = UXKitLogger.createLogger(ScreenShareMediaPresentationView.class);
    private final ScreenShareEvents events = new ScreenShareEvents();


    @Nullable
    private VideoView videoView;

    @SuppressLint("ClickableViewAccessibility")
    public ScreenShareMediaPresentationView(@NonNull Context context) {
        super(context);

        ScaleGestureDetector pinchDetector = PinchGestureProvider.create(context,
                this::onVideoViewFill,
                this::onVideoViewFit);

        setOnTouchListener((view, event) -> pinchDetector.onTouchEvent(event));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        EventBus eventBus = EventBus.getDefault();
        if (!eventBus.isRegistered(events)) eventBus.register(events);

        // also maange the screenshare state from here
        updateStreamToDisplay();
    }

    @Override
    protected void onDetachedFromWindow() {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.isRegistered(events)) eventBus.unregister(events);

        super.onDetachedFromWindow();
    }

    /**
     * A video start has been requested with a specific key and information
     *
     * @param videoPresentationStarted valid information
     */
    @Override
    public void start(@NonNull VideoPresentationStarted videoPresentationStarted) {
        // not usefull here
    }

    /**
     * A video has been stopped
     *
     * @param videoPresentationStopped a valid instance
     */
    @Override
    public void stop(@NonNull VideoPresentationStopped videoPresentationStopped) {
        // not usefull here
    }

    /**
     * A video is playing/resuming
     *
     * @param videoPresentationPlay a valid instance
     */
    @Override
    public void play(@NonNull VideoPresentationPlay videoPresentationPlay) {
        // not usefull here
    }

    /**
     * A video has been paused
     *
     * @param videoPresentationPaused a valid instance
     */
    @Override
    public void pause(@NonNull VideoPresentationPaused videoPresentationPaused) {
        // not usefull here
    }

    /**
     * A video's timer has been changed to a specific position
     *
     * @param videoPresentationSeek a valid instance
     */
    @Override
    public void seek(@NonNull VideoPresentationSeek videoPresentationSeek) {
        // not usefull here
    }

    @Nullable
    private VideoView getOrManageVideoView() {
        if (null != videoView) return videoView;

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.uxkit_screenshare, this);

        videoView = layout.findViewById(R.id.uxkit_screenshare_videoview);
        return videoView;
    }

    private void updateStreamToDisplay() {
        VideoView videoView = getOrManageVideoView();

        if (null == videoView) {
            Log.e("updateStreamToDisplay the videoView is invalid", new IllegalStateException("invalid videoView"));
            return;
        }

        Runnable invalid_state = () -> {
            videoView.setVisibility(View.GONE);
            if (videoView.isAttached()) videoView.unAttach();
        };

        Participant withScreenShare = Map.find(VoxeetSDK.conference().getParticipants(),
                participant -> {
                    MediaStream stream = participant.streamsHandler().getFirst(MediaStreamType.ScreenShare);
                    return null != stream && stream.videoTracks().size() > 0;
                });

        if (null == withScreenShare) {
            invalid_state.run();
            return;
        }

        MediaStream stream = Map.find(withScreenShare.streams(), mediaStream -> {
            if (null == mediaStream) return false;
            if (!MediaStreamType.ScreenShare.equals(mediaStream.getType())) return false;
            return mediaStream.videoTracks().size() > 0;
        });

        if (null == stream) {
            invalid_state.run();
            return;
        }

        String peerId = videoView.getPeerId();
        boolean isDifferent = null == peerId || !peerId.equals(withScreenShare.getId());
        String participantId = withScreenShare.getId();

        if (null == participantId) return;

        if (View.VISIBLE != videoView.getVisibility()) {
            videoView.setVisibility(View.VISIBLE);
        }

        if (!videoView.isAttached() || isDifferent) {
            videoView.attach(withScreenShare.getId(), stream);
        }
    }

    /**
     * Change the VideoView to fill (the stream will fill the whole video space)
     * Aspect ratio is conserved
     */
    private void onVideoViewFill() {
        if (null == videoView) return;
        videoView.setVideoFill();
    }

    /**
     * Change the VideoView to fit (the stream will be modified to be fully seen in the view
     * Aspect ratio is conserved
     */
    private void onVideoViewFit() {
        if (null == videoView) return;
        videoView.setVideoFit();
    }

    private final class ScreenShareEvents {

        @Subscribe
        public void onEvent(@NonNull StreamAddedEvent event) {
            Log.d("ScreenShareEvents/StreamAddedEvent " + event);
            updateStreamToDisplay();
        }

        @Subscribe
        public void onEvent(@NonNull StreamUpdatedEvent event) {
            Log.d("ScreenShareEvents/StreamUpdatedEvent " + event);
            updateStreamToDisplay();
        }

        @Subscribe
        public void onEvent(@NonNull StreamRemovedEvent event) {
            Log.d("ScreenShareEvents/StreamRemovedEvent " + event);
            updateStreamToDisplay();
        }
    }
}
