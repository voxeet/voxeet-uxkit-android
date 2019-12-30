package com.voxeet.toolkit.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.voxeet.sdk.VoxeetSdk;
import com.voxeet.sdk.utils.Annotate;
import com.voxeet.toolkit.incoming.IncomingNotification;

/**
 * Help integration to automatically invitation into conferences
 * <p>
 * This can be safely use without checcking for the use of the incoming notification helper
 * or the fullscreen helper since it relies on the following :
 * <p>
 * - notification are created with a notificationId set to the hashCode of the conferenceId
 * - the [NotificationManager#cancel(int)](https://developer.android.com/reference/android/app/NotificationManager.html#cancel(int)) will safely removes it
 * <p>
 * The best integration should be done as in the [VoxeetAppCompatActivity](https://github.com/voxeet/voxeet-uxkit-android/commit/b68ce943044ce609e5ec8e0ee99327709284f2ab), thus call for dismiss when all of the following are met :
 * - onNewIntent called for the current activity (can be an invitation clicked)
 * - onResume called after an activity creation (was destroyed/inexistant) (can be an invitation clicked)
 * - ConferenceStatusUpdatedEvent for the Conference, the SDK may be calling for joining in parallel, this way, the notification gets cleaned at the same time
 */
@Annotate
public class IncomingNotificationHelper {

    /**
     * Dismiss a possible Notification from the SystemBar if the `IncomingNotification.EXTRA_NOTIFICATION_ID` is referenced in the `Intent`
     *
     * @param context the context to use to obtain the `NotificationManager` instance
     * @param intent the Activity's Intent
     * @return indication of the removal process follow up
     */
    public static boolean dismiss(@NonNull Context context,
                                  @NonNull Intent intent) {
        String key = IncomingNotification.EXTRA_NOTIFICATION_ID;
        if (intent.hasExtra(key)) {
            int notificationId = intent.getIntExtra(key, -1);
            if (dismiss(context, notificationId)) {
                intent.removeExtra(key);
                return true;
            }
        }
        return false;
    }

    /**
     * Dismiss a possible Notification from the SystemBar if the `notificationId` is ongoing.
     * This method is safe to call since the `NotificationManager` will simply drop the call if no `Notification` with such ID exists
     *
     * @param context the context to use to obtain the `NotificationManager` instance
     * @param notificationId the notificationId to dismiss
     * @return indication of the removal process follow up
     */
    public static boolean dismiss(@NonNull Context context,
                                  int notificationId) {
        if (-1 != notificationId) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
            return true;
        }
        return false;
    }

    /**
     * Dismiss a possible Notification from the SystemBar if the `Activity` leads to a notificationId or if a `Conference` is ongoing
     * This method is safe to call since the `NotificationManager` will simply drop the call if no `Notification` with such ID exists
     *
     * @param activity the Activity to use as getter for information
     * @return indication of the removal process follow up
     */
    public static boolean dismiss(@NonNull Activity activity) {
        if (null != VoxeetSdk.conference()) {
            String conferenceId = VoxeetSdk.conference().getConferenceId();
            if (!TextUtils.isEmpty(conferenceId)) {
                IncomingNotificationHelper.dismiss(activity, conferenceId.hashCode());
            }
        }
        return IncomingNotificationHelper.dismiss(activity, activity.getIntent());

    }
}
