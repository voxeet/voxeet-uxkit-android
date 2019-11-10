package com.voxeet.toolkit.application;

import android.app.Application;
import android.support.annotation.NonNull;

import com.voxeet.sdk.VoxeetSdk;
import com.voxeet.sdk.json.UserInfo;
import com.voxeet.sdk.preferences.VoxeetPreferences;
import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;

import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.PromiseSolver;
import eu.codlab.simplepromise.solve.Solver;


/**
 * This class guide the users to implement the SDK following the standard features
 */
@Annotate
public abstract class VoxeetApplication extends Application {

    @NoDocumentation
    public VoxeetApplication() {
        super();
    }

    /**
     * Initialize the SDK. This promise is used by the DefaultiIncomingActivity for instance
     * <p>
     * This way, it can make sure that the SDK is properly initialized when using the various features
     * of the Voxeet SDK
     * <p>
     * TODO make every call to this method to possibly wait for any other attempt to call it
     * In fact, for now, every attempt to use this method will lead to creating 1 call
     * So... many parallel calls for the "token" based implementation could lead to many attempts
     * We should barrier calls in such case !
     * Such implementation will, in the f
     *
     * @return a valid promise which will initialize the SDK
     */
    @NonNull
    protected abstract Promise<Boolean> uniqueInitializeSDK();

    /**
     * Initialize the SDK. This promise is used by the DefaultIncomingActivity for instance
     *
     * This way, it can make sure that the SDK is properly initialized when using the various features
     * of the Voxeet SDK
     *
     * @return a valid promise which will initialize the SDK
     */

    /**
     * Initialize the SDK. This promise is used by the DefaultIncomingActivity for instance
     * <p>
     * TODO make every call to this method to possibly wait for any other attempt to call it
     * In fact, for now, every attempt to use this method will lead to creating 1 call
     * So... many parallel calls for the "token" based implementation could lead to many attempts
     * We should barrier calls in such case !
     *
     * @return a valid promise which will initialize the SDK
     */
    public Promise<Boolean> initializeSDK() {
        return new Promise<>(new PromiseSolver<Boolean>() {
            @Override
            public void onCall(@NonNull Solver<Boolean> solver) {
                //TODO as said right above, for now, the implementation does not check for multiple calls
                //TODO append the current 'solver' in a list and call uniqueInitialSDK if no solver existed
                //when the uniqueInitialize resoles, simply flush the awaiting 'solver's
                solver.resolve(uniqueInitializeSDK());
            }
        });
    }

    /**
     * Check if the SDK knows a default user. Useful to auto connect users
     *
     * @return the existence of an user that has been logged in but not logged out in the past
     */
    public boolean hasDefaultUser() {
        UserInfo savedUserInfo = VoxeetPreferences.getSavedUserInfo();
        return null != savedUserInfo;
    }

    /**
     * Log the default user saved on this device
     * <p>
     * TODO make every call to this method to possibly wait for any other attempt to call it
     * In fact, for now, every attempt to use this method will lead to creating 1 call
     * So... many parallel calls for the "token" based implementation could lead to many attempts
     * We should barrier calls in such case !
     *
     * @return a valid promise which will log the user
     */
    public Promise<Boolean> logSavedUser() {
        //TODO as said right above, for now, the implementation does not check for multiple calls
        //TODO append the current 'solver' in a list and call logUserWithChain if no solver existed
        UserInfo userInfos = VoxeetPreferences.getSavedUserInfo();
        if (null != userInfos)
            return VoxeetSdk.session().open(userInfos);
        return new Promise<>(new PromiseSolver<Boolean>() {
            @Override
            public void onCall(@NonNull Solver<Boolean> solver) {
                solver.resolve(true);
            }
        });
    }
}
