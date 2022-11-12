package com.voxeet.uxkit.mp4.manifests;

import android.content.Context;
import android.content.pm.ProviderInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.sdk.manifests.AbstractManifestComponentProvider;
import com.voxeet.uxkit.common.presentation.controller.MediaPlayerProviderController;
import com.voxeet.uxkit.mp4.MP4MediaPresentationProvider;

public final class MP4MediaPresentationProviderManifestComponent extends AbstractManifestComponentProvider {

    @Override
    protected void init(@NonNull Context context, @Nullable ProviderInfo providerInfo) {
        MediaPlayerProviderController.register(new MP4MediaPresentationProvider());
    }

    @Override
    protected String getComponentName() {
        return MP4MediaPresentationProviderManifestComponent.class.getSimpleName();
    }

    @Override
    protected String getDefaultAuthority() {
        return "com.voxeet.uxkit.mp4.manifests.";
    }
}
