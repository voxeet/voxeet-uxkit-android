package com.voxeet.uxkit.implementation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.sdk.json.ParticipantInfo;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.sdk.views.VideoView;
import com.voxeet.uxkit.R;
import com.voxeet.uxkit.utils.VoxeetSpeakersTimerInstance;
import com.voxeet.uxkit.views.internal.rounded.RoundedImageView;

/**
 * Simple View to manage how a Participant will be displayed on top
 */
@Annotate
public class VoxeetParticipantView extends LinearLayout implements VoxeetSpeakersTimerInstance.SpeakersUpdated {


    private static final String TAG = VoxeetParticipantView.class.getSimpleName();
    private VideoView videoView;
    private TextView name;
    private RoundedImageView avatar;
    private ImageView overlay;

    private boolean videoActivable = true;
    private boolean selected;

    @Nullable
    private Participant participant;
    private boolean showName;
    private int avatarSize;
    private int selectedUserColor;
    private int yellowOrange;
    private int white;
    private int grey999;

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context the context
     */
    @NoDocumentation
    public VoxeetParticipantView(Context context) {
        super(context);

        init();
    }

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    @NoDocumentation
    public VoxeetParticipantView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    @NoDocumentation
    public VoxeetParticipantView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.view_participant_view_cell, this, false);
        addView(v);

        Resources resources = getContext().getResources();

        white = resources.getColor(R.color.white);
        yellowOrange = resources.getColor(R.color.yellowOrange);
        grey999 = resources.getColor(R.color.grey999);

        videoView = v.findViewById(R.id.participant_video_view);
        name = v.findViewById(R.id.name);
        overlay = v.findViewById(R.id.overlay_avatar);
        avatar = v.findViewById(R.id.avatar);
    }

    public void setParticipant(@Nullable Participant participant) {
        this.participant = participant;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (null != participant) {
            refresh();
        }

        VoxeetSpeakersTimerInstance.instance.register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        VoxeetSpeakersTimerInstance.instance.unregister(this);

        super.onDetachedFromWindow();
    }

    @Override
    public void onSpeakersUpdated() {
        int color = 0;

        if (null != participant) {
            double level = VoxeetSpeakersTimerInstance.instance.audioLevel(participant);

            if (selected) {
                name.setTypeface(Typeface.DEFAULT_BOLD);
                name.setTextColor(white);

                color = selectedUserColor;
            } else if (level > 0.02) {
                name.setTypeface(Typeface.DEFAULT_BOLD);
                name.setTextColor(white);

                color = yellowOrange;
            }
        }

        if (0 != color) {
            overlay.setBackgroundColor(color);
            overlay.setVisibility(View.VISIBLE);
        } else {
            name.setTypeface(Typeface.DEFAULT);
            name.setTextColor(grey999);

            overlay.setVisibility(View.GONE);
        }
    }

    public void refresh() {
        boolean on_air = false;
        if (null != participant) {
            on_air = participant.isLocallyActive();

            name.setText(Opt.of(participant).then(Participant::getInfo).then(ParticipantInfo::getName).or(""));

            loadStreamOnto();
        } else {
            name.setText("");
        }

        onSpeakersUpdated();
        name.setVisibility(showName ? View.VISIBLE : View.GONE);

        if (on_air) {
            setAlpha(1f);
            avatar.setAlpha(1.0f);
        } else {
            setAlpha(0.5f);
            avatar.setAlpha(0.4f);
        }

        //will go to default
        loadViaPicasso();
    }

    private void loadViaPicasso() {
        try {
            String url = Opt.of(participant).then(Participant::getInfo)
                    .then(ParticipantInfo::getAvatarUrl).orNull();

            if (!TextUtils.isEmpty(url)) {
                Picasso.get()
                        .load(url)
                        .noFade()
                        .resize(avatarSize, avatarSize)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(avatar);
            } else {
                Picasso.get()
                        .load(R.drawable.default_avatar)
                        .into(avatar);
            }
        } catch (Exception e) {
            Log.e(TAG, "error " + e.getMessage());
        }
    }

    @Nullable
    private MediaStream getMediaStream(@Nullable String userId) {
        return Opt.of(userId).then(id -> VoxeetSDK.conference().findParticipantById(id))
                .then(Participant::streamsHandler)
                .then(handler -> handler.getFirst(MediaStreamType.Camera)).orNull();
    }

    private void loadStreamOnto() {
        if(!videoActivable) {
            setNoVideo();
            return;
        }

        String id = Opt.of(participant).then(Participant::getId).orNull();
        MediaStream normalStream = getMediaStream(id);

        boolean attached = false;

        if (null != id && null != normalStream && normalStream.videoTracks().size() > 0) {
            String currentAttached = videoView.getPeerId();
            boolean same = id.equals(currentAttached);

            if (!videoView.hasVideo() || !same) {
                videoView.attach(id, normalStream);
                videoView.setVisibility(View.VISIBLE);
                avatar.setVisibility(View.GONE);
                attached = true;
            } else if (same) {
                attached = true;
            }
        }

        if (!attached) {
            setNoVideo();
        }
    }

    private void setNoVideo() {
        videoView.unAttach();
        videoView.setVisibility(View.GONE);
        avatar.setVisibility(View.VISIBLE);
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public void setAvatarSize(int avatarSize) {
        this.avatarSize = avatarSize;
    }

    public void setSelectedUserColor(int selectedUserColor) {
        this.selectedUserColor = selectedUserColor;
    }

    @Override
    public int getId() {
        return Opt.of(participant).then(Participant::getId).then(String::hashCode).or(0);
    }

    public void setVideoActivable(boolean state) {
        this.videoActivable = state;
    }
}
