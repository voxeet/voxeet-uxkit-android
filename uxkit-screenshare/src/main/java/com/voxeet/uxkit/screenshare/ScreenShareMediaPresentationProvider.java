package com.voxeet.uxkit.screenshare;

import android.content.Context;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.presentation.provider.AbstractMediaPlayerProvider;

/**
 * Manage and create screenshare videos
 */
public class ScreenShareMediaPresentationProvider extends AbstractMediaPlayerProvider<ScreenShareMediaPresentationView> {

    private final static ShortLogger Log = UXKitLogger.createLogger(ScreenShareMediaPresentationProvider.class);

    /**
     * Default constructor
     */
    public ScreenShareMediaPresentationProvider() {
    }

    /**
     * Method called to check if a given video can be used by this provider.
     * To be compatible, developers needs to send an attempt with "screenshare://"
     * <p>
     * A way to inject those event is to create programmatically a VideoPresentationStartedEvent when a MediaStreamType.ScreenShare is detected
     *
     * @param url an url to be checked upon
     * @return true if this provider can be used
     */
    @Override
    public boolean isUrlCompatible(@NonNull String url) {
        boolean compatible = url.startsWith("screenshare://");
        Log.d("isUrlCompatible " + url + " " + compatible);
        return compatible;
    }

    /**
     * Creates an instance of the ScreenShareMediaPresentationView
     *
     * @param context a valid context to be linked upon
     * @return the newly created instance
     */
    @NonNull
    @Override
    public ScreenShareMediaPresentationView createMediaPlayerView(@NonNull Context context) {
        Log.d("createMediaPlayerView called");
        return new ScreenShareMediaPresentationView(context);
    }
}
