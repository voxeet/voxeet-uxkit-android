package com.voxeet.uxkit.manifests;

import android.content.Context;
import android.content.pm.ProviderInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.VoxeetSDK;
import com.voxeet.sdk.manifests.AbstractManifestComponentProvider;
import com.voxeet.uxkit.BuildConfig;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

public final class UXKitManifestComponent extends AbstractManifestComponentProvider {

    private final static ShortLogger Log = UXKitLogger.createLogger(UXKitManifestComponent.class);

    @Override
    protected void init(@NonNull Context context, @Nullable ProviderInfo providerInfo) {
        // in out of order manifest instanciation, prevents a crash
        VoxeetSDK.setApplication(context);

        VoxeetSDK.registerComponentVersion("android-uxkit", BuildConfig.VERSION_NAME);
    }

    @Override
    protected String getComponentName() {
        return UXKitManifestComponent.class.getSimpleName();
    }

    @Override
    protected String getDefaultAuthority() {
        return "com.voxeet.uxkit.manifests.";
    }
}
