package fr.voxeet.sdk.sample.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.voxeet.VoxeetSDK;
import com.voxeet.promise.PromiseInOut;
import com.voxeet.promise.solve.ErrorPromise;
import com.voxeet.promise.solve.PromiseExec;
import com.voxeet.promise.solve.Solver;
import com.voxeet.sdk.events.sdk.ConferenceStatusUpdatedEvent;
import com.voxeet.sdk.events.sdk.SocketStateChangeEvent;
import com.voxeet.sdk.json.ParticipantInfo;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.sample.R;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.SessionService;
import com.voxeet.uxkit.activities.VoxeetAppCompatActivity;
import com.voxeet.uxkit.controllers.VoxeetToolkit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.voxeet.sdk.sample.application.SampleApplication;
import fr.voxeet.sdk.sample.main_screen.UserAdapter;
import fr.voxeet.sdk.sample.main_screen.ParticipantItem;
import fr.voxeet.sdk.sample.users.UsersHelper;

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

    @Nullable
    @Bind(R.id.force_test_overlay_switch)
    View force_test_overlay_switch;

    private SampleApplication _application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        ButterKnife.bind(this);

        users.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        users.setAdapter(new UserAdapter(this, UsersHelper.USER_ITEMS));

        if (null != force_test_overlay_switch) {
            force_test_overlay_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityToTestOverlay.start(MainActivity.this);
                }
            });
        }
    }

    @OnClick(R.id.join_conf)
    public void joinButton() {
        joinCall();
    }

    @OnClick(R.id.disconnect)
    public void onDisconnectClick() {
        SessionService userService = VoxeetSDK.session();
        if (null != userService) userService.close()
                .then(defaultConsume())
                .error(createErrorDump());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == RECORD_AUDIO_RESULT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                joinCall();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getApplication() instanceof SampleApplication) {
            _application = (SampleApplication) getApplication();
        }
    }

    @Override
    public void onBackPressed() {
        if (null != VoxeetSDK.conference() && VoxeetSDK.conference().isLive()) {
            VoxeetSDK.conference().leave()
                    .then(defaultConsume())
                    .error(createErrorDump());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onUserSelected(ParticipantItem user_item) {
        _application.selectUser(user_item.getParticipantInfo());
    }

    private void joinCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RESULT);
        } else {
            String conferenceAlias = joinConfEditText.getText().toString();

            VoxeetToolkit.instance().enable(VoxeetToolkit.instance().getConferenceToolkit());

            ConferenceService service = VoxeetSDK.conference();

            if (!VoxeetSDK.instance().isInitialized()) {
                Toast.makeText(this, "Invalid state of the SDK !", Toast.LENGTH_SHORT).show();
                return;
            }

            PromiseInOut<Conference, Object> create = service.create(conferenceAlias)
                    .then(new PromiseExec<Conference, Object>() {
                        @Override
                        public void onCall(@Nullable Conference result, @NonNull Solver<Object> solver) {
                            try {
                                String conferenceId = null != result ? result.getId() : null;
                                if (null == conferenceId)
                                    throw new NullPointerException("ConferenceId null");
                                solver.resolve(service.join(conferenceId));
                            } catch (Exception e) {
                                solver.reject(e);
                            }
                        }
                    });

            if (VoxeetSDK.conference().isLive()) {
                VoxeetSDK.conference()
                        .leave()
                        .then(create)
                        //.then(VoxeetSdk.conference().startVideo())
                        .then(defaultConsume())
                        .error(createErrorDump());
            } else {
                create//.then(VoxeetSdk.conference().startVideo())
                        .then(defaultConsume())
                        .error(createErrorDump());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketStateChangeEvent event) {
        UserAdapter adapter = (UserAdapter) users.getAdapter();
        switch (event.state) {
            case CONNECTED:
                joinConf.setEnabled(true);
                disconnect.setVisibility(View.VISIBLE);

                if (null != adapter) adapter.setSelected(_application.getCurrentUser());
                break;
            case CLOSING:
            case CLOSED:
                joinConf.setEnabled(false);
                disconnect.setVisibility(View.GONE);
                if (null != adapter) adapter.reset();
            default:
        }
    }

    @Override
    protected void onConferenceState(@NonNull ConferenceStatusUpdatedEvent event) {
        super.onConferenceState(event);

        switch (event.state) {
            case JOINED:
                onConferenceJoinedSuccessEvent();
        }
    }

    private void onConferenceJoinedSuccessEvent() {
        SessionService userService = VoxeetSDK.session();
        ConferenceService conferenceService = VoxeetSDK.conference();

        List<ParticipantInfo> users = UsersHelper.getExternalIds(userService.getParticipantId());

        conferenceService.invite(conferenceService.getConferenceId(), users)
                .then(defaultConsume())
                .error(createErrorDump());
    }

    private <TYPE> PromiseExec<TYPE, Object> defaultConsume() {
        return new PromiseExec<TYPE, Object>() {
            @Override
            public void onCall(@Nullable TYPE result, @NonNull Solver<Object> solver) {
                Log.d(TAG, "onCall: promise managed done");
            }
        };
    }

    private ErrorPromise createErrorDump() {
        return new ErrorPromise() {
            @Override
            public void onError(@NonNull Throwable error) {
                error.printStackTrace();
            }
        };
    }
}
