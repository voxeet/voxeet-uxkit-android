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
import com.voxeet.sdk.utils.annotate;

@annotate
public class YoutubeViewProvider extends AbstractMediaPlayerView implements YouTubePlayer.OnInitializedListener {

    private YouTubePlayerView youtubeView;
    private YouTubePlayer youtubePlayer;

    private String lastKey;

    public YoutubeViewProvider(@NonNull String youtubeKey, @NonNull Context context) {
        super(context);
        init(youtubeKey);
    }

    @Override
    public void start(@NonNull VideoPresentationStarted videoPresentationStarted) {
        lastKey = videoPresentationStarted.getKey();

        youtubePlayer.loadVideo(getVideoId(videoPresentationStarted.getUrl()));
        youtubePlayer.play();
    }

    @Override
    public void stop(@NonNull VideoPresentationStopped videoPresentationStopped) {
        if (null == lastKey || !lastKey.equals(videoPresentationStopped.getKey())) return;
        youtubePlayer.pause();
    }

    @Override
    public void play(@NonNull VideoPresentationPlay videoPresentationPlay) {
        if (null == lastKey || !lastKey.equals(videoPresentationPlay.getKey())) return;
        youtubePlayer.play();
    }

    @Override
    public void pause(@NonNull VideoPresentationPaused videoPresentationPaused) {
        if (null == lastKey || !lastKey.equals(videoPresentationPaused.getKey())) return;
        youtubePlayer.pause();
    }

    @Override
    public void seek(@NonNull VideoPresentationSeek videoPresentationSeek) {
        if (null == lastKey || !lastKey.equals(videoPresentationSeek.getKey())) return;
        youtubePlayer.seekToMillis((int) videoPresentationSeek.getTimestamp());
    }

    private void init(@NonNull String youtubeKey) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.voxeet_youtube, this, false);
        addView(view);
        youtubeView = findViewById(R.id.voxeet_youtube);

        youtubeView.initialize(youtubeKey, this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        this.youtubePlayer = youTubePlayer;
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    @Nullable
    private String getVideoId(@Nullable String url) {
        if (null == url) return "";

        if(url.startsWith("https://youtu.be/")) {
            String[] split = url.split("youtu.be/");
            if(split.length > 1) {
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
