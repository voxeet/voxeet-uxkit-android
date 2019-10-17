package com.voxeet.toolkit.presentation.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.voxeet.sdk.json.VideoPresentationPaused;
import com.voxeet.sdk.json.VideoPresentationPlay;
import com.voxeet.sdk.json.VideoPresentationSeek;
import com.voxeet.sdk.json.VideoPresentationStarted;
import com.voxeet.sdk.json.VideoPresentationStopped;
import com.voxeet.sdk.utils.Annotate;

/**
 * Abstract View to manage and expose Video Presentation
 */
@Annotate
public abstract class AbstractMediaPlayerView extends FrameLayout {

    /**
     * Create an instance of the View directly from the MediaPlayerProvider
     *
     * @param context a direct context to build the view onto
     */
    public AbstractMediaPlayerView(Context context) {
        super(context);
    }

    /**
     * Create an instance of the View directly from the Android's View manager (or manually with AttributeSet)
     *
     * @param context a context to build the view onto
     * @param attrs the various given attributes
     */
    public AbstractMediaPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Create an instance of the View directly from the Android's View manager (or manually with AttributeSet)
     *
     * @param context a context to build the view onto
     * @param attrs the various given attributes
     * @param defStyleAttr the Default Style to use
     */
    public AbstractMediaPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Create an instance of the View directly from the Android's View manager (or manually with AttributeSet)
     *
     * @param context a context to build the view onto
     * @param attrs the various given attributes
     * @param defStyleAttr the Default Style to use
     * @param defStyleRes from Lollipop, also defines the default style resource
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AbstractMediaPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Start a video from valid information
     *
     * @param videoPresentationStarted representation of the holder to get the info from
     */
    public abstract void start(@NonNull VideoPresentationStarted videoPresentationStarted);

    /**
     * Stop the current video
     * @param videoPresentationStopped representation of the holder to get the info from
     */
    public abstract void stop(@NonNull VideoPresentationStopped videoPresentationStopped);

    /**
     * Play the current video
     *
     * @param videoPresentationPlay representation of the holder to get the info from
     */
    public abstract void play(@NonNull VideoPresentationPlay videoPresentationPlay);

    /**
     * Pause the current video
     *
     * @param videoPresentationPaused representation of the holder to get the info from
     */
    public abstract void pause(@NonNull VideoPresentationPaused videoPresentationPaused);

    /**
     * Directly change a video presentation to a defined timestamp
     *
     * @param videoPresentationSeek representation of the holder to get the info from
     */
    public abstract void seek(@NonNull VideoPresentationSeek videoPresentationSeek);
}
