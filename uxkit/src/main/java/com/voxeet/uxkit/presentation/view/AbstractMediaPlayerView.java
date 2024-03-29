package com.voxeet.uxkit.presentation.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Abstract View to manage and expose Video Presentation
 */
@Deprecated
public abstract class AbstractMediaPlayerView extends com.voxeet.uxkit.common.presentation.view.AbstractMediaPlayerView {

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

}
