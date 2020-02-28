package com.voxeet.toolkit.youtube;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Simple implementation to help integrate youtube into Apps
 */
@Deprecated
public class YoutubeMediaPresentationView extends com.voxeet.uxkit.youtube.YoutubeMediaPresentationView {

    /**
     * Available constructor to create an Youtube View to hold incoming requests
     *
     * @param youtubeKey the API key provided by the developer.console.google.com website
     * @param context    a valid context used to inflate the view when needed
     */
    public YoutubeMediaPresentationView(@NonNull String youtubeKey, @NonNull Context context) {
        super(youtubeKey, context);
    }
}
