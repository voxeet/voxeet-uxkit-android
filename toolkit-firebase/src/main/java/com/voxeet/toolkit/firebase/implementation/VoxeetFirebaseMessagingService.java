package com.voxeet.toolkit.firebase.implementation;

import android.support.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.voxeet.sdk.push.center.NotificationCenterFactory;
import com.voxeet.sdk.services.notification.INotificationTokenProvider;
import com.voxeet.sdk.services.notification.NotificationTokenHolderFactory;
import com.voxeet.sdk.utils.Annotate;

@Annotate
public class VoxeetFirebaseMessagingService extends FirebaseMessagingService {

    public VoxeetFirebaseMessagingService() {
        super();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        INotificationTokenProvider provider = NotificationTokenHolderFactory.provider;
        if (null != provider) {
            provider.log("New notification with body " + remoteMessage.getData());

            boolean managed = NotificationCenterFactory.instance.manageRemoteMessage(getApplicationContext(), remoteMessage.getData());

            provider.log("notification managed := " + managed);
        }
    }
}
