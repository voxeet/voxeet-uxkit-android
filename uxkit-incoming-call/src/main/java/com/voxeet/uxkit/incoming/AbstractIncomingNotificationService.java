package com.voxeet.uxkit.incoming;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.voxeet.sdk.push.center.invitation.InvitationBundle;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.incoming.implementation.DefaultIncomingNotification;
import com.voxeet.uxkit.incoming.notification.NotificationBundle;

public abstract class AbstractIncomingNotificationService<T extends AbstractIncomingNotificationIntentProvider> extends Service {

    private final static ShortLogger Log = UXKitLogger.createLogger(DefaultIncomingNotification.class);

    final static int DEFAULT_NOTIFICATION_ID = 234;

    // will hold the various static configuration for the IncomingNotification
    // to edit, preferrably use either Factory component in the manifest or Application override when dealing with FCM
    public final static IncomingNotificationConfiguration Configuration = new IncomingNotificationConfiguration();

    private T provider;

    @Override
    public void onCreate() {
        provider = createIncomingNotificationIntentProvider();
        Log.d("onCreate: ");

        createNotificationChannel(this);
        startForegroundDefault();
    }

    @NonNull
    protected abstract T createIncomingNotificationIntentProvider();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) {
            Log.d("receiving invalid bundle");
        }

        Log.d("receiving start for service");
        if (null == intent) return Service.START_STICKY;
        Bundle bundle = intent.getExtras();
        if (null == bundle) return Service.START_STICKY;

        InvitationBundle serviceInvitationBundle = new InvitationBundle(bundle);
        NotificationBundle notificationBundle = provider.createNotification(serviceInvitationBundle, false);

        if (null == notificationBundle) {
            stopSelf();
            return START_STICKY;
        }

        int notificationId = notificationBundle.notificationId;
        Notification lastNotification = notificationBundle.notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(notificationId, lastNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST);
        } else {
            startForeground(notificationId, lastNotification);
        }

        Log.d("showing notification overhead");
        return Service.START_STICKY;
    }

    public void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < 26) return;

        String channelId = provider.getChannelId(context);

        NotificationChannel channel = new NotificationChannel(channelId,
                context.getString(R.string.voxeet_incoming_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.voxeet_incoming_notification_channel_description));
        channel.enableLights(true);
        channel.setLightColor(0);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100L, 200L});
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
                .build();
        channel.setSound(provider.getRingtoneUri(), audioAttributes);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != mNotificationManager) {
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private void startForegroundDefault() {
        int notificationId = DEFAULT_NOTIFICATION_ID;
        String channelId = provider.getChannelId(this);
        Log.d("startForegroundDefault: " + channelId);

        Notification lastNotification = new NotificationCompat.Builder(this, channelId)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setContentTitle("starting")
                .setContentText("starting")
                .setSmallIcon(R.drawable.ic_incoming_call_notification)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //shouldn't happen, creating overhead above
            startForeground(notificationId, lastNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST);
        } else {
            startForeground(notificationId, lastNotification);
        }
    }
}
