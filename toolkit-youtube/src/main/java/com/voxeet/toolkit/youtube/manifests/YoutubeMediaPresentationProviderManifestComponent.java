package com.voxeet.toolkit.youtube.manifests;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.sdk.core.services.videopresentation.MediaPlayerProviderComponentHolder;
import com.voxeet.sdk.manifests.AbstractManifestComponentProvider;
import com.voxeet.toolkit.youtube.YoutubeMediaPresentationProvider;

public final class YoutubeMediaPresentationProviderManifestComponent extends AbstractManifestComponentProvider {

    @Override
    protected void init(@NonNull Context context) {
        MediaPlayerProviderComponentHolder.register(new YoutubeMediaPresentationProvider());
    }

    @Override
    protected String getComponentName() {
        return YoutubeMediaPresentationProviderManifestComponent.class.getSimpleName();
    }

    @Override
    protected String getDefaultAuthority() {
        return "com.voxeet.toolkit.mp4.manifests.";
    }
}
