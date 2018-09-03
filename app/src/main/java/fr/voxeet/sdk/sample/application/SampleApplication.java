package fr.voxeet.sdk.sample.application;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.PromiseInOut;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.PromiseSolver;
import eu.codlab.simplepromise.solve.Solver;
import fr.voxeet.sdk.sample.BuildConfig;
import fr.voxeet.sdk.sample.Recording;
import fr.voxeet.sdk.sample.activities.IncomingCallActivity;
import fr.voxeet.sdk.sample.activities.MainActivity;
import fr.voxeet.sdk.sample.oauth.OAuthCalls;
import fr.voxeet.sdk.sample.oauth.OAuthCallsFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sdk.voxeet.com.toolkit.activities.notification.IncomingCallFactory;
import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.utils.EventDebugger;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.preferences.VoxeetPreferences;
import voxeet.com.sdk.json.UserInfo;

/**
 * Created by RomainBenmansour on 06,April,2016
 */
public class SampleApplication extends MultiDexApplication {
    /**
     * When testing the OAuth feature, please change the USE_SDK_OAUTH_URL from the gradle.properties
     * file to a valid String value
     */
    private final static String USE_SDK_OAUTH_URL = BuildConfig.USE_SDK_OAUTH_URL;

    private static final int ONE_MINUTE = 60 * 1000;

    private static final String TAG = SampleApplication.class.getSimpleName();

    @NonNull
    private List<Recording> recordedConference = new ArrayList<>();
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


        //change the overlay used by default
        VoxeetToolkit.getInstance().getConferenceToolkit().setScreenShareEnabled(true);
        VoxeetToolkit.getInstance().getConferenceToolkit().setDefaultOverlayState(OverlayState.EXPANDED);
        VoxeetToolkit.getInstance().getReplayMessageToolkit().setDefaultOverlayState(OverlayState.EXPANDED);

        //the default case of this SDK is to have the SDK with consumerKey and consumerSecret embedded
        if (!useOauthFeature()) {
            initializeSdk();
        }
    }

    public void saveRecordingConference(Recording newRecording) {
        for (Recording recording : recordedConference) {
            if (recording.conferenceId.equalsIgnoreCase(newRecording.conferenceId))
                return;
        }

        recordedConference.add(newRecording);
    }

    @NonNull
    public List<Recording> getRecordedConferences() {
        return recordedConference;
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
            if (!useOauthFeature()) {
                logSelectedUser();
            } else {
                //if we are in a OAuth management, we must wait for the initialization
                //and then, log the user !
                initializeSdk().then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                        logSelectedUser();
                    }
                }).error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        error.printStackTrace();
                        Toast.makeText(SampleApplication.this, "Error while retrieving the accessToken or login ; please see logs", Toast.LENGTH_SHORT).show();
                    }
                });
            }
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
        VoxeetSdk.getInstance().logUser(_current_user);
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
    private PromiseInOut<String, Boolean> initializeSdk() {
        if (!useOauthFeature()) {

            //prevent close from
            //init the SDK
            VoxeetSdk.initialize(this,
                    BuildConfig.CONSUMER_KEY,
                    BuildConfig.CONSUMER_SECRET,
                    _current_user); //can be null - will be removed in a later version

            onSdkInitialized();

            //we do not need to check anythin in this case
            return null;
        } else {
            return retrieveAccessToken().then(new PromiseExec<String, Boolean>() {
                @Override
                public void onCall(@Nullable String accessToken, @NonNull Solver<Boolean> solver) {

                    VoxeetSdk.initialize(SampleApplication.this,
                            accessToken,
                            new Callable<String>() {
                                @Override
                                public String call() throws Exception {
                                    String token = createOAuthCalls().retrieveRefreshToken().toBlocking().single();
                                    //when using the Web Sample, the tokens are surrounded by "
                                    //we remove them here, but in production, it should be injected into your objects
                                    if (null != token) token = token.replaceAll("\"", "");
                                    return token;
                                }
                            },
                            _current_user); //can be null - will be removed in a later version

                    onSdkInitialized();

                    solver.resolve(true);
                }
            });
        }
    }

    private void onSdkInitialized() {
        VoxeetSdk.getInstance().getConferenceService().setTimeOut(ONE_MINUTE);

        VoxeetPreferences.setDefaultActivity(IncomingCallActivity.class.getCanonicalName());
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
     *
     * @return true if oauth must be used
     */
    private boolean useOauthFeature() {
        return false;//null != SampleApplication.USE_SDK_OAUTH_URL && SampleApplication.USE_SDK_OAUTH_URL.length() > 0;
    }

    /**
     * Create a new OAuthCalls proxy object
     *
     * @return a valid instance
     */
    private OAuthCalls createOAuthCalls() {
        return OAuthCallsFactory.createOAuthCalls(SampleApplication.USE_SDK_OAUTH_URL);
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
                Observable<String> call = createOAuthCalls().retrieveAccessToken();
                call.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<String>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                solver.reject(e);
                            }

                            @Override
                            public void onNext(String accessToken) {
                                //when using the Web Sample, the tokens are surrounded by "
                                //we remove them here, but in production, it should be injected into your objects
                                if (null != accessToken)
                                    accessToken = accessToken.replaceAll("\"", "");
                                solver.resolve(accessToken);
                            }
                        });
            }
        });
    }

}
