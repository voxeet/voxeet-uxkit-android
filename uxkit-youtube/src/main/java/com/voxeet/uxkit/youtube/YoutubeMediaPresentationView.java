package com.voxeet.uxkit.youtube;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.voxeet.sdk.json.Event;
import com.voxeet.sdk.json.VideoPresentationPaused;
import com.voxeet.sdk.json.VideoPresentationPlay;
import com.voxeet.sdk.json.VideoPresentationSeek;
import com.voxeet.sdk.json.VideoPresentationStarted;
import com.voxeet.sdk.json.VideoPresentationStopped;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.presentation.view.AbstractMediaPlayerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation to help integrate youtube into Apps
 */
public class YoutubeMediaPresentationView extends AbstractMediaPlayerView {

    private final static ShortLogger Log = UXKitLogger.createLogger(YoutubeMediaPresentationView.class);

    private YouTubePlayerView youtubeView;
    private YouTubePlayer youtubePlayer;

    @NonNull
    private List<Event> pendingEvents = new ArrayList<>();

    private long seek = 0;

    private String lastKey;

    /**
     * Available constructor to create an Youtube View to hold incoming requests
     *
     * @param context    a valid context used to inflate the view when needed
     */
    public YoutubeMediaPresentationView(@NonNull Context context) {
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
        String videoId = getVideoId(videoPresentationStarted.url);
        Log.d("start " + videoId);

        if (null != youtubePlayer) {
            youtubePlayer.loadVideo(getVideoId(videoPresentationStarted.url), videoPresentationStarted.timestamp);
            youtubePlayer.play();
        } else {
            pendingEvents.add(videoPresentationStarted);
        }
    }

    /**
     * A video has been stopped
     *
     * @param videoPresentationStopped a valid instance
     */
    @Override
    public void stop(@NonNull VideoPresentationStopped videoPresentationStopped) {
        Log.d("stop lastKey:=" + lastKey + " key:=" + videoPresentationStopped.key);
        if (null == lastKey || !lastKey.equals(videoPresentationStopped.key)) return;
        if (null != youtubePlayer) {
            youtubePlayer.pause();
        } else {
            pendingEvents.add(videoPresentationStopped);
        }
    }

    /**
     * A video is playing/resuming
     *
     * @param videoPresentationPlay a valid instance
     */
    @Override
    public void play(@NonNull VideoPresentationPlay videoPresentationPlay) {
        Log.d("play lastKey:=" + lastKey + " key:=" + videoPresentationPlay.key+" "+ videoPresentationPlay.timestamp);
        if (null == lastKey || !lastKey.equals(videoPresentationPlay.key)) return;
        if (null != youtubePlayer) {
            seek = videoPresentationPlay.timestamp;
            if (seek > 0) youtubePlayer.seekTo((int) (seek / 1000));
            youtubePlayer.play();
        } else {
            pendingEvents.add(videoPresentationPlay);
        }
    }

    /**
     * A video has been paused
     *
     * @param videoPresentationPaused a valid instance
     */
    @Override
    public void pause(@NonNull VideoPresentationPaused videoPresentationPaused) {
        Log.d("pause lastKey:=" + lastKey + " key:=" + videoPresentationPaused.key+" "+ videoPresentationPaused.timestamp);
        if (null == lastKey || !lastKey.equals(videoPresentationPaused.key)) return;
        if (null != youtubePlayer) {
            seek = videoPresentationPaused.timestamp;
            if (seek > 0) youtubePlayer.seekTo((int) (seek / 1000));
            youtubePlayer.pause();
        } else {
            pendingEvents.add(videoPresentationPaused);
        }
    }

    /**
     * A video's timer has been changed to a specific position
     *
     * @param videoPresentationSeek a valid instance
     */
    @Override
    public void seek(@NonNull VideoPresentationSeek videoPresentationSeek) {
        Log.d("seek lastKey:=" + lastKey + " key:=" + videoPresentationSeek.key + " " + videoPresentationSeek);
        if (null == lastKey || !lastKey.equals(videoPresentationSeek.key)) return;
        if (null != youtubePlayer) {
            seek = videoPresentationSeek.timestamp;
            youtubePlayer.seekTo((int) (seek / 1000));
        } else {
            pendingEvents.add(videoPresentationSeek);
        }
    }

    /**
     * Callback when youtube has been properly initialized
     *
     * @param youTubePlayer the player to use
     */
    private void onInitializationSuccess(@NonNull YouTubePlayer youTubePlayer) {
        Log.d("onInitializationSuccess " + youtubePlayer);
        this.youtubePlayer = youTubePlayer;
        youtubeView.setEnabled(false);
        youtubeView.setOnTouchListener((view1, motionEvent) -> false);

        youtubeView.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                Log.d("onYouTubePlayerEnterFullScreen");
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
                Log.d("onYouTubePlayerEnterFullScreen");
            }
        });

        youtubeView.enableBackgroundPlayback(true);

        for (Event pending : pendingEvents) {
            switch (pending.getClass().getSimpleName()) {
                case "VideoPresentationStarted":
                    start((VideoPresentationStarted) pending);
                    break;
                case "VideoPresentationStopped":
                    stop((VideoPresentationStopped) pending);
                    break;
                case "VideoPresentationPlay":
                    play((VideoPresentationPlay) pending);
                    break;
                case "VideoPresentationPaused":
                    pause((VideoPresentationPaused) pending);
                    break;
                case "VideoPresentationSeek":
                    seek((VideoPresentationSeek) pending);
                    break;
                default:
                    Log.d("unknown class " + pending.getClass().getSimpleName());
            }
        }

        pendingEvents.clear();
    }

    private void init() {
        try {
            //WIP keeping those lines to integrate volume slider
            //LayoutInflater.from(getContext()).inflate(R.layout.voxeet_youtube, this);
            //LinearLayout parent = findViewById(R.id.voxeet_youtube_parent);

            youtubeView = new YouTubePlayerView(getContext());
            youtubeView.setEnabled(false);
            youtubeView.setOnTouchListener((view, motionEvent) -> false);
            youtubeView.setEnableAutomaticInitialization(false);

            addView(youtubeView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            IFramePlayerOptions options = new IFramePlayerOptions.Builder().controls(0).build();

            youtubeView.initialize(new YoutubePlayerViewListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    super.onReady(youtubePlayer);
                    onInitializationSuccess(youTubePlayer);
                }
            }, options);
        } catch (Exception e) {
            Log.e(e);
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
