package com.voxeet.uxkit.youtube;

import androidx.annotation.NonNull;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

public abstract class YoutubePlayerViewListener implements YouTubePlayerListener {

    private final ShortLogger Log = UXKitLogger.createLogger(getClass());

    @Override
    public void onApiChange(@NonNull YouTubePlayer youTubePlayer) {
        Log.d("onApiChange");
    }

    @Override
    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float v) {
        //no-op to prevent log flood
    }

    @Override
    public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError playerError) {
        Log.d("onError" + playerError);
    }

    @Override
    public void onPlaybackQualityChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlaybackQuality playbackQuality) {
        Log.d("onPlayBackQualityChange " + playbackQuality);
    }

    @Override
    public void onPlaybackRateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlaybackRate playbackRate) {
        Log.d("onPlaybackRateChange " + playbackRate);
    }

    @Override
    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
        Log.d("onReady");
    }

    @Override
    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState playerState) {
        Log.d("onStateChange " + playerState);
    }

    @Override
    public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float v) {
        Log.d("onVideoDuration " + v);
    }

    @Override
    public void onVideoId(@NonNull YouTubePlayer youTubePlayer, @NonNull String s) {
        Log.d("onVideoId " + s);
    }

    @Override
    public void onVideoLoadedFraction(@NonNull YouTubePlayer youTubePlayer, float v) {
        //no-op to prevent log flood
    }
}
