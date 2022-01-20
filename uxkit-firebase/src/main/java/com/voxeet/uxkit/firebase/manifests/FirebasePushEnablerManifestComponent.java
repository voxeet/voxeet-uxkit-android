package com.voxeet.uxkit.firebase.manifests;

import android.content.Context;
import android.content.pm.ProviderInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.FirebaseApp;
import com.voxeet.sdk.manifests.AbstractManifestComponentProvider;
import com.voxeet.sdk.push.utils.NotificationHelper;
import com.voxeet.sdk.services.notification.NotificationTokenHolderFactory;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.firebase.implementation.FirebaseProvider;

public final class FirebasePushEnablerManifestComponent extends AbstractManifestComponentProvider {

    private final static ShortLogger Log = UXKitLogger.createLogger(FirebasePushEnablerManifestComponent.class);

    @Override
    protected void init(@NonNull Context context, @Nullable ProviderInfo providerInfo) {
        Log.d("init: enabling Firebase");
        FirebaseProvider provider = new FirebaseProvider();

        NotificationHelper.createNotificationChannel(context);
        NotificationTokenHolderFactory.provider = provider;
        provider.enable(true).log(true);
        try {
            FirebaseApp.initializeApp(context);
        } catch (Exception e) {
            Log.e(e);
        }
    }

    @Override
    protected String getComponentName() {
        return FirebasePushEnablerManifestComponent.class.getSimpleName();
    }

    @Override
    protected String getDefaultAuthority() {
        return "com.voxeet.uxkit.firebase.manifests.";
    }
}
