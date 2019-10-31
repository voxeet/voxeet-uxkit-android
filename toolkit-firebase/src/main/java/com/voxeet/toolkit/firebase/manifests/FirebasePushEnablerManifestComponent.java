package com.voxeet.toolkit.firebase.manifests;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.voxeet.toolkit.firebase.implementation.FirebaseProvider;
import com.voxeet.sdk.core.services.notification.NotificationTokenHolderFactory;
import com.voxeet.sdk.manifests.AbstractManifestComponentProvider;

public final class FirebasePushEnablerManifestComponent extends AbstractManifestComponentProvider {

    @Override
    protected void init(@NonNull Context context) {
        Log.d(getClass().getSimpleName(), "init: enabling Firebase");
        FirebaseProvider provider = new FirebaseProvider();

        FirebaseProvider.createNotificationChannel(context);
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
        return "com.voxeet.toolkit.firebase.manifests.";
    }
}
