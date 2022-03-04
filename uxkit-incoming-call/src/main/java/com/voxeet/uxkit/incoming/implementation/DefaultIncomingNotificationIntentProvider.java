package com.voxeet.uxkit.incoming.implementation;

import android.content.BroadcastReceiver;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.voxeet.sdk.factories.VoxeetIntentFactory;
import com.voxeet.sdk.utils.AndroidManifest;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.incoming.AbstractIncomingNotificationIntentProvider;
import com.voxeet.uxkit.incoming.manifest.DismissNotificationBroadcastReceiver;

public class DefaultIncomingNotificationIntentProvider extends AbstractIncomingNotificationIntentProvider {

    private final static ShortLogger Log = UXKitLogger.createLogger(DefaultIncomingNotificationIntentProvider.class);

    public DefaultIncomingNotificationIntentProvider(@NonNull Context context) {
        super(context, Log);
    }

    @NonNull
    @Override
    protected Class<? extends BroadcastReceiver> getAcceptedBroadcastReceiverClass() {
        return AcceptedNotificationBroadcastReceiver.class;
    }

    @NonNull
    @Override
    protected Class<? extends AppCompatActivity> getAcceptedBounceActivityForBroadcastReceiverClass() {
        return DefaultAndroid12BounceActivity.class;
    }

    @NonNull
    @Override
    protected Class<? extends BroadcastReceiver> getDismissedBroadcastReceiverClass() {
        return DismissNotificationBroadcastReceiver.class;
    }

    @Nullable
    @Override
    protected Class<? extends AppCompatActivity> getIncomingCallActivityClass() {
        String voxeet_default_incoming = AndroidManifest.readMetadata(context, "voxeet_incoming_class", null);
        Log.d("getIncomingCallActivityClass, attempt to create a klass for " + voxeet_default_incoming);

        return (Class<? extends AppCompatActivity>) VoxeetIntentFactory.createClass(voxeet_default_incoming);
    }
}
