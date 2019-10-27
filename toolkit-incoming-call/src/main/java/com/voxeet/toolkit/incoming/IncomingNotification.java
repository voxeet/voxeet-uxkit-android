package com.voxeet.toolkit.incoming;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.voxeet.push.center.invitation.IIncomingInvitationListener;
import com.voxeet.push.center.invitation.InvitationBundle;
import com.voxeet.push.firebase.FirebaseController;
import com.voxeet.sdk.utils.AndroidManifest;
import com.voxeet.toolkit.incoming.factory.IVoxeetActivity;
import com.voxeet.toolkit.incoming.factory.IncomingCallFactory;
import com.voxeet.toolkit.incoming.manifest.DismissNotificationBroadcastReceiver;

import java.security.SecureRandom;

public class IncomingNotification implements IIncomingInvitationListener {
    public final static int INCOMING_NOTIFICATION_REQUEST_CODE = 928;
    private static final String TAG = IncomingNotification.class.getSimpleName();
    public final static String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";

    private SecureRandom random;
    private int notificationId = -1;

    public IncomingNotification() {
        random = new SecureRandom();
    }

    @Override
    public void onInvitation(@NonNull Context context, @NonNull InvitationBundle invitationBundle) {
        notificationId = random.nextInt(Integer.MAX_VALUE / 2);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = FirebaseController.getChannelId(context);

        Intent accept = createIntent(context, invitationBundle);
        Intent dismiss = createDismissIntent(context, invitationBundle);
        dismiss.putExtra(EXTRA_NOTIFICATION_ID, notificationId);

        if (null == accept) {
            Log.d(TAG, "onInvitation: accept intent is null !! did you set the voxeet_incoming_accepted_class prop");
            return;
        }

        PendingIntent pendingIntentAccepted = PendingIntent.getActivity(context, INCOMING_NOTIFICATION_REQUEST_CODE, accept, 0);
        PendingIntent pendingIntentDismissed = PendingIntent.getBroadcast(context, INCOMING_NOTIFICATION_REQUEST_CODE, dismiss, PendingIntent.FLAG_UPDATE_CURRENT);

        String inviterName = !TextUtils.isEmpty(invitationBundle.inviterName) ? invitationBundle.inviterName : "";

        Notification lastNotification = new NotificationCompat.Builder(context, channelId)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(context.getString(R.string.voxeet_incoming_notification_from_user, inviterName))
                .setContentText(context.getString(R.string.voxeet_incoming_notification_accept))
                .setSmallIcon(R.drawable.ic_incoming_call_notification)
                .addAction(R.drawable.ic_incoming_call_dismiss, context.getString(R.string.voxeet_incoming_notification_button_dismiss), pendingIntentDismissed)
                .addAction(R.drawable.ic_incoming_call_accept, context.getString(R.string.voxeet_incoming_notification_button_accept), pendingIntentAccepted)
                .setAutoCancel(true)
                .setOngoing(true)
                .build();
        //TODO Android Use Full Screen Intent with according permission -> possible improvement

        notificationManager.notify(notificationId, lastNotification);

    }

    @Nullable
    private Intent createIntent(@NonNull Context context, @NonNull InvitationBundle invitationBundle) {
        Bundle extra = invitationBundle.asBundle();

        Class<? extends IVoxeetActivity> klass = IncomingCallFactory.getAcceptedIncomingActivityKlass();
        if (null == klass) {
            String klass_fully_qualified = AndroidManifest.readMetadata(context, "voxeet_incoming_accepted_class", null);
            if (null != klass_fully_qualified) {
                try {
                    klass = (Class<? extends IVoxeetActivity>) Class.forName(klass_fully_qualified);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        //we have an invalid klass, returning null
        if (null == klass) return null;

        Intent intent = new Intent(context, klass);

        //inject the extras from the current "loaded" activity
        Bundle extras = IncomingCallFactory.getAcceptedIncomingActivityExtras();
        if (null != extras) {
            intent.putExtras(extras);
        }

        for (String key : IncomingFullScreen.DEFAULT_NOTIFICATION_KEYS) {
            if (extra.containsKey(key)) {
                intent.putExtra(key, extra.getString(key));
            }
        }

        return intent;
    }

    @NonNull
    private Intent createDismissIntent(@NonNull Context context, @NonNull InvitationBundle invitationBundle) {
        Bundle extra = invitationBundle.asBundle();
        Intent intent = new Intent(context, DismissNotificationBroadcastReceiver.class);

        for (String key : IncomingFullScreen.DEFAULT_NOTIFICATION_KEYS) {
            if (extra.containsKey(key)) {
                intent.putExtra(key, extra.getString(key));
            }
        }

        return intent;
    }

    @Override
    public void onInvitationCanceled(@NonNull Context context, @NonNull String conferenceId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (-1 != notificationId) notificationManager.cancel(notificationId);
        notificationId = 0;
    }
}
