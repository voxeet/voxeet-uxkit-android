package com.voxeet.uxkit.presentation.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.uxkit.presentation.provider.AbstractMediaPlayerProvider;

/**
 * The purpose of this class is to provide a static way to register instance of MediaPlayerProvider when a VideoPresentation is started for the following (by default provided when using our direct optionnal dependencies) :
 * - api (':toolkit-exoplayer-support') to support ExoPlayer for mp4 urls
 * - api (':toolkit-youtube') to support Youtube playback for ... youtube urls
 */
@Deprecated
public class MediaPlayerProviderController {

    private MediaPlayerProviderController() {

    }


    /**
     * Register a given Media Player Provider
     * <p>
     * Perfect to integrate thrid party services like Youtube, Twitch, Vimeo, etc...
     * <p>
     * This interface is available for developer to register their own implementation without requiring from Voxeet to release new versions.
     * <p>
     * Due to licensing possible issues, some services won't be provided by Voxeet. For instance, GPLv3 libraries used by an X provider or specific per-license vendor library.
     *
     * @param mediaPlayerProvider
     */
    public static void register(@NonNull AbstractMediaPlayerProvider mediaPlayerProvider) {
        com.voxeet.uxkit.common.presentation.controller.MediaPlayerProviderController.register(mediaPlayerProvider);
    }

    /**
     * Given a specific url, tries to get a MediaPlayer which will be able to manage it
     * <p>
     * Perfect to integrate third party services like Youtube, Twitch, Vimeo, etc...
     * <p>
     * This interface is available for developer to register their own implementation without requiring from Voxeet to release new versions.
     * <p>
     * Due to licensing possible issues, some services won't be provided by Voxeet. For instance, GPLv3 libraries used by an X provider or specific per-license vendor library.
     *
     * @param url the url to check against (note: it can also be anything else but we recommend an url when starting a video presentation)
     * @return the proper manager instance of null if none have been registered
     */
    @Nullable
    public static com.voxeet.uxkit.common.presentation.provider.AbstractMediaPlayerProvider getCompatibleMediaPlayerProvider(@NonNull String url) {
        return com.voxeet.uxkit.common.presentation.controller.MediaPlayerProviderController.getCompatibleMediaPlayerProvider(url);
    }
}
