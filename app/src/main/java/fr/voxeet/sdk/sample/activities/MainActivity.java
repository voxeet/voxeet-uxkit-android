package fr.voxeet.sdk.sample.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.voxeet.toolkit.activities.VoxeetAppCompatActivity;
import com.voxeet.toolkit.controllers.VoxeetToolkit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;
import fr.voxeet.sdk.sample.R;
import fr.voxeet.sdk.sample.application.SampleApplication;
import fr.voxeet.sdk.sample.main_screen.UserAdapter;
import fr.voxeet.sdk.sample.main_screen.UserItem;
import fr.voxeet.sdk.sample.users.UsersHelper;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.preferences.VoxeetPreferences;
import voxeet.com.sdk.core.services.LocalStatsService;
import voxeet.com.sdk.core.services.localstats.LocalStatsUserInfo;
import voxeet.com.sdk.events.error.ConferenceLeftError;
import voxeet.com.sdk.events.success.ConferenceDestroyedPushEvent;
import voxeet.com.sdk.events.success.ConferenceJoinedSuccessEvent;
import voxeet.com.sdk.events.success.ConferenceLeftSuccessEvent;
import voxeet.com.sdk.events.success.ConferenceRefreshedEvent;
import voxeet.com.sdk.events.success.SocketConnectEvent;
import voxeet.com.sdk.events.success.SocketStateChangeEvent;
import voxeet.com.sdk.json.UserInfo;
import voxeet.com.sdk.json.internal.MetadataHolder;
import voxeet.com.sdk.models.abs.Conference;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;

public class MainActivity extends VoxeetAppCompatActivity implements UserAdapter.UserClickListener {

    private static final int RECORD_AUDIO_RESULT = 0x20;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.join_conf_text)
    EditText joinConfEditText;

    @Bind(R.id.join_conf)
    protected Button joinConf;

    @Bind(R.id.disconnect)
    protected View disconnect;

    @Bind(R.id.recycler_users)
    protected RecyclerView users;

    private SampleApplication _application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        ButterKnife.bind(this);

        users.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        users.setAdapter(new UserAdapter(this, UsersHelper.USER_ITEMS));

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (null != VoxeetSdk.getInstance() && VoxeetSdk.getInstance().getConferenceService().isLive()) {
                    Conference conference = VoxeetSdk.getInstance().getConferenceService().getConference();

                    if (null != conference) {
                        List<DefaultConferenceUser> users = conference.getConferenceUsers();
                        LocalStatsService service = VoxeetSdk.getInstance().getLocalStatsService();
                        for (DefaultConferenceUser user : users) {
                            LocalStatsUserInfo stats = service.getUserInfo(conference.getConferenceId(),
                                    user.getUserId());

                            boolean disconnected = stats.isDisconnected();
                            boolean fluctuates = stats.isFluctuating();

                            Log.d("MainActivity", "run: userId:=" + user.getUserId() + " " + user.getUserInfo() + " disconnected:=" + disconnected + " fluctuates:=" + fluctuates);
                        }
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    @OnClick(R.id.join_conf)
    public void joinButton() {
        joinCall();
    }

    private boolean lock = false;

    @Nullable
    @OnClick(R.id.reidentify)
    public void reidentify() {
        if (!lock && null != VoxeetSdk.getInstance()) {
            lock = true;
            VoxeetSdk.getInstance().closeSocket();
            VoxeetSdk.getInstance().logCurrentlySelectedUserWithChain()
                    .then(new PromiseExec<Boolean, Object>() {
                        @Override
                        public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                            Log.d(TAG, "onCall: user identify := " + result);
                            lock = false;
                        }
                    })
                    .error(new ErrorPromise() {
                        @Override
                        public void onError(@NonNull Throwable error) {
                            lock = false;
                            error.printStackTrace();
                        }
                    });
        }
    }

    @OnClick(R.id.reset_http)
    public void resetHttp() {
        if (null != VoxeetSdk.getInstance()) {
            VoxeetSdk.getInstance().resetVoxeetHttp();
        }
    }

    @OnClick(R.id.disconnect)
    public void onDisconnectClick() {

        VoxeetSdk.getInstance().logout()
                .then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {

                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(Throwable error) {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case RECORD_AUDIO_RESULT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    joinCall();
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getApplication() instanceof SampleApplication) {
            _application = (SampleApplication) getApplication();
        }

        if (null != VoxeetSdk.getInstance()) {
            if (VoxeetSdk.getInstance().getConferenceService().isLive()) {
                VoxeetSdk.getInstance().getLocalStatsService().startAutoFetch();
            } else {
                VoxeetSdk.getInstance().getLocalStatsService().stopAutoFetch();
            }
        }
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (null != VoxeetSdk.getInstance() && VoxeetSdk.getInstance().getConferenceService().isLive()) {
            VoxeetSdk.getInstance().getConferenceService().leave()
                    .then(new PromiseExec<Boolean, Object>() {
                        @Override
                        public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {

                        }
                    })
                    .error(new ErrorPromise() {
                        @Override
                        public void onError(@NonNull Throwable error) {

                        }
                    });
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onUserSelected(UserItem user_item) {
        _application.selectUser(user_item.getUserInfo());
    }

    private void joinCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RESULT);
        else {
            String conferenceAlias = joinConfEditText.getText().toString();

            VoxeetToolkit.getInstance().enable(VoxeetToolkit.getInstance().getReplayMessageToolkit());

            Promise<Boolean> promise = VoxeetToolkit.getInstance().getConferenceToolkit().join(conferenceAlias,
                    new MetadataHolder().setStats(true));

            if (VoxeetSdk.getInstance().getConferenceService().isLive()) {
                VoxeetSdk.getInstance().getConferenceService()
                        .leave()
                        .then(new PromiseExec<Boolean, Boolean>() {
                            @Override
                            public void onCall(@Nullable Boolean result, @NonNull Solver<Boolean> solver) {
                                solver.resolve(promise);
                            }
                        })
                        .then(new PromiseExec<Boolean, Boolean>() {
                            @Override
                            public void onCall(@Nullable Boolean result, @NonNull Solver<Boolean> solver) {
                                solver.resolve(VoxeetSdk.getInstance().getConferenceService().startVideo());
                            }
                        })
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
            } else {
                promise.then(new PromiseExec<Boolean, Boolean>() {
                    @Override
                    public void onCall(@Nullable Boolean result, @NonNull Solver<Boolean> solver) {
                        solver.resolve(VoxeetSdk.getInstance().getConferenceService().startVideo());
                    }
                }).then(new PromiseExec<Boolean, Boolean>() {
                    @Override
                    public void onCall(@Nullable Boolean result, @NonNull Solver<Boolean> solver) {
                    }
                }).error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        error.printStackTrace();
                    }
                });
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final SocketConnectEvent event) {
        Log.d("MainActivity", "SocketConnectEvent" + event.message());
        joinConf.setEnabled(true);
        disconnect.setVisibility(View.VISIBLE);

        //TODO resume select the current logged user
        ((UserAdapter) users.getAdapter()).setSelected(_application.getCurrentUser());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketStateChangeEvent event) {
        Log.d("MainActivity", "SocketStateChangeEvent " + event.message());

        switch (event.message()) {
            case "CLOSING":
            case "CLOSED":
                joinConf.setEnabled(false);
                disconnect.setVisibility(View.GONE);
                ((UserAdapter) users.getAdapter()).reset();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceJoinedSuccessEvent event) {
        VoxeetSdk.getInstance().getLocalStatsService().startAutoFetch();

        List<UserInfo> external_ids = UsersHelper.getExternalIds(VoxeetPreferences.id());

        VoxeetToolkit.getInstance().getConferenceToolkit()
                .invite(external_ids)
                .then(new PromiseExec<List<ConferenceRefreshedEvent>, Object>() {
                    @Override
                    public void onCall(@Nullable List<ConferenceRefreshedEvent> result, @NonNull Solver<Object> solver) {

                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });

        VoxeetSdk.getInstance().getConferenceService()
                .startVideo()
                .then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                        Toast.makeText(MainActivity.this, "start := " + result, Toast.LENGTH_SHORT).show();
                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        error.printStackTrace();
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceLeftSuccessEvent event) {
        VoxeetSdk.getInstance().getLocalStatsService().stopAutoFetch();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceLeftError event) {
        VoxeetSdk.getInstance().getLocalStatsService().stopAutoFetch();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPushEvent event) {
        VoxeetSdk.getInstance().getLocalStatsService().stopAutoFetch();
    }
}
