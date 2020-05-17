package com.voxeet.uxkit.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.uxkit.R;
import com.voxeet.uxkit.implementation.VoxeetParticipantView;

import java.util.ArrayList;
import java.util.List;

public class ParticipantViewAdapter extends RecyclerView.Adapter<ParticipantViewAdapter.ViewHolder> {

    private final String TAG = ParticipantViewAdapter.class.getSimpleName();

    private boolean namesEnabled = true;

    private List<Participant> users;

    private int avatarSize;

    private int lastPosition = -1;

    private String selectedUserId = null;

    private IParticipantViewListener listener;

    private int selectedUserColor;

    private ParticipantViewAdapter() {

    }

    /**
     * Instantiates a new Participant view adapter.
     *
     * @param context the context
     */
    public ParticipantViewAdapter(@NonNull Context context) {
        this();
        this.selectedUserColor = context.getResources().getColor(R.color.blue);

        this.users = new ArrayList<>();

        this.namesEnabled = true;

        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.meeting_list_avatar_double);
    }

    public void updateUsers() {
        filter();
        sort();

        notifyDataSetChanged();
    }

    /**
     * Set the corresponding users
     *
     * @param users the list of user to populate the adapter
     */
    public void setUsers(List<Participant> users) {
        for (Participant user : users) {
            if (!this.users.contains(user))
                this.users.add(user);
        }

        List<Participant> to_remove = new ArrayList<>();
        for (Participant user : this.users) {
            if (!users.contains(user)) to_remove.add(user);
        }
        this.users.removeAll(to_remove);

        filter();
        sort();
    }

    private void filter() {

    }

    private boolean is(Participant p, ConferenceParticipantStatus s) {
        return null != p && s.equals(p.getStatus());
    }

    private void sort() {
        if (null != users) {
            ArrayList<Participant> tmp = new ArrayList<>();
            ArrayList<Participant> air = new ArrayList<>();
            ArrayList<Participant> inv = new ArrayList<>();
            ArrayList<Participant> left = new ArrayList<>();
            ArrayList<Participant> other = new ArrayList<>();

            for (Participant participant : users) {
                if (participant.isLocallyActive()) {
                    air.add(participant);
                } else if (is(participant, ConferenceParticipantStatus.RESERVED)) {
                    inv.add(participant);
                } else if (is(participant, ConferenceParticipantStatus.LEFT)) {
                    left.add(participant);
                } else {
                    other.add(participant);
                }
            }
            tmp.addAll(air);
            tmp.addAll(inv);
            tmp.addAll(left);
            tmp.addAll(other);
            users = tmp;
        }
    }

    /**
     * Sets the color when a conference user has been selected.
     *
     * @param color the color
     */
    public void setSelectedUserColor(int color) {
        selectedUserColor = color;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new VoxeetParticipantView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Participant user = getItem(position);
        boolean on_air = user.isLocallyActive();

        VoxeetParticipantView participantView = holder.participantView;

        participantView.setParticipant(user);
        participantView.setSelectedUserColor(selectedUserColor);
        participantView.setShowName(namesEnabled);
        participantView.setAvatarSize(avatarSize);
        participantView.setSelected(equalsToUser(selectedUserId, user));

        participantView.refresh();

        holder.itemView.setOnLongClickListener(view -> {
            if (equalsToUser(selectedUserId, user)) {
                selectedUserId = null;

                if (listener != null)
                    listener.onParticipantUnselected(user);
                notifyDataSetChanged();
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (!on_air) {
                Log.d(TAG, "onClick: click on an invalid user, we can't select hier");
                return;
            }

            if (null != user.getId()) {
                Log.d(TAG, "onClick: selecting the user " + user.getId());
                if (!equalsToUser(selectedUserId, user)) {
                    selectedUserId = user.getId();

                    if (listener != null)
                        listener.onParticipantSelected(user);
                } else {
                    selectedUserId = null; //deselecting

                    if (listener != null)
                        listener.onParticipantUnselected(user);
                }

                notifyDataSetChanged();
            }
        });

        setAnimation(holder.itemView, position);
    }

    private boolean equalsToUser(@Nullable String selectedUserId, @Nullable Participant user) {
        return null != selectedUserId && null != user && selectedUserId.equals(user.getId());
    }

    /**
     * Animation when a new participant is joining the conference.
     *
     * @param viewToAnimate the valid view which must be animated
     * @param position      the position in the list
     */
    private void setAnimation(@NonNull View viewToAnimate, int position) {
        if (position > lastPosition) { // If the bound view wasn't previously displayed on screen, it's animated
            AlphaAnimation animation = new AlphaAnimation(0.2f, 1.0f);
            animation.setDuration(500);
            animation.setFillAfter(true);
            viewToAnimate.startAnimation(animation);

            lastPosition = position;
        }
    }

    private Participant getItem(int position) {
        return users.get(position);
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
    public void setParticipantListener(IParticipantViewListener listener) {
        this.listener = listener;
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
    static class ViewHolder extends RecyclerView.ViewHolder {

        VoxeetParticipantView participantView;

        /**
         * Instantiates a new View holder.
         *
         * @param view the view
         */
        ViewHolder(@NonNull VoxeetParticipantView view) {
            super(view);

            this.participantView = view;
        }
    }
}
