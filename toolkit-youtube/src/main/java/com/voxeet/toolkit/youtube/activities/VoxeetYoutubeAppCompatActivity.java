package com.voxeet.toolkit.youtube.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.voxeet.VoxeetSDK;
import com.voxeet.promise.solve.ErrorPromise;
import com.voxeet.promise.solve.PromiseExec;
import com.voxeet.promise.solve.Solver;
import com.voxeet.sdk.events.error.PermissionRefusedEvent;
import com.voxeet.sdk.events.sdk.ConferenceStatusUpdatedEvent;
import com.voxeet.sdk.services.screenshare.RequestScreenSharePermissionEvent;
import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;
import com.voxeet.sdk.utils.Validate;
import com.voxeet.toolkit.activities.notification.IncomingBundleChecker;
import com.voxeet.toolkit.controllers.VoxeetToolkit;
import com.voxeet.toolkit.incoming.factory.IVoxeetActivity;
import com.voxeet.toolkit.incoming.factory.IncomingCallFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * VoxeetYoutubeAppCompatActivity manages the call state
 * This class is to be used in the context of any requirement to be able to play Youtube URL
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
@Annotate
public class VoxeetYoutubeAppCompatActivity extends YouTubeBaseActivity implements IVoxeetActivity {


    private static final String TAG = VoxeetYoutubeAppCompatActivity.class.getSimpleName();
    private IncomingBundleChecker mIncomingBundleChecker;

    @NoDocumentation
    public VoxeetYoutubeAppCompatActivity() {
        super();
    }

    @NoDocumentation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create a check incoming call
        mIncomingBundleChecker = new IncomingBundleChecker(getIntent(), null);
    }

    @NoDocumentation
    @Override
    protected void onResume() {
        super.onResume();

        if (null != VoxeetSDK.instance()) {
            VoxeetSDK.instance().register(this);
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

        if (null != VoxeetSDK.screenShare()) {
            VoxeetSDK.screenShare().consumeRightsToScreenShare();
        }

        VoxeetToolkit.instance().getConferenceToolkit().forceReattach();
    }

    @NoDocumentation
    @Override
    protected void onPause() {
        if (null != VoxeetSDK.localStats()) {
            //stop fetching stats if any pending
            if (!VoxeetSDK.conference().isLive()) {
                VoxeetSDK.localStats().stopAutoFetch();
            }
        }

        EventBus.getDefault().unregister(this);

        super.onPause();
    }

    @NoDocumentation
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

    @NoDocumentation
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case PermissionRefusedEvent.RESULT_CAMERA: {
                Log.d(TAG, "onActivityResult: camera is ok now");
                if (null != VoxeetSDK.conference() && VoxeetSDK.conference().isLive()) {
                    VoxeetSDK.conference().startVideo()
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

    @NoDocumentation
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean managed = false;

        if (null != VoxeetSDK.screenShare()) {
            managed = VoxeetSDK.screenShare().onActivityResult(requestCode, resultCode, data);
        }

        if (!managed) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RequestScreenSharePermissionEvent event) {
        VoxeetSDK.screenShare()
                .sendUserPermissionRequest(this);
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Specific event used to manage the current "incoming" call feature
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onEvent(ConferenceStatusUpdatedEvent event) {
        mIncomingBundleChecker.flushIntent();
        onConferenceState(event);
    }

    @NoDocumentation
    protected void onConferenceState(@NonNull ConferenceStatusUpdatedEvent event) {

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
