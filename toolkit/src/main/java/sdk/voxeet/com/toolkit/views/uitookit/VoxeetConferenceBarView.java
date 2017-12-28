package sdk.voxeet.com.toolkit.views.uitookit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.voxeet.android.media.Media;
import com.voxeet.toolkit.R;

import java.util.ArrayList;
import java.util.List;

import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import voxeet.com.sdk.core.VoxeetPreferences;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Created by ROMMM on 9/29/15.
 */
public class VoxeetConferenceBarView extends VoxeetView {

    private final String TAG = VoxeetConferenceBarView.class.getSimpleName();

    /**
     * The constant RESULT_CAMERA.
     */
    public static final int RESULT_CAMERA = 0x0012;

    public static final int RECORD = 0x0200;
    public static final int MUTE = 0x201;
    public static final int HANG_UP = 0x202;
    public static final int SPEAKER = 0x203;
    public static final int VIDEO = 0x204;

    /**
     * handling buttons visibility
     */
    private boolean displayRecord = true;

    private boolean displayAudio = true;

    private boolean displayMute = true;

    private boolean displayCamera = true;

    private boolean displayLeave = true;

    private LinearLayout container;

    private ImageView microphone;

    private ImageView speaker;

    private ImageView camera;

    private ImageView hangup;

    private ImageView recording;

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

    public VoxeetConferenceBarView(Context context, Builder builder) {
        super(context, true);

        if (builder.params != null)
            this.setLayoutParams(builder.params);

        for (Builder.ConferenceBarComponent component : builder.components)
            addButton(component.action, component.drawable, component.listener);
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

        displayLeave = attributes.getBoolean(R.styleable.VoxeetConferenceBarView_leave_button, true);

        setSpeakerSelector(attributes.getDrawable(R.styleable.VoxeetConferenceBarView_audio_selector));

        setCameraSelector(attributes.getDrawable(R.styleable.VoxeetConferenceBarView_camera_selector));

        setHangUpSelector(attributes.getDrawable(R.styleable.VoxeetConferenceBarView_hang_up_selector));

        setMuteSelector(attributes.getDrawable(R.styleable.VoxeetConferenceBarView_mute_selector));

        setRecordSelector(attributes.getDrawable(R.styleable.VoxeetConferenceBarView_record_selector));

        attributes.recycle();
    }

    /**
     * Sets camera button drawable. Should be a selector.
     *
     * @param drawable the drawable
     */
    public void setCameraSelector(Drawable drawable) {
        if (drawable != null && camera != null)
            camera.setImageDrawable(drawable);
    }

    /**
     * Sets camera button drawable. Should be a selector.
     *
     * @param cameraSelector the drawable
     */
    public void setCameraSelector(int cameraSelector) {
        setCameraSelector(getContext().getResources().getDrawable(cameraSelector));
    }

    /**
     * Sets record drawable. Should be a selector.
     *
     * @param drawable the drawable
     */
    public void setRecordSelector(Drawable drawable) {
        if (drawable != null && recording != null)
            recording.setImageDrawable(drawable);
    }

    /**
     * Sets record drawable. Should be a selector.
     *
     * @param recordSelector the record selector
     */
    public void setRecordSelector(int recordSelector) {
        setRecordSelector(getContext().getResources().getDrawable(recordSelector));
    }

    /**
     * Sets hang up drawable. Should be a selector.
     *
     * @param drawable the drawable
     */
    public void setHangUpSelector(Drawable drawable) {
        if (drawable != null && hangup != null)
            hangup.setImageDrawable(drawable);
    }

    /**
     * Sets hang up drawable. Should be a selector.
     *
     * @param hangUpSelector the hang up selector
     */
    public void setHangUpSelector(int hangUpSelector) {
        setHangUpSelector(getContext().getResources().getDrawable(hangUpSelector));
    }

    /**
     * Sets audio drawable. Should be a selector.
     *
     * @param drawable the drawable
     */
    public void setSpeakerSelector(Drawable drawable) {
        if (drawable != null && microphone != null)
            speaker.setImageDrawable(drawable);
    }

    /**
     * Sets audio drawable. Should be a selector.
     *
     * @param audioSelector the audio selector
     */
    public void setSpeakerSelector(int audioSelector) {
        setSpeakerSelector(getContext().getResources().getDrawable(audioSelector));
    }

    /**
     * Sets mute drawable. Should be a selector.
     *
     * @param drawable the drawable
     */
    public void setMuteSelector(Drawable drawable) {
        if (drawable != null && microphone != null)
            microphone.setImageDrawable(drawable);
    }

    /**
     * Sets mute drawable. Should be a selector.
     *
     * @param muteSelector the mute selector
     */
    public void setMuteSelector(int muteSelector) {
        setMuteSelector(getContext().getResources().getDrawable(muteSelector));
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
    protected void onConferenceJoined(String conferenceId) {
    }

    @Override
    protected void onConferenceUpdated(List<DefaultConferenceUser> conferenceId) {

    }

    @Override
    protected void onConferenceCreation(String conferenceId) {

    }

    @Override
    protected void onConferenceUserJoined(DefaultConferenceUser conferenceUser) {

    }

    @Override
    protected void onConferenceUserUpdated(DefaultConferenceUser conferenceUser) {

    }

    @Override
    protected void onConferenceUserLeft(DefaultConferenceUser conferenceUser) {
    }

    /**
     * Sets the recording button color if the conference is being recorded or not.
     *
     * @param isRecording
     */
    @Override
    protected void onRecordingStatusUpdated(boolean isRecording) {
        if (recording != null)
            recording.setSelected(isRecording);
    }

    @Override
    protected void onMediaStreamUpdated(String userId) {
        if (camera != null && userId.equalsIgnoreCase(VoxeetPreferences.id()) && mediaStreams.get(userId) != null) {
            camera.setSelected(mediaStreams.get(userId).hasVideo());
        }
    }

    @Override
    protected void onConferenceDestroyed() {
    }

    @Override
    protected void onConferenceLeft() {
    }

    @Override
    public void init() {
        setWillNotDraw(false);
    }

    @Override
    protected void bindView(View v) {
        container = (LinearLayout) v.findViewById(R.id.container);

        if (!builderMode) { // theses views are only available when default layout is selected
            speaker = (ImageView) v.findViewById(R.id.speaker);
            speaker.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    speaker.setSelected(!speaker.isSelected());

                    VoxeetSdk.getInstance()
                            .getConferenceService().setAudioRoute(speaker.isSelected() ? Media.AudioRoute.ROUTE_SPEAKER : Media.AudioRoute.ROUTE_PHONE);
                }
            });

            hangup = (ImageView) v.findViewById(R.id.hangup);
            hangup.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    VoxeetSdk.getInstance()
                    .getConferenceService().leave();
                }
            });

            microphone = (ImageView) v.findViewById(R.id.microphone);
            microphone.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean new_muted_state = !VoxeetSdk.getInstance().getConferenceService().isMuted();

                    microphone.setSelected(new_muted_state);

                    VoxeetSdk.getInstance().getConferenceService().muteConference(new_muted_state);
                }
            });

            camera = (ImageView) v.findViewById(R.id.camera);
            camera.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            && getContext().checkCallingOrSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
                        VoxeetToolkit.getCurrentActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, RESULT_CAMERA);
                    else
                        VoxeetSdk.getInstance().getConferenceService().toggleVideo();
                }
            });

            recording = (ImageView) v.findViewById(R.id.recording);
            recording.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    VoxeetSdk.getInstance().getConferenceService().toggleRecording();
                }
            });
        }
    }

    /**
     * Updating buttons visibility when changing one of the attributes programmatically.
     */
    private void setUserPreferences() {
        if (recording != null)
            recording.setVisibility(displayRecord ? VISIBLE : GONE);

        if (microphone != null)
            microphone.setVisibility(displayMute ? VISIBLE : GONE);

        if (speaker != null)
            speaker.setVisibility(displayAudio ? VISIBLE : GONE);

        if (camera != null)
            camera.setVisibility(displayCamera ? VISIBLE : GONE);

        if (hangup != null)
            hangup.setVisibility(displayLeave ? VISIBLE : GONE);
    }

    /**
     * Display or not the record conference button.
     *
     * @param displayRecord the display record value
     */
    public void setDisplayRecord(boolean displayRecord) {
        this.displayRecord = displayRecord;

        setUserPreferences();
    }

    /**
     * Display or not the audio routes popup.
     *
     * @param displayAudio the display audio value
     */
    public void setDisplayAudio(boolean displayAudio) {
        this.displayAudio = displayAudio;

        setUserPreferences();
    }

    /**
     * Display or not the mute myself button.
     *
     * @param displayMute the display mute value
     */
    public void setDisplayMute(boolean displayMute) {
        this.displayMute = displayMute;

        setUserPreferences();
    }

    /**
     * Display or not the own camera button
     *
     * @param displayCamera the display camera value
     */
    public void setDisplayCamera(boolean displayCamera) {
        this.displayCamera = displayCamera;

        setUserPreferences();
    }

    /**
     * Display or not the hangup button
     *
     * @param displayLeave the display leave value
     */
    public void setDisplayLeave(boolean displayLeave) {
        this.displayLeave = displayLeave;

        setUserPreferences();
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    protected void inflateLayout() {
        if (builderMode)
            inflate(getContext(), R.layout.voxeet_conference_bar_view_2, this);
        else
            inflate(getContext(), R.layout.voxeet_conference_bar_view, this);
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

    public static class Builder {

        private final static int NOT_FOUND = -1;
        private final static int FOUND = 0;

        private FrameLayout.LayoutParams params;

        private Context context;

        private List<ConferenceBarComponent> components;

        private SparseIntArray array;

        public Builder with(Context context) {
            this.array = new SparseIntArray(5);

            this.components = new ArrayList<>();

            this.context = context;

            return this;
        }

        public Builder setLayoutParams(LayoutParams params) {
            this.params = params;

            return this;
        }

        public Builder hangUp(int drawable, OnClickListener listener) {
            if (array.get(HANG_UP, NOT_FOUND) == NOT_FOUND)
                components.add(new ConferenceBarComponent(HANG_UP, drawable, listener));

            array.put(HANG_UP, FOUND);

            return this;
        }

        public Builder speaker(int drawable, OnClickListener listener) {
            if (array.get(SPEAKER, NOT_FOUND) == NOT_FOUND)
                components.add(new ConferenceBarComponent(SPEAKER, drawable, listener));

            array.put(SPEAKER, FOUND);

            return this;
        }

        public Builder mute(int drawable, OnClickListener listener) {
            if (array.get(MUTE, NOT_FOUND) == NOT_FOUND)
                components.add(new ConferenceBarComponent(MUTE, drawable, listener));

            array.put(MUTE, FOUND);

            return this;
        }

        public Builder record(int drawable, OnClickListener listener) {
            if (array.get(RECORD, NOT_FOUND) == NOT_FOUND)
                components.add(new ConferenceBarComponent(RECORD, drawable, listener));

            array.put(RECORD, FOUND);

            return this;
        }

        public Builder video(int drawable, OnClickListener listener) {
            if (array.get(VIDEO, NOT_FOUND) == NOT_FOUND)
                components.add(new ConferenceBarComponent(VIDEO, drawable, listener));

            array.put(VIDEO, FOUND);

            return this;
        }

        public VoxeetConferenceBarView build() {
            return new VoxeetConferenceBarView(context, this);
        }

        private class ConferenceBarComponent {

            int drawable;

            int action;

            OnClickListener listener;

            ConferenceBarComponent(int action, int drawable, OnClickListener listener) {
                this.action = action;

                this.drawable = drawable;

                this.listener = listener;
            }
        }
    }
}
