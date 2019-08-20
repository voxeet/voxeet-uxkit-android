package com.voxeet.toolkit.youtube;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.sdk.core.services.videopresentation.AbstractMediaPlayerProvider;
import com.voxeet.sdk.utils.annotate;

@annotate
public class YoutubeMediaPresentationProvider extends AbstractMediaPlayerProvider<YoutubeViewProvider> {

    private String youtubeKey;

    private YoutubeMediaPresentationProvider() {

    }

    public YoutubeMediaPresentationProvider(@NonNull String youtubeKey) {
        this();

        this.youtubeKey = youtubeKey;
    }

    @Override
    public boolean isUrlCompatible(@NonNull String url) {
        return url.startsWith("https://youtube.com/") || url.startsWith("https://youtu.be/");
    }

    @NonNull
    @Override
    public YoutubeViewProvider createMediaPlayerView(@NonNull Context context) {
        return new YoutubeViewProvider(youtubeKey, context);
    }
}
