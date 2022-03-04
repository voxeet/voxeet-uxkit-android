package com.voxeet.uxkit.incoming;

import static com.voxeet.uxkit.incoming.AbstractIncomingNotificationService.DEFAULT_NOTIFICATION_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.voxeet.sdk.json.ParticipantInfo;
import com.voxeet.sdk.models.ParticipantNotification;
import com.voxeet.sdk.push.center.invitation.InvitationBundle;
import com.voxeet.sdk.utils.AndroidManifest;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.incoming.implementation.DefaultAndroid12BounceActivity;
import com.voxeet.uxkit.incoming.notification.NotificationBundle;
import com.voxeet.uxkit.incoming.utils.IncomingNotificationServiceHelper;

public abstract class AbstractIncomingNotificationIntentProvider {
    private final static String SDK_CHANNEL_ID = "voxeet_sdk_incoming_channel_id";
    public final static String DEFAULT_ID = "IncomingVideoConference";

    public final static int INCOMING_NOTIFICATION_REQUEST_CODE = 98;
    public final static String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";
    protected final Context context;
    private final ShortLogger log;


    public AbstractIncomingNotificationIntentProvider(@NonNull Context context,
                                                      @NonNull ShortLogger log) {
        this.context = context;
        this.log = log;
    }

    @NonNull
    protected abstract Class<? extends BroadcastReceiver> getAcceptedBroadcastReceiverClass();

    /**
     * For Android 12, it's not possible to use bounce activities anymore. So a workaround is used for now
     *
     * @return
     */
    @NonNull
    protected Class<? extends AppCompatActivity> getAcceptedBounceActivityForBroadcastReceiverClass() {
        return DefaultAndroid12BounceActivity.class;
    }

    @NonNull
    protected abstract Class<? extends BroadcastReceiver> getDismissedBroadcastReceiverClass();

    @Nullable
    protected abstract Class<? extends AppCompatActivity> getIncomingCallActivityClass();

    @Nullable
    public Intent createAcceptIntent(@NonNull Context context, @NonNull InvitationBundle invitationBundle) {
        Bundle extra = invitationBundle.asBundle();
        Intent intent = new Intent(context, getAcceptedBroadcastReceiverClass());
        log.d("createAcceptIntent for " + getAcceptedBroadcastReceiverClass().getSimpleName());
        pushKeysIntoIntent(extra, intent);

        return intent;
    }

    @Nullable
    public Intent createAcceptBounceActivity(@NonNull Context context, @NonNull InvitationBundle invitationBundle) {
        Bundle extra = invitationBundle.asBundle();
        Intent intent = new Intent(context, getAcceptedBounceActivityForBroadcastReceiverClass());
        log.d("createAcceptBounceActivity for " + getAcceptedBounceActivityForBroadcastReceiverClass().getSimpleName());
        pushKeysIntoIntent(extra, intent);
        intent.putExtra(DefaultAndroid12BounceActivity.FULLY_QUALIFIED_NAME, getAcceptedBroadcastReceiverClass().getCanonicalName());

        return intent;
    }

    @NonNull
    public Intent createDismissIntent(@NonNull Context context, @NonNull InvitationBundle invitationBundle) {
        Bundle extra = invitationBundle.asBundle();
        Intent intent = new Intent(context, getDismissedBroadcastReceiverClass());
        log.d("createAcceptIntent for " + getDismissedBroadcastReceiverClass().getSimpleName());
        pushKeysIntoIntent(extra, intent);

        return intent;
    }

    @Nullable
    public Intent createCallingIntent(@NonNull Context context, @NonNull InvitationBundle invitationBundle) {
        Bundle extra = invitationBundle.asBundle();
        Class<? extends AppCompatActivity> klass = getIncomingCallActivityClass();

        if (klass == null) {
            log.d("createAcceptIntent didn't get a non null getIncomingCallActivityClass()");
            return null;
        }

        Intent intent = new Intent(context, klass);
        log.d("createAcceptIntent for " + getIncomingCallActivityClass().getSimpleName());
        pushKeysIntoIntent(extra, intent);

        return intent;
    }

    private void pushKeysIntoIntent(@NonNull Bundle extra, @NonNull Intent intent) {
        try {
            for (String key : IncomingNotificationServiceHelper.DEFAULT_NOTIFICATION_KEYS) {
                if (extra.containsKey(key)) {
                    intent.putExtra(key, extra.getString(key));
                }
            }
        } catch (Throwable throwable) {
            log.e("pushKeysIntoIntent exception", throwable);
        }
    }

    @NonNull
    public String getChannelId(@NonNull Context context) {
        return Opt.of(AndroidManifest.readMetadata(context, SDK_CHANNEL_ID, DEFAULT_ID)).or(DEFAULT_ID);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @Nullable
    public NotificationBundle createNotification(InvitationBundle serviceInvitationBundle, boolean configurable) {
        int notificationId = DEFAULT_NOTIFICATION_ID;
        if (null != serviceInvitationBundle.conferenceId) {
            notificationId = serviceInvitationBundle.conferenceId.hashCode();
        }

        String channelId = getChannelId(context);

        Intent accept = createAcceptIntent(context, serviceInvitationBundle);
        Intent acceptForBounceActivity = createAcceptBounceActivity(context, serviceInvitationBundle);
        Intent dismiss = createDismissIntent(context, serviceInvitationBundle);
        Intent callingIntent = createCallingIntent(context, serviceInvitationBundle);

        if (null != accept) accept.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        if (null != acceptForBounceActivity) acceptForBounceActivity.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        if (null != dismiss) dismiss.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        if (null != callingIntent) callingIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);

        if (null == accept) {
            return null;
        }

        int flag = PendingIntent.FLAG_UPDATE_CURRENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flag |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntentAccepted = PendingIntent.getBroadcast(context, INCOMING_NOTIFICATION_REQUEST_CODE, accept, flag);
        PendingIntent pendingIntentDismissed = PendingIntent.getBroadcast(context, INCOMING_NOTIFICATION_REQUEST_CODE, dismiss, flag);
        PendingIntent pendingCallingIntent = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntentAccepted = PendingIntent.getActivity(context, INCOMING_NOTIFICATION_REQUEST_CODE, acceptForBounceActivity, flag);
        }

        if (null != callingIntent) {
            pendingCallingIntent = PendingIntent.getActivity(context, INCOMING_NOTIFICATION_REQUEST_CODE, callingIntent, flag);
        }

        String inviterName = Opt.of(serviceInvitationBundle.inviter).then(ParticipantNotification::getInfo).then(ParticipantInfo::getName).or("");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setContentTitle(context.getString(R.string.voxeet_incoming_notification_from_user, inviterName))
                .setContentText(context.getString(R.string.voxeet_incoming_notification_accept))
                .setSmallIcon(R.drawable.ic_incoming_call_notification)
                .addAction(R.drawable.ic_incoming_call_dismiss, context.getString(R.string.voxeet_incoming_notification_button_dismiss), pendingIntentDismissed)
                .addAction(R.drawable.ic_incoming_call_accept, context.getString(R.string.voxeet_incoming_notification_button_accept), pendingIntentAccepted)
                .setSound(getRingtoneUri());

        if (configurable) {
            builder.setOngoing(IncomingNotificationEnvironment.Configuration.IsOnGoing)
                    .setAutoCancel(IncomingNotificationEnvironment.Configuration.IsAutoCancel);
        }

        if (null != pendingCallingIntent) {
            log.d("pendingCallIntent is defined, a setFullScreenIntent will be registered with high priority");
            builder.setFullScreenIntent(pendingCallingIntent, true);
        }

        Notification notification = builder.build();

        log.d("returning NotificationBundle containing notificationId " + notificationId);
        return new NotificationBundle(notificationId, notification);
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .build();

            String channelId = getChannelId(context);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    context.getString(R.string.voxeet_incoming_notification_channel_name),
                    importance
            );
            channel.setDescription(context.getString(R.string.voxeet_incoming_notification_channel_description));
            channel.enableLights(true);
            channel.setLightColor(Color.WHITE);

            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            channel.setSound(getRingtoneUri(), audioAttributes);

            if (null != notificationManager) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    Uri getRingtoneUri() {
        //return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/raw/incoming_call");
    }

}
