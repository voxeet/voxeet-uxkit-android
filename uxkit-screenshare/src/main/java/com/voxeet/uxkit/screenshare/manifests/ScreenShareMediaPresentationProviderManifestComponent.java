package com.voxeet.uxkit.screenshare.manifests;

import android.content.Context;
import android.content.pm.ProviderInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.sdk.manifests.AbstractManifestComponentProvider;
import com.voxeet.uxkit.common.presentation.controller.MediaPlayerProviderController;
import com.voxeet.uxkit.screenshare.ScreenShareMediaPresentationProvider;

public final class ScreenShareMediaPresentationProviderManifestComponent extends AbstractManifestComponentProvider {

    @Override
    protected void init(@NonNull Context context, @Nullable ProviderInfo providerInfo) {
        MediaPlayerProviderController.register(new ScreenShareMediaPresentationProvider());
    }

    @Override
    protected String getComponentName() {
        return ScreenShareMediaPresentationProviderManifestComponent.class.getSimpleName();
    }

    @Override
    protected String getDefaultAuthority() {
        return "com.voxeet.uxkit.screenshare.manifests.";
    }
}
