package com.voxeet.uxkit.activities.notification;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.voxeet.VoxeetSDK;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ErrorPromise;
import com.voxeet.promise.solve.ThenPromise;
import com.voxeet.promise.solve.ThenVoid;
import com.voxeet.sdk.events.sdk.ConferenceStatusUpdatedEvent;
import com.voxeet.sdk.events.v2.ParticipantUpdatedEvent;
import com.voxeet.sdk.exceptions.ExceptionManager;
import com.voxeet.sdk.json.ConferenceDestroyedPush;
import com.voxeet.sdk.json.ConferenceEnded;
import com.voxeet.sdk.json.ParticipantInfo;
import com.voxeet.sdk.media.audio.AudioRoute;
import com.voxeet.sdk.media.audio.SoundManager;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.sdk.preferences.VoxeetPreferences;
import com.voxeet.sdk.services.AudioService;
import com.voxeet.sdk.services.SessionService;
import com.voxeet.sdk.services.conference.information.ConferenceStatus;
import com.voxeet.sdk.utils.AndroidManifest;
import com.voxeet.sdk.utils.AudioType;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.R;
import com.voxeet.uxkit.application.VoxeetApplication;
import com.voxeet.uxkit.utils.LoadLastSavedOverlayStateEvent;
import com.voxeet.uxkit.views.internal.rounded.RoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DefaultIncomingCallActivity extends AppCompatActivity implements IncomingBundleChecker.IExtraBundleFillerListener {

    private final static String TAG = DefaultIncomingCallActivity.class.getSimpleName();
    private static final String DEFAULT_VOXEET_INCOMING_CALL_DURATION_KEY = "voxeet_incoming_call_duration";
    private static final int DEFAULT_VOXEET_INCOMING_CALL_DURATION_VALUE = 40 * 1000;
    private static final int RECORD_AUDIO_RESULT = 0x10;

    protected TextView mUsername;
    protected TextView mStateTextView;
    protected TextView mDeclineTextView;
    protected TextView mAcceptTextView;
    protected RoundedImageView mAvatar;
    protected EventBus mEventBus;

    private IncomingBundleChecker mIncomingBundleChecker;
    private Handler mHandler;
    private boolean isResumed;
    private Ringtone ringTone;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.voxeet_activity_incoming_call);

        mUsername = findViewById(R.id.voxeet_incoming_username);
        mAvatar = findViewById(R.id.voxeet_incoming_avatar_image);
        mStateTextView = findViewById(R.id.voxeet_incoming_text);
        mAcceptTextView = findViewById(R.id.voxeet_incoming_accept);
        mDeclineTextView = findViewById(R.id.voxeet_incoming_decline);

        mDeclineTextView.setOnClickListener(view -> onDecline());

        mAcceptTextView.setOnClickListener(view -> onAccept());

        mHandler = new Handler();
        mHandler.postDelayed(() -> {
            try {
                if (null != mHandler)
                    finish();
            } catch (Exception e) {
                ExceptionManager.sendException(e);
            }
        }, AndroidManifest.readMetadataInt(this, DEFAULT_VOXEET_INCOMING_CALL_DURATION_KEY,
                DEFAULT_VOXEET_INCOMING_CALL_DURATION_VALUE));

        tryInitializedSDK().then((ThenPromise<Boolean, Boolean>) result -> {
            if (!Opt.of(VoxeetSDK.session()).then(SessionService::isSocketOpen).or(false)) {
                ParticipantInfo userInfo = VoxeetPreferences.getSavedUserInfo();

                if (null != userInfo) return VoxeetSDK.session().open(userInfo);
                return Promise.resolve(false);
            }
            return Promise.resolve(true);
        }).then(aBoolean -> {
            Log.d(TAG, "onCall: user logged !");
        }).error(simpleError(false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;

        boolean useRingtone = "true".equals(AndroidManifest.readMetadata(this, "voxeet_use_ringtone", "true"));
        SoundManager soundManager = AudioService.getSoundManager();

        if (null != soundManager) {
            ringTone = soundManager.getSystemRingtone();
            if (useRingtone && null != ringTone) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringTone.setLooping(true);
                }
                if (!ringTone.isPlaying()) ringTone.play();
            } else {
                soundManager.setAudioRoute(AudioRoute.ROUTE_SPEAKER);
                soundManager.playSoundType(AudioType.RING);
            }
        }


        tryInitializedSDK().then((result) -> {
            if (!isResumed) {
                Log.d(TAG, "onCall: not resumed, quit promise");
                return;
            }

            Activity activity = DefaultIncomingCallActivity.this;

            mEventBus = VoxeetSDK.instance().getEventBus();

            if (mIncomingBundleChecker.isBundleValid() && mEventBus != null) {
                if (!mEventBus.isRegistered(activity)) {
                    mEventBus.register(activity);
                }

                mUsername.setText(mIncomingBundleChecker.getUserName());
                try {
                    Picasso.get()
                            .load(mIncomingBundleChecker.getAvatarUrl())
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(mAvatar);
                } catch (Exception e) {

                }
            } else {
                finish();
            }

            if (!mIncomingBundleChecker.isSameConference(VoxeetSDK.conference().getConferenceId())) {
                mEventBus.post(new LoadLastSavedOverlayStateEvent());
            }
        }).error(simpleError(true));
    }

    @Override
    protected void onPause() {

        if (null != ringTone && ringTone.isPlaying()) {
            ringTone.stop();
        }
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
    public void onEvent(ConferenceDestroyedPush event) {
        if (mIncomingBundleChecker.isSameConference(event.conferenceId)) {
            finish();
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Specific event used to manage the current "incoming" call feature
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEnded event) {
        if (mIncomingBundleChecker.isSameConference(event.conferenceId)) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ParticipantUpdatedEvent event) {
        if (ConferenceParticipantStatus.DECLINE.equals(event.participant.getStatus())) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceStatusUpdatedEvent event) {
        if (ConferenceStatus.JOINING.equals(event.state) && mIncomingBundleChecker.isSameConference(Opt.of(event.conference).then(Conference::getId).orNull())) {
            finish();
        }
    }

    @Nullable
    protected String getConferenceId() {
        return Opt.of(mIncomingBundleChecker).then(IncomingBundleChecker::isBundleValid).or(false) ? mIncomingBundleChecker.getConferenceId() : null;
    }

    protected void onDecline() {
        tryInitializedSDK().then((ThenPromise<Boolean, Boolean>) aBoolean -> {
            if (getConferenceId() != null && null != VoxeetSDK.conference()) {
                return VoxeetSDK.conference().decline(getConferenceId());
            } else {
                return Promise.resolve(false);
            }
        }).then((ThenVoid<Boolean>) o -> finish())
                .error(simpleError(true));
    }

    protected void onAccept() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RESULT);
        } else {
            onAcceptWithPermission();
        }
    }

    private void onAcceptWithPermission() {
        tryInitializedSDK().then(aBoolean -> {
            if (mIncomingBundleChecker.isBundleValid()) {
                Intent intent = mIncomingBundleChecker.createActivityAccepted(DefaultIncomingCallActivity.this);
                //start the accepted call
                startActivity(intent);

                //and finishing this one - before the prejoined event
                finish();
                overridePendingTransition(0, 0);
            }
        }).error(simpleError(true));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case RECORD_AUDIO_RESULT: {
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    if (Manifest.permission.RECORD_AUDIO.equals(permission) && grantResult == PackageManager.PERMISSION_GRANTED) {
                        onAcceptWithPermission();
                    } else {
                        //possible message to show? display?
                    }
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        return new Promise<>(solver -> {
            Application app = getApplication();
            if (null != VoxeetSDK.instance()) {
                //the SDK is already initialized
                solver.resolve(true);
            } else if (app instanceof VoxeetApplication) {
                //if we have a VoxeetApplication, we can manage the state
                ((VoxeetApplication) app).initializeSDK().then((result, sss) -> {
                    Log.d(TAG, "onCall: sdk initialized now");
                    solver.resolve(result);
                }).error(solver::reject);
            } else {
                //if we are
                Log.d(TAG, "onCall: sdk not initialized, please make your App override VoxeetApplication and follow the README.md");
                solver.resolve(false);
            }
        });
    }

    private ErrorPromise simpleError(boolean quit) {
        return error -> {
            error.printStackTrace();
            if (quit) finish();
        };
    }
}
