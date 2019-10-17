package com.voxeet.toolkit.presentation.provider;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.sdk.utils.Annotate;
import com.voxeet.toolkit.presentation.view.AbstractMediaPlayerView;

/**
 * Abstract class to be extended by developers to create their own management of incoming Video Presentation information
 * @param <MEDIA_PLAYER_VIEW>
 */
@Annotate
public abstract class AbstractMediaPlayerProvider<MEDIA_PLAYER_VIEW extends AbstractMediaPlayerView> {

    protected AbstractMediaPlayerProvider() {

    }

    /**
     * Check for url compatibility with the current MediaPlayerProvider
     *
     * @param url a valid url to check upon
     * @return the compatibility information with the current MediaPlayerProvider
     */
    public abstract boolean isUrlCompatible(@NonNull String url);

    /**
     * Create a new instance of a View which will be used to display and manage the lifecycle of any Video presentation
     *
     * @param context a valid context to build the View onto
     * @return a valid instance of a View to add and manage
     */
    @NonNull
    public abstract MEDIA_PLAYER_VIEW createMediaPlayerView(@NonNull Context context);
}
