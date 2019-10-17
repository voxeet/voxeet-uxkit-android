package com.voxeet.toolkit.mp4.manifests;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.sdk.manifests.AbstractManifestComponentProvider;
import com.voxeet.toolkit.mp4.MP4MediaPresentationProvider;
import com.voxeet.toolkit.presentation.controller.MediaPlayerProviderController;

public final class MP4MediaPresentationProviderManifestComponent extends AbstractManifestComponentProvider {

    @Override
    protected void init(@NonNull Context context) {
        MediaPlayerProviderController.register(new MP4MediaPresentationProvider());
    }

    @Override
    protected String getComponentName() {
        return MP4MediaPresentationProviderManifestComponent.class.getSimpleName();
    }

    @Override
    protected String getDefaultAuthority() {
        return "com.voxeet.toolkit.mp4.manifests.";
    }
}
