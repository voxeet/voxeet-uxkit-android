package com.voxeet.uxkit.incoming.utils;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.voxeet.sdk.push.center.invitation.InvitationBundle;
import com.voxeet.sdk.push.center.management.Constants;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.incoming.AbstractIncomingNotificationIntentProvider;
import com.voxeet.uxkit.incoming.AbstractIncomingNotificationService;
import com.voxeet.uxkit.incoming.notification.NotificationBundle;

import java.lang.ref.WeakReference;

public class IncomingNotificationServiceHelper {

    private static WeakReference<? extends AbstractIncomingNotificationService> startedService;
    public static final String[] DEFAULT_NOTIFICATION_KEYS = new String[]{
            Constants.INVITER_ID,
            Constants.INVITER_NAME,
            Constants.NOTIF_TYPE,
            Constants.INVITER_EXTERNAL_ID,
            Constants.INVITER_URL,
            Constants.CONF_ID,
            Constants.CONF_ALIAS
    };

    /**
     * In order to help automatically stop the incoming notification service,
     * call this method with the instance of your service once it calls the onForeground
     * @param service
     */
    public static void registerIncomingNotificationServiceForAutoStop(@Nullable AbstractIncomingNotificationService service) {
        startedService = new WeakReference<>(service);
    }

    private final static ShortLogger Log = UXKitLogger.createLogger(IncomingNotificationServiceHelper.class);

    public static void stop(@NonNull Class<? extends AbstractIncomingNotificationService> klass,
                            @NonNull Context context) {
        IncomingNotificationServiceHelper.stop(klass, context, null, null);
    }

    public static void stopRegisteredIncomingNotificationServiceForAutoStop() {
        AbstractIncomingNotificationService service = startedService.get();

        if (null == service) return;
        service.stopSelf();
    }

    public static void stop(@NonNull Class<? extends AbstractIncomingNotificationService> klass,
                            @NonNull Context context,
                            @Nullable String conferenceId,
                            @Nullable Bundle bundle) {
        if (null != conferenceId) {
            int notificationId = conferenceId.hashCode();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (-1 != notificationId) notificationManager.cancel(notificationId);
        }

        //TODO manage the case where conferenceId are different from the one creating this notification
        Intent intent = new Intent(context, klass);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        context.stopService(intent);
    }

    public static void start(@NonNull Class<? extends AbstractIncomingNotificationService> klass,
                             @NonNull Context context,
                             @NonNull InvitationBundle invitation,
                             @NonNull AbstractIncomingNotificationIntentProvider default_provider) {
        if (!IncomingNotificationServiceHelper.isBackgroundRestricted(context)) {
            Intent intent = new Intent(context, klass);
            intent.putExtras(invitation.asBundle());
            ContextCompat.startForegroundService(context, intent);
        } else {
            NotificationBundle notificationBundle = default_provider.createNotification(invitation, true);
            if (null == notificationBundle) {
                Log.d("start: invalid notification obtained,; skipping");
                return;
            }
            int notificationId = notificationBundle.notificationId;
            Notification notification = notificationBundle.notification;

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, notification);
        }
    }

    public static boolean isBackgroundRestricted(@NonNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return activityManager.isBackgroundRestricted();
        }
        return false;
    }
}
