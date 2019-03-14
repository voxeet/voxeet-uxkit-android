package com.voxeet.toolkit.activities.notification;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.application.VoxeetApplication;
import com.voxeet.toolkit.utils.LoadLastSavedOverlayStateEvent;
import com.voxeet.toolkit.views.internal.rounded.RoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.PromiseSolver;
import eu.codlab.simplepromise.solve.Solver;
import voxeet.com.sdk.audio.SoundManager;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.preferences.VoxeetPreferences;
import voxeet.com.sdk.core.services.AudioService;
import voxeet.com.sdk.events.success.ConferenceDestroyedPushEvent;
import voxeet.com.sdk.events.success.ConferenceEndedEvent;
import voxeet.com.sdk.events.success.ConferencePreJoinedEvent;
import voxeet.com.sdk.events.success.DeclineConferenceResultEvent;
import voxeet.com.sdk.exceptions.ExceptionManager;
import voxeet.com.sdk.json.UserInfo;
import voxeet.com.sdk.utils.AndroidManifest;
import voxeet.com.sdk.utils.AudioType;

public class DefaultIncomingCallActivity extends AppCompatActivity implements IncomingBundleChecker.IExtraBundleFillerListener {

    private final static String TAG = DefaultIncomingCallActivity.class.getSimpleName();
    private static final String DEFAULT_VOXEET_INCOMING_CALL_DURATION_KEY = "voxeet_incoming_call_duration";
    private static final int DEFAULT_VOXEET_INCOMING_CALL_DURATION_VALUE = 40 * 1000;

    protected TextView mUsername;
    protected TextView mStateTextView;
    protected TextView mDeclineTextView;
    protected TextView mAcceptTextView;
    protected RoundedImageView mAvatar;
    protected EventBus mEventBus;

    private IncomingBundleChecker mIncomingBundleChecker;
    private Handler mHandler;
    private boolean isResumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isResumed = false;

        //we preInit the AudioService,
        AudioService.preInitSounds(getApplicationContext());

        mIncomingBundleChecker = new IncomingBundleChecker(getIntent(), this);

        //add few Flags to start the activity before its setContentView
        //note that if your device is using a keyguard (code or password)
        //when the call will be accepted, you still need to unlock it
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.voxeet_activity_incoming_call);

        mUsername = (TextView) findViewById(R.id.voxeet_incoming_username);
        mAvatar = (RoundedImageView) findViewById(R.id.voxeet_incoming_avatar_image);
        mStateTextView = (TextView) findViewById(R.id.voxeet_incoming_text);
        mAcceptTextView = (TextView) findViewById(R.id.voxeet_incoming_accept);
        mDeclineTextView = (TextView) findViewById(R.id.voxeet_incoming_decline);

        mDeclineTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDecline();
            }
        });

        mAcceptTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAccept();
            }
        });

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (null != mHandler)
                        finish();
                } catch (Exception e) {
                    ExceptionManager.sendException(e);
                }
            }
        }, AndroidManifest.readMetadataInt(this, DEFAULT_VOXEET_INCOMING_CALL_DURATION_KEY,
                DEFAULT_VOXEET_INCOMING_CALL_DURATION_VALUE));

        tryInitializedSDK().then(new PromiseExec<Boolean, Boolean>() {
            @Override
            public void onCall(@Nullable Boolean result, @NonNull Solver<Boolean> solver) {
                Log.d(TAG, "onCall: initialized ? " + result);
                
                if(!VoxeetSdk.getInstance().isSocketOpen()) {
                    Log.d(TAG, "onCall: try to log user");
                    UserInfo userInfo = VoxeetPreferences.getSavedUserInfo();

                    if (null != userInfo) {
                        solver.resolve(VoxeetSdk.getInstance().logUserWithChain(userInfo));
                    } else {
                        solver.resolve(false);
                    }
                } else {
                    solver.resolve(true);
                }
            }
        }).then(new PromiseExec<Boolean, Object>() {
            @Override
            public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                Log.d(TAG, "onCall: user logged !");
            }
        }).error(simpleError(false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;

        SoundManager soundManager = AudioService.getSoundManager();
        if (null != soundManager) {
            soundManager.setInVoiceCallSoundType().playSoundType(AudioType.RING);
        }


        tryInitializedSDK().then(new PromiseExec<Boolean, Object>() {
            @Override
            public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                if(!isResumed) {
                    Log.d(TAG, "onCall: not resumed, quit promise");
                    return;
                }

                Activity activity = DefaultIncomingCallActivity.this;

                mEventBus = VoxeetSdk.getInstance().getEventBus();

                if (mIncomingBundleChecker.isBundleValid() && mEventBus != null) {
                    if(!mEventBus.isRegistered(activity)) {
                        mEventBus.register(activity);
                    }

                    mUsername.setText(mIncomingBundleChecker.getUserName());
                    Picasso.get()
                            .load(mIncomingBundleChecker.getAvatarUrl())
                            .into(mAvatar);
                } else {
                    Toast.makeText(activity, getString(R.string.invalid_bundle), Toast.LENGTH_SHORT).show();
                    finish();
                }

                if (!mIncomingBundleChecker.isSameConference(VoxeetSdk.getInstance().getConferenceService().getConferenceId())) {
                    mEventBus.post(new LoadLastSavedOverlayStateEvent());
                }
            }
        }).error(simpleError(true));
    }

    @Override
    protected void onPause() {
        isResumed = false;

        SoundManager soundManager = AudioService.getSoundManager();
        if (null != soundManager) {
            soundManager.resetDefaultSoundType().stopSoundType(AudioType.RING);
        }

        if (mEventBus != null) {
            mEventBus.unregister(this);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mHandler = null;

        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPushEvent event) {
        if (mIncomingBundleChecker.isSameConference(event.getPush().getConferenceId())) {
            finish();
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Specific event used to manage the current "incoming" call feature
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEndedEvent event) {
        if (mIncomingBundleChecker.isSameConference(event.getEvent().getConferenceId())) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeclineConferenceResultEvent event) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferencePreJoinedEvent event) {
        if (mIncomingBundleChecker.isSameConference(event.getConferenceId())) {
            finish();
        }
    }

    @Nullable
    protected String getConferenceId() {
        return mIncomingBundleChecker != null && mIncomingBundleChecker.isBundleValid() ? mIncomingBundleChecker.getConferenceId() : null;
    }

    protected void onDecline() {
        tryInitializedSDK().then(new PromiseExec<Boolean, DeclineConferenceResultEvent>() {
            @Override
            public void onCall(@Nullable Boolean result, @NonNull Solver<DeclineConferenceResultEvent> solver) {
                if (getConferenceId() != null && null != VoxeetSdk.getInstance()) {
                    solver.resolve(VoxeetSdk.getInstance().getConferenceService().decline(getConferenceId()));
                } else {
                    solver.resolve((DeclineConferenceResultEvent) null);
                }
            }
        }).then(new PromiseExec<DeclineConferenceResultEvent, Object>() {
            @Override
            public void onCall(@Nullable DeclineConferenceResultEvent result, @NonNull Solver<Object> solver) {
                finish();
            }
        }).error(simpleError(true));
    }

    protected void onAccept() {
        tryInitializedSDK().then(new PromiseExec<Boolean, DeclineConferenceResultEvent>() {
            @Override
            public void onCall(@Nullable Boolean result, @NonNull Solver<DeclineConferenceResultEvent> solver) {
                if (mIncomingBundleChecker.isBundleValid()) {
                    Intent intent = mIncomingBundleChecker.createActivityAccepted(DefaultIncomingCallActivity.this);
                    //start the accepted call
                    startActivity(intent);

                    //and finishing this one - before the prejoined event
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        }).error(simpleError(true));
    }

    /**
     * Give the possibility to add custom extra infos before starting a conference
     *
     * @return a nullable extra bundle (will not be the bundle sent but a value with a key)
     */
    @Nullable
    @Override
    public Bundle createExtraBundle() {
        //override to return a custom intent to add in the possible notification
        //note that everything which could have been backed up from the previous activity
        //will be injected after the creation - usefull if the app is mainly based on
        //passed intents
        return null;
    }

    /**
     * Get the instance of the bundle checker corresponding to this activity
     *
     * @return an instance or null corresponding to the current bundle checker
     */
    @Nullable
    protected IncomingBundleChecker getBundleChecker() {
        return mIncomingBundleChecker;
    }

    /**
     * Initialize the SDK if needed
     * Resolves true if the SDK is initialized
     * false otherwise (see logs)
     *
     * @return a promise resolving the initialization state
     */
    private Promise<Boolean> tryInitializedSDK() {
        return new Promise<>(new PromiseSolver<Boolean>() {
            @Override
            public void onCall(@NonNull Solver<Boolean> solver) {
                Application app = getApplication();
                if (null != VoxeetSdk.getInstance()) {
                    //the SDK is already initialized
                    solver.resolve(true);
                } else if (null != app && app instanceof VoxeetApplication) {
                    //if we have a VoxeetApplication, we can manage the state
                    ((VoxeetApplication) app).initializeSDK().then(new PromiseExec<Boolean, Object>() {
                        @Override
                        public void onCall(@Nullable Boolean result, @NonNull Solver<Object> sss) {
                            Log.d(TAG, "onCall: sdk initialized now");
                            solver.resolve(result);
                        }
                    }).error(new ErrorPromise() {
                        @Override
                        public void onError(@NonNull Throwable error) {
                            solver.reject(error);
                        }
                    });
                } else {
                    //if we are
                    Log.d(TAG, "onCall: sdk not initialized, please make your App override VoxeetApplication and follow the README.md");
                    solver.resolve(false);
                }
            }
        });
    }

    private ErrorPromise simpleError(boolean quit) {
        return new ErrorPromise() {
            @Override
            public void onError(@NonNull Throwable error) {
                error.printStackTrace();
                if (quit) finish();
            }
        };
    }
}
