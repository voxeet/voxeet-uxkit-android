package com.voxeet.toolkit.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.voxeet.promise.solve.ErrorPromise;
import com.voxeet.promise.solve.PromiseExec;
import com.voxeet.promise.solve.Solver;
import com.voxeet.sdk.VoxeetSdk;
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
import com.voxeet.toolkit.service.AbstractSDKService;
import com.voxeet.toolkit.service.SDKBinder;
import com.voxeet.toolkit.service.SystemServiceFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
@Annotate
public class VoxeetAppCompatActivity extends AppCompatActivity implements IVoxeetActivity {

    private static final String TAG = VoxeetAppCompatActivity.class.getSimpleName();
    private IncomingBundleChecker mIncomingBundleChecker;

    @Nullable
    private AbstractSDKService sdkService;

    @NoDocumentation
    public VoxeetAppCompatActivity() {
        super();
    }


    @Nullable
    public AbstractSDKService getSdkService() {
        return sdkService;
    }

    public void onSdkServiceAvailable() {
        //nothing, override to change
    }

    @NoDocumentation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create a check incoming call
        mIncomingBundleChecker = new IncomingBundleChecker(getIntent(), null);

        startService();
    }

    @NoDocumentation
    @Override
    protected void onResume() {
        super.onResume();

        SystemServiceFactory.setLastAppCompatActivity(this.getClass());
        startService();

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

        if(null != VoxeetToolkit.instance()) {
            //to prevent uninitialized toolkit but ... it's highly recommended for future releases to always init
            VoxeetToolkit.instance().getConferenceToolkit().forceReattach();
        }
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
    public final void onEvent(ConferenceStatusUpdatedEvent event) {
        mIncomingBundleChecker.flushIntent();
        onConferenceState(event);
    }

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

    private void startService() {
        try {
            if (SystemServiceFactory.hasSDKServiceClass()) {
                Intent intent = new Intent(this, SystemServiceFactory.getSDKServiceClass());
                startService(intent);
                bindService(intent, sdkConnection, Context.BIND_AUTO_CREATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection sdkConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            if (binder instanceof SDKBinder) {
                try {
                    sdkService = ((SDKBinder) binder).getService();
                    if (null != sdkService) {
                        onSdkServiceAvailable();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            sdkService = null;
        }
    };
}
