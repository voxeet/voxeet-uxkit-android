package com.voxeet.toolkit.youtube.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.voxeet.sdk.VoxeetSdk;
import com.voxeet.sdk.events.error.PermissionRefusedEvent;
import com.voxeet.sdk.events.sdk.ConferenceStateEvent;
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

import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;

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

        if (null != VoxeetSdk.instance()) {
            VoxeetSdk.instance().register(this);
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

        if (null != VoxeetSdk.screenShare()) {
            VoxeetSdk.screenShare().consumeRightsToScreenShare();
        }

        VoxeetToolkit.getInstance().getConferenceToolkit().forceReattach();
    }

    @NoDocumentation
    @Override
    protected void onPause() {
        if (null != VoxeetSdk.localStats()) {
            //stop fetching stats if any pending
            if (!VoxeetSdk.conference().isLive()) {
                VoxeetSdk.localStats().stopAutoFetch();
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
                if (null != VoxeetSdk.conference() && VoxeetSdk.conference().isLive()) {
                    VoxeetSdk.conference().startVideo()
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

        if (null != VoxeetSdk.screenShare()) {
            managed = VoxeetSdk.screenShare().onActivityResult(requestCode, resultCode, data);
        }

        if (!managed) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RequestScreenSharePermissionEvent event) {
        VoxeetSdk.screenShare()
                .sendUserPermissionRequest(this);
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Specific event used to manage the current "incoming" call feature
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onEvent(ConferenceStateEvent event) {
        mIncomingBundleChecker.flushIntent();
        onConferenceState(event);
    }

    @NoDocumentation
    protected void onConferenceState(@NonNull ConferenceStateEvent event) {

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
