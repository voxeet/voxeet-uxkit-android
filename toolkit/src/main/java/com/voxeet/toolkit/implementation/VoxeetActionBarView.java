package com.voxeet.toolkit.implementation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.MediaStreamType;
import com.voxeet.audio.AudioRoute;
import com.voxeet.sdk.VoxeetSdk;
import com.voxeet.sdk.events.error.PermissionRefusedEvent;
import com.voxeet.sdk.events.sdk.AudioRouteChangeEvent;
import com.voxeet.sdk.events.sdk.StartScreenShareAnswerEvent;
import com.voxeet.sdk.events.sdk.StopScreenShareAnswerEvent;
import com.voxeet.sdk.events.v2.VideoStateEvent;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.User;
import com.voxeet.sdk.services.AudioService;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.services.conference.information.ConferenceUserType;
import com.voxeet.sdk.services.media.VideoState;
import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.AudioType;
import com.voxeet.sdk.utils.NoDocumentation;
import com.voxeet.sdk.utils.Validate;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.configuration.ActionBar;
import com.voxeet.toolkit.controllers.VoxeetToolkit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;

/**
 * Class used to display the various action buttons in the conference
 */
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

    /**
     * Instantiates a new Voxeet conference bar view.
     *
     * @param context the context
     */
    @NoDocumentation
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
    @NoDocumentation
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
    @NoDocumentation
    public VoxeetActionBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        updateAttrs(attrs);

        setUserPreferences();
    }

    @NoDocumentation
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (null != VoxeetSdk.conference()) {
            ConferenceService service = VoxeetSdk.conference();
            ConferenceInformation information = service.getCurrentConference();

            if (null != information && information.isOwnVideoStarted() && !VideoState.STARTED.equals(information.getVideoState())) {
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

            updateCameraState();
        }
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

        updateSpeakerButton();
        ConferenceService service = VoxeetSdk.conference();
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
    public void onStreamAddedEvent(@NonNull Conference conference, @NonNull User user, @NonNull MediaStream mediaStream) {
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
    public void onStreamUpdatedEvent(@NonNull Conference conference, @NonNull User user, @NonNull MediaStream mediaStream) {
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
    public void onStreamRemovedEvent(@NonNull Conference conference, @NonNull User user, @NonNull MediaStream mediaStream) {
        super.onStreamRemovedEvent(conference, user, mediaStream);
        invalidateOwnStreams();
    }

    /**
     * Method call to refresh the internal state for :
     * - the own camera button
     * - the screenshare button
     */
    public void invalidateOwnStreams() {
        User user = VoxeetSdk.conference().findUserById(VoxeetSdk.session().getUserId());

        if (null != user) {
            MediaStream cameraStream = user.streamsHandler().getFirst(MediaStreamType.Camera);
            MediaStream screenStream = user.streamsHandler().getFirst(MediaStreamType.ScreenShare);
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
    }

    @NoDocumentation
    @Override
    public void init() {
        setWillNotDraw(false);
    }

    @NoDocumentation
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

        if (null != selector_camera) camera.setImageDrawable(selector_camera);
        if (null != selector_microphone) microphone.setImageDrawable(selector_microphone);
        if (null != selector_screenshare) screenshare.setImageDrawable(selector_screenshare);
        if (null != selector_speaker) speaker.setImageDrawable(selector_speaker);
        if (null != selector_hangup) hangup.setImageDrawable(selector_hangup);

        if (!checkMicrophonePermission()) {
            microphone.setSelected(true);
            VoxeetSdk.conference().mute(true);
        }

        updateCameraState();
    }

    private void on3DView() {
        if (null != view3d_listener) view3d_listener.onView3D();
    }

    /**
     * Toggle the mute button
     */
    protected void toggleMute() {
        boolean new_muted_state = !VoxeetSdk.conference().isMuted();

        if (new_muted_state || checkMicrophonePermission()) {
            //if we unmute, check for microphone state
            microphone.setSelected(new_muted_state);

            VoxeetSdk.conference().mute(new_muted_state);
        }
    }

    /**
     * Activate or deactivate the local camera
     */
    protected void toggleCamera() {
        ConferenceService conferenceService = VoxeetSdk.conference();
        if (checkCameraPermission() && null != conferenceService) {
            Promise<Boolean> video = null;

            ConferenceInformation information = conferenceService.getCurrentConference();
            if (null != information) {
                switch (information.getVideoState()) {
                    case STARTED:
                        video = conferenceService.stopVideo();
                        break;
                    case STOPPED:
                        video = conferenceService.startVideo();
                        break;
                    default:
                }
            }

            if (null != video) {
                camera.setEnabled(false);
                video.then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                        camera.setEnabled(true);
                        updateCameraState();
                    }
                }).error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        camera.setEnabled(true);
                        updateCameraState();
                    }
                });
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VideoStateEvent event) {
        updateCameraState(event.videoState);
    }

    private void updateCameraState() {
        ConferenceInformation information = VoxeetSdk.conference().getCurrentConference();
        if (null != information) updateCameraState(information.getVideoState());
    }

    private void updateCameraState(@NonNull VideoState videoState) {
        if(null == camera) return;

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

        updateSpeakerButton();

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

    @NoDocumentation
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


    private boolean checkPermission(@NonNull String permission, @NonNull String error_message,
                                    int result_code) {
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
            AudioService service = VoxeetSdk.audio();

            boolean headset = service.isWiredHeadsetOn();
            headset |= service.isBluetoothHeadsetConnected();
            Log.d("SpeakerButton", "updateSpeakerButton: has wired head set on =" + service.isWiredHeadsetOn() + " bluetooth = " + service.isBluetoothHeadsetConnected());
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

    /**
     * Set the 3D Button's click listener
     *
     * @param listener the listener instance
     * @return the current instance
     */
    @Deprecated
    @NonNull
    public VoxeetActionBarView setView3dListener(@Nullable OnView3D listener) {
        view3d_listener = listener;
        invalidateView3D();
        return this;
    }

    private void invalidateView3D() {
        view_3d_wrapper.setVisibility(view3d_listener != null ? View.VISIBLE : View.GONE);
    }

    /**
     * Interface called when user has clicked on the 3d button. The listener should then display a view to manipulate the users
     */
    @Annotate
    @Deprecated
    public interface OnView3D {

        /**
         * Method called when the button is pressed. Warning : make sure not the fire any exceptions inside.
         */
        void onView3D();
    }

    private boolean isListener() {
        ConferenceInformation information = VoxeetSdk.conference().getCurrentConference();
        return null == information || ConferenceUserType.LISTENER.equals(information.getConferenceUserType());
    }

    private boolean isValidOverride(Integer button_on, Integer button_off) {
        return null != button_off && null != button_on;
    }

    @Nullable
    private StateListDrawable createOverridenSelectorPressed(Integer button_on, Integer
            button_off) {
        if (!isValidOverride(button_on, button_off)) return null;

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
        if (!isValidOverride(button_on, button_off)) return null;

        Resources resources = getContext().getResources();
        Drawable drawable_on = resources.getDrawable(button_on);
        Drawable drawable_off = resources.getDrawable(button_off);

        StateListDrawable states = new StateListDrawable();

        states.addState(new int[]{android.R.attr.stateNotNeeded}, drawable_off);
        states.addState(new int[]{android.R.attr.state_selected}, drawable_on);

        return states;
    }
}
