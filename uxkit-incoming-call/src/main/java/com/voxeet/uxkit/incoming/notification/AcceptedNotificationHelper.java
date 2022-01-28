package com.voxeet.uxkit.incoming.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.voxeet.sdk.push.center.invitation.InvitationBundle;
import com.voxeet.sdk.utils.AndroidManifest;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.activity.ActivityInfoHolder;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.incoming.utils.IncomingNotificationServiceHelper;

public class AcceptedNotificationHelper {

    private final static ShortLogger Log = UXKitLogger.createLogger(AcceptedNotificationHelper.class);

    public static String getIncomingAcceptedClass(@NonNull Context context) {
        return AndroidManifest.readMetadata(context, "voxeet_incoming_accepted_class", null);
    }

    public static Intent create(@NonNull Context context, @NonNull InvitationBundle invitationBundle) {
        Bundle extra = invitationBundle.asBundle();

        Class<? extends AppCompatActivity> klass = ActivityInfoHolder.getAcceptedIncomingActivityKlass();
        if (null == klass) {
            Log.d("createIntent: IncomingCallFactory.getAcceptedIncomingActivityKlass() is null");

            String klass_fully_qualified = getIncomingAcceptedClass(context);
            if (null != klass_fully_qualified) {
                try {
                    klass = (Class<? extends AppCompatActivity>) Class.forName(klass_fully_qualified);
                    Log.d("createIntent : obtained class " + klass.getSimpleName() + " to forward to");
                } catch (ClassNotFoundException e) {
                    Log.e("createIntent: " + klass_fully_qualified + " resolution issue", e);
                }
            }
        }

        //we have an invalid klass, returning null
        if (null == klass) {
            Log.d("klass is null, returning nothing");
            return null;
        }

        Intent intent = new Intent(context, klass);

        //inject the extras from the current "loaded" activity
        Bundle extras = ActivityInfoHolder.getAcceptedIncomingActivityExtras();
        if (null != extras) {
            intent.putExtras(extras);
        }

        for (String key : IncomingNotificationServiceHelper.DEFAULT_NOTIFICATION_KEYS) {
            if (extra.containsKey(key)) {
                intent.putExtra(key, extra.getString(key));
            }
        }

        return intent;
    }
}
