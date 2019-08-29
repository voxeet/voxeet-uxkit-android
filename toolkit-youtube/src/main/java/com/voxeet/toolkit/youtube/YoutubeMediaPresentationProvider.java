package com.voxeet.toolkit.youtube;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.sdk.core.services.videopresentation.AbstractMediaPlayerProvider;
import com.voxeet.sdk.utils.annotate;

/**
 * Manage and create youtube videos
 */
@annotate
public class YoutubeMediaPresentationProvider extends AbstractMediaPlayerProvider<YoutubeViewProvider> {

    private String youtubeKey;

    private YoutubeMediaPresentationProvider() {

    }

    /**
     * Constructor with the developer's app key
     * @param youtubeKey a valid key obtained from google's developer website
     */
    public YoutubeMediaPresentationProvider(@NonNull String youtubeKey) {
        this();

        this.youtubeKey = youtubeKey;
    }

    /**
     * Method called to check if a given video can be used by this provider
     * @param url an url to be checked upon
     * @return true if this provider can be used
     */
    @Override
    public boolean isUrlCompatible(@NonNull String url) {
        return url.startsWith("https://youtube.com/") || url.startsWith("https://youtu.be/");
    }

    /**
     * Creates an instance of the YoutubeViewProvider
     * @param context a valid context to be linked upon
     * @return the newly created instance
     */
    @NonNull
    @Override
    public YoutubeViewProvider createMediaPlayerView(@NonNull Context context) {
        return new YoutubeViewProvider(youtubeKey, context);
    }
}
