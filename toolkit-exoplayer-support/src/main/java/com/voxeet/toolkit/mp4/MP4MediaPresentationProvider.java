package com.voxeet.toolkit.mp4;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.sdk.core.services.videopresentation.AbstractMediaPlayerProvider;
import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;

/**
 * Manage and create standard and classic mp4 view manager
 */
@Annotate
public class MP4MediaPresentationProvider extends AbstractMediaPlayerProvider<MP4MediaPresentationView> {

    /**
     * Default constructor, since the class will only create standard views, no arguments are required
     */
    public MP4MediaPresentationProvider() {

    }

    /**
     * Method called to check if a given video can be used by this provider
     * @param url an url to be checked upon
     * @return true if this provider can be used
     */
    @Override
    public boolean isUrlCompatible(@NonNull String url) {
        //TODO lowercase ?
        //TODO proper url parser - no time currently for this v0
        String[] found_end = url.split("\\?");
        return found_end[0].endsWith(".mp4");
    }

    /**
     * Creates an instance of the MP4MediaPresentationView
     * @param context a valid context to be linked upon
     * @return the newly created instance
     */
    @NonNull
    @Override
    public MP4MediaPresentationView createMediaPlayerView(@NonNull Context context) {
        return new MP4MediaPresentationView(context);
    }
}
