package com.voxeet.uxkit.mp4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.voxeet.sdk.json.VideoPresentationPaused;
import com.voxeet.sdk.json.VideoPresentationPlay;
import com.voxeet.sdk.json.VideoPresentationSeek;
import com.voxeet.sdk.json.VideoPresentationStarted;
import com.voxeet.sdk.json.VideoPresentationStopped;
import com.voxeet.uxkit.presentation.view.AbstractMediaPlayerView;

/**
 * Simple implementation to help integrate ExoPlayer for MP4 into Apps
 */
public class MP4MediaPresentationView extends AbstractMediaPlayerView {

    private ExoPlayer exoPlayer;
    private StyledPlayerView playerView;

    private MediaSource mediaSource;

    private String lastKey;

    /**
     * Available constructor to create an MP4 View to hold incoming requests
     *
     * @param context a valid context used to inflate the view when needed
     */
    public MP4MediaPresentationView(@NonNull Context context) {
        super(context);

        init();
    }

    /**
     * A video start has been requested with a specific key and information
     *
     * @param videoPresentationStarted valid information
     */
    @Override
    public void start(@NonNull VideoPresentationStarted videoPresentationStarted) {
        lastKey = videoPresentationStarted.key;

        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(getContext())
                    .setTrackSelector(new DefaultTrackSelector(getContext()))
                    .setLoadControl(new DefaultLoadControl())
                    .setRenderersFactory(new DefaultRenderersFactory(getContext())).build();

            playerView.setPlayer(exoPlayer);
            playerView.hideController();
            playerView.setUseController(false);
            exoPlayer.setPlayWhenReady(true);
        }

        if (null == mediaSource) mediaSource = createMediaSource(videoPresentationStarted.url);

        exoPlayer.prepare(mediaSource, false, false);
        exoPlayer.seekTo(videoPresentationStarted.timestamp);
    }

    /**
     * A video has been stopped
     *
     * @param videoPresentationStopped a valid instance
     */
    @Override
    public void stop(@NonNull VideoPresentationStopped videoPresentationStopped) {
        if (null == lastKey || !lastKey.equals(videoPresentationStopped.key)) return;
        exoPlayer.setPlayWhenReady(false);
        exoPlayer.stop();
    }

    /**
     * A video is playing/resuming
     *
     * @param videoPresentationPlay a valid instance
     */
    @Override
    public void play(@NonNull VideoPresentationPlay videoPresentationPlay) {
        if (null == lastKey || !lastKey.equals(videoPresentationPlay.key)) return;
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.prepare(mediaSource, false, false);
        exoPlayer.seekTo(videoPresentationPlay.timestamp);
    }

    /**
     * A video has been paused
     *
     * @param videoPresentationPaused a valid instance
     */
    @Override
    public void pause(@NonNull VideoPresentationPaused videoPresentationPaused) {
        if (null == lastKey || !lastKey.equals(videoPresentationPaused.key)) return;
        exoPlayer.setPlayWhenReady(false);
        exoPlayer.getPlaybackState();
    }

    /**
     * A video's timer has been changed to a specific position
     *
     * @param videoPresentationSeek a valid instance
     */
    @Override
    public void seek(@NonNull VideoPresentationSeek videoPresentationSeek) {
        if (null == lastKey || !lastKey.equals(videoPresentationSeek.key)) return;
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.seekTo(videoPresentationSeek.timestamp);
        exoPlayer.getPlaybackState();
    }

    private MediaSource createMediaSource(@NonNull String url) {
        String userAgent = Util.getUserAgent(getContext(), getContext().getPackageName());

        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory().setUserAgent(userAgent);
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(url));
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.voxeet_exoplayer_mp4, this, false);
        addView(view);
        playerView = findViewById(R.id.voxeet_exoplayer_mp4);
    }
}
