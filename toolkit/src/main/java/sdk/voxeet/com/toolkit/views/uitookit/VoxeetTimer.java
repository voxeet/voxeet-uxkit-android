package sdk.voxeet.com.toolkit.views.uitookit;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.voxeet.toolkit.R;

import java.util.List;

import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Created by ROMMM on 9/29/15.
 */
public class VoxeetTimer extends VoxeetView {
    private final String TAG = VoxeetTimer.class.getSimpleName();

    private final int DEFAULT_MODE = 0;
    private final int CONFERENCE_MODE = 1;

    private int action = 1;

    private RoundedImageView recordingImageAlpha;

    private RoundedImageView recordingImage;

    private ViewGroup colorLayout;

    private TextView timer;

    private long startTime = -1;

    private long timeInMilliseconds;

    private int notInConferenceColor = getResources().getColor(R.color.blue);

    private int inConferenceColor = getResources().getColor(R.color.green);

    private int recordingColor = getResources().getColor(R.color.red);

    private int textColor = getResources().getColor(R.color.lightestGrey);

    private ValueAnimator colorAnimation;

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            int secs = (int) (timeInMilliseconds / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            timer.setText("" + mins + ":"
                    + String.format("%02d", secs));
            handler.postDelayed(this, 1000);
        }
    };

    /**
     * Instantiates a new Voxeet timer.
     *
     * @param context the context
     */
    public VoxeetTimer(Context context) {
        super(context);
    }

    /**
     * Instantiates a new Voxeet timer.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetTimer(Context context, AttributeSet attrs) {
        super(context, attrs);

        updateAttrs(attrs);
    }

    /**
     * Instantiates a new Voxeet timer.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public VoxeetTimer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        updateAttrs(attrs);
    }

    /**
     * Sets the timer mode. {@link #DEFAULT_MODE} or {@link #CONFERENCE_MODE}
     *
     * @param mode the mode
     */
    public void setMode(int mode) {
        if (mode > 1)
            action = CONFERENCE_MODE;

        invalidate();
    }

    /**
     * Sets the color when not in conference.
     *
     * @param defaultColor the default color
     */
    public void setNotInConferenceColor(int defaultColor) {
        this.notInConferenceColor = defaultColor;

        updateColors();
    }

    /**
     * Sets the in conference color.
     *
     * @param defaultColor the default color
     */
    public void setInConferenceColor(int defaultColor) {
        this.inConferenceColor = defaultColor;

        updateColors();
    }

    /**
     * Sets the color when conference is being recording.
     *
     * @param recordingColor the recording color
     */
    public void setRecordingColor(int recordingColor) {
        this.recordingColor = recordingColor;

        updateColors();
    }

    /**
     * Sets text color.
     *
     * @param textColor the text color
     */
    public void setTextColor(int textColor) {
        this.textColor = textColor;

        updateColors();
    }

    /**
     * Hides / Displays the colored circle indicating the conference status
     *
     * @param isEnabled the text color
     */
    public void enableColor(boolean isEnabled) {
        if (isEnabled)
            colorLayout.setVisibility(VISIBLE);
        else
            colorLayout.setVisibility(GONE);

        invalidate();
    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetTimer);

        action = attributes.getInt(R.styleable.VoxeetTimer_timer_mode, CONFERENCE_MODE);
        updateAction();

        ColorStateList color = attributes.getColorStateList(R.styleable.VoxeetTimer_not_in_conference_color);
        if (color != null)
            notInConferenceColor = color.getColorForState(getDrawableState(), 0);

        color = attributes.getColorStateList(R.styleable.VoxeetTimer_default_color);
        if (color != null)
            inConferenceColor = color.getColorForState(getDrawableState(), 0);

        color = attributes.getColorStateList(R.styleable.VoxeetTimer_recording_color);
        if (color != null)
            recordingColor = color.getColorForState(getDrawableState(), 0);

        color = attributes.getColorStateList(R.styleable.VoxeetTimer_text_color);
        if (color != null)
            textColor = color.getColorForState(getDrawableState(), R.color.lightestGrey);

        enableColor(attributes.getBoolean(R.styleable.VoxeetTimer_color_enabled, true));

        attributes.recycle();

        updateColors();
    }

    private void updateAction() {
        if (action == DEFAULT_MODE)
            start();
    }

    private void updateColors() {
        if (VoxeetSdk.getInstance().getConferenceService().isLive()) {
            recordingImage.setColorFilter(inConferenceColor);
            recordingImageAlpha.setColorFilter(inConferenceColor);
        } else {
            recordingImage.setColorFilter(notInConferenceColor);
            recordingImageAlpha.setColorFilter(notInConferenceColor);
        }

        timer.setTextColor(textColor);

        invalidate();
    }

    @Override
    protected void onConferenceJoined(String conferenceId) {
        if (action == CONFERENCE_MODE) {
            startTime = SystemClock.uptimeMillis();

            handler.post(updateTimerThread);
        }

        colorAnimation(notInConferenceColor, inConferenceColor);
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

    private void colorAnimation(int oldColor, int newColor) {
        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, newColor);
        colorAnimation.setDuration(1000);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int color = (int) animator.getAnimatedValue();
                recordingImage.setColorFilter(color);
                recordingImageAlpha.setColorFilter(color);
            }

        });
        colorAnimation.start();
    }

    @Override
    protected void onRecordingStatusUpdated(final boolean recording) {
        int currentColor = recording ? inConferenceColor : recordingColor;
        int nextColor = recording ? recordingColor : inConferenceColor;

        colorAnimation(currentColor, nextColor);
    }

    @Override
    protected void onMediaStreamUpdated(String userId) {

    }

    @Override
    protected void onConferenceDestroyed() {
        if (action == CONFERENCE_MODE) {
            recordingImage.clearAnimation();

            handler.removeCallbacks(updateTimerThread);
        }
    }

    @Override
    protected void onConferenceLeft() {
        if (action == CONFERENCE_MODE)
            handler.removeCallbacks(updateTimerThread);

        colorAnimation(inConferenceColor, notInConferenceColor);
    }

    @Override
    public void init() {
        recordingImage.setColorFilter(notInConferenceColor);
        recordingImageAlpha.setColorFilter(notInConferenceColor);
    }

    @Override
    protected void bindView(View v) {
        timer = (TextView) v.findViewById(R.id.timer_conference);

        colorLayout = (ViewGroup) v.findViewById(R.id.color_layout);

        recordingImage = (RoundedImageView) v.findViewById(R.id.recording_status_image);

        recordingImageAlpha = (RoundedImageView) v.findViewById(R.id.recording_status_image_alpha);
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    protected void inflateLayout() {
        inflate(getContext(), R.layout.voxeet_timer_view, this);
    }

    /**
     * Start the timer.
     */
    public void start() {
        if (startTime == -1) {
            startTime = SystemClock.uptimeMillis();

            handler.post(updateTimerThread);
        }
    }

}
