package com.voxeet.uxkit.firebase.implementation;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.voxeet.sdk.push.center.NotificationCenter;
import com.voxeet.sdk.push.center.RemoteMessageFactory;
import com.voxeet.sdk.services.notification.INotificationTokenProvider;
import com.voxeet.sdk.services.notification.NotificationTokenHolderFactory;

public class VoxeetFirebaseMessagingService extends FirebaseMessagingService {

    public VoxeetFirebaseMessagingService() {
        super();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        INotificationTokenProvider provider = NotificationTokenHolderFactory.provider;
        if (null != provider) {
            provider.log("New notification with body " + remoteMessage.getData());

            boolean managed = RemoteMessageFactory.manageRemoteMessage(getApplicationContext(), remoteMessage.getData());

            provider.log("notification managed := " + managed);
        }
    }
}
