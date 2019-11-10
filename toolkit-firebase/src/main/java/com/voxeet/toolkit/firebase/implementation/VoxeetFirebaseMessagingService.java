package com.voxeet.toolkit.firebase.implementation;

import android.support.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.voxeet.push.center.NotificationCenterFactory;
import com.voxeet.push.utils.Annotate;
import com.voxeet.sdk.services.notification.INotificationTokenProvider;
import com.voxeet.sdk.services.notification.NotificationTokenHolderFactory;

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
