package com.voxeet.toolkit.youtube;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.sdk.core.services.videopresentation.AbstractMediaPlayerProvider;
import com.voxeet.sdk.utils.Annotate;

/**
 * Manage and create youtube videos
 */
@Annotate
public class YoutubeMediaPresentationProvider extends AbstractMediaPlayerProvider<YoutubeMediaPresentationView> {

    @Nullable
    private static String youtubeKey;

    /**
     * Constructor with the developer's app key
     */
    public YoutubeMediaPresentationProvider() {
    }

    /**
     * Set the Youtube API Key for the current session
     *
     * This method must be call upon initialization or as soon as it is known by the app (before any conference,
     * if any video must be played during a call, a crash will be triggered)
     *
     * @param youtubeKey a valid key obtained from google's developer website
     */
    public static void setApiKey(@NonNull String youtubeKey) {
        YoutubeMediaPresentationProvider.youtubeKey = youtubeKey;
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
    public YoutubeMediaPresentationView createMediaPlayerView(@NonNull Context context) {
        if(null == youtubeKey) throw new NullPointerException("oopsi invalid youtube key");
        return new YoutubeMediaPresentationView(youtubeKey, context);
    }
}
