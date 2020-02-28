package com.voxeet.toolkit.youtube;

import android.support.annotation.NonNull;

/**
 * Manage and create youtube videos
 */
@Deprecated
public class YoutubeMediaPresentationProvider extends com.voxeet.uxkit.youtube.YoutubeMediaPresentationProvider {

    /**
     * Set the Youtube API Key for the current session
     *
     * This method must be call upon initialization or as soon as it is known by the app (before any conference,
     * if any video must be played during a call, a crash will be triggered)
     *
     * @param youtubeKey a valid key obtained from google's developer website
     */
    public static void setApiKey(@NonNull String youtubeKey) {
        com.voxeet.uxkit.youtube.YoutubeMediaPresentationProvider.setApiKey(youtubeKey);
    }
}
