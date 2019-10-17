package com.voxeet.toolkit.presentation.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.sdk.utils.Annotate;
import com.voxeet.toolkit.presentation.provider.AbstractMediaPlayerProvider;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The purpose of this class is to provide a static way to register instance of MediaPlayerProvider when a VideoPresentation is started for the following (by default provided when using our direct optionnal dependencies) :
 * - api (':toolkit-exoplayer-support') to support ExoPlayer for mp4 urls
 * - api (':toolkit-youtube') to support Youtube playback for ... youtube urls
 */
@Annotate
public class MediaPlayerProviderController {

    private final static CopyOnWriteArrayList<AbstractMediaPlayerProvider> MEDIA_PLAYER_PROVIDERS = new CopyOnWriteArrayList<>();

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
        MEDIA_PLAYER_PROVIDERS.add(mediaPlayerProvider);
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
    public static AbstractMediaPlayerProvider getCompatibleMediaPlayerProvider(@NonNull String url) {
        for (AbstractMediaPlayerProvider provider : MEDIA_PLAYER_PROVIDERS) {
            if (provider.isUrlCompatible(url)) return provider;
        }

        return null;
    }
}
