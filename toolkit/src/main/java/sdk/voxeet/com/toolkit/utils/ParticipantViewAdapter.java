package sdk.voxeet.com.toolkit.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.voxeet.android.media.MediaStream;
import com.voxeet.toolkit.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sdk.voxeet.com.toolkit.views.android.RoundedImageView;
import sdk.voxeet.com.toolkit.views.uitookit.nologic.VideoView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetParticipantView;
import voxeet.com.sdk.models.ConferenceUserStatus;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Created by romainbenmansour on 22/02/2017.
 */
public class ParticipantViewAdapter extends RecyclerView.Adapter<ParticipantViewAdapter.ViewHolder> {

    private final String TAG = ParticipantViewAdapter.class.getSimpleName();

    private boolean namesEnabled = true;

    private List<DefaultConferenceUser> users;

    private Context context;

    private final int avatarSize;

    private int lastPosition = -1;

    private int selectedPosition = -1;

    private VoxeetParticipantView.ParticipantViewListener listener;

    private Map<String, MediaStream> mediaStreamMap;

    private int overlayColor;

    private int parentWidth;

    private int parentHeight;

    /**
     * Instantiates a new Participant view adapter.
     *
     * @param context the context
     */
    public ParticipantViewAdapter(Context context) {
        this.overlayColor = context.getResources().getColor(R.color.blue);

        this.context = context;

        this.users = new ArrayList<>();

        this.namesEnabled = true;

        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.meeting_list_avatar_double);
    }

    /**
     * Removes the conference user if being part of the conference.
     *
     * @param conferenceUser the conference user
     */
    public void removeUser(DefaultConferenceUser conferenceUser) {
        if (users.contains(conferenceUser))
            users.remove(conferenceUser);
    }

    /**
     * Adds the conference user if not already added.
     *
     * @param conferenceUser the conference user
     */
    public void addUser(DefaultConferenceUser conferenceUser) {
        if (!users.contains(conferenceUser))
            users.add(conferenceUser);
    }

    /**
     * Sets the color when a conference user has been selected.
     *
     * @param color the color
     */
    public void setOverlayColor(int color) {
        overlayColor = color;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.participant_view_cell, parent, false);

        parentWidth = parent.getWidth();

        parentHeight = parent.getHeight();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final DefaultConferenceUser user = getItem(position);

//        if ((parentWidth / getItemCount()) <= parentHeight) {
//            holder.itemView.getLayoutParams().height = parentWidth / getItemCount();
//            holder.itemView.getLayoutParams().width = parentWidth / getItemCount();
//        }

        if (user.getStatus() != null && !user.getStatus().equalsIgnoreCase(ConferenceUserStatus.ON_AIR.name())) {
            holder.itemView.setAlpha(0.5f);
        } else
            holder.itemView.setAlpha(1f);

        holder.name.setText(user.getUserInfo().getName());
        holder.name.setVisibility(namesEnabled ? View.VISIBLE : View.GONE);

        loadViaPicasso(user, holder.avatar);

        if (selectedPosition == position) {
            holder.name.setTypeface(Typeface.DEFAULT_BOLD);
            holder.name.setTextColor(context.getResources().getColor(R.color.white));

            holder.overlay.setVisibility(View.VISIBLE);
            holder.overlay.setBackgroundColor(overlayColor);
        } else {
            holder.name.setTypeface(Typeface.DEFAULT);
            holder.name.setTextColor(context.getResources().getColor(R.color.grey999));

            holder.overlay.setVisibility(View.GONE);
        }

        if (mediaStreamMap != null) {
            MediaStream mediaStream = mediaStreamMap.get(user.getUserId());
            if (mediaStream != null) {
                if (mediaStream.hasVideo()) {
                    holder.videoView.setVisibility(View.VISIBLE);
                    holder.videoView.attach(user.getUserId(), mediaStream);
                } else {
                    holder.videoView.setVisibility(View.GONE);
                    holder.videoView.unAttach();
                }
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition != holder.getAdapterPosition()) {
                    selectedPosition = holder.getAdapterPosition();

                    if (listener != null)
                        listener.onParticipantSelected(user);
                } else {
                    selectedPosition = -1;

                    if (listener != null)
                        listener.onParticipantUnselected(user);
                }

                notifyDataSetChanged();
            }
        });

        setAnimation(holder.itemView, position);
    }

    /**
     * Animation when a new participant is joining the conference.
     *
     * @param viewToAnimate
     * @param position
     */
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) { // If the bound view wasn't previously displayed on screen, it's animated
            AlphaAnimation animation = new AlphaAnimation(0.2f, 1.0f);
            animation.setDuration(500);
            animation.setFillAfter(true);
            viewToAnimate.startAnimation(animation);

            lastPosition = position;
        }
    }

    private DefaultConferenceUser getItem(int position) {
        return users.get(position);
    }

    /**
     * Displays user's avatar in the specified imageView.
     *
     * @param conferenceUser
     * @param imageView
     */
    private void loadViaPicasso(DefaultConferenceUser conferenceUser, ImageView imageView) {
        try {
            Picasso.with(context)
                    .load(conferenceUser.getUserInfo().getAvatarUrl())
                    .noFade()
                    .resize(avatarSize, avatarSize)
                    .error(R.color.red)
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "error " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Sets participant listener.
     *
     * @param listener the listener
     */
    public void setParticipantListener(VoxeetParticipantView.ParticipantViewListener listener) {
        this.listener = listener;
    }

    /**
     * On media stream updated. Needs to call notifyDataSetChanged to refresh the videos.
     *
     * @param mediaStreams the media streams
     */
    public void onMediaStreamUpdated(Map<String, MediaStream> mediaStreams) {
        this.mediaStreamMap = mediaStreams;
    }

    /**
     * Clear participants.
     */
    public void clearParticipants() {
        this.users.clear();
    }

    /**
     * Sets names enabled.
     *
     * @param enabled the enabled
     */
    public void setNamesEnabled(boolean enabled) {
        namesEnabled = enabled;
    }

    /**
     * The type View holder.
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private VideoView videoView;

        private TextView name;

        private RoundedImageView avatar;

        private ImageView overlay;

        /**
         * Instantiates a new View holder.
         *
         * @param view the view
         */
        ViewHolder(@NonNull View view) {
            super(view);

            videoView = (VideoView) view.findViewById(R.id.participant_video_view);

            name = (TextView) view.findViewById(R.id.name);

            overlay = (ImageView) view.findViewById(R.id.overlay_avatar);

            avatar = (RoundedImageView) view.findViewById(R.id.avatar);
        }
    }
}
