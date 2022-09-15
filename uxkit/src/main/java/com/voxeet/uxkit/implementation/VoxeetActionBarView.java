package com.voxeet.uxkit.implementation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;
import com.voxeet.sdk.events.sdk.AudioRouteChangeEvent;
import com.voxeet.sdk.events.sdk.StartScreenShareAnswerEvent;
import com.voxeet.sdk.events.sdk.StopScreenShareAnswerEvent;
import com.voxeet.sdk.events.v2.VideoStateEvent;
import com.voxeet.sdk.events.v3.MicrophoneStateEvent;
import com.voxeet.sdk.media.audio.SoundManager;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.services.AudioService;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.ScreenShareService;
import com.voxeet.sdk.services.SessionService;
import com.voxeet.sdk.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.services.conference.information.ConferenceParticipantType;
import com.voxeet.sdk.services.media.MediaState;
import com.voxeet.sdk.services.media.MicrophoneState;
import com.voxeet.sdk.services.video.LocalVideo;
import com.voxeet.sdk.utils.AudioType;
import com.voxeet.sdk.utils.Filter;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.sdk.utils.Validate;
import com.voxeet.uxkit.R;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.configuration.ActionBar;
import com.voxeet.uxkit.controllers.VoxeetToolkit;
import com.voxeet.uxkit.events.UXKitNotInConferenceEvent;
import com.voxeet.uxkit.implementation.devices.IMediaDeviceControlListener;
import com.voxeet.uxkit.utils.ActionBarPermissionHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to display the various action buttons in the conference
 */
public class VoxeetActionBarView extends VoxeetView {

    private final static ShortLogger Log = UXKitLogger.createLogger(VoxeetActionBarView.class);

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
    private SoundManager.Call<List<MediaDevice>> onAudioDeviceUpdate = update -> {
        VoxeetActionBarView.this.devices = null != update ? update : new ArrayList<>();
        updateSpeakerButtonWithDevices();
    };
    private List<MediaDevice> devices = new ArrayList<>();

    @Nullable
    private IMediaDeviceControlListener onSpeakerAction;

    /**
     * Instantiates a new Voxeet conference bar view.
     *
     * @param context the context
     */
    public VoxeetActionBarView(Context context) {
        super(context);

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

    /**
     * Change how the mute button must be shown
     *
     * @param displayMute the new mute display state
     * @return the current instance
     */
    @NonNull
    public VoxeetActionBarView setDisplayMute(boolean displayMute) {
        this.displayMute = displayMute;
        setUserPreferences();
        return this;
    }

    /**
     * Change how the camera button must be shown
     *
     * @param displayCamera the new mute display state
     * @return the current instance
     */
    @NonNull
    public VoxeetActionBarView setDisplayCamera(boolean displayCamera) {
        this.displayCamera = displayCamera;
        setUserPreferences();
        return this;
    }

    /**
     * Change how the speaker button must be shown
     *
     * @param displaySpeaker the new mute display state
     * @return the current instance
     */
    @NonNull
    public VoxeetActionBarView setDisplaySpeaker(boolean displaySpeaker) {
        this.displaySpeaker = displaySpeaker;
        setUserPreferences();
        return this;
    }

    /**
     * Change how the screenshare button must be shown
     *
     * @param displayScreenShare the new mute display state
     * @return the current instance
     */
    @NonNull
    public VoxeetActionBarView setDisplayScreenShare(boolean displayScreenShare) {
        this.displayScreenShare = displayScreenShare;
        setUserPreferences();
        return this;
    }

    /**
     * Change how the leave/hang up button must be shown
     *
     * @param displayLeave the new mute display state
     * @return the current instance
     */
    @NonNull
    public VoxeetActionBarView setDisplayLeave(boolean displayLeave) {
        this.displayLeave = displayLeave;
        setUserPreferences();
        return this;
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

    private boolean attached = false;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        attached = true;

        VoxeetSDK.audio().getLocal().registerUpdateDevices(this.onAudioDeviceUpdate);
        updateSpeakerButton();

        checkMicrophoneButtonState();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        ConferenceInformation information = VoxeetSDK.conference().getCurrentConference();

        if (null != information && information.isOwnVideoStarted() && !MediaState.STARTED.equals(information.getVideoState())) {
            VoxeetSDK.video().getLocal().start()
                    .then(aBoolean -> {
                        Log.d("onAttachedToWindow: starting video ? success:=" + aBoolean);
                        updateCameraState();
                    })
                    .error(Log::e);
        }

        updateCameraState();
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onDetachedFromWindow();
    }

    /**
     * Must be called on any "resume" information (or parent's attached to window, etc...)
     */
    @Override
    public void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        checkMicrophoneButtonState();
        updateSpeakerButton();
        ConferenceService service = VoxeetSDK.conference();
        ConferenceInformation information = service.getCurrentConference();

        if (null != screenshare && null != information) {
            screenshare.setSelected(information.isScreenShareOn());
        }
        updateCameraState();
    }

    /**
     * Must be called on detached window for instance
     */
    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onStop();
    }

    /**
     * Method call when a stream is added to any user in the conference
     *
     * @param conference  the given conference
     * @param user        the user involved in the event
     * @param mediaStream the newly added stream
     */
    @Override
    public void onStreamAddedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamAddedEvent(conference, user, mediaStream);
        invalidateOwnStreams();
    }

    /**
     * Method call when a stream is updated for any user in the conference
     *
     * @param conference  the given conference
     * @param user        the user involved in the event
     * @param mediaStream the stream which has been updated
     */
    @Override
    public void onStreamUpdatedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamUpdatedEvent(conference, user, mediaStream);
        invalidateOwnStreams();
    }

    /**
     * Method call when a stream is remove from any user in the conference
     *
     * @param conference  the given conference
     * @param user        the user involved in the event
     * @param mediaStream the removed stream
     */
    @Override
    public void onStreamRemovedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamRemovedEvent(conference, user, mediaStream);
        invalidateOwnStreams();
    }

    /**
     * Method call to refresh the internal state for :
     * - the own camera button
     * - the screenshare button
     */
    public void invalidateOwnStreams() {

        Participant participant = Opt.of(VoxeetSDK.session()).then(SessionService::getParticipantId)
                .then(id -> VoxeetSDK.conference().findParticipantById(id)).orNull();
        if (null != participant) {
            MediaStream cameraStream = participant.streamsHandler().getFirst(MediaStreamType.Camera);
            MediaStream screenStream = participant.streamsHandler().getFirst(MediaStreamType.ScreenShare);
            if (camera != null && null != cameraStream) {
                camera.setSelected(cameraStream.videoTracks().size() > 0);
            }

            if (null != screenshare) {
                screenshare.setSelected(null != screenStream && screenStream.videoTracks().size() > 0);
            }
        } else {
            //the user is not yet in the conference or won't be
            updateVisibilities(View.GONE);
        }

        checkMicrophoneButtonState();
    }

    @Override
    public void init() {
        setWillNotDraw(false);
    }

    @Override
    protected void bindView(View v) {
        speaker = v.findViewById(R.id.speaker);
        speaker_wrapper = v.findViewById(R.id.speaker_wrapper);
        updateSpeakerButton();
        speaker.setOnClickListener(v16 -> {
            if (null != onSpeakerAction) {
                try {
                    onSpeakerAction.onMediaRouteButtonInteraction();
                } catch (Exception e) {
                    Log.e(e);
                }
                return;
            }
            speaker.setSelected(!speaker.isSelected());


            ActionBarPermissionHelper.checkBluetoothConnectPermission()
                    .then((ThenPromise<Boolean, List<MediaDevice>>) ok -> VoxeetSDK.audio().getLocal().enumerateDevices())
                    .then((ThenPromise<List<MediaDevice>, Boolean>) mediaDevices -> {
                        MediaDevice standard = oneOf(mediaDevices, ConnectionState.CONNECTED, DeviceType.INTERNAL_SPEAKER, DeviceType.NORMAL_MEDIA);
                        //MediaDevice external = oneOf(mediaDevices, ConnectionState.CONNECTED, DeviceType.EXTERNAL_SPEAKER, DeviceType.BLUETOOTH, DeviceType.WIRED_HEADSET);

                        MediaDevice to_connect;
                        if (null != standard) {
                            to_connect = oneOf(mediaDevices, ConnectionState.DISCONNECTED, DeviceType.BLUETOOTH, DeviceType.WIRED_HEADSET, DeviceType.EXTERNAL_SPEAKER);
                        } else {
                            to_connect = oneOf(mediaDevices, ConnectionState.DISCONNECTED, DeviceType.INTERNAL_SPEAKER, DeviceType.NORMAL_MEDIA);
                        }

                        if (null != to_connect) {
                            return VoxeetSDK.audio().getLocal().connect(to_connect);
                        }

                        try {
                            throw new IllegalStateException("No devices found");
                        } catch (Exception e) {
                            return Promise.reject(e);
                        }
                    }).error(error -> Toast.makeText(getContext(), "No devices found", Toast.LENGTH_SHORT).show());
        });

        hangup = v.findViewById(R.id.hangup);
        hangup_wrapper = v.findViewById(R.id.hangup_wrapper);
        hangup.setOnClickListener(v1 -> {
            VoxeetSDK.audio().getLocal().getSoundManager().playSoundType(AudioType.HANGUP);

            if (!VoxeetSDK.conference().isLive()) {
                EventBus.getDefault().post(new UXKitNotInConferenceEvent());
                return;
            }

            VoxeetSDK.conference().leave()
                    .then(aBoolean -> {
                        //manage the result ?
                    })
                    .error(Log::e);
        });

        microphone = v.findViewById(R.id.microphone);
        microphone_wrapper = v.findViewById(R.id.microphone_wrapper);
        microphone.setOnClickListener(v12 -> toggleMute());

        camera = v.findViewById(R.id.camera);
        camera_wrapper = v.findViewById(R.id.camera_wrapper);
        camera.setOnClickListener(v13 -> toggleCamera());

        screenshare = v.findViewById(R.id.screenshare);
        screenshare_wrapper = v.findViewById(R.id.screenshare_wrapper);
        if (null != screenshare) {
            screenshare.setOnClickListener(v14 -> toggleScreenShare());
        }

        ActionBar configuration = VoxeetToolkit.instance().getConferenceToolkit().Configuration.ActionBar;
        StateListDrawable selector_camera = createOverridenSelector(configuration.camera_on, configuration.camera_off);
        StateListDrawable selector_microphone = createOverridenSelector(configuration.mic_on, configuration.mic_off);
        StateListDrawable selector_screenshare = createOverridenSelector(configuration.screenshare_on, configuration.screenshare_off);
        StateListDrawable selector_speaker = createOverridenSelector(configuration.speaker_on, configuration.speaker_off);
        StateListDrawable selector_hangup = createOverridenSelectorPressed(configuration.hangup, configuration.hangup_pressed);

        if (null != selector_camera) camera.setImageDrawable(selector_camera);
        if (null != selector_microphone) microphone.setImageDrawable(selector_microphone);
        if (null != selector_screenshare) screenshare.setImageDrawable(selector_screenshare);
        if (null != selector_speaker) speaker.setImageDrawable(selector_speaker);
        if (null != selector_hangup) hangup.setImageDrawable(selector_hangup);

        if (!Validate.hasMicrophonePermissions(v.getContext())) {
            microphone.setSelected(true);
            VoxeetSDK.conference().mute(true);
        }

        updateCameraState();
        checkMicrophoneButtonState();
    }

    /**
     * Toggle the mute button
     */
    protected void toggleMute() {
        ActionBarPermissionHelper.checkMicrophonePermission().then(ok -> {
            boolean new_muted_state = !VoxeetSDK.conference().isMuted();
            microphone.setEnabled(ok);
            if (!ok) {
                //force mute
                new_muted_state = true;
            }

            // if we unmute, check for microphone state
            microphone.setSelected(new_muted_state);

            VoxeetSDK.conference().mute(new_muted_state);
        }).error(Log::e);
    }

    /**
     * Activate or deactivate the local camera
     */
    protected void toggleCamera() {
        ActionBarPermissionHelper.checkCameraPermission().then(ok -> {
            if (!ok) {
                Log.d("toggleCamera :: impossible, no permissions");
                return;
            }
            LocalVideo localVideo = VoxeetSDK.video().getLocal();
            Promise<Boolean> video = null;

            ConferenceInformation information = VoxeetSDK.conference().getCurrentConference();
            if (null != information) {
                switch (information.getVideoState()) {
                    case STARTED:
                        video = localVideo.stop();
                        break;
                    case STOPPED:
                        video = localVideo.start();
                        break;
                    default:
                }
            }

            if (null != video) {
                camera.setEnabled(false);
                video.then(aBoolean -> {
                    camera.setEnabled(true);
                    updateCameraState();
                }).error(error -> {
                    camera.setEnabled(true);
                    updateCameraState();
                });
            }
        }).error(Log::e);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VideoStateEvent event) {
        updateCameraState(event.mediaState);
    }

    private void updateCameraState() {
        ConferenceInformation information = VoxeetSDK.conference().getCurrentConference();
        if (null != information) updateCameraState(information.getVideoState());
    }

    private void updateCameraState(@NonNull MediaState videoState) {
        if (null == camera) return;

        switch (videoState) {
            case STARTED:
            case STOPPED:
                camera.setEnabled(true);
                break;
            case STOPPING:
            case STARTING:
                camera.setEnabled(false);
        }
    }

    /**
     * Activate or deactivate the local screenshare
     */
    protected void toggleScreenShare() {
        if (canScreenShare()) {
            ScreenShareService screenShareService = VoxeetSDK.screenShare();
            ConferenceInformation currentConference = VoxeetSDK.conference().getCurrentConference();
            if (currentConference == null) return;
            if (currentConference.isScreenShareOn()) {
                screenShareService.stopScreenShare().error(Throwable::printStackTrace);
            } else {
                screenShareService.sendRequestStartScreenShare();
            }
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

        boolean screenShareEnabled = VoxeetToolkit.instance().getConferenceToolkit().isScreenShareEnabled();
        if (screenshare != null)
            screenshare_wrapper.setVisibility(displayScreenShare && !listener && screenShareEnabled ? VISIBLE : GONE);
    }

    /**
     * Call this method when a conference has been successfully joined to update the visibilities
     *
     * @param conference the conference involved
     */
    @Override
    public void onConferenceJoined(@NonNull Conference conference) {
        super.onConferenceJoined(conference);

        updateVisibilities(View.VISIBLE);
        invalidateOwnStreams();
    }

    /**
     * Call this method when a conference is being created to update the visibilities
     */
    @Override
    public void onConferenceCreating() {
        super.onConferenceCreating();

        updateVisibilities(View.GONE);
        invalidateOwnStreams();
    }

    /**
     * Call this method when a conference being created to update the visilibity
     *
     * @param conference the conference involved
     */
    @Override
    public void onConferenceCreation(@NonNull Conference conference) {
        super.onConferenceCreation(conference);

        updateVisibilities(View.GONE);
        invalidateOwnStreams();
    }

    /**
     * Call this method on a conference joining to update the various visibilities
     *
     * @param conference the conference
     */
    @Override
    public void onConferenceJoining(@NonNull Conference conference) {
        super.onConferenceJoining(conference);

        updateVisibilities(View.GONE);
        invalidateOwnStreams();
    }


    private void updateVisibilities(int visibility) {
        boolean listener = isListener();

        //updateSpeakerButton();

        if (microphone != null)
            microphone_wrapper.setVisibility(displayMute && !listener ? visibility : GONE);

        //always show the speaker button
        if (speaker != null)
            speaker_wrapper.setVisibility(displaySpeaker ? View.VISIBLE : GONE);

        if (camera != null)
            camera_wrapper.setVisibility(displayCamera && !listener ? visibility : GONE);

        if (hangup != null)
            hangup_wrapper.setVisibility(displayLeave ? visibility : GONE);

        boolean screenShareEnabled = VoxeetToolkit.instance().getConferenceToolkit().isScreenShareEnabled();
        if (screenshare != null)
            screenshare_wrapper.setVisibility(displayScreenShare && screenShareEnabled ? visibility : GONE);
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_conference_bar_view;
    }

    /**
     * Method to call when the mode of display has been changed
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

    private void checkMicrophoneButtonState() {
        // also invalidate information about mute stream
        if (null == microphone) return;

        boolean no_perm = !Validate.hasMicrophonePermissions(getContext());
        MediaState state = Opt.of(VoxeetSDK.conference().getCurrentConference())
                .then(ConferenceInformation::getAudioState).or(MediaState.STOPPED);

        boolean no_audio = state.equals(MediaState.STOPPED) || state.equals(MediaState.STOPPING);

        if (no_perm || no_audio) {
            microphone.setSelected(true); //mute state is selected
            microphone.setEnabled(false);
        } else {
            microphone.setSelected(VoxeetSDK.conference().isMuted());
            microphone.setEnabled(true);
        }
    }

    private boolean canScreenShare() {
        return Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT;
    }

    private void updateSpeakerButton() {
        AudioService service = VoxeetSDK.audio();

        service.getLocal().enumerateDevices().then(devices -> {
            VoxeetActionBarView.this.devices = devices;
            updateSpeakerButtonWithDevices();
        }).error(Log::e);
    }

    private void updateSpeakerButtonWithDevices() {
        Log.d("updateSpeakerButtonWithDevices: ");
        ActionBar configuration = VoxeetToolkit.instance().getConferenceToolkit().Configuration.ActionBar;
        StateListDrawable selector_speaker = createOverridenSelector(configuration.speaker_on, configuration.speaker_off);
        StateListDrawable selector_hangup = createOverridenSelectorPressed(configuration.hangup, configuration.hangup_pressed);

        if (null != selector_speaker) speaker.setImageDrawable(selector_speaker);
        else if (null != VoxeetSDK.instance() && attached) {
            List<MediaDevice> connecting = Filter.filter(devices, device -> ConnectionState.CONNECTING.equals(device.connectionState()));
            List<MediaDevice> connected = Filter.filter(devices, device -> ConnectionState.CONNECTED.equals(device.connectionState()));

            DeviceType deviceType = DeviceType.INTERNAL_SPEAKER;

            if (connecting.size() > 0) {
                for (MediaDevice device : connecting) {
                    if (null != device) deviceType = device.deviceType();
                }

                speaker.setEnabled(false);
                speaker.setSelected(false);
            } else if (connected.size() > 0) {
                for (MediaDevice device : connected) {
                    if (null != device) deviceType = device.deviceType();
                }

                speaker.setEnabled(true);
                speaker.setSelected(true);
            }

            speaker.setImageResource(getSpeakerIcon(deviceType));
        }
    }

    private boolean isListener() {
        ConferenceInformation information = VoxeetSDK.conference().getCurrentConference();
        return null == information || ConferenceParticipantType.LISTENER.equals(information.getConferenceParticipantType());
    }

    private boolean isValidOverride(Integer button_on, Integer button_off) {
        return null != button_off && null != button_on;
    }

    @Nullable
    private StateListDrawable createOverridenSelectorPressed(Integer button_on, Integer
            button_off) {
        if (!isValidOverride(button_on, button_off)) return null;

        Resources resources = getContext().getResources();
        Drawable drawable_on = ResourcesCompat.getDrawable(resources, button_on, null);
        Drawable drawable_off = ResourcesCompat.getDrawable(resources, button_off, null);

        StateListDrawable states = new StateListDrawable();

        states.addState(new int[]{android.R.attr.stateNotNeeded}, drawable_off);
        states.addState(new int[]{android.R.attr.state_pressed}, drawable_on);

        return states;
    }

    @Nullable
    private StateListDrawable createOverridenSelector(Integer button_on, Integer button_off) {
        if (!isValidOverride(button_on, button_off)) return null;

        Resources resources = getContext().getResources();
        Drawable drawable_on = ResourcesCompat.getDrawable(resources, button_on, null);
        Drawable drawable_off = ResourcesCompat.getDrawable(resources, button_off, null);

        StateListDrawable states = new StateListDrawable();

        states.addState(new int[]{android.R.attr.stateNotNeeded}, drawable_off);
        states.addState(new int[]{android.R.attr.state_selected}, drawable_on);

        return states;
    }

    @DrawableRes
    private int getSpeakerIcon(DeviceType deviceType) {
        switch (deviceType) {
            case BLUETOOTH:
                return R.drawable.speaker_on_bluetooth;
            case EXTERNAL_SPEAKER:
                return R.drawable.speaker_on;
            case WIRED_HEADSET:
                return R.drawable.speaker_on_headset;
            case INTERNAL_SPEAKER:
            case NORMAL_MEDIA:
                return R.drawable.speaker_off;
        }
        return R.drawable.speaker_on_bluetooth;
    }

    @Nullable
    private MediaDevice oneOf(@NonNull List<MediaDevice> list, @NonNull ConnectionState connectionState, @NonNull DeviceType... deviceTypes) {
        for (DeviceType deviceType : deviceTypes) {
            List<MediaDevice> filtered = Filter.filter(list, mediaDevice -> ConnectionState.CONNECTED.equals(mediaDevice.platformConnectionState()) && connectionState.equals(mediaDevice.connectionState()) && deviceType.equals(mediaDevice.deviceType()));
            if (filtered.size() > 0) return filtered.get(0);
        }
        return null;
    }

    public VoxeetActionBarView setMediaDeviceControl(@Nullable IMediaDeviceControlListener onSpeakerAction) {
        this.onSpeakerAction = onSpeakerAction;
        return this;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull MicrophoneStateEvent event) {
        Conference conference = VoxeetSDK.conference().getConference();
        if (null == conference) return;

        microphone.setSelected(MicrophoneState.MUTED.equals(event.state));
        checkMicrophoneButtonState();
    }
}
