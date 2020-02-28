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
public class FirebaseProvider implements INotificationTokenProvider {

    private static final String SDK_CHANNEL_ID = "voxeet_sdk_channel_id";
    private static final String SDK_CHANNEL_NAME = "voxeet_sdk_channel_name";
    private static final String SDK_CHANNEL_DESCRIPTION = "voxeet_sdk_channel_description";
    private static final String SDK_CHANNEL_COLOR = "voxeet_sdk_channel_color";

    private static final String DEFAULT_ID = "VideoConference";
    private static final String DEFAULT_NAME = "Video Conference";
    private static final String DEFAULT_DESCRIPTION = "Incoming calls are managed here";
    private static final int DEFAULT_COLOR = Color.WHITE;

    private static boolean ChannelSet = false;

    private boolean _enabled;
    private boolean _can_log;

    public FirebaseProvider() {
        _enabled = false;
        _can_log = false;
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

    /**
     * Defined if the current controller will log incoming strings
     *
     * @param can_log true or false
     * @return the current controller to chain
     */
    public FirebaseProvider log(boolean can_log) {
        _can_log = can_log;
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
        if (_can_log) Log.d(getClass().getSimpleName(), string);
    }

    @Override
    @Nullable
    public String getToken() {
        try {
            String token = FirebaseInstanceId.getInstance().getToken();
            if (TextUtils.isEmpty(token))
                Log.d("FirebaseProvider", "getToken: the token is null from FirebaseInstanceId...");
            return token;
        } catch (IllegalStateException e) {
            Log.d("FirebaseProvider", "FirebaseInstanceId.getInstance().getAccessToken() returned an IllegalStateException, you have an issue with your project configuration (google-services.json for instance)");
            e.printStackTrace();
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
