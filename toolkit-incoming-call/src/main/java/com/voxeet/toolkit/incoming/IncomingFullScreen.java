package com.voxeet.toolkit.incoming;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.WindowManager;

import com.voxeet.push.center.invitation.IIncomingInvitationListener;
import com.voxeet.push.center.invitation.InvitationBundle;
import com.voxeet.push.center.management.Constants;
import com.voxeet.sdk.factories.VoxeetIntentFactory;
import com.voxeet.sdk.utils.AndroidManifest;

public class IncomingFullScreen implements IIncomingInvitationListener {

    public static final String[] DEFAULT_NOTIFICATION_KEYS = new String[]{
            Constants.INVITER_ID,
            Constants.INVITER_NAME,
            Constants.NOTIF_TYPE,
            Constants.INVITER_EXTERNAL_ID,
            Constants.INVITER_URL,
            Constants.CONF_ID
    };

    @NonNull
    private Class<? extends Activity> incomingCallClass;

    private IncomingFullScreen() {

    }

    public IncomingFullScreen(@NonNull Class<? extends Activity> incomingCallClass) {
        this();

        this.incomingCallClass = incomingCallClass;
    }

    @SuppressLint("WrongConstant")
    @Override

    public void onInvitation(@NonNull Context context, @NonNull InvitationBundle invitationBundle) {

        String voxeet_default_incoming = AndroidManifest.readMetadata(context, "voxeet_incoming_class", null);
        Log.d("NotificationCenterFactory", "onInvitation: " + voxeet_default_incoming);

        Bundle extra = invitationBundle.asBundle();

        Class<?> newClass = VoxeetIntentFactory.createClass(voxeet_default_incoming);
        if (null == newClass) newClass = this.incomingCallClass;

        Intent intent = new Intent();
        intent.setClass(context, newClass);

        for (String key : DEFAULT_NOTIFICATION_KEYS) {
            if (extra.containsKey(key)) {
                intent.putExtra(key, extra.getString(key));
            }
        }

        //force conference join since it is a push
        intent.putExtra("join", true);
        intent.putExtra("callMode", 0x0001);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        context.startActivity(intent);
    }

    @Override
    public void onInvitationCanceled(@NonNull Context context, @NonNull String conferenceId) {
        //in this mode, the parent call will send event
    }
}
