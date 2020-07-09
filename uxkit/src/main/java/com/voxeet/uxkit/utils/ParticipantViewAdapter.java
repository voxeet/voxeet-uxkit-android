package com.voxeet.uxkit.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.uxkit.R;
import com.voxeet.uxkit.implementation.VoxeetParticipantView;
import com.voxeet.uxkit.implementation.VoxeetParticipantsView;

import java.util.ArrayList;
import java.util.List;

public class ParticipantViewAdapter extends RecyclerView.Adapter<ParticipantViewAdapter.ViewHolder> {

    private final String TAG = VoxeetParticipantsView.class.getSimpleName();

    @NonNull
    private LinearLayoutManager layoutManager;

    private boolean namesEnabled = true;

    private ArrayList<Participant> air = new ArrayList<>();
    private ArrayList<Participant> inv = new ArrayList<>();
    private ArrayList<Participant> left = new ArrayList<>();
    private ArrayList<Participant> other = new ArrayList<>();

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
    public ParticipantViewAdapter(@NonNull LinearLayoutManager layoutManager, @NonNull Context context) {
        this();
        this.layoutManager = layoutManager;
        this.selectedUserColor = context.getResources().getColor(R.color.blue);
        this.namesEnabled = true;

        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.meeting_list_avatar_double);
    }

    public void updateUsers() {
        sort();

        refreshVisible();
    }

    /**
     * Set the corresponding users
     *
     * @param users the list of user to populate the adapter
     */
    public void setUsers(List<Participant> users) {
        //add all the new users
        for (Participant participant : users) {
            if (air.contains(participant) || inv.contains(participant) || left.contains(participant) || other.contains(participant)) {
                //existing participant
            } else {
                addParticipant(participant);
            }
        }

        //remove all the users in the list but not anymore known
        updateArray(air, 0, p -> !users.contains(p));
        updateArray(inv, air.size(), p -> !users.contains(p));
        updateArray(left, air.size() + inv.size(), p -> !users.contains(p));
        updateArray(other, air.size() + inv.size() + left.size(), p -> !users.contains(p));

        //sort all the users not new and not removed
        sort();

        //refresh the visible users
        refreshVisible();
    }

    private boolean is(@Nullable Participant p, @NonNull ConferenceParticipantStatus s) {
        return null != p && s.equals(p.getStatus());
    }

    private ArrayList<Participant> updateArray(@NonNull ArrayList<Participant> array, int delta, @NonNull Apply apply) {
        Participant participant;
        ArrayList<Participant> tmp = new ArrayList<>();
        int index = 0;
        while (index < array.size()) {
            participant = array.get(index);
            if (apply.is(participant)) {
                tmp.add(participant);
                array.remove(participant);
                notifyItemRemoved(delta + index);
            } else {
                index++;
            }
        }

        return tmp;
    }

    private void sort() {
        ArrayList<Participant> to_readd = new ArrayList<>();

        ArrayList<Participant> removed_from_air = updateArray(air, 0, p -> !p.isLocallyActive());
        ArrayList<Participant> removed_from_inv = updateArray(inv, air.size(), p -> !is(p, ConferenceParticipantStatus.RESERVED));
        ArrayList<Participant> removed_from_left = updateArray(left, air.size() + inv.size(), p -> !is(p, ConferenceParticipantStatus.LEFT));
        ArrayList<Participant> removed_from_other = updateArray(other, air.size() + inv.size() + left.size(), p -> p.isLocallyActive() || is(p, ConferenceParticipantStatus.LEFT) || is(p, ConferenceParticipantStatus.RESERVED));

        to_readd.addAll(removed_from_air);
        to_readd.addAll(removed_from_inv);
        to_readd.addAll(removed_from_left);
        to_readd.addAll(removed_from_other);

        for (Participant participant : to_readd) {
            addParticipant(participant);
        }

    }


    private void addParticipant(@NonNull Participant participant) {
        if (participant.isLocallyActive()) {
            air.add(participant);
            notifyItemInserted(air.size() - 1);
        } else if (is(participant, ConferenceParticipantStatus.RESERVED)) {
            inv.add(participant);
            notifyItemInserted(inv.size() + air.size() - 1);
        } else if (is(participant, ConferenceParticipantStatus.LEFT)) {
            left.add(participant);
            notifyItemInserted(left.size() + inv.size() + air.size() - 1);
        } else {
            notifyItemInserted(other.size() + left.size() + inv.size() + air.size() - 1);
            other.add(participant);
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
        VoxeetParticipantView participantView = holder.participantView;

        participantView.setTag(holder);
        manageParticipantView(participantView, holder);
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

    @Nullable
    private Participant getItem(int position) {
        if (position < air.size()) return air.get(position);
        position -= air.size();

        if (position < inv.size()) return inv.get(position);
        position -= inv.size();

        if (position < left.size()) return left.get(position);
        position -= left.size();

        if (position < other.size())
            return other.get(position);
        return null;
    }

    @Override
    public int getItemCount() {
        return air.size() + inv.size() + left.size() + other.size();
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
        this.air.clear();
        this.inv.clear();
        this.left.clear();
        this.other.clear();
        notifyDataSetChanged();
    }

    /**
     * Sets names enabled.
     *
     * @param enabled the enabled
     */
    public void setNamesEnabled(boolean enabled) {
        namesEnabled = enabled;
    }

    private void refreshVisible() {
        int first = 0; //layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.getChildCount(); //layoutManager.findLastVisibleItemPosition();

        if (first >= 0 && first <= last) {

            for (int index = first; index < layoutManager.getChildCount() && index <= last; index++) {
                Log.d(TAG, "refreshVisible: index := " + index);
                View view = layoutManager.getChildAt(index);
                if (null != view && view instanceof VoxeetParticipantView) {
                    VoxeetParticipantView participantView = (VoxeetParticipantView) view;

                    if (null != participantView.getTag() && participantView.getTag() instanceof ViewHolder) {
                        manageParticipantView(participantView, (ViewHolder) participantView.getTag());
                    } else {
                        Log.d(TAG, "refreshVisible: invalid tag " + participantView.getTag());
                    }
                } else {
                    Log.d(TAG, "refreshVisible: invalid view " + view);
                }
            }
        }
    }

    private void manageParticipantView(@NonNull VoxeetParticipantView participantView, @Nullable ViewHolder holder) {
        if (null == holder) return;
        participantView.setTag(holder);

        int position = holder.getLayoutPosition();

        final Participant user = getItem(position);
        if (null == user) {
            participantView.setVisibility(View.INVISIBLE);
            return;
        } else if (participantView.getVisibility() != View.VISIBLE) {
            participantView.setVisibility(View.VISIBLE);
        }

        boolean on_air = user.isLocallyActive();
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
                updateUsers();
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

                updateUsers();
            }
        });

        setAnimation(holder.itemView, position);
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

    private interface Apply {
        boolean is(Participant participant);
    }
}
