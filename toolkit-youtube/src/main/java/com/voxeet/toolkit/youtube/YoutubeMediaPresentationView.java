package com.voxeet.toolkit.youtube;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.voxeet.sdk.core.services.videopresentation.AbstractMediaPlayerView;
import com.voxeet.sdk.json.VideoPresentationPaused;
import com.voxeet.sdk.json.VideoPresentationPlay;
import com.voxeet.sdk.json.VideoPresentationSeek;
import com.voxeet.sdk.json.VideoPresentationStarted;
import com.voxeet.sdk.json.VideoPresentationStopped;
import com.voxeet.sdk.utils.Annotate;

/**
 * Simple implementation to help integrate youtube into Apps
 */
@Annotate
public class YoutubeMediaPresentationView extends AbstractMediaPlayerView implements YouTubePlayer.OnInitializedListener {

    private YouTubePlayerView youtubeView;
    private YouTubePlayer youtubePlayer;

    private String lastKey;

    /**
     * Available constructor to create an Youtube View to hold incoming requests
     *
     * @param youtubeKey the API key provided by the developer.console.google.com website
     * @param context    a valid context used to inflate the view when needed
     */
    public YoutubeMediaPresentationView(@NonNull String youtubeKey, @NonNull Context context) {
        super(context);
        init(youtubeKey);
    }

    /**
     * A video start has been requested with a specific key and information
     *
     * @param videoPresentationStarted valid information
     */
    @Override
    public void start(@NonNull VideoPresentationStarted videoPresentationStarted) {
        lastKey = videoPresentationStarted.key;

        youtubePlayer.loadVideo(getVideoId(videoPresentationStarted.url));
        youtubePlayer.play();
    }

    /**
     * A video has been stopped
     *
     * @param videoPresentationStopped a valid instance
     */
    @Override
    public void stop(@NonNull VideoPresentationStopped videoPresentationStopped) {
        if (null == lastKey || !lastKey.equals(videoPresentationStopped.key)) return;
        youtubePlayer.pause();
    }

    /**
     * A video is playing/resuming
     *
     * @param videoPresentationPlay a valid instance
     */
    @Override
    public void play(@NonNull VideoPresentationPlay videoPresentationPlay) {
        if (null == lastKey || !lastKey.equals(videoPresentationPlay.key)) return;
        youtubePlayer.play();
    }

    /**
     * A video has been paused
     *
     * @param videoPresentationPaused a valid instance
     */
    @Override
    public void pause(@NonNull VideoPresentationPaused videoPresentationPaused) {
        if (null == lastKey || !lastKey.equals(videoPresentationPaused.key)) return;
        youtubePlayer.pause();
    }

    /**
     * A video's timer has been changed to a specific position
     *
     * @param videoPresentationSeek a valid instance
     */
    @Override
    public void seek(@NonNull VideoPresentationSeek videoPresentationSeek) {
        if (null == lastKey || !lastKey.equals(videoPresentationSeek.key)) return;
        youtubePlayer.seekToMillis((int) videoPresentationSeek.timestamp);
    }

    /**
     * Callback when youtube has been properly initialized
     *
     * @param provider      the specific provider for the session
     * @param youTubePlayer the player to use
     * @param b
     */
    @Override
    public void onInitializationSuccess(@NonNull YouTubePlayer.Provider provider, @NonNull YouTubePlayer youTubePlayer, boolean b) {
        this.youtubePlayer = youTubePlayer;
    }

    /**
     * Played to initialize the provider and the player
     *
     * @param provider                    the specific provider
     * @param youTubeInitializationResult the initialization result with the reason
     */
    @Override
    public void onInitializationFailure(@NonNull YouTubePlayer.Provider provider, @NonNull YouTubeInitializationResult youTubeInitializationResult) {

    }

    private void init(@NonNull String youtubeKey) {
        try {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.voxeet_youtube, this, false);
            addView(view);
            youtubeView = findViewById(R.id.voxeet_youtube);

            youtubeView.initialize(youtubeKey, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private String getVideoId(@Nullable String url) {
        if (null == url) return "";

        if (url.startsWith("https://youtu.be/")) {
            String[] split = url.split("youtu.be/");
            if (split.length > 1) {
                return split[1];
            }
        }

        String[] split = url.split("v=");
        if (split.length > 0) {
            return split[0].split("&")[0];
        }
        return "";
    }
}
