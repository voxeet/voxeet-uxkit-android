package com.voxeet.uxkit.implementation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.sdk.exceptions.ExceptionManager;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.sdk.models.v2.ParticipantType;
import com.voxeet.sdk.utils.Filter;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.R;
import com.voxeet.uxkit.utils.VoxeetSpeakersTimerInstance;
import com.voxeet.uxkit.utils.WindowHelper;
import com.voxeet.uxkit.views.internal.VoxeetVuMeter;
import com.voxeet.uxkit.views.internal.rounded.RoundedImageView;

import java.util.List;

/**
 * View made to display a given user
 */
public class VoxeetSpeakerView extends VoxeetView implements VoxeetSpeakersTimerInstance.ActiveSpeakerListener, VoxeetSpeakersTimerInstance.SpeakersUpdated {
    private final String TAG = VoxeetSpeakerView.class.getSimpleName();

    private int currentWidth;

    private int orientation = 1;

    private VoxeetVuMeter vuMeter;

    @NonNull
    private RoundedImageView currentSpeakerView;

    private Participant currentSpeaker = null;

    private boolean selected = false;

    private boolean mDisplaySpeakerName = false;
    private TextView speakerName;
    private boolean mAttached;
    private float delta;

    /**
     * Instantiates a new Voxeet current speaker view.
     *
     * @param context the context
     */
    public VoxeetSpeakerView(Context context) {
        super(context);
    }

    /**
     * Instantiates a new Voxeet current speaker view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetSpeakerView(Context context, AttributeSet attrs) {
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
    public VoxeetSpeakerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        updateAttrs(attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();


        VoxeetSpeakersTimerInstance.instance.registerActiveSpeakerListener(this);
        VoxeetSpeakersTimerInstance.instance.register(this);
        mAttached = true;
        onResume();
    }

    @Override
    protected void onDetachedFromWindow() {
        VoxeetSpeakersTimerInstance.instance.unregisterActiveSpeakerListener(this);
        VoxeetSpeakersTimerInstance.instance.unregister(this);
        mAttached = false;
        onPause();

        super.onDetachedFromWindow();
    }

    private void updateAttrs(AttributeSet attrs) {
        delta = WindowHelper.dpToPx(getContext(), 10);

        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetSpeakerView);
        ColorStateList color = attributes.getColorStateList(R.styleable.VoxeetSpeakerView_vu_meter_color);
        attributes.recycle();

        if (color != null)
            vuMeter.setMeterColor(color.getColorForState(getDrawableState(), 0));
    }

    /**
     * call this method when a conference has been destroyed
     */
    @Override
    public void onConferenceDestroyed() {
        super.onConferenceDestroyed();

        afterLeaving();
    }

    /**
     * Call this method when a conference has been left
     */
    @Override
    public void onConferenceLeft() {
        super.onConferenceLeft();

        afterLeaving();
    }

    private void afterLeaving() {
        currentSpeakerView.setImageDrawable(null);

        vuMeter.reset();
    }

    /**
     * Call this method to init this instance of the view
     */
    @Override
    public void init() {
        setShowSpeakerName(true);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        orientation = newConfig.orientation;
        if (orientation <= 0) orientation = 0;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        currentWidth = w / orientation;
        int width = currentWidth / 2;

        FrameLayout.LayoutParams paramsMeter = (FrameLayout.LayoutParams) vuMeter.getLayoutParams();
        paramsMeter.gravity = Gravity.CENTER;
        paramsMeter.width = (int) (width + delta);
        paramsMeter.height = (int) (width + delta);
        vuMeter.setLayoutParams(paramsMeter);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) currentSpeakerView.getLayoutParams();
        params.gravity = Gravity.CENTER;
        params.width = width;
        params.height = width;
        currentSpeakerView.setLayoutParams(params);
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_current_speaker_view;
    }

    @Override
    protected void bindView(View v) {
        currentSpeakerView = v.findViewById(R.id.speaker_image);
        vuMeter = v.findViewById(R.id.vu_meter);
        speakerName = v.findViewById(R.id.speaker_name);
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

    private void invalidateSpeakerName() {
        vuMeter.setVisibility(View.VISIBLE);
        speakerName.setVisibility(mDisplaySpeakerName ? View.VISIBLE : View.GONE);
    }

    /**
     * Find user by id conference user.
     *
     * @param userId the user id
     * @return the conference user
     */
    private Participant findUserById(@Nullable final String userId) {
        return VoxeetSDK.conference().findParticipantById(userId);
    }

    private boolean loadViaPicasso(Participant conferenceUser, int avatarSize, ImageView imageView) {
        try {
            String avatarUrl = null;
            if (null != conferenceUser && null != conferenceUser.getInfo()) {
                avatarUrl = conferenceUser.getInfo().getAvatarUrl();
            }

            if (!TextUtils.isEmpty(avatarUrl)) {
                Picasso.get()
                        .load(conferenceUser.getInfo().getAvatarUrl())
                        .noFade()
                        .resize(avatarSize, avatarSize)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(imageView);
            } else {
                Picasso.get()
                        .load(R.drawable.default_avatar)
                        .noFade()
                        .resize(avatarSize, avatarSize)
                        .into(imageView);
            }
            return true;
        } catch (Exception e) {
            ExceptionManager.sendException(e);
            Log.e(TAG, "error " + e.getMessage());
            return false;
        }
    }

    /**
     * Goes to selected mode and focuses on the user selected instead of updating the speaker view
     * depending on the voice levels.
     *
     * @param user the user to lock onto
     */
    public void lockScreen(@NonNull Participant user) {
        vuMeter.onParticipantSelected();

        currentSpeaker = findUserById(user.getId());

        selected = true;

        String userName = null;
        if (null != currentSpeaker && null != currentSpeaker.getInfo()) {
            userName = currentSpeaker.getInfo().getName();
        }
        if (userName != null) {
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

    /**
     * Get the selected user for this instance of the view. Deprecated in favor of getCurrentSpeaker()
     *
     * @return the UserId
     */
    @Nullable
    @Deprecated
    public String getSelectedUserId() {
        return selected && null != currentSpeaker ? currentSpeaker.getId() : null;
    }

    /**
     * Call this method to restore the various callbacks
     */
    @Override
    public void onResume() {
        VoxeetSpeakersTimerInstance.instance.registerActiveSpeakerListener(this);
        VoxeetSpeakersTimerInstance.instance.register(this);
    }

    /**
     * Call this method to pause the various callbacks
     */
    public void onPause() {

    }

    @Override
    public void onActiveSpeakerUpdated(@Nullable String activeSpeakerUserId) {
        if (null != currentSpeaker && !currentSpeaker.isLocallyActive()) {
            currentSpeaker = null;
        }

        if ((selected || null == activeSpeakerUserId) && currentSpeaker != null && currentSpeaker.getId() != null) {
            //if we had a user but he disappeared... or simply no new user and someone was active
            Participant participant = findUserById(currentSpeaker.getId());
            if (selected) { //in both selected or update for new active, we check if in case of selected, we are still
                selected = null != participant && participant.isLocallyActive();
            }
        } else {
            //had a user but predicate did not pass
            selected = false;
            currentSpeaker = findUserById(activeSpeakerUserId);
        }

        if (!selected) {
            Participant activeSpeaker = findUserById(VoxeetSDK.conference().currentSpeaker());
            if (null != activeSpeaker) {
                currentSpeaker = activeSpeaker;
            }
        }

        if (currentSpeaker != null && currentSpeaker.getInfo() != null) {
            speakerName.setText(currentSpeaker.getInfo().getName());
            invalidateSpeakerName();
        }

        if (currentWidth <= 0) {
            currentWidth = getWidth() * 2; //currentWidth = width /2 when resize
        }

        if (currentSpeaker != null && currentWidth > 0)
            loadViaPicasso(currentSpeaker, currentWidth / 2, currentSpeakerView);
    }

    @Override
    public void onSpeakersUpdated() {
        if (null != currentSpeaker) {
            if (!currentSpeaker.isLocallyActive()) currentSpeaker = null;
        }

        if (null == currentSpeaker) {
            //we don't have any speaker, look for at least the first one
            List<Participant> participants = Filter.filter(VoxeetSDK.conference().getParticipants(), participant -> {
                ParticipantType type = Opt.of(participant.participantType()).or(ParticipantType.NONE);

                if ("00000000-0000-0000-0000-000000000000".equals(participant.getId()))
                    return false;
                if (!(type.equals(ParticipantType.DVC) || type.equals(ParticipantType.USER) || type.equals(ParticipantType.PSTN))) {
                    return false;
                }

                if (Opt.of(participant.getId()).or("").equals(VoxeetSDK.session().getParticipantId())) {
                    //prevent own user to be "active speaker"
                    return false;
                }
                if (ConferenceParticipantStatus.ON_AIR == participant.getStatus()) return true;
                return ConferenceParticipantStatus.CONNECTING == participant.getStatus() && null != participant.streamsHandler().getFirst(MediaStreamType.Camera);
            });

            if (participants.size() > 0) {
                currentSpeaker = participants.get(0);
                onActiveSpeakerUpdated(currentSpeaker.getId());
            }
        }

        if (currentSpeaker != null && null != VoxeetSDK.conference()) {
            double value = VoxeetSDK.conference().audioLevel(currentSpeaker);
            vuMeter.updateMeter(value);
        }
    }
}
