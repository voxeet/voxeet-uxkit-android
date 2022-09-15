package com.voxeet.uxkit.incoming.manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.voxeet.VoxeetSDK;
import com.voxeet.promise.Promise;
import com.voxeet.sdk.exceptions.VoxeetSDKNotInitializedException;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.push.center.NotificationCenter;
import com.voxeet.sdk.push.center.invitation.InvitationBundle;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.NotificationService;
import com.voxeet.sdk.services.SessionService;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

public class DismissNotificationBroadcastReceiver extends BroadcastReceiver {

    private final static ShortLogger Log = UXKitLogger.createLogger(DismissNotificationBroadcastReceiver.class.getSimpleName());
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        InvitationBundle invitationBundle = null;

        if (null != bundle) invitationBundle = new InvitationBundle(bundle);

        //TODO create a way to register for dismissed conference invitation when the sdk is unintialized
        if (null != invitationBundle && null != invitationBundle.conferenceId) {
            InvitationBundle finalInvitationBundle = invitationBundle;
            createPromise(invitationBundle.conferenceId).then((result, solver) -> {
                Log.d("sending onInvitationCanceledReceived");
                NotificationCenter.instance.onInvitationCanceledReceived(context, finalInvitationBundle.conferenceId);
            }).error(error -> NotificationCenter.instance.onInvitationCanceledReceived(context, finalInvitationBundle.conferenceId));
        } else {
            Log.d("invalid bundle or bundle.conferenceId");
        }
    }

    private Promise<Boolean> createPromise(@NonNull String conferenceId) {
        NotificationService notificationService = VoxeetSDK.notification();
        ConferenceService conferenceService = VoxeetSDK.conference();
        SessionService sessionService = VoxeetSDK.session();
        if (!VoxeetSDK.instance().isInitialized()) {
            return new Promise<>(solver -> solver.reject(new VoxeetSDKNotInitializedException("SDK Uninitialized in " + DismissNotificationBroadcastReceiver.class.getSimpleName())));
        }

        Conference conference = conferenceService.getConference(conferenceId);
        if (sessionService.isOpen()) {
            return notificationService.decline(conference);
        } else {
            //TODO refactor with open decline resolve error
            return new Promise<>(solver -> sessionService.open().then((result, internal_solver) -> notificationService.decline(conference)
                    .then((result1, internal_solver1) -> solver.resolve(result1))
                    .error(solver::reject)
            ).error(solver::reject));
        }
    }
}
