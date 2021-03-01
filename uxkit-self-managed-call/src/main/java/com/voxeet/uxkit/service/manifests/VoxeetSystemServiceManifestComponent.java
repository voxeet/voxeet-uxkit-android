package com.voxeet.uxkit.service.manifests;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.sdk.manifests.AbstractManifestComponentProvider;

public final class VoxeetSystemServiceManifestComponent extends AbstractManifestComponentProvider {

    @Override
    protected void init(@NonNull Context context, @Nullable ProviderInfo providerInfo) {
        //SystemServiceFactory.registerSDKServiceClass(ToDoCallService.class);
    }

    @Override
    protected String getComponentName() {
        return VoxeetSystemServiceManifestComponent.class.getSimpleName();
    }

    @Override
    protected String getDefaultAuthority() {
        return "com.voxeet.uxkit.service.manifests.";
    }
}
