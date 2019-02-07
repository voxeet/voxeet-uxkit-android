package com.voxeet.toolkit.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.voxeet.android.media.audio.AudioManager;
import com.voxeet.toolkit.activities.notification.IncomingBundleChecker;
import com.voxeet.toolkit.activities.notification.IncomingCallFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;
import voxeet.com.sdk.audio.SoundManager;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.services.AudioService;
import voxeet.com.sdk.core.services.ScreenShareService;
import voxeet.com.sdk.events.error.ConferenceJoinedError;
import voxeet.com.sdk.events.error.PermissionRefusedEvent;
import voxeet.com.sdk.events.success.ConferenceJoinedSuccessEvent;
import voxeet.com.sdk.events.success.ConferencePreJoinedEvent;
import voxeet.com.sdk.utils.Validate;

/**
 * VoxeetAppCompatActivity manages the call state
 * <p>
 * In the current merged state, this class is not used
 * <p>
 * However, it is extremely easy to use this class now :
 * - manages automatically the bundles to join conferences when "resumed"
 * - automatically registers its subclasses's extra info to propagate to "recreated" instances
 * <p>
 * Few things to consider :
 * - singleTop / singleInstance
 */

public class VoxeetAppCompatActivity extends AppCompatActivity {


    private static final String TAG = VoxeetAppCompatActivity.class.getSimpleName();
    private IncomingBundleChecker mIncomingBundleChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create a check incoming call
        mIncomingBundleChecker = new IncomingBundleChecker(getIntent(), null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null != VoxeetSdk.getInstance()) {
            VoxeetSdk.getInstance().register(this, this);
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this); //registering this activity
        }

        if (canBeRegisteredToReceiveCalls()) {
            IncomingCallFactory.setTempAcceptedIncomingActivity(this.getClass());
            IncomingCallFactory.setTempExtras(getIntent().getExtras());
        }

        if (mIncomingBundleChecker.isBundleValid()) {
            mIncomingBundleChecker.onAccept();
        }

        if (null != VoxeetSdk.getInstance()) {
            VoxeetSdk.getInstance().getScreenShareService().consumeRightsToScreenShare();
        }
    }

    @Override
    protected void onPause() {
        if (null != VoxeetSdk.getInstance()) {
            //stop fetching stats if any pending
            if (!VoxeetSdk.getInstance().getConferenceService().isLive()) {
                VoxeetSdk.getInstance().getLocalStatsService().stopAutoFetch();
            } else {
                SoundManager manager = AudioService.getSoundManager();
                if(null != manager) {
                    manager.requestAudioFocus();
                }
            }
        }

        EventBus.getDefault().unregister(this);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mIncomingBundleChecker = new IncomingBundleChecker(intent, null);
        if (mIncomingBundleChecker.isBundleValid()) {
            mIncomingBundleChecker.onAccept();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull PermissionRefusedEvent event) {
        switch (event.getPermission()) {
            case CAMERA:
            case MICROPHONE:
                Validate.requestMandatoryPermissions(this,
                        event.getPermission().getPermissions(),
                        event.getPermission().getRequestCode());
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case PermissionRefusedEvent.RESULT_CAMERA: {
                Log.d(TAG, "onActivityResult: camera is ok now");
                if (null != VoxeetSdk.getInstance() && VoxeetSdk.getInstance().getConferenceService().isLive()) {
                    VoxeetSdk.getInstance().getConferenceService().startVideo()
                            .then(new PromiseExec<Boolean, Object>() {
                                @Override
                                public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {

                                }
                            })
                            .error(new ErrorPromise() {
                                @Override
                                public void onError(@NonNull Throwable error) {
                                    error.printStackTrace();
                                }
                            });
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean managed = false;

        if (null != VoxeetSdk.getInstance()) {
            managed = VoxeetSdk.getInstance().getScreenShareService().onActivityResult(requestCode, resultCode, data);
        }

        if (!managed) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ScreenShareService.RequestScreenSharePermissionEvent event) {
        VoxeetSdk.getInstance().getScreenShareService()
                .sendUserPermissionRequest(this);
    }

    //those two methods were here to manage sound type
    //no longer required
    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onEven(ConferencePreJoinedEvent event) {
        setVolumeControlStream(SoundManager.STREAM_VOICE_CALL);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPushEvent event) {
        mIncomingBundleChecker.flushIntent();

        setVolumeControlStream(SoundManager.USE_DEFAULT_STREAM_TYPE);
    }*/

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Specific event used to manage the current "incoming" call feature
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferencePreJoinedEvent event) {
        mIncomingBundleChecker.flushIntent();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceJoinedSuccessEvent event) {
        mIncomingBundleChecker.flushIntent();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceJoinedError event) {
        mIncomingBundleChecker.flushIntent();
    }

    /**
     * Get the current voxeet bundle checker
     * <p>
     * usefull to retrieve info about the notification (if such)
     * - user name
     * - avatar url
     * - conference id
     * - user id
     * - external user id
     * - extra bundle (custom)
     *
     * @return a nullable object
     */
    @Nullable
    protected IncomingBundleChecker getExtraVoxeetBundleChecker() {
        return mIncomingBundleChecker;
    }

    /**
     * Method called during the onResume of this
     *
     * @return true by default, override to change behaviour
     */
    protected boolean canBeRegisteredToReceiveCalls() {
        return true;
    }
}
