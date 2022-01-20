package com.voxeet.uxkit.firebase.implementation;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.iid.FirebaseInstanceId;
import com.voxeet.sdk.push.utils.NotificationHelper;
import com.voxeet.sdk.services.notification.INotificationTokenProvider;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

/**
 * Simple Firebase wrapper
 * <p>
 * Note that when the app is awaken from a killed-state (notifications), don't forget to init it in your
 * own Application override
 * <p>
 * note : in the current version, the isEnabled() method has been renamed to
 * isTokenUploadAllowed(), in a future release it can switch back to its "normal" behaviour
 */
public class FirebaseProvider implements INotificationTokenProvider {

    private final static ShortLogger Log = UXKitLogger.createLogger(FirebaseProvider.class);

    private boolean _enabled;

    public FirebaseProvider() {
        _enabled = false;
    }

    /**
     * Define if the current workflow is enabled
     * <p>
     * Note : enabling the controller will only tell if the user's device token will
     * be uploaded.
     *
     * @param is_enable true or false
     * @return the current controller to chain
     */
    public FirebaseProvider enable(boolean is_enable) {
        _enabled = is_enable;
        return this;
    }

    @Deprecated
    public FirebaseProvider log(boolean can_log) {
        return this;
    }

    /**
     * @return if the user token will be uploaded
     */
    @Override
    public boolean isTokenUploadAllowed() {
        return _enabled;
    }

    public void log(@NonNull String string) {
        Log.d(string);
    }

    @Override
    @Nullable
    public String getToken() {
        try {
            String token = FirebaseInstanceId.getInstance().getToken();
            if (TextUtils.isEmpty(token))
                Log.d("getToken: the token is null from FirebaseInstanceId...");
            return token;
        } catch (IllegalStateException e) {
            Log.e("FirebaseInstanceId.getInstance().getAccessToken() returned an IllegalStateException, you have an issue with your project configuration (google-services.json for instance)", e);
            return null;
        }
    }

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
