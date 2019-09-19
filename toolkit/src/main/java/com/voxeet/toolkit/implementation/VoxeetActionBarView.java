package com.voxeet.toolkit.implementation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.content.Intent;

import com.voxeet.android.media.MediaStream;
import com.voxeet.audio.AudioRoute;
import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.core.preferences.VoxeetPreferences;
import com.voxeet.sdk.core.services.ConferenceService;
import com.voxeet.sdk.core.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.core.services.conference.information.ConferenceUserType;
import com.voxeet.sdk.events.error.PermissionRefusedEvent;
import com.voxeet.sdk.events.sdk.AudioRouteChangeEvent;
import com.voxeet.sdk.events.sdk.StartScreenShareAnswerEvent;
import com.voxeet.sdk.events.sdk.StopScreenShareAnswerEvent;
import com.voxeet.sdk.utils.AudioType;
import com.voxeet.sdk.utils.Validate;
import com.voxeet.sdk.utils.Annotate;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.configuration.ActionBar;
import com.voxeet.toolkit.controllers.VoxeetToolkit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;

@Annotate
public class VoxeetActionBarView extends VoxeetView {

    private final String TAG = VoxeetActionBarView.class.getSimpleName();

    /**
     * handling buttons visibility
     */
    private boolean displaySpeaker = true;
    private boolean displayMute = true;
    private boolean displayCamera = true;
    private boolean displayLeave = true;
    private boolean displayScreenShare = true;

    private ImageView microphone;
    private ImageView speaker;
    private ImageView camera;
    private ImageView hangup;
    private ImageView screenshare;

    private ViewGroup microphone_wrapper;
    private ViewGroup speaker_wrapper;
    private ViewGroup camera_wrapper;
    private ViewGroup hangup_wrapper;
    private ViewGroup screenshare_wrapper;
    private ViewGroup view_3d_wrapper;

    private View view_3d;
    private OnView3D view3d_listener;
    private Context currentContext;

    /**
     * Instantiates a new Voxeet conference bar view.
     *
     * @param context the context
     */
    public VoxeetActionBarView(Context context) {
        super(context);
        currentContext = context;

        setUserPreferences();
    }

    /**
     * Instantiates a new Voxeet conference bar view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        updateAttrs(attrs);

        setUserPreferences();
    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetActionBarView);

        displaySpeaker = attributes.getBoolean(R.styleable.VoxeetActionBarView_speaker_button, true);
        displayMute = attributes.getBoolean(R.styleable.VoxeetActionBarView_mute_button, true);
        displayCamera = attributes.getBoolean(R.styleable.VoxeetActionBarView_video_button, true);
        displayScreenShare = attributes.getBoolean(R.styleable.VoxeetActionBarView_screenshare_button, true);
        displayLeave = attributes.getBoolean(R.styleable.VoxeetActionBarView_leave_button, true);

        attributes.recycle();
    }

    public void setDisplayMute(boolean displayMute) {
        this.displayMute = displayMute;
        setUserPreferences();
    }

    public void setDisplayCamera(boolean displayCamera) {
        this.displayCamera = displayCamera;
        setUserPreferences();
    }

    public void setDisplaySpeaker(boolean displaySpeaker) {
        this.displaySpeaker = displaySpeaker;
        setUserPreferences();
    }

    public void setDisplayScreenShare(boolean displayScreenShare) {
        this.displayScreenShare = displayScreenShare;
        setUserPreferences();
    }

    public void setDisplayLeave(boolean displayLeave) {
        this.displayLeave = displayLeave;
        setUserPreferences();
    }

    /**
     * Instantiates a new Voxeet conference bar view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public VoxeetActionBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        updateAttrs(attrs);

        setUserPreferences();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (null != VoxeetSdk.conference()) {
            ConferenceService service = VoxeetSdk.conference();
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
            screenshare.setSelected(VoxeetSdk.conference().isScreenShareOn());
        }
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onStop();
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

                VoxeetSdk.audio().setAudioRoute(speaker.isSelected() ? AudioRoute.ROUTE_SPEAKER : AudioRoute.ROUTE_PHONE);
                Intent intent = new Intent();
                intent.setAction("OnCallReceive");
                intent.putExtra("isSpeaker", true);
                currentContext.sendBroadcast(intent);
            }
        });

        hangup = (ImageView) v.findViewById(R.id.hangup);
        hangup_wrapper = v.findViewById(R.id.hangup_wrapper);
        hangup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VoxeetSdk.audio().playSoundType(AudioType.HANGUP);

                VoxeetSdk.conference().leave()
                        .then(new PromiseExec<Boolean, Object>() {
                            @Override
                            public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                                //manage the result ?
                                Intent intent = new Intent();
                                intent.setAction("OnCallReceive");
                                intent.putExtra("isLeave", true);
                                currentContext.sendBroadcast(intent);
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

        ActionBar configuration = VoxeetToolkit.getInstance().getConferenceToolkit().Configuration.ActionBar;
        StateListDrawable selector_camera = createOverridenSelector(configuration.camera_on, configuration.camera_off);
        StateListDrawable selector_microphone = createOverridenSelector(configuration.mic_on, configuration.mic_off);
        StateListDrawable selector_screenshare = createOverridenSelector(configuration.screenshare_on, configuration.screenshare_off);
        StateListDrawable selector_speaker = createOverridenSelector(configuration.speaker_on, configuration.speaker_off);
        StateListDrawable selector_hangup = createOverridenSelectorPressed(configuration.hangup, configuration.hangup_pressed);

        if(null != selector_camera) camera.setImageDrawable(selector_camera);
        if(null != selector_microphone) microphone.setImageDrawable(selector_microphone);
        if(null != selector_screenshare) screenshare.setImageDrawable(selector_screenshare);
        if(null != selector_speaker) speaker.setImageDrawable(selector_speaker);
        if(null != selector_hangup) hangup.setImageDrawable(selector_hangup);

        if (!checkMicrophonePermission()) {
            microphone.setSelected(true);
            VoxeetSdk.conference().mute(true);
        }
    }

    private void on3DView() {
        if (null != view3d_listener) view3d_listener.onView3D();
    }

    protected void toggleMute() {
        boolean new_muted_state = !VoxeetSdk.conference().isMuted();

        if (new_muted_state || checkMicrophonePermission()) {
            //if we unmute, check for microphone state
            microphone.setSelected(new_muted_state);

            VoxeetSdk.conference().mute(new_muted_state);
            Intent intent = new Intent();
            intent.setAction("OnCallReceive");
            intent.putExtra("isMute", true);
            currentContext.sendBroadcast(intent);
        }
    }

    protected void toggleCamera() {
        if (checkCameraPermission()) {
            VoxeetSdk.conference().toggleVideo();
            Intent intent = new Intent();
            intent.setAction("OnCallReceive");
            intent.putExtra("isVideo", true);
            currentContext.sendBroadcast(intent);
        }
    }

    protected void toggleScreenShare() {
        if (canScreenShare()) {
            Point size = VoxeetSdk.screenShare().getScreenSize(getContext());
            VoxeetSdk.screenShare()
                    .setScreenSizeInformation(VoxeetSdk.screenShare().getScreenSizeScaled(size, 720))
                    .toggleScreenShare();
        }
    }

    /**
     * Updating buttons visibility when changing one of the attributes programmatically.
     */
    private void setUserPreferences() {
        boolean listener = isListener();

        if (microphone != null)
            microphone_wrapper.setVisibility(displayMute && !listener ? VISIBLE : GONE);

        if (speaker != null)
            speaker_wrapper.setVisibility(displaySpeaker ? VISIBLE : GONE);

        if (camera != null)
            camera_wrapper.setVisibility(displayCamera && !listener ? VISIBLE : GONE);

        if (hangup != null)
            hangup_wrapper.setVisibility(displayLeave ? VISIBLE : GONE);

        boolean screenShareEnabled = VoxeetToolkit.getInstance().getConferenceToolkit().isScreenShareEnabled();
        if (screenshare != null)
            screenshare_wrapper.setVisibility(displayScreenShare && !listener && screenShareEnabled ? VISIBLE : GONE);
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

        if (microphone != null)
            microphone_wrapper.setVisibility(displayMute && !listener ? visibility : GONE);

        if (speaker != null)
            speaker_wrapper.setVisibility(displaySpeaker ? visibility : GONE);

        if (camera != null)
            camera_wrapper.setVisibility(displayCamera && !listener ? visibility : GONE);

        if (hangup != null)
            hangup_wrapper.setVisibility(displayLeave ? visibility : GONE);


        boolean screenShareEnabled = VoxeetToolkit.getInstance().getConferenceToolkit().isScreenShareEnabled();
        if (screenshare != null)
            screenshare_wrapper.setVisibility(displayScreenShare && screenShareEnabled ? visibility : GONE);
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

    private void updateSpeakerButton() {
        if (null != VoxeetSdk.instance()) {
            boolean headset = VoxeetSdk.audio().isWiredHeadsetOn();
            headset |= VoxeetSdk.audio().isBluetoothHeadsetConnected();
            if (headset) {
                speaker.setAlpha(0.5f);
                speaker.setEnabled(false);
                speaker.setSelected(false);
            } else {
                speaker.setAlpha(1.0f);
                speaker.setEnabled(true);
                speaker.setSelected(VoxeetSdk.audio().isSpeakerOn());
            }
        }
    }

    @Deprecated
    public void setView3dListener(OnView3D listener) {
        view3d_listener = listener;
        invalidateView3D();
    }

    private void invalidateView3D() {
        view_3d_wrapper.setVisibility(view3d_listener != null ? View.VISIBLE : View.GONE);
    }

    @Deprecated
    public interface OnView3D {
        void onView3D();
    }

    private boolean isListener() {
        ConferenceInformation information = VoxeetSdk.conference()
                .getCurrentConferenceInformation();
        Log.d(TAG, "isListener: " + information);
        return null == information || ConferenceUserType.LISTENER.equals(information.getConferenceUserType());
    }

    private boolean isValidOverride(Integer button_on, Integer button_off) {
        return null != button_off && null != button_on;
    }

    @Nullable
    private StateListDrawable createOverridenSelectorPressed(Integer button_on, Integer button_off) {
        if(!isValidOverride(button_on, button_off)) return null;

        Resources resources = getContext().getResources();
        Drawable drawable_on = resources.getDrawable(button_on);
        Drawable drawable_off = resources.getDrawable(button_off);

        StateListDrawable states = new StateListDrawable();

        states.addState(new int[]{android.R.attr.stateNotNeeded}, drawable_off);
        states.addState(new int[]{android.R.attr.state_pressed}, drawable_on);

        return states;
    }

    @Nullable
    private StateListDrawable createOverridenSelector(Integer button_on, Integer button_off) {
        if(!isValidOverride(button_on, button_off)) return null;

        Resources resources = getContext().getResources();
        Drawable drawable_on = resources.getDrawable(button_on);
        Drawable drawable_off = resources.getDrawable(button_off);

        StateListDrawable states = new StateListDrawable();

        states.addState(new int[]{android.R.attr.stateNotNeeded}, drawable_off);
        states.addState(new int[]{android.R.attr.state_selected}, drawable_on);

        return states;
    }
}
