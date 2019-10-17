package com.voxeet.toolkit.mp4;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.voxeet.sdk.json.VideoPresentationPaused;
import com.voxeet.sdk.json.VideoPresentationPlay;
import com.voxeet.sdk.json.VideoPresentationSeek;
import com.voxeet.sdk.json.VideoPresentationStarted;
import com.voxeet.sdk.json.VideoPresentationStopped;
import com.voxeet.sdk.utils.Annotate;
import com.voxeet.toolkit.presentation.view.AbstractMediaPlayerView;

/**
 * Simple implementation to help integrate ExoPlayer for MP4 into Apps
 */
@Annotate
public class MP4MediaPresentationView extends AbstractMediaPlayerView {

    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;

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
            exoPlayer = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(getContext()),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl());

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
        String UA = Util.getUserAgent(getContext(), getContext().getPackageName());
        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(UA)).createMediaSource(Uri.parse(url));
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.voxeet_exoplayer_mp4, this, false);
        addView(view);
        playerView = findViewById(R.id.voxeet_exoplayer_mp4);
    }
}
