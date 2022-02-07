package com.voxeet.uxkit.youtube;

import android.content.Context;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.presentation.provider.AbstractMediaPlayerProvider;

/**
 * Manage and create youtube videos
 */
public class YoutubeMediaPresentationProvider extends AbstractMediaPlayerProvider<YoutubeMediaPresentationView> {

    private final static ShortLogger Log = UXKitLogger.createLogger(YoutubeMediaPresentationProvider.class);

    /**
     * Constructor with the developer's app key
     */
    public YoutubeMediaPresentationProvider() {
    }

    /**
     * Method called to check if a given video can be used by this provider
     * @param url an url to be checked upon
     * @return true if this provider can be used
     */
    @Override
    public boolean isUrlCompatible(@NonNull String url) {
        boolean compatible = url.startsWith("https://youtube.com/") || url.startsWith("https://youtu.be/");
        Log.d("isUrlCompatible " + url + " " + compatible);
        return compatible;
    }

    /**
     * Creates an instance of the YoutubeViewProvider
     * @param context a valid context to be linked upon
     * @return the newly created instance
     */
    @NonNull
    @Override
    public YoutubeMediaPresentationView createMediaPlayerView(@NonNull Context context) {
        Log.d("createMediaPlayerView called");
        return new YoutubeMediaPresentationView(context);
    }
}
