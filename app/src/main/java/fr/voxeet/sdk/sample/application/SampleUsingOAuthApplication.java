package fr.voxeet.sdk.sample.application;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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
import fr.voxeet.sdk.sample.oauth.OAuthCalls;
import fr.voxeet.sdk.sample.oauth.OAuthCallsFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import voxeet.com.sdk.core.FirebaseController;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.preferences.VoxeetPreferences;
import voxeet.com.sdk.core.services.authenticate.token.RefreshTokenCallback;
import voxeet.com.sdk.core.services.authenticate.token.TokenCallback;
import voxeet.com.sdk.json.UserInfo;

public class SampleUsingOAuthApplication extends VoxeetApplication {
    /**
     * When testing the OAuth feature, please change the USE_SDK_OAUTH_URL from the gradle.properties
     * file to a valid String value
     */
    private final static String USE_SDK_OAUTH_URL = BuildConfig.USE_SDK_OAUTH_URL;

    private static final int ONE_MINUTE = 60 * 1000;

    private static final String TAG = SampleUsingOAuthApplication.class.getSimpleName();

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

        VoxeetToolkit.initialize(this, EventBus.getDefault());
        VoxeetToolkit.getInstance().enableOverlay(true);

        FirebaseController.getInstance().enable(true);

        //change the overlay used by default
        VoxeetToolkit.getInstance().getConferenceToolkit().setScreenShareEnabled(true);
        VoxeetToolkit.getInstance().getConferenceToolkit().setDefaultOverlayState(OverlayState.EXPANDED);
        VoxeetToolkit.getInstance().getReplayMessageToolkit().setDefaultOverlayState(OverlayState.EXPANDED);
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
            //if we are in a OAuth management, we must wait for the initialization
            //and then, log the user !
            uniqueInitializeSDK().then(new PromiseExec<Boolean, Object>() {
                @Override
                public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                    logSelectedUser();
                }
            }).error(new ErrorPromise() {
                @Override
                public void onError(@NonNull Throwable error) {
                    error.printStackTrace();
                    Toast.makeText(SampleUsingOAuthApplication.this, "Error while retrieving the accessToken or login ; please see logs", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //we have an user
            VoxeetSdk.getInstance()
                    .logout()
                    .then(new PromiseExec<Boolean, Object>() {
                        @Override
                        public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
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
        Log.d("MainActivity", "logSelectedUser " + _current_user.toString());
        VoxeetSdk.getInstance().logUserWithChain(_current_user)
                .then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                        Toast.makeText(SampleUsingOAuthApplication.this, "logged", Toast.LENGTH_SHORT).show();
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
                retrieveAccessToken().then(new PromiseExec<String, Boolean>() {
                    @Override
                    public void onCall(@Nullable String accessToken, @NonNull Solver<Boolean> internal_solver) {

                        VoxeetSdk.initialize(SampleUsingOAuthApplication.this,
                                accessToken,
                                new RefreshTokenCallback() {
                                    @Override
                                    public void onRequired(TokenCallback callback) {
                                        Call<String> call = createOAuthCalls().retrieveAccessToken();
                                        call.enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(Call<String> call, Response<String> response) {
                                                String body = response.body();
                                                if(null == body) body = "";
                                                else body = body.replaceAll("\"", "");
                                                callback.ok(body);
                                            }

                                            @Override
                                            public void onFailure(Call<String> call, Throwable t) {
                                                callback.error(t);
                                            }
                                        });
                                    }
                                },
                                _current_user); //can be null - will be removed in a later version

                        onSdkInitialized();

                        solver.resolve(true);
                    }
                }).error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        error.printStackTrace();
                    }
                });
            }
        });
    }

    private void onSdkInitialized() {
        VoxeetSdk.getInstance().getConferenceService().setTimeOut(ONE_MINUTE);

        VoxeetPreferences.setDefaultActivity(DefaultIncomingCallActivity.class.getCanonicalName());
        //register the Application and add at least one subscriber
        VoxeetSdk.getInstance().register(this, this);

        sdkInitialized = true;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                                                                                           *
     * OAuth specific features                                                                   *
     *                                                                                           *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * When you want to use the OAuth sample, please update your gradle.properties file with
     * the scheme://ip or scheme://domain name
     * <p>
     * Create a new OAuthCalls proxy object
     *
     * @return a valid instance
     */
    private OAuthCalls createOAuthCalls() {
        return OAuthCallsFactory.createOAuthCalls(SampleUsingOAuthApplication.USE_SDK_OAUTH_URL);
    }

    /**
     * Retrieve the current third party accessToken given the OAuthCalls interface
     *
     * @return
     */
    private Promise<String> retrieveAccessToken() {
        return new Promise<>(new PromiseSolver<String>() {
            @Override
            public void onCall(@NonNull Solver<String> solver) {
                Call<String> call = createOAuthCalls().retrieveAccessToken();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        //when using the Web Sample, the tokens are surrounded by "
                        //we remove them here, but in production, it should be injected into your objects
                        String accessToken = response.body();
                        if (null != accessToken)
                            accessToken = accessToken.replaceAll("\"", "");
                        solver.resolve(accessToken);
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable e) {
                        solver.reject(e);
                    }
                });
            }
        });
    }
}
