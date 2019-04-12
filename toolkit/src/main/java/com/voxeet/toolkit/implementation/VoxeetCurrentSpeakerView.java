package com.voxeet.toolkit.implementation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.exceptions.ExceptionManager;
import com.voxeet.sdk.models.abs.ConferenceUser;
import com.voxeet.sdk.utils.ConferenceUtils;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.views.internal.VoxeetVuMeter;
import com.voxeet.toolkit.views.internal.rounded.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ROMMM on 9/29/15.
 */
public class VoxeetCurrentSpeakerView extends VoxeetView {
    private final String TAG = VoxeetCurrentSpeakerView.class.getSimpleName();

    private final int REFRESH_SPEAKER = 500;

    private final int REFRESH_METER = 500;

    private Handler handler = new Handler(Looper.getMainLooper());

    private int currentWidth;

    private int orientation = 1;

    private VoxeetVuMeter vuMeter;

    @NonNull
    private RoundedImageView currentSpeakerView;

    private ConferenceUser currentSpeaker = null;

    private boolean selected = false;

    private Runnable updateSpeakerRunnable = new Runnable() {
        @Override
        public void run() {
            if (selected && currentSpeaker != null && currentSpeaker.getUserId() != null) {
                //if we had a user but he disappeared...
                selected = findUserById(currentSpeaker.getUserId()) != null;
            } else {
                //had a user but predicate did not pass
                selected = false;
            }

            if (!selected) {
                currentSpeaker = findUserById(VoxeetSdk.getInstance().getConferenceService().currentSpeaker());
                if (currentSpeaker != null && currentSpeaker.getUserInfo() != null) {
                    speakerName.setText(currentSpeaker.getUserInfo().getName());
                    invalidateSpeakerName();
                }
            }

            if (currentSpeaker != null && currentWidth > 0)
                loadViaPicasso(currentSpeaker, currentWidth / 2, currentSpeakerView);

            if (mAttached) handler.postDelayed(this, REFRESH_SPEAKER);
        }
    };

    private Runnable updateVuMeterRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentSpeaker != null && null != VoxeetSdk.getInstance())
                vuMeter.updateMeter(VoxeetSdk.getInstance().getConferenceService().getPeerVuMeter(currentSpeaker.getUserId()));

            if (mAttached) handler.postDelayed(this, REFRESH_METER);
        }
    };
    private List<ConferenceUser> mConferenceUsers;
    private boolean mDisplaySpeakerName = false;
    private TextView speakerName;
    private boolean mAttached;

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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mAttached = true;

        onResume();
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttached = false;

        onPause();
        super.onDetachedFromWindow();
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
    public void onConferenceUsersListUpdate(List<ConferenceUser> conferenceUsers) {
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
     *
     * @param display
     */
    protected void setShowSpeakerName(boolean display) {
        mDisplaySpeakerName = display;

        invalidateSpeakerName();
    }

    /**
     * Return wether the user name will be visible instead of the vu meter
     *
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
    public ConferenceUser findUserById(@Nullable final String userId) {
        return ConferenceUtils.findUserById(userId, mConferenceUsers);
    }

    private void loadViaPicasso(ConferenceUser conferenceUser, int avatarSize, ImageView imageView) {
        try {
            String avatarUrl = null;
            if (null != conferenceUser && null != conferenceUser.getUserInfo()) {
                avatarUrl = conferenceUser.getUserInfo().getAvatarUrl();
            }

            if (!TextUtils.isEmpty(avatarUrl)) {
                Picasso.get()
                        .load(conferenceUser.getUserInfo().getAvatarUrl())
                        .noFade()
                        .resize(avatarSize, avatarSize)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.color.transparent)
                        .into(imageView);
            } else {
                Picasso.get()
                        .load(R.drawable.default_avatar)
                        .noFade()
                        .resize(avatarSize, avatarSize)
                        .into(imageView);
            }
        } catch (Exception e) {
            ExceptionManager.sendException(e);
            Log.e(TAG, "error " + e.getMessage());
        }
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

        String userName = null;
        if (null != currentSpeaker && null != currentSpeaker.getUserInfo()) {
            userName = currentSpeaker.getUserInfo().getName();
        }
        if (currentSpeaker != null) {
            speakerName.setText(userName);
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

    @Nullable
    public String getSelectedUserId() {
        return selected && null != currentSpeaker ? currentSpeaker.getUserId() : null;
    }



    @Override
    public void onResume() {
        super.onResume();

        handler.removeCallbacks(updateSpeakerRunnable);
        handler.removeCallbacks(updateVuMeterRunnable);

        handler.removeCallbacksAndMessages(updateSpeakerRunnable);
        handler.removeCallbacksAndMessages(updateVuMeterRunnable);

        handler.post(updateSpeakerRunnable);
        handler.post(updateVuMeterRunnable);
    }

    public void onPause() {
        handler.removeCallbacks(null);
        handler.removeCallbacksAndMessages(null);
    }
}
