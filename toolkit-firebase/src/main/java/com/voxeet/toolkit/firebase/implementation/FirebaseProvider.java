package com.voxeet.toolkit.firebase.implementation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.voxeet.push.utils.Annotate;
import com.voxeet.sdk.core.services.notification.INotificationTokenProvider;

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

    public static String getChannelId(@NonNull Context context) {
        return readMetadata(context, SDK_CHANNEL_ID, DEFAULT_ID);
    }

    public static boolean createNotificationChannel(@NonNull Context context) {
        return createNotificationChannel(context,
                readMetadata(context, SDK_CHANNEL_ID, DEFAULT_ID),
                readMetadata(context, SDK_CHANNEL_NAME, DEFAULT_NAME),
                readMetadata(context, SDK_CHANNEL_DESCRIPTION, DEFAULT_DESCRIPTION),
                readMetadataInt(context, SDK_CHANNEL_COLOR, DEFAULT_COLOR));
    }

    public static boolean createNotificationChannel(@NonNull Context context,
                                                    @NonNull String id,
                                                    @NonNull CharSequence name,
                                                    @NonNull String description,
                                                    int argb) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return true;
        } else {
            if (ChannelSet) return false;
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(argb);

            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            if (null != mNotificationManager) {
                mNotificationManager.createNotificationChannel(channel);
                ChannelSet = true;
            }
            return true;
        }
    }

    private static int readMetadataInt(@NonNull Context context, @NonNull String key, int argb) {
        try {
            String metaData = readMetadata(context, key, null);
            if (!TextUtils.isEmpty(metaData)) return Integer.parseInt(metaData);
        } catch (Exception e) {

        }
        return argb;
    }

    private static String readMetadata(@NonNull Context context, @NonNull String key, @NonNull String def) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            String value = bundle.getString(key);
            if (!TextUtils.isEmpty(value)) return value;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return def;
    }
}
