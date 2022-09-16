package com.voxeet.uxkit.implementation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.sdk.services.SessionService;
import com.voxeet.uxkit.R;
import com.voxeet.uxkit.configuration.Users;
import com.voxeet.uxkit.controllers.VoxeetToolkit;
import com.voxeet.uxkit.utils.IParticipantViewListener;
import com.voxeet.uxkit.utils.ParticipantViewAdapter;
import com.voxeet.uxkit.utils.ToolkitUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple View to manage how Users are displayed "on top" of the screen (or wherever the default list should be positionned)
 */
public class VoxeetParticipantsView extends VoxeetView {

    private RecyclerView recyclerView;

    private boolean videoActivable = true;

    @Nullable
    private ParticipantViewAdapter adapter;

    private LinearLayoutManager horizontalLayout;

    private boolean displaySelf = false;
    private boolean displayNonAir = true;

    private Handler mHandler;

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context the context
     */
    public VoxeetParticipantsView(Context context) {
        super(context);

        internalInit();
    }

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetParticipantsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        internalInit();
        updateAttrs(attrs);
    }

    private void internalInit() {
        mHandler = new Handler(Looper.getMainLooper());

        horizontalLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        if (adapter == null) {
            adapter = new ParticipantViewAdapter(horizontalLayout, getContext());
        }


        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(horizontalLayout);

        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.conference_view_avatar_size));
    }

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public VoxeetParticipantsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        updateAttrs(attrs);
    }

    /**
     * Displays or hides the names of the conference users.
     *
     * @param enabled the enabled
     */
    public void setNamesEnabled(boolean enabled) {
        adapter.setNamesEnabled(enabled);
        adapter.updateUsers();
    }

    /**
     * Sets the color of the overlay when a user is selected.
     *
     * @param color the color
     */
    public void setSelectedUserColor(int color) {
        adapter.setSelectedUserColor(color);
        adapter.updateUsers();
    }

    public boolean isDisplaySelf() {
        return displaySelf;
    }

    public boolean isDisplayNonAir() {
        return displayNonAir;
    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetParticipantsView);

        boolean nameEnabled = attributes.getBoolean(R.styleable.VoxeetParticipantsView_display_name, true);

        displaySelf = attributes.getBoolean(R.styleable.VoxeetParticipantsView_display_self, false);

        displayNonAir = attributes.getBoolean(R.styleable.VoxeetParticipantsView_display_user_lefts, true);

        Users configuration = VoxeetToolkit.instance().getConferenceToolkit().Configuration.Users;
        ColorStateList color = attributes.getColorStateList(R.styleable.VoxeetParticipantsView_speaking_user_color);
        if (null != configuration.speaking_user_color)
            setSelectedUserColor(configuration.speaking_user_color);
        else if (color != null)
            setSelectedUserColor(color.getColorForState(getDrawableState(), 0));

        setNamesEnabled(nameEnabled);

        attributes.recycle();
    }

    public void update(@NonNull Conference conference) {
        List<Participant> participants = ToolkitUtils.filterParticipants(conference.getParticipants());
        adapter.setUsers(filter(participants));
        adapter.updateUsers();
    }

    /**
     * Mehtod to call when a User Added Event has been fired externally
     *
     * @param conference the conference
     * @param user       the user
     */
    @Override
    public void onUserAddedEvent(@NonNull Conference conference, @NonNull Participant user) {
        super.onUserAddedEvent(conference, user);

        List<Participant> participants = ToolkitUtils.filterParticipants(conference.getParticipants());
        adapter.setUsers(filter(participants));
        adapter.updateUsers();
    }

    /**
     * Mehtod to call when a User Updated Event has been fired externally
     *
     * @param conference the conference
     * @param user       the user
     */
    @Override
    public void onUserUpdatedEvent(@NonNull Conference conference, @NonNull Participant user) {
        super.onUserUpdatedEvent(conference, user);

        postOnUi(() -> {
            List<Participant> participants = ToolkitUtils.filterParticipants(conference.getParticipants());
            adapter.setUsers(filter(participants));
            adapter.updateUsers();
        });
    }

    private List<Participant> filter(List<Participant> users) {
        SessionService sessionService = VoxeetSDK.session();
        List<Participant> filter = new ArrayList<>();
        int added = 0;
        int invited = 0;
        for (Participant user : users) {
            if (null == user) continue;
            boolean invite = ConferenceParticipantStatus.RESERVED.equals(user.getStatus());

            if (isDisplaySelf() || !sessionService.isLocalParticipant(user)) {
                boolean had = isDisplayNonAir(); //if display every users
                if (!had) had = user.isLocallyActive();
                if (!had) had = invite;
                if (had) added++;
                if (had) filter.add(user);

                if (invite) invited++;
            }
        }

        if (added == 1 && invited < 1) {
            //TODO add configuration for this mode
            filter = new ArrayList<>();
        }
        return filter;
    }

    /**
     * Method to call when a Stream has been added to a specified user
     *
     * @param conference  the conference
     * @param user        the user
     * @param mediaStream the corresponding stream that fired the event
     */
    @Override
    public void onStreamAddedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamAddedEvent(conference, user, mediaStream);
        postOnUi(() -> {
            if (adapter != null) {
                adapter.updateUsers();
            }
        });
    }

    /**
     * Method to call when a Stream has been updated from a specified user
     *
     * @param conference  the conference
     * @param user        the user
     * @param mediaStream the corresponding stream that fired the event
     */
    @Override
    public void onStreamUpdatedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamUpdatedEvent(conference, user, mediaStream);
        postOnUi(() -> {
            if (adapter != null) {
                adapter.updateUsers();
            }
        });
    }

    /**
     * Method to call when a Stream has been removed from a specified user
     *
     * @param conference  the conference
     * @param user        the user
     * @param mediaStream the corresponding stream that fired the event
     */
    @Override
    public void onStreamRemovedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        super.onStreamRemovedEvent(conference, user, mediaStream);
        postOnUi(() -> {
            if (adapter != null) {
                adapter.updateUsers();
            }
        });
    }

    /**
     * Method to call when a conference has been destroyed
     */
    @Override
    public void onConferenceDestroyed() {
        super.onConferenceDestroyed();

        adapter.clearParticipants();
        adapter.updateUsers();
    }

    /**
     * Method call when a conference has been left
     */
    @Override
    public void onConferenceLeft() {
        super.onConferenceLeft();

        adapter.clearParticipants();
        adapter.updateUsers();
    }

    @Override
    public void init() {
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_participant_view;
    }

    @Override
    protected void bindView(View view) {
        recyclerView = view.findViewById(R.id.participant_recycler_view);
    }

    /**
     * Sets participant listener.
     *
     * @param listener the listener
     */
    public void setParticipantListener(IParticipantViewListener listener) {
        if (adapter != null)
            adapter.setParticipantListener(listener);
    }

    private void postOnUi(@NonNull Runnable runnable) {
        mHandler.post(runnable);
    }

    public void notifyDatasetChanged() {
        if (null != adapter) {
            adapter.updateUsers();
        }
    }

    @MainThread
    public void setVideoActivable(boolean state) {
        this.videoActivable = state;
        if (null != adapter) {
            adapter.setVideoActivable(state);
        }
    }
}
