package com.voxeet.uxkit.incoming.implementation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.voxeet.sdk.push.center.invitation.InvitationBundle;
import com.voxeet.uxkit.common.activity.ActivityInfoHolder;
import com.voxeet.uxkit.common.activity.VoxeetCommonAppCompatActivityWrapper;
import com.voxeet.uxkit.incoming.notification.AcceptedNotificationHelper;
import com.voxeet.uxkit.incoming.utils.IncomingNotificationServiceHelper;

public class AcceptedNotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        InvitationBundle invitationBundle = new InvitationBundle(intent.getExtras());
        Intent createdIntent = AcceptedNotificationHelper.create(context, invitationBundle);

        VoxeetCommonAppCompatActivityWrapper wrapper = ActivityInfoHolder.getCurrentAcceptedIncomingCallActivity();

        // we also stop the registered service since we accepted the call
        IncomingNotificationServiceHelper.stopRegisteredIncomingNotificationServiceForAutoStop();

        if (null == wrapper) {
            context.startActivity(createdIntent);
            return;
        }

        // and bring to front
        wrapper.bringBackParent(context, createdIntent);
    }
}
