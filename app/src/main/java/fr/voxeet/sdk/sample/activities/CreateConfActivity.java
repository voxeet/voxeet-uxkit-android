package fr.voxeet.sdk.sample.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.voxeet.android.media.MediaStream;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;
import fr.voxeet.sdk.sample.R;
import fr.voxeet.sdk.sample.Recording;
import fr.voxeet.sdk.sample.adapters.ParticipantAdapter;
import fr.voxeet.sdk.sample.adapters.RecordedConferencesAdapter;
import fr.voxeet.sdk.sample.application.SampleApplication;
import fr.voxeet.sdk.sample.dialogs.ConferenceOutput;
import fr.voxeet.sdk.sample.users.UsersHelper;
import fr.voxeet.sdk.sample.utils.FileUtils;
import sdk.voxeet.com.toolkit.activities.workflow.VoxeetAppCompatActivity;
import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.views.uitookit.nologic.VideoView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetConferenceBarView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetLoadingView;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.preferences.VoxeetPreferences;
import voxeet.com.sdk.events.success.ConferenceJoinedSuccessEvent;
import voxeet.com.sdk.events.success.ConferenceLeftSuccessEvent;
import voxeet.com.sdk.events.success.ConferenceRefreshedEvent;
import voxeet.com.sdk.events.success.ConferenceUserJoinedEvent;
import voxeet.com.sdk.events.success.ConferenceUserLeftEvent;
import voxeet.com.sdk.events.success.ConferenceUserUpdatedEvent;
import voxeet.com.sdk.events.success.FilePresentationStartedEvent;
import voxeet.com.sdk.events.success.FilePresentationStoppedEvent;
import voxeet.com.sdk.events.success.FilePresentationUpdatedEvent;
import voxeet.com.sdk.events.success.MessageReceived;
import voxeet.com.sdk.events.success.QualityIndicators;
import voxeet.com.sdk.events.success.ScreenStreamAddedEvent;
import voxeet.com.sdk.events.success.ScreenStreamRemovedEvent;
import voxeet.com.sdk.json.ConferenceEnded;
import voxeet.com.sdk.json.FilePresentationStarted;
import voxeet.com.sdk.json.FilePresentationStopped;
import voxeet.com.sdk.json.FilePresentationUpdated;
import voxeet.com.sdk.json.RecordingStatusUpdateEvent;
import voxeet.com.sdk.json.UserInfo;
import voxeet.com.sdk.models.FilePresentationConverted;
import voxeet.com.sdk.models.RecordingStatus;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;

import static android.view.View.VISIBLE;

/**
 * Created by RomainBenmansour on 4/21/16.
 */
public class CreateConfActivity extends VoxeetAppCompatActivity {

    public static final String INVIT_EXTERNAL_IDS = "INVIT_EXTERNAL_IDS";
    private int action;

    private static final String TAG = CreateConfActivity.class.getSimpleName();

    private static final int CAMERA_REQUEST = 0x0010;

    @Bind(R.id.toolbar)
    protected Toolbar toolbar;

    @Bind(R.id.participants)
    protected ListView participants;

    @Bind(R.id.join_conf_layout)
    protected ViewGroup joinLayout;

    @Bind(R.id.conference_options)
    protected ViewGroup conferenceOptions;

    @Bind(R.id.text_layout)
    protected ViewGroup sendText;

    @Bind(R.id.send_broadcast)
    protected Button sendBroadcast;

    @Bind(R.id.conference_editext)
    protected EditText editextConference;

    @Bind(R.id.broadcast_editext)
    protected EditText broadcastConference;

    @Bind(R.id.conference_alias)
    protected TextView aliasId;

    @Bind(R.id.screen_share)
    protected VideoView screenShare;

    @Bind(R.id.video_stream)
    protected VideoView videoStream;

    @Bind(R.id.leaveConf)
    protected Button leave;

    @Bind(R.id.toggle_video)
    protected Button video;

    @Bind(R.id.record)
    protected Button record;

    @Bind(R.id.mute)
    protected Button mute;

    @Bind(R.id.recorded_conf_list)
    protected ListView recordedConferences;

    @Bind(R.id.conference_bar)
    protected VoxeetConferenceBarView conferenceBarView;

    @Bind(R.id.loading_view)
    protected VoxeetLoadingView loadingView;

    @Bind(R.id.start_presentation)
    protected View startPresentation;

    @Bind(R.id.stop_presentation)
    protected View stopPresentation;

    @Bind(R.id.next_page)
    protected View nextPage;

    @Bind(R.id.previous_page)
    protected View previousPage;

    @Bind(R.id.presentation_lander)
    protected ImageView presentation_lander;

    @NonNull
    private Map<String, MediaStream> mediaStreamMap = new HashMap<>();

    private ParticipantAdapter adapter;

    @Nullable
    private ProgressDialog dialog = null;

    private ConferenceOutput conferenceOutput = null;
    private FilePresentationStarted mFilePresentationStarted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_conf_activity);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new ParticipantAdapter(this);

        participants.setAdapter(adapter);

        conferenceOutput = new ConferenceOutput();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else
            initConf();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.previous_page)
    public void onPreviousPage() {
        if (null == mFilePresentationStarted || !mFilePresentationStarted.isOwner()) return;

        if (mFilePresentationStarted.getPosition() > 0) {
            updatePresentation(mFilePresentationStarted.getPosition() - 1);
        }
    }

    @OnClick(R.id.next_page)
    public void onNextPage() {
        if (null == mFilePresentationStarted || !mFilePresentationStarted.isOwner()) return;

        if (mFilePresentationStarted.getPosition() + 1 < mFilePresentationStarted.getImageCount()) {
            updatePresentation(mFilePresentationStarted.getPosition() + 1);
        }
    }

    @OnClick(R.id.stop_presentation)
    public void stopPresentation() {
        hideButtonsPresentation();

        VoxeetSdk.getInstance()
                .getFilePresentationService()
                .stopPresentation(mFilePresentationStarted.getFileId())
                .then(new PromiseExec<FilePresentationStopped, Object>() {
                    @Override
                    public void onCall(@Nullable FilePresentationStopped result, @NonNull Solver<Object> solver) {
                        Toast.makeText(CreateConfActivity.this, "presentation stopped", Toast.LENGTH_SHORT).show();
                        showStartPresentation();
                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        Toast.makeText(CreateConfActivity.this, "error while stopping presentation", Toast.LENGTH_SHORT).show();
                        showStartPresentation();
                    }
                });
    }

    @OnClick(R.id.start_presentation)
    public void startPresentation() {
        hideButtonsPresentation();

        String asset = "iOSUnitTest.pdf";
        File file = FileUtils.extractAssetToTempFile(CreateConfActivity.this, asset);

        VoxeetSdk.getInstance()
                .getFilePresentationService()
                .convertFile(file)
                .then(new PromiseExec<FilePresentationConverted, FilePresentationStarted>() {
                    @Override
                    public void onCall(@Nullable FilePresentationConverted result, @NonNull final Solver<FilePresentationStarted> solver) {
                        Log.d(TAG, "onCall: " + result);
                        VoxeetSdk.getInstance()
                                .getFilePresentationService()
                                .startPresentation(result)
                                .then(new PromiseExec<FilePresentationStarted, Object>() {
                                    @Override
                                    public void onCall(@Nullable FilePresentationStarted result, @NonNull Solver<Object> in_solver) {
                                        solver.resolve(result);
                                    }
                                })
                                .error(new ErrorPromise() {
                                    @Override
                                    public void onError(@NonNull Throwable error) {
                                        solver.reject(error);
                                    }
                                });
                    }
                })
                .then(new PromiseExec<FilePresentationStarted, Object>() {
                    @Override
                    public void onCall(@Nullable FilePresentationStarted result, @NonNull Solver<Object> solver) {
                        Toast.makeText(CreateConfActivity.this, "file started " + result.getFileId(), Toast.LENGTH_SHORT).show();

                        onEvent(result);
                        showStopPresentation();
                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        error.printStackTrace();
                        Toast.makeText(CreateConfActivity.this, "error while starting presentation", Toast.LENGTH_SHORT)
                                .show();

                        showStartPresentation();
                    }
                });
    }

    @OnClick(R.id.toggle_video)
    public void toggleVideo() {
        VoxeetSdk.getInstance().getConferenceService().toggleVideo();
    }

    @OnClick(R.id.leaveConf)
    public void leave() {
        VoxeetSdk.getInstance().getConferenceService().leave()
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

    @OnClick(R.id.record)
    public void toggleRecord() {
        VoxeetSdk.getInstance().getConferenceService().toggleRecording();
    }

    @OnClick(R.id.send_broadcast)
    public void sendBroadcast() {
        //TODO check
        //VoxeetSdk.getInstance().getConferenceService().sendMessage(broadcastConference.getText().toString());
    }

    @OnClick(R.id.mute)
    public void mute() {
        boolean muted = !VoxeetSdk.getInstance().getConferenceService().isMuted();
        if (muted)
            mute.setText("Muted");
        else
            mute.setText("Not Muted");

        VoxeetSdk.getInstance().getConferenceService().muteConference(muted);
    }

    @OnClick(R.id.audio_routes)
    public void audioRoutes() {
        if (conferenceOutput != null) {
            if (conferenceOutput.isVisible()) {
                conferenceOutput.dismiss();
            } else {
                conferenceOutput.show(CreateConfActivity.this.getFragmentManager(), ConferenceOutput.TAG);
            }
        }
    }

    @Deprecated
    @OnClick(R.id.join)
    public void join() {
        join(editextConference.getText().toString());
    }

    public void join(String confAlias) {
        if (!VoxeetSdk.getInstance().register(this, this)) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.register_conf_error), Toast.LENGTH_SHORT).show();
            finish();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != imm) {
            imm.hideSoftInputFromWindow(joinLayout.getWindowToken(), 0);
        }

        Promise<Boolean> promise = null;
        switch (action) {
            case MainActivity.JOIN:
                promise = VoxeetToolkit.getInstance().getConferenceToolkit().join(confAlias);
                break;
            case MainActivity.REPLAY:
                promise = VoxeetToolkit.getInstance().getReplayMessageToolkit().replay(confAlias, 0);
                break;
            default:
        }

        if (promise != null) {
            promise
                    .then(new PromiseExec<Boolean, Object>() {
                        @Override
                        public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {

                        }
                    })
                    .error(new ErrorPromise() {
                        @Override
                        public void onError(Throwable error) {
                            error.printStackTrace();
                        }
                    });
        }

        showProgress();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                initConf();
            else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();

                finish();
            }
        }
    }

    private void initConf() {
        if (getIntent().hasExtra("join")) {
            action = MainActivity.JOIN;

            setTitle("");//Join IConference");

            String confIdOrAlias = null;

            if (getIntent().hasExtra("confAlias")) {
                confIdOrAlias = getIntent().getStringExtra("confAlias");
            } else if (getIntent().hasExtra("conferenceId")) {
                confIdOrAlias = getIntent().getStringExtra("conferenceId");
            }

            if (confIdOrAlias != null) {
                join(confIdOrAlias);
                //displayJoin();
            } else {
                finish();
                return;
            }
        } else if (getIntent().hasExtra("replay")) {
            action = MainActivity.REPLAY;

            setTitle("Replay IConference");

            List<Recording> confs = ((SampleApplication) getApplication()).getRecordedConferences();
            if (confs.size() > 0) {
                recordedConferences.setVisibility(VISIBLE);
                recordedConferences.setAdapter(new RecordedConferencesAdapter(this, confs));
                recordedConferences.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        VoxeetToolkit.getInstance().getReplayMessageToolkit().replay(((Recording) recordedConferences.getItemAtPosition(i)).conferenceId, 0);

                        showProgress();

                        joinLayout.setVisibility(View.GONE);
                    }
                });
            }

            displayJoin();
        } else if (getIntent().hasExtra("demo")) {
            action = MainActivity.DEMO;

            showProgress();

            setTitle("Demo IConference");

            VoxeetToolkit.getInstance().getConferenceToolkit().demo()
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
        } else if (getIntent().hasExtra("create")) {
            action = MainActivity.CREATE;

            showProgress();

            setTitle("Create IConference");

            VoxeetToolkit.getInstance().getConferenceToolkit().create();
        } else {
            Toast.makeText(this, getString(R.string.invalid_activity_start_intent), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!VoxeetSdk.getInstance().register(this, this)) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.register_conf_error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayJoin() {
        joinLayout.setVisibility(VISIBLE);
    }

    @Override
    public void onBackPressed() {
//        if (VoxeetSdk.isSdkConferenceLive())
//            displayLeaveDialog();
//        else
        super.onBackPressed();
    }

    private void displayLeaveDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.leave_conference_title));
        alertDialog.setMessage(getString(R.string.leave_conference_message));
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                VoxeetSdk.getInstance().getConferenceService().leave();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void updateStreams(DefaultConferenceUser user, MediaStream mediaStream) {
        mediaStreamMap.put(user.getUserId(), mediaStream);

        if (user.getUserId().equalsIgnoreCase(VoxeetPreferences.id())) {
            if (action == MainActivity.REPLAY)
                addParticipant(user);
            else if (mediaStream != null && mediaStream.hasVideo()) { //enabling own video
                videoStream.setVisibility(VISIBLE);
                videoStream.attach(user.getUserId(), mediaStream);
            } else { // disabling own video
                videoStream.setVisibility(View.GONE);
                videoStream.unAttach();
            }
        } else { // displaying participant conference video
            addParticipant(user);
        }
    }

    private void addParticipant(DefaultConferenceUser user) {
        adapter.addParticipant(user);
        adapter.updateMediaStreams(mediaStreamMap);
        adapter.notifyDataSetChanged();
    }

    public void onEvent(FilePresentationStarted event) {
        if (null != event) {
            if (mFilePresentationStarted == null || !event.getFileId().equals(mFilePresentationStarted.getFileId())) {
                mFilePresentationStarted = event;
                loadImageFromCurrentPresentation(mFilePresentationStarted.getPosition());

                if (mFilePresentationStarted.isOwner()) {
                    showNavigationButtons();
                } else {
                    hideNavigationButtons();
                }
            }
        }
    }

    private void loadImageFromCurrentPresentation(int page) {
        presentation_lander.setVisibility(View.VISIBLE);
        Picasso.with(this)
                .load(VoxeetSdk.getInstance()
                        .getFilePresentationService()
                        .getImageUrl(mFilePresentationStarted.getFileId(),
                                page))
                .into(presentation_lander);
    }

    public void onEvent(FilePresentationStopped event) {
        if (null != event) {
            mFilePresentationStarted = null;

            presentation_lander.setVisibility(View.GONE);
            hideButtonsPresentation();
            hideNavigationButtons();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FilePresentationStartedEvent event) {
        //You can override the event also here
        //especially when the presentation was not started by the current user
        onEvent(event.getEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FilePresentationStoppedEvent event) {
        //You can override the event also here
        //especially when the presentation was not started by the current user
        onEvent(event.getEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FilePresentationUpdatedEvent event) {
        onEvent(event.getEvent());
    }


    public void onEvent(FilePresentationUpdated event) {

        Log.d(TAG, "onEvent: " + mFilePresentationStarted.getFileId() + " " + event.getFileId()
                + " " + mFilePresentationStarted.getPosition() + " " + event.getPosition());
        if (null != event && null != mFilePresentationStarted
                && mFilePresentationStarted.getFileId().equals(event.getFileId())
                && mFilePresentationStarted.getPosition() != event.getPosition()) {
            mFilePresentationStarted.setPosition(event.getPosition());

            loadImageFromCurrentPresentation(mFilePresentationStarted.getPosition());

            if (mFilePresentationStarted.isOwner()) {
                showNavigationButtons();
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceJoinedSuccessEvent event) {
        Log.d("CreateConfActivity", "ConferencejoinedSuccessEvent " + event.getConferenceId() + " " + event.getAliasId());
        hideProgress();

        leave.setVisibility(VISIBLE);

        joinLayout.setVisibility(View.GONE);

        conferenceOptions.setVisibility(VISIBLE);

        if (action == MainActivity.DEMO || action == MainActivity.REPLAY) {
            record.setVisibility(View.GONE);
            video.setVisibility(View.GONE);
        } else {
            aliasId.setVisibility(VISIBLE);
            aliasId.setText(event.getAliasId() != null ? event.getAliasId() : event.getConferenceId());

            sendText.setVisibility(VISIBLE);
        }

        List<UserInfo> external_ids = UsersHelper.getExternalIds(VoxeetPreferences.id());

        showStartPresentation();

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

                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceLeftSuccessEvent event) {
        Log.d(TAG, "onEvent: " + event.getClass().getSimpleName());
        onConferenceEnding();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceEnded event) {
        Log.d(TAG, "onEvent: " + event.getClass().getSimpleName());
        onConferenceEnding();
    }

    private void onConferenceEnding() {
        VoxeetSdk.getInstance().unregister(CreateConfActivity.this);

        screenShare.unAttach(); // unattaching just in case

        videoStream.unAttach(); // unattaching just in case

        finish();
    }

    @Subscribe
    public void onEvent(final ConferenceUserLeftEvent event) {
        Log.d(TAG, "onEvent: " + event.getClass().getSimpleName());
        adapter.removeParticipant(event.getUser());
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onEvent(final ConferenceUserJoinedEvent event) {
        Log.d("CreateConfActivity", "ConferenceUserJoinedEvent " + event.message() + " " + event.getUser().getUserInfo().getExternalId() + " " + event.getUser().isOwner());
        updateStreams(event.getUser(), event.getMediaStream());
    }

    @Subscribe
    public void onEvent(final ConferenceUserUpdatedEvent event) {
        Log.d(TAG, "onEvent: " + event.getClass().getSimpleName());
        updateStreams(event.getUser(), event.getMediaStream());
    }

    @Subscribe
    public void onEvent(ScreenStreamAddedEvent event) {
        Log.d(TAG, "onEvent: " + event.getClass().getSimpleName());
        MediaStream mediaStream = event.getMediaStream();
        if (mediaStream != null && mediaStream.hasVideo()) { // attaching stream
            screenShare.setVisibility(VISIBLE);
            screenShare.attach(event.getPeer(), mediaStream);
        }
    }

    @Subscribe
    public void onEvent(RecordingStatusUpdateEvent event) {
        Log.d(TAG, "onEvent: " + event.getClass().getSimpleName());
        if (event.getRecordingStatus().equalsIgnoreCase(RecordingStatus.RECORDING.name())) {
            ((SampleApplication) getApplication()).saveRecordingConference(new Recording(event.getConferenceId(), event.getTimeStamp()));

            record.setText("Stop Recording");
        } else {
            record.setText("Start Recording");
        }
    }

    @Subscribe
    public void onEvent(ScreenStreamRemovedEvent event) { // unattaching stream
        Log.d(TAG, "onEvent: " + event.getClass().getSimpleName());
        screenShare.setVisibility(View.GONE);
        screenShare.unAttach();
    }

    @Subscribe
    public void onEvent(MessageReceived event) {
        Log.e(TAG, event.getMessage());
    }

    public void showProgress() {
        try {
            if (dialog != null)
                return;
            dialog = ProgressDialog.show(this, "", getString(R.string.loading));
            dialog.setCancelable(true);
        } catch (Exception e) {
            Log.e(TAG, "error", e.getCause());
        }
    }

    public void hideProgress() {
        try {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "error", e.getCause());
        }
    }

    private void showStartPresentation() {
        startPresentation.setVisibility(View.VISIBLE);
        stopPresentation.setVisibility(View.GONE);

        hideNavigationButtons();
    }

    private void showStopPresentation() {
        startPresentation.setVisibility(View.GONE);
        stopPresentation.setVisibility(View.VISIBLE);

        showNavigationButtons();
    }

    private void hideButtonsPresentation() {
        startPresentation.setVisibility(View.GONE);
        stopPresentation.setVisibility(View.GONE);
    }

    private void hideNavigationButtons() {
        nextPage.setVisibility(View.GONE);
        previousPage.setVisibility(View.GONE);
    }

    private void showNavigationButtons() {
        if (null != mFilePresentationStarted && mFilePresentationStarted.isOwner()) {
            nextPage.setVisibility(View.GONE);
            previousPage.setVisibility(View.GONE);
            if (mFilePresentationStarted.getPosition() + 1 < mFilePresentationStarted.getImageCount())
                nextPage.setVisibility(View.VISIBLE);

            if (mFilePresentationStarted.getPosition() - 1 >= 0)
                previousPage.setVisibility(View.VISIBLE);
        } else {
            hideNavigationButtons();
        }
    }

    private void updatePresentation(int new_position) {
        if (!mFilePresentationStarted.isOwner())
            hideButtonsPresentation();
        else
            showStopPresentation();

        VoxeetSdk.getInstance()
                .getFilePresentationService()
                .updatePresentation(mFilePresentationStarted.getFileId(), new_position)
                .then(new PromiseExec<FilePresentationUpdated, Object>() {
                    @Override
                    public void onCall(@Nullable FilePresentationUpdated result, @NonNull Solver<Object> solver) {
                        //invalidate)
                        onEvent(result);
                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        error.printStackTrace();
                        Log.d(TAG, "onError: " + error.getMessage());
                        showNavigationButtons();
                    }
                });


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QualityIndicators event) {
        Log.d(TAG, "onEvent: MOS := " + event.getMos());
    }
}
