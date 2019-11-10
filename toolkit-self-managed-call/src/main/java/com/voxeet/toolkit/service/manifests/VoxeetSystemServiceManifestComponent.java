package com.voxeet.toolkit.service.manifests;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.sdk.manifests.AbstractManifestComponentProvider;

public final class VoxeetSystemServiceManifestComponent extends AbstractManifestComponentProvider {

    @Override
    protected void init(@NonNull Context context) {
        //SystemServiceFactory.registerSDKServiceClass(ToDoCallService.class);
    }

    @Override
    protected String getComponentName() {
        return VoxeetSystemServiceManifestComponent.class.getSimpleName();
    }

    @Override
    protected String getDefaultAuthority() {
        return "com.voxeet.toolkit.service.manifests.";
    }
}
