package fr.voxeet.sdk.sample.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.voxeet.sdk.sample.R;
import fr.voxeet.sdk.sample.application.SampleApplication;
import fr.voxeet.sdk.sample.main_screen.UserAdapter;
import fr.voxeet.sdk.sample.main_screen.UserItem;
import fr.voxeet.sdk.sample.users.UsersHelper;
import sdk.voxeet.com.toolkit.controllers.ReplayMessageToolkitController;
import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import voxeet.com.sdk.core.VoxeetPreferences;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.events.success.SocketConnectEvent;
import voxeet.com.sdk.events.success.SocketStateChangeEvent;

public class MainActivity extends AppCompatActivity implements UserAdapter.UserClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int RECORD_AUDIO_RESULT = 0x20;
    private static final int REQUEST_EXTERNAL_STORAGE = 0x21;

    public static final int JOIN = 0x1000;
    public static final int CREATE = 0x1010;
    public static final int DEMO = 0x1020;
    public static final int REPLAY = 0x1030;

    private int lastAction;

    @Bind(R.id.join_conf_text)
    EditText joinConfEditText;

    @Bind(R.id.join_conf)
    protected Button joinConf;

    @Bind(R.id.disconnect)
    protected Button disconnect;

    @Bind(R.id.recycler_users)
    protected RecyclerView users;

    private final static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //In the example, this field will be set to true when "starting" a conference after login
    private boolean _start_after_log_event = false;
    private Intent _after_relogged_intent = null;

    private SampleApplication _application;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO_RESULT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    switch (lastAction) {
                        case JOIN:
                            joinCall();
                            break;
                        default:
                            throw new IllegalStateException("Invalid last option");
                    }
                }
                return;
            }
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Storage granted", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        ButterKnife.bind(this);

        verifyStoragePermissions(this);

        users.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        users.setAdapter(new UserAdapter(this, UsersHelper.USER_ITEMS));
    }

    @OnClick(R.id.join_conf)
    public void joinButton() {
        joinCall();
    }

    @OnClick(R.id.disconnect)
    public void onDisconnectClick() {

        VoxeetSdk.getInstance().logout();
    }

    public void verifyStoragePermissions(Activity context) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    context,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void joinCall() {
        conferenceActivity(lastAction = JOIN);
    }

    private void createConf() {
        conferenceActivity(lastAction = CREATE);
    }

    public void conferenceActivity(int lastAction) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RESULT);
        else {
            Intent intent = new Intent(MainActivity.this, CreateConfActivity.class);

            switch (lastAction) {
                case DEMO:
                    intent.putExtra("demo", true);
                    break;
                case CREATE:
                    intent.putExtra("create", true);
                    break;
                case REPLAY:
                    intent.putExtra("replay", true);
                    break;
                case JOIN:
                default:
                    intent.putExtra("confAlias", joinConfEditText.getText().toString());
                    intent.putExtra("join", true);
                    break;
            }
            startActivity(intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final SocketConnectEvent event) {
        Log.d("MainActivity", "SocketConnectEvent" + event.message());
        joinConf.setEnabled(true);
        disconnect.setVisibility(View.VISIBLE);

        ((UserAdapter) users.getAdapter()).setSelected(_application.getCurrentUser());

        if (_start_after_log_event && _after_relogged_intent != null) {
            //startActivity(_after_relogged_intent);
            _after_relogged_intent = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketStateChangeEvent event) {
        Log.d("MainActivity", "SocketStateChangeEvent " + event.message());

        switch (event.message()) {
            case "CLOSING":
                joinConf.setEnabled(false);
                disconnect.setVisibility(View.GONE);
                ((UserAdapter)users.getAdapter()).reset();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        _application = (SampleApplication) getApplication();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if(VoxeetToolkit.getInstance().getReplayMessageToolkit().isShowing()) {
            VoxeetSdk.getInstance().getConferenceService().leave();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onUserSelected(UserItem user_item) {
        _application.selectUser(user_item.getUserInfo());
    }
}
