package com.voxeet.toolkit.incoming.manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.voxeet.push.center.NotificationCenterFactory;
import com.voxeet.push.center.invitation.InvitationBundle;
import com.voxeet.sdk.VoxeetSdk;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.SessionService;

import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.PromiseSolver;
import eu.codlab.simplepromise.solve.Solver;

public class DismissNotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = DismissNotificationBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        ConferenceService conferenceService = VoxeetSdk.conference();
        InvitationBundle invitationBundle = null;

        if (null == conferenceService) return;

        if (null != bundle) invitationBundle = new InvitationBundle(bundle);

        if (null != invitationBundle && null != invitationBundle.conferenceId) {
            InvitationBundle finalInvitationBundle = invitationBundle;
            createPromise(invitationBundle.conferenceId).then(new PromiseExec<Boolean, Object>() {
                @Override
                public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                    Log.d(TAG, "onCall done");
                    NotificationCenterFactory.instance.onInvitationCanceledReceived(context, finalInvitationBundle.asMap(),
                            Build.MANUFACTURER, Build.VERSION.SDK_INT);
                }
            }).error(new ErrorPromise() {
                @Override
                public void onError(@NonNull Throwable error) {
                    NotificationCenterFactory.instance.onInvitationCanceledReceived(context, finalInvitationBundle.asMap(),
                            Build.MANUFACTURER, Build.VERSION.SDK_INT);
                }
            });
        }
    }

    private Promise<Boolean> createPromise(@NonNull String conferenceId) {
        ConferenceService conferenceService = VoxeetSdk.conference();
        SessionService sessionService = VoxeetSdk.session();
        if (null == sessionService || null == conferenceService) {
            return new Promise<>(new PromiseSolver<Boolean>() {
                @Override
                public void onCall(@NonNull Solver<Boolean> solver) {
                    solver.reject(new IllegalStateException("SDK Uninitialized in " + DismissNotificationBroadcastReceiver.class.getSimpleName()));
                }
            });
        }

        if (sessionService.isSocketOpen()) {
            return conferenceService.decline(conferenceId);
        } else {
            return new Promise<>(new PromiseSolver<Boolean>() {
                @Override
                public void onCall(@NonNull final Solver<Boolean> solver) {
                    sessionService.open().then(new PromiseExec<Boolean, Object>() {
                        @Override
                        public void onCall(@Nullable Boolean result, @NonNull Solver internal_solver) {
                            conferenceService.decline(conferenceId).then(new PromiseExec<Boolean, Object>() {
                                @Override
                                public void onCall(@Nullable Boolean result, @NonNull Solver<Object> internal_solver) {
                                    solver.resolve(result);
                                }
                            }).error(new ErrorPromise() {
                                @Override
                                public void onError(@NonNull Throwable error) {
                                    solver.reject(error);
                                }
                            });
                        }
                    }).error(new ErrorPromise() {
                        @Override
                        public void onError(@NonNull Throwable error) {
                            solver.reject(error);
                        }
                    });
                }
            });
        }
    }
}
