package com.voxeet.toolkit.firebase.implementation;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.voxeet.sdk.push.utils.NotificationHelper;
import com.voxeet.sdk.services.notification.INotificationTokenProvider;
import com.voxeet.sdk.utils.Annotate;

/**
 * Simple Firebase wrapper
 * <p>
 * Note that when the app is awaken from a killed-state (notifications), don't forget to init it in your
 * own Application override
 * <p>
 * note : in the current version, the isEnabled() method has been renamed to
 * isTokenUploadAllowed(), in a future release it can switch back to its "normal" behaviour
 */
@Annotate
@Deprecated
public class FirebaseProvider extends com.voxeet.uxkit.firebase.implementation.FirebaseProvider {

    @Deprecated
    public static String getChannelId(@NonNull Context context) {
        return NotificationHelper.getChannelId(context);
    }

    @Deprecated
    public static boolean createNotificationChannel(@NonNull Context context) {
        return NotificationHelper.createNotificationChannel(context);
    }

    @Deprecated
    public static boolean createNotificationChannel(@NonNull Context context,
                                                    @NonNull String id,
                                                    @NonNull CharSequence name,
                                                    @NonNull String description,
                                                    int argb) {
        return NotificationHelper.createNotificationChannel(context, id, name, description, argb);
    }
}
