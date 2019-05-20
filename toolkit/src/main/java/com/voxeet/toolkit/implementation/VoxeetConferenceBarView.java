package com.voxeet.toolkit.implementation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.voxeet.android.media.MediaStream;
import com.voxeet.audio.AudioRoute;
import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.core.abs.ConferenceService;
import com.voxeet.sdk.core.abs.information.ConferenceInformation;
import com.voxeet.sdk.core.abs.information.ConferenceUserType;
import com.voxeet.sdk.core.preferences.VoxeetPreferences;
import com.voxeet.sdk.events.AudioRouteChangeEvent;
import com.voxeet.sdk.events.error.PermissionRefusedEvent;
import com.voxeet.sdk.events.success.StartScreenShareAnswerEvent;
import com.voxeet.sdk.events.success.StopScreenShareAnswerEvent;
import com.voxeet.sdk.utils.AudioType;
import com.voxeet.sdk.utils.Validate;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.controllers.VoxeetToolkit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;

/**
 * Created by ROMMM on 9/29/15.
 */
public class VoxeetConferenceBarView extends VoxeetView {

    private final String TAG = VoxeetConferenceBarView.class.getSimpleName();

    /**
     * The constant RESULT_CAMERA.
     */

    public static final int RECORD = 0x0200;
    public static final int MUTE = 0x201;
    public static final int HANG_UP = 0x202;
    public static final int SPEAKER = 0x203;
    public static final int VIDEO = 0x204;

    private static final String[] MANDATORY_STRINGS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    /**
     * handling buttons visibility
     */
    private boolean displayRecord = true;
    private boolean displayAudio = true;
    private boolean displayMute = true;
    private boolean displayCamera = true;
    private boolean displayLeave = true;
    private boolean displayScreenshare = true;

    private LinearLayout container;

    private ImageView microphone;
    private ImageView speaker;
    private ImageView camera;
    private ImageView hangup;
    private ImageView recording;
    private View screenshare;

    private ViewGroup microphone_wrapper;
    private ViewGroup speaker_wrapper;
    private ViewGroup camera_wrapper;
    private ViewGroup hangup_wrapper;
    private ViewGroup recording_wrapper;
    private ViewGroup screenshare_wrapper;
    private ViewGroup view_3d_wrapper;

    private View view_3d;
    private OnView3D view3d_listener;

    /**
     * Instantiates a new Voxeet conference bar view.
     *
     * @param context the context
     */
    public VoxeetConferenceBarView(Context context) {
        super(context);

        setUserPreferences();
    }

    /**
     * Instantiates a new Voxeet conference bar view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetConferenceBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        updateAttrs(attrs);

        setUserPreferences();
    }

    private VoxeetConferenceBarView addButton(int action, int drawable, OnClickListener listener) {
        ImageView imageView = from(action);
        imageView.setImageResource(drawable);

        // setting layout params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(10, 10, 10, 10);

        imageView.setLayoutParams(params);

        imageView.setPadding(10, 10, 10, 10);

        // listener
        imageView.setOnClickListener(listener);

        container.addView(imageView);

        return this;
    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetConferenceBarView);

        displayRecord = attributes.getBoolean(R.styleable.VoxeetConferenceBarView_record_button, true);
        displayAudio = attributes.getBoolean(R.styleable.VoxeetConferenceBarView_audio_button, true);
        displayMute = attributes.getBoolean(R.styleable.VoxeetConferenceBarView_mute_button, true);
        displayCamera = attributes.getBoolean(R.styleable.VoxeetConferenceBarView_video_button, true);
        displayScreenshare = attributes.getBoolean(R.styleable.VoxeetConferenceBarView_screenshare_button, true);
        displayLeave = attributes.getBoolean(R.styleable.VoxeetConferenceBarView_leave_button, true);

        attributes.recycle();
    }

    public void setDisplayRecord(boolean displayRecord) {
        this.displayRecord = displayRecord;
        setUserPreferences();
    }

    public void setDisplayAudio(boolean displayAudio) {
        this.displayAudio = displayAudio;
        setUserPreferences();
    }

    public void setDisplayMute(boolean displayMute) {
        this.displayMute = displayMute;
        setUserPreferences();
    }

    public void setDisplayCamera(boolean displayCamera) {
        this.displayCamera = displayCamera;
        setUserPreferences();
    }

    public void setDisplayLeave(boolean displayLeave) {
        this.displayLeave = displayLeave;
        setUserPreferences();
    }

    public void setDisplayScreenshare(boolean displayScreenshare) {
        this.displayScreenshare = displayScreenshare;
        setUserPreferences();
    }

    /**
     * Instantiates a new Voxeet conference bar view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public VoxeetConferenceBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        updateAttrs(attrs);

        setUserPreferences();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (null != VoxeetSdk.getInstance() && null != VoxeetSdk.getInstance().getConferenceService()) {
            ConferenceService service = VoxeetSdk.getInstance().getConferenceService();
            ConferenceInformation information = service.getCurrentConferenceInformation();

            if (null != information && information.isOwnVideoStarted() && !service.isVideoOn()) {
                service.startVideo().then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                        Log.d(TAG, "onAttachedToWindow: starting video ? success:=" + result);
                    }
                }).error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        Log.d(TAG, "onAttachedToWindow: starting video ? thrown:=" + error);
                        error.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        updateSpeakerButton();

        if (null != screenshare) {
            screenshare.setSelected(VoxeetSdk.getInstance().getConferenceService().isScreenShareOn());
        }
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onStop();
    }

    /**
     * Sets the recording button color if the conference is being recorded or not.
     *
     * @param isRecording
     */
    @Override
    public void onRecordingStatusUpdated(boolean isRecording) {
        super.onRecordingStatusUpdated(isRecording);

        if (recording != null)
            recording.setSelected(isRecording);
    }

    @Override
    public void onMediaStreamUpdated(String userId, Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamUpdated(userId, mediaStreams);

        if (camera != null && userId.equalsIgnoreCase(VoxeetPreferences.id()) && mediaStreams.get(userId) != null) {
            camera.setSelected(mediaStreams.get(userId).videoTracks().size() > 0);
        }
    }

    @Override
    public void onScreenShareMediaStreamUpdated(@NonNull String userId, @NonNull Map<String, MediaStream> screen_share_media_streams) {
        super.onScreenShareMediaStreamUpdated(userId, screen_share_media_streams);


        MediaStream stream = screen_share_media_streams.get(userId);

        if (screenshare != null && userId.equalsIgnoreCase(VoxeetPreferences.id()) && null != stream) {
            screenshare.setSelected(stream.isScreenShare());
        }
    }

    @Override
    public void init() {
        setWillNotDraw(false);
    }

    @Override
    protected void bindView(View v) {
        container = (LinearLayout) v.findViewById(R.id.container);

        view_3d = v.findViewById(R.id.view_3d);
        view_3d_wrapper = v.findViewById(R.id.view_3d_wrapper);
        if (null != view_3d) {
            invalidateView3D();
            view_3d.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    on3DView();
                }
            });
        }
        speaker = (ImageView) v.findViewById(R.id.speaker);
        speaker_wrapper = v.findViewById(R.id.speaker_wrapper);
        updateSpeakerButton();
        speaker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                speaker.setSelected(!speaker.isSelected());

                VoxeetSdk.getInstance()
                        .getAudioService()
                        .setAudioRoute(speaker.isSelected() ? AudioRoute.ROUTE_SPEAKER : AudioRoute.ROUTE_PHONE);
            }
        });

        hangup = (ImageView) v.findViewById(R.id.hangup);
        hangup_wrapper = v.findViewById(R.id.hangup_wrapper);
        hangup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VoxeetSdk.getInstance()
                        .getAudioService().playSoundType(AudioType.HANGUP);

                VoxeetSdk.getInstance()
                        .getConferenceService()
                        .leave()
                        .then(new PromiseExec<Boolean, Object>() {
                            @Override
                            public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                                //manage the result ?
                            }
                        })
                        .error(new ErrorPromise() {
                            @Override
                            public void onError(Throwable error) {
                                //manage the error ?
                            }
                        });
            }
        });

        microphone = v.findViewById(R.id.microphone);
        microphone_wrapper = v.findViewById(R.id.microphone_wrapper);
        microphone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMute();
            }
        });

        camera = (ImageView) v.findViewById(R.id.camera);
        camera_wrapper = v.findViewById(R.id.camera_wrapper);
        camera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCamera();
            }
        });

        recording = (ImageView) v.findViewById(R.id.recording);
        recording_wrapper = v.findViewById(R.id.recording_wrapper);
        recording.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VoxeetSdk.getInstance().getConferenceService().toggleRecording();
            }
        });

        screenshare = v.findViewById(R.id.screenshare);
        screenshare_wrapper = v.findViewById(R.id.screenshare_wrapper);
        if (null != screenshare) {
            screenshare.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleScreenShare();
                }
            });
        }

        if (!checkMicrophonePermission()) {
            microphone.setSelected(true);
            VoxeetSdk.getInstance().getConferenceService().mute(true);
        }
    }

    private void on3DView() {
        if (null != view3d_listener) view3d_listener.onView3D();
    }

    protected void toggleMute() {
        boolean new_muted_state = !VoxeetSdk.getInstance().getConferenceService().isMuted();

        if (new_muted_state || checkMicrophonePermission()) {
            //if we unmute, check for microphone state
            microphone.setSelected(new_muted_state);

            VoxeetSdk.getInstance().getConferenceService().mute(new_muted_state);
        }
    }

    protected void toggleCamera() {
        if (checkCameraPermission()) {
            VoxeetSdk.getInstance().getConferenceService().toggleVideo();
        }
    }

    protected void toggleScreenShare() {
        if (canScreenShare()) {
            Point size = VoxeetSdk.getInstance().getScreenShareService().getScreenSize(getContext());
            VoxeetSdk.getInstance().getScreenShareService()
                    .setScreenSizeInformation(VoxeetSdk.getInstance().getScreenShareService().getScreenSizeScaled(size, 720))
                    .toggleScreenShare();
        }
    }

    protected void turnCamera(boolean on) {
        if (checkCameraPermission()) {

            VoxeetSdk.getInstance().getConferenceService()
                    .startRecording()
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
    }

    /**
     * Updating buttons visibility when changing one of the attributes programmatically.
     */
    private void setUserPreferences() {
        boolean listener = isListener();

        if (recording != null)
            recording_wrapper.setVisibility(displayRecord && !listener? VISIBLE : GONE);

        if (microphone != null)
            microphone_wrapper.setVisibility(displayMute && !listener ? VISIBLE : GONE);

        if (speaker != null)
            speaker_wrapper.setVisibility(displayAudio ? VISIBLE : GONE);

        if (camera != null)
            camera_wrapper.setVisibility(displayCamera && !listener ? VISIBLE : GONE);

        if (hangup != null)
            hangup_wrapper.setVisibility(displayLeave ? VISIBLE : GONE);

        boolean screenShareEnabled = VoxeetToolkit.getInstance().getConferenceToolkit().isScreenShareEnabled();
        if (screenshare != null)
            screenshare_wrapper.setVisibility(displayScreenshare && !listener && screenShareEnabled ? VISIBLE : GONE);
    }

    @Override
    public void onConferenceJoined(@NonNull String conference_id) {
        super.onConferenceJoined(conference_id);

        updateVisibilities(View.VISIBLE);
    }

    @Override
    public void onConferenceCreating() {
        super.onConferenceCreating();

        updateVisibilities(View.GONE);
    }

    @Override
    public void onConferenceCreation(@NonNull String conferenceId) {
        super.onConferenceCreation(conferenceId);

        updateVisibilities(View.GONE);
    }

    @Override
    public void onConferenceJoining(@NonNull String conference_id) {
        super.onConferenceJoining(conference_id);

        updateVisibilities(View.GONE);
    }

    private void updateVisibilities(int visibility) {
        boolean listener = isListener();

        if (recording != null)
            recording_wrapper.setVisibility(displayRecord && !listener? visibility : GONE);

        if (microphone != null)
            microphone_wrapper.setVisibility(displayMute && !listener ? visibility : GONE);

        if (speaker != null)
            speaker_wrapper.setVisibility(displayAudio ? visibility : GONE);

        if (camera != null)
            camera_wrapper.setVisibility(displayCamera && !listener ? visibility : GONE);

        if (hangup != null)
            hangup_wrapper.setVisibility(displayLeave ? visibility : GONE);


        boolean screenShareEnabled = VoxeetToolkit.getInstance().getConferenceToolkit().isScreenShareEnabled();
        if (screenshare != null)
            screenshare_wrapper.setVisibility(displayScreenshare && screenShareEnabled ? visibility : GONE);
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_conference_bar_view;
    }

    /**
     * On toggle size.
     *
     * @param isMaxedOut the view is maxed out or not
     */
    public void onToggleSize(boolean isMaxedOut) {
        setVisibility(isMaxedOut ? VISIBLE : GONE);
    }

    public ImageView from(int action) {
        switch (action) {
            case HANG_UP:
                return (hangup = new ImageView(getContext()));
            case MUTE:
                return (microphone = new ImageView(getContext()));
            case SPEAKER:
                return (speaker = new ImageView(getContext()));
            case VIDEO:
                return (camera = new ImageView(getContext()));
            case RECORD:
                return (recording = new ImageView(getContext()));
            default:
                return new ImageView(getContext());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull StopScreenShareAnswerEvent event) {
        if (null != screenshare) {
            screenshare.setSelected(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull StartScreenShareAnswerEvent event) {
        if (null != screenshare) {
            screenshare.setSelected(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AudioRouteChangeEvent event) {
        updateSpeakerButton();
    }


    private boolean checkMicrophonePermission() {
        return checkPermission(Manifest.permission.RECORD_AUDIO,
                "checkMicrophonePermission : RECORD_AUDIO permission  _is not_ set in your manifest. Please update accordingly",
                PermissionRefusedEvent.RESULT_MICROPHONE);
    }


    private boolean checkCameraPermission() {
        return checkPermission(Manifest.permission.CAMERA,
                "checkCameraPermission: CAMERA permission _is not_ set in your manifest. Please update accordingly",
                PermissionRefusedEvent.RESULT_CAMERA);
    }

    private boolean canScreenShare() {
        return Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT;
    }


    private boolean checkPermission(@NonNull String permission, @NonNull String error_message, int result_code) {
        if (!Validate.hasPermissionInManifest(getContext(), permission)) {
            Log.d(TAG, error_message);
            return false;
        } else if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Validate.requestMandatoryPermissions(VoxeetToolkit.getInstance().getCurrentActivity(),
                        new String[]{permission}, result_code);
            }
            return false;
        } else {
            return true;
        }
    }

    private void requestMandatoryPermissions() {
        List<String> permissions_to_request = new ArrayList<>();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : MANDATORY_STRINGS) {
                if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    permissions_to_request.add(permission);
                }
            }

            Validate.requestMandatoryPermissions(VoxeetToolkit.getInstance().getCurrentActivity(),
                    permissions_to_request.toArray(new String[permissions_to_request.size()]),
                    PermissionRefusedEvent.RESULT_MANDATORY);
        }
    }

    private void updateSpeakerButton() {
        if (null != VoxeetSdk.getInstance()) {
            boolean headset = VoxeetSdk.getInstance().getAudioService().isWiredHeadsetOn();
            headset |= VoxeetSdk.getInstance().getAudioService().isBluetoothHeadsetConnected();
            if (headset) {
                speaker.setAlpha(0.5f);
                speaker.setEnabled(false);
                speaker.setSelected(false);
            } else {
                speaker.setAlpha(1.0f);
                speaker.setEnabled(true);
                speaker.setSelected(VoxeetSdk.getInstance().getAudioService().isSpeakerOn());
            }
        }
    }

    public void setView3dListener(OnView3D listener) {
        view3d_listener = listener;
        invalidateView3D();
    }

    private void invalidateView3D() {
        view_3d_wrapper.setVisibility(view3d_listener != null ? View.VISIBLE : View.GONE);
    }

    public interface OnView3D {
        void onView3D();
    }

    private boolean isListener() {
        ConferenceInformation information =  VoxeetSdk.getInstance().getConferenceService()
                .getCurrentConferenceInformation();
        Log.d(TAG, "isListener: " + information);
        return null == information || ConferenceUserType.LISTENER.equals(information.getConferenceUserType());
    }
}
