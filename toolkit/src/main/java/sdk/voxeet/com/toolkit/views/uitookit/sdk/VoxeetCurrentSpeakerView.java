package sdk.voxeet.com.toolkit.views.uitookit.sdk;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.squareup.picasso.Picasso;
import com.voxeet.toolkit.R;

import java.util.ArrayList;
import java.util.List;

import sdk.voxeet.com.toolkit.views.android.RoundedImageView;
import sdk.voxeet.com.toolkit.views.uitookit.nologic.VoxeetVuMeter;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Created by ROMMM on 9/29/15.
 */
public class VoxeetCurrentSpeakerView extends VoxeetView {
    private final String TAG = VoxeetCurrentSpeakerView.class.getSimpleName();

    private final int REFRESH_SPEAKER = 250;

    private final int REFRESH_METER = 100;

    private Handler handler = new Handler(Looper.getMainLooper());

    private int currentWidth;

    private int orientation = 1;

    private VoxeetVuMeter vuMeter;

    @NonNull
    private RoundedImageView currentSpeakerView;

    private DefaultConferenceUser currentSpeaker = null;

    private boolean selected = false;

    private Runnable updateSpeakerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!selected) {
                currentSpeaker = findUserById(VoxeetSdk.getInstance().getConferenceService().currentSpeaker());
                if (currentSpeaker != null && currentSpeaker.getUserInfo() != null) {
                    speakerName.setText(currentSpeaker.getUserInfo().getName());
                    invalidateSpeakerName();
                }
            }

            if (currentSpeaker != null && currentWidth > 0)
                loadViaPicasso(currentSpeaker, currentWidth / 2, currentSpeakerView);

            handler.postDelayed(this, REFRESH_SPEAKER);
        }
    };

    private Runnable updateVuMeterRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentSpeaker != null)
                vuMeter.updateMeter(VoxeetSdk.getInstance().getConferenceService().getPeerVuMeter(currentSpeaker.getUserId()));

            handler.postDelayed(this, REFRESH_METER);
        }
    };
    private List<DefaultConferenceUser> mConferenceUsers;
    private boolean mDisplaySpeakerName = false;
    private TextView speakerName;

    /**
     * Instantiates a new Voxeet current speaker view.
     *
     * @param context the context
     */
    public VoxeetCurrentSpeakerView(Context context) {
        super(context);
    }

    /**
     * Instantiates a new Voxeet current speaker view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetCurrentSpeakerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        updateAttrs(attrs);
    }

    /**
     * Instantiates a new Voxeet current speaker view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public VoxeetCurrentSpeakerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        updateAttrs(attrs);
    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetCurrentSpeakerView);
        ColorStateList color = attributes.getColorStateList(R.styleable.VoxeetCurrentSpeakerView_vu_meter_color);
        attributes.recycle();

        if (color != null)
            vuMeter.setMeterColor(color.getColorForState(getDrawableState(), 0));
    }

    @Override
    public void onConferenceDestroyed() {
        super.onConferenceDestroyed();

        afterLeaving();
    }

    @Override
    public void onConferenceLeft() {
        super.onConferenceLeft();

        afterLeaving();
    }

    private void afterLeaving() {
        currentSpeakerView.setImageDrawable(null);

        vuMeter.reset();

        handler.removeCallbacks(updateSpeakerRunnable);
        handler.removeCallbacks(updateVuMeterRunnable);
    }

    @Override
    public void init() {

        setShowSpeakerName(false);
        mConferenceUsers = new ArrayList<>();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        orientation = newConfig.orientation;
    }

    @Override
    public void onConferenceUsersListUpdate(List<DefaultConferenceUser> conferenceUsers) {
        super.onConferenceUsersListUpdate(conferenceUsers);

        mConferenceUsers = conferenceUsers;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        currentWidth = w / orientation;

        FrameLayout.LayoutParams paramsMeter = (FrameLayout.LayoutParams) vuMeter.getLayoutParams();
        paramsMeter.gravity = Gravity.CENTER;
        paramsMeter.width = currentWidth;
        paramsMeter.height = currentWidth;

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) currentSpeakerView.getLayoutParams();
        params.gravity = Gravity.CENTER;
        params.width = currentWidth / 2;
        params.height = currentWidth / 2;

        requestLayout();
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_current_speaker_view;
    }

    @Override
    protected void bindView(View v) {
        currentSpeakerView = (RoundedImageView) v.findViewById(R.id.speaker_image);

        vuMeter = (VoxeetVuMeter) v.findViewById(R.id.vu_meter);

        speakerName = (TextView) v.findViewById(R.id.speaker_name);
    }

    /**
     * When showing speaker name, it will disable the VuMeter
     * @param display
     */
    protected void setShowSpeakerName(boolean display) {
        mDisplaySpeakerName = display;

        invalidateSpeakerName();
    }

    /**
     * Return wether the user name will be visible instead of the vu meter
     * @return true or false
     */
    protected boolean isShowSpeakerName() {
        return mDisplaySpeakerName;
    }

    private void invalidateSpeakerName() {
        vuMeter.setVisibility(mDisplaySpeakerName ? View.GONE : View.VISIBLE);
        speakerName.setVisibility(mDisplaySpeakerName ? View.VISIBLE : View.GONE);
    }

    /**
     * Find user by id conference user.
     *
     * @param userId the user id
     * @return the conference user
     */
    public DefaultConferenceUser findUserById(@Nullable final String userId) {
        return Iterables.find(mConferenceUsers, new Predicate<DefaultConferenceUser>() {
            @Override
            public boolean apply(DefaultConferenceUser input) {
                return input.getUserId().equalsIgnoreCase(userId);
            }
        }, null);
    }

    private void loadViaPicasso(DefaultConferenceUser conferenceUser, int avatarSize, ImageView imageView) {
        try {
            Picasso.with(getContext())
                    .load(conferenceUser.getUserInfo().getAvatarUrl())
                    .noFade()
                    .resize(avatarSize, avatarSize)
                    .error(R.color.transparent)
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "error " + e.getMessage());
        }
    }

    /**
     * Pauses the handler responsible of updating the vu meter and the speaker photo.
     */
    public void onPause() {
        handler.removeCallbacks(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(null);
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Starts the handler responsible of updating the vu meter and the speaker photo.
     */
    public void start() {
        handler.post(updateSpeakerRunnable);
        handler.post(updateVuMeterRunnable);
    }

    /**
     * Resumes the handler responsible of updating the vu meter and the speaker photo.
     */
    @Override
    public void onResume() {
        super.onResume();

        start();
    }

    /**
     * Goes to selected mode and focuses on the user selected instead of updating the speaker view
     * depending on the voice levels.
     *
     * @param userId the user id
     */
    public void lockScreen(String userId) {
        vuMeter.onParticipantSelected();

        currentSpeaker = findUserById(userId);

        selected = true;

        if (currentSpeaker != null) {
            speakerName.setText(currentSpeaker.getUserInfo().getName());
        }
    }

    /**
     * Stops the selected mode.
     */
    public void unlockScreen() {
        vuMeter.onParticipantUnselected();

        onResume();

        selected = false;
    }
}
