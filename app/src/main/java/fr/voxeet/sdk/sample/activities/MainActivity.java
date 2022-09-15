package fr.voxeet.sdk.sample.activities;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.voxeet.VoxeetSDK;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;
import com.voxeet.promise.solve.ThenVoid;
import com.voxeet.sdk.events.sdk.ConferenceStatusUpdatedEvent;
import com.voxeet.sdk.events.sdk.SocketStateChangeEvent;
import com.voxeet.sdk.json.ParticipantInfo;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.sample.R;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.SessionService;
import com.voxeet.sdk.services.builders.ConferenceCreateOptions;
import com.voxeet.sdk.services.builders.ConferenceJoinOptions;
import com.voxeet.sdk.services.conference.information.ConferenceStatus;
import com.voxeet.uxkit.activities.VoxeetAppCompatActivity;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.common.permissions.PermissionController;
import com.voxeet.uxkit.common.permissions.PermissionResult;
import com.voxeet.uxkit.controllers.VoxeetToolkit;
import com.voxeet.uxkit.service.VoxeetSystemService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.voxeet.sdk.sample.application.SampleApplication;
import fr.voxeet.sdk.sample.main_screen.ParticipantItem;
import fr.voxeet.sdk.sample.main_screen.UserAdapter;
import fr.voxeet.sdk.sample.users.UsersHelper;

public class MainActivity extends VoxeetAppCompatActivity<VoxeetSystemService> implements UserAdapter.UserClickListener {

    private static final ShortLogger Log = UXKitLogger.createLogger(MainActivity.class);

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
            force_test_overlay_switch.setOnClickListener(v -> ActivityToTestOverlay.start(MainActivity.this));
        }
    }

    @OnClick(R.id.join_conf)
    public void joinButton() {
        joinCall();
    }

    @OnClick(R.id.disconnect)
    public void onDisconnectClick() {
        SessionService userService = VoxeetSDK.session();

        userService.close().then(defaultConsume()).error(Log::e);
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
        if (VoxeetSDK.conference().isLive()) {
            VoxeetSDK.conference().leave()
                    .then(defaultConsume())
                    .error(Log::e);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onUserSelected(ParticipantItem user_item) {
        _application.selectUser(user_item.getParticipantInfo());
    }

    private void joinCall() {
        String conferenceAlias = joinConfEditText.getText().toString();

        VoxeetToolkit.instance().enable(VoxeetToolkit.instance().getConferenceToolkit());

        ConferenceService service = VoxeetSDK.conference();

        if (!VoxeetSDK.instance().isInitialized()) {
            Toast.makeText(this, "Invalid state of the SDK !", Toast.LENGTH_SHORT).show();
            return;
        }

        //create temp promises
        Promise<Conference> create = new Promise<>(solver -> service
                .create(new ConferenceCreateOptions.Builder().setConferenceAlias(conferenceAlias).build())
                .then((ThenPromise<Conference, Conference>) conference -> null == conference ?
                        Promise.reject(new NullPointerException("Conference null")) :
                        service.join(new ConferenceJoinOptions.Builder(conference).build()))
                .then((ThenVoid<Conference>) solver::resolve)
                .error(solver::reject));

        // create leave and join back
        Promise<Conference> leaveAndCreate = new Promise<>(solver -> VoxeetSDK.conference().leave()
                .then(create).then((ThenVoid<Conference>) solver::resolve)
                .error(solver::reject));

        // now do the job
        PermissionController.requestPermissions(Manifest.permission.RECORD_AUDIO)
                .then((ThenPromise<List<PermissionResult>, Boolean>) permissionResults -> {
                    if (!permissionResults.get(0).isGranted)
                        return Promise.reject(new IllegalStateException("microphone permission error"));

                    return Promise.resolve(true);
                })
                .then((ThenPromise<Boolean, Conference>) aBoolean -> VoxeetSDK.conference().isLive() ? leaveAndCreate : create)
                .then(defaultConsume())
                .error(Log::e);
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

        if (event.state == ConferenceStatus.JOINED) {
            onConferenceJoinedSuccessEvent(event.conference);
        }
    }

    private void onConferenceJoinedSuccessEvent(Conference conference) {
        SessionService userService = VoxeetSDK.session();

        List<ParticipantInfo> users = UsersHelper.getExternalIds(userService.getParticipantId());

        VoxeetSDK.notification().invite(conference, users)
                .then(defaultConsume())
                .error(Log::e);
    }

    private <TYPE> ThenVoid<TYPE> defaultConsume() {
        return o -> Log.d("onCall: promise managed done");
    }
}
