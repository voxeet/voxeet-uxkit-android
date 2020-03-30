package com.voxeet.uxkit.incoming.manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.voxeet.VoxeetSDK;
import com.voxeet.promise.Promise;
import com.voxeet.sdk.push.center.NotificationCenter;
import com.voxeet.sdk.push.center.invitation.InvitationBundle;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.SessionService;

public class DismissNotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        ConferenceService conferenceService = VoxeetSDK.conference();
        InvitationBundle invitationBundle = null;

        if (null == conferenceService) return;

        if (null != bundle) invitationBundle = new InvitationBundle(bundle);

        if (null != invitationBundle && null != invitationBundle.conferenceId) {
            InvitationBundle finalInvitationBundle = invitationBundle;
            createPromise(invitationBundle.conferenceId).then((result, solver) -> {
                NotificationCenter.instance.onInvitationCanceledReceived(context, finalInvitationBundle.conferenceId);
            }).error(error -> NotificationCenter.instance.onInvitationCanceledReceived(context, finalInvitationBundle.conferenceId));
        }
    }

    private Promise<Boolean> createPromise(@NonNull String conferenceId) {
        ConferenceService conferenceService = VoxeetSDK.conference();
        SessionService sessionService = VoxeetSDK.session();
        if (null == sessionService || null == conferenceService) {
            return new Promise<>(solver -> solver.reject(new IllegalStateException("SDK Uninitialized in " + DismissNotificationBroadcastReceiver.class.getSimpleName())));
        }

        if (sessionService.isSocketOpen()) {
            return conferenceService.decline(conferenceId);
        } else {
            //TODO refactor with open decline resolve error
            return new Promise<>(solver -> sessionService.open().then((result, internal_solver) -> conferenceService.decline(conferenceId)
                    .then((result1, internal_solver1) -> solver.resolve(result1))
                    .error(solver::reject)
            ).error(solver::reject));
        }
    }
}
