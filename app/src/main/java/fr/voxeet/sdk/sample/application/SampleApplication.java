package fr.voxeet.sdk.sample.application;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.voxeet.toolkit.activities.notification.DefaultIncomingCallActivity;
import com.voxeet.toolkit.application.VoxeetApplication;
import com.voxeet.toolkit.controllers.VoxeetToolkit;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.utils.EventDebugger;

import org.greenrobot.eventbus.EventBus;

import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.PromiseSolver;
import eu.codlab.simplepromise.solve.Solver;
import fr.voxeet.sdk.sample.BuildConfig;
import voxeet.com.sdk.core.FirebaseController;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.preferences.VoxeetPreferences;
import voxeet.com.sdk.json.UserInfo;

/**
 * Created by RomainBenmansour on 06,April,2016
 */
public class SampleApplication extends VoxeetApplication {
    /**
     * When testing the OAuth feature, please change the USE_SDK_OAUTH_URL from the gradle.properties
     * file to a valid String value
     */
    private final static String USE_SDK_OAUTH_URL = BuildConfig.USE_SDK_OAUTH_URL;

    private static final int ONE_MINUTE = 60 * 1000;

    private static final String TAG = SampleApplication.class.getSimpleName();

    private UserInfo _current_user;
    private EventDebugger mEventDebugger;
    private boolean sdkInitialized;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: starting Voxeet Sample");

        sdkInitialized = false;

        mEventDebugger = new EventDebugger();
        mEventDebugger.register();

        VoxeetToolkit.initialize(this, EventBus.getDefault())
                .enableOverlay(true);

        FirebaseController.getInstance().enable(true);

        //change the overlay used by default
        VoxeetToolkit.getInstance().getConferenceToolkit().setScreenShareEnabled(true)
                .setDefaultOverlayState(OverlayState.EXPANDED);

        //the default case of this SDK is to have the SDK with consumerKey and consumerSecret embedded
        uniqueInitializeSDK().then(new PromiseExec<Boolean, Object>() {
            @Override
            public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                Log.d(TAG, "onCall: SDK initialized using keys");
            }
        }).error(new ErrorPromise() {
            @Override
            public void onError(@NonNull Throwable error) {
                error.printStackTrace();
            }
        });
    }

    /**
     * Select an user, tells the sdk to wether validate or
     * log the selected user
     *
     * @param user_info The user info selected in our UI
     * @return true if it was the first log
     */
    public boolean selectUser(UserInfo user_info) {
        //first case, the user was disconnected
        _current_user = user_info;
        if (_current_user == null || !sdkInitialized) {
            logSelectedUser();
        } else {
            //we have an user
            VoxeetSdk.getInstance()
                    .logout()
                    .then(new PromiseExec<Boolean, Object>() {
                        @Override
                        public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                            Log.d(TAG, "onCall: user disconnected");
                            logSelectedUser();
                        }
                    })
                    .error(new ErrorPromise() {
                        @Override
                        public void onError(Throwable error) {
                            logSelectedUser();
                        }
                    });
        }
        return false;
    }

    /**
     * Call this method to log the current selected user
     */
    public void logSelectedUser() {
        VoxeetSdk.getInstance().logUserWithChain(_current_user)
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

    public UserInfo getCurrentUser() {
        return _current_user;
    }


    /**
     * For the example, this method will return a promise compatible with the two different type of usage
     * of this SDK
     *
     * @return a promise in case of the OAuth use case, null otherwise
     */
    @NonNull
    @Override
    public Promise<Boolean> uniqueInitializeSDK() {
        return new Promise<>(new PromiseSolver<Boolean>() {
            @Override
            public void onCall(@NonNull Solver<Boolean> solver) {
                VoxeetSdk.initialize(SampleApplication.this,
                        BuildConfig.CONSUMER_KEY,
                        BuildConfig.CONSUMER_SECRET,
                        _current_user); //can be null - will be removed in a later version

                onSdkInitialized();
                solver.resolve(true);
            }
        });
    }

    private void onSdkInitialized() {
        VoxeetSdk.getInstance().getConferenceService().setTimeOut(ONE_MINUTE);

        //it's possible to use the meta-data in the AndroidManifest to directly control the default incoming activity
        VoxeetPreferences.setDefaultActivity(DefaultIncomingCallActivity.class.getCanonicalName());

        //register the Application and add at least one subscriber
        VoxeetSdk.getInstance().register(this, this);

        sdkInitialized = true;
    }
}
