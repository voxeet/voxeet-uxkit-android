package com.voxeet.toolkit.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.voxeet.sdk.VoxeetSdk;
import com.voxeet.toolkit.incoming.IncomingNotification;

public class IncomingNotificationHelper {

    public static void dismiss(@NonNull Context context,
                               @NonNull Intent intent) {
        String key = IncomingNotification.EXTRA_NOTIFICATION_ID;
        if (intent.hasExtra(key)) {
            int notificationId = intent.getIntExtra(key, -1);
            if (dismiss(context, notificationId)) {
                intent.removeExtra(key);
            }
        }
    }


    public static boolean dismiss(@NonNull Context context,
                                  int notificationId) {
        if (-1 != notificationId) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
            return true;
        }
        return false;
    }

    public static boolean dismiss(@NonNull Activity activity) {
        if (null != VoxeetSdk.conference()) {
            String conferenceId = VoxeetSdk.conference().getConferenceId();
            if (!TextUtils.isEmpty(conferenceId)) {
                IncomingNotificationHelper.dismiss(activity, conferenceId.hashCode());
            }
        }
        IncomingNotificationHelper.dismiss(activity, activity.getIntent());
    }
}
