package sdk.voxeet.com.toolkit.activities.notification;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.voxeet.toolkit.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;
import sdk.voxeet.com.toolkit.utils.LoadLastSavedOverlayStateEvent;
import sdk.voxeet.com.toolkit.views.android.RoundedImageView;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.events.success.ConferenceDestroyedPushEvent;
import voxeet.com.sdk.events.success.ConferenceEndedEvent;
import voxeet.com.sdk.events.success.ConferencePreJoinedEvent;
import voxeet.com.sdk.events.success.DeclineConferenceResultEvent;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

                }
            }
        }, readMetadataInt(this, DEFAULT_VOXEET_INCOMING_CALL_DURATION_KEY,
                DEFAULT_VOXEET_INCOMING_CALL_DURATION_VALUE));
    }

    @Override
    protected void onResume() {
        super.onResume();

        mEventBus = VoxeetSdk.getInstance().getEventBus();

        if (mIncomingBundleChecker.isBundleValid() && mEventBus != null) {
            mEventBus.register(this);

            mUsername.setText(mIncomingBundleChecker.getUserName());
            Picasso.get()
                    .load(mIncomingBundleChecker.getAvatarUrl())
                    .into(mAvatar);
        } else {
            Toast.makeText(this, getString(R.string.invalid_bundle), Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!mIncomingBundleChecker.isSameConference(VoxeetSdk.getInstance().getConferenceService().getConferenceId())) {
            mEventBus.post(new LoadLastSavedOverlayStateEvent());
        }
    }

    @Override
    protected void onPause() {
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
        if (getConferenceId() != null && null != VoxeetSdk.getInstance()) {
            VoxeetSdk.getInstance().getConferenceService().decline(getConferenceId())
                    .then(new PromiseExec<DeclineConferenceResultEvent, Object>() {
                        @Override
                        public void onCall(@Nullable DeclineConferenceResultEvent result, @NonNull Solver<Object> solver) {
                            finish();
                        }
                    })
                    .error(new ErrorPromise() {
                        @Override
                        public void onError(Throwable error) {
                            finish();
                        }
                    });
        } else {
            finish();
        }
    }

    protected void onAccept() {
        if (mIncomingBundleChecker.isBundleValid()) {
            Intent intent = mIncomingBundleChecker.createActivityAccepted(this);
            //start the accepted call activity
            startActivity(intent);

            //and finishing this one - before the prejoined event
            finish();
            overridePendingTransition(0, 0);
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

    //TODO refactor with SDK
    private static int readMetadataInt(@NonNull Context context, @NonNull String key, int argb) {
        try {
            String metaData = readMetadata(context, key, null);
            if (!TextUtils.isEmpty(metaData)) return Integer.parseInt(metaData);
        } catch (Exception e) {

        }
        return argb;
    }

    //TODO refactor with SDK
    private static String readMetadata(@NonNull Context context, @NonNull String key, @NonNull String def) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            String value = bundle.getString(key);
            if (!TextUtils.isEmpty(value)) return value;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return def;
    }
}
