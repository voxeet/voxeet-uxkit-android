package com.voxeet.uxkit.firebase.manifests;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.voxeet.sdk.manifests.AbstractManifestComponentProvider;
import com.voxeet.sdk.push.utils.NotificationHelper;
import com.voxeet.sdk.services.notification.NotificationTokenHolderFactory;
import com.voxeet.uxkit.firebase.implementation.FirebaseProvider;

public final class FirebasePushEnablerManifestComponent extends AbstractManifestComponentProvider {

    @Override
    protected void init(@NonNull Context context, @Nullable ProviderInfo providerInfo) {
        Log.d(getClass().getSimpleName(), "init: enabling Firebase");
        FirebaseProvider provider = new FirebaseProvider();

        NotificationHelper.createNotificationChannel(context);
        NotificationTokenHolderFactory.provider = provider;
        provider.enable(true).log(true);
        try {
            FirebaseApp.initializeApp(context);
        } catch (Exception e) {
            e.printStackTrace();
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
