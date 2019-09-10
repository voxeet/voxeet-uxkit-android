package com.voxeet.toolkit.implementation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.User;
import com.voxeet.sdk.models.v1.ConferenceUserStatus;
import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.configuration.Users;
import com.voxeet.toolkit.controllers.VoxeetToolkit;
import com.voxeet.toolkit.utils.IParticipantViewListener;
import com.voxeet.toolkit.utils.ParticipantViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple View to manage how Users are displayed "on top" of the screen (or wherever the default list should be positionned)
 */
@Annotate
public class VoxeetUsersView extends VoxeetView {

    private RecyclerView recyclerView;

    private ParticipantViewAdapter adapter;

    private RecyclerView.LayoutManager horizontalLayout;

    private boolean displaySelf = false;
    private boolean displayNonAir = true;

    private Handler mHandler;

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context the context
     */
    @NoDocumentation
    public VoxeetUsersView(Context context) {
        super(context);

        internalInit();
    }

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    @NoDocumentation
    public VoxeetUsersView(Context context, AttributeSet attrs) {
        super(context, attrs);

        internalInit();
        updateAttrs(attrs);
    }

    private void internalInit() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    @NoDocumentation
    public VoxeetUsersView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        adapter.notifyDataSetChanged();
    }

    /**
     * Sets the color of the overlay when a user is selected.
     *
     * @param color the color
     */
    public void setSelectedUserColor(int color) {
        adapter.setSelectedUserColor(color);
        adapter.notifyDataSetChanged();
    }

    @NoDocumentation
    public boolean isDisplaySelf() {
        return displaySelf;
    }

    @NoDocumentation
    public boolean isDisplayNonAir() {
        return displayNonAir;
    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetUsersView);

        boolean nameEnabled = attributes.getBoolean(R.styleable.VoxeetUsersView_display_name, true);

        displaySelf = attributes.getBoolean(R.styleable.VoxeetUsersView_display_self, false);

        displayNonAir = attributes.getBoolean(R.styleable.VoxeetUsersView_display_user_lefts, true);

        Users configuration = VoxeetToolkit.getInstance().getConferenceToolkit().Configuration.Users;
        ColorStateList color = attributes.getColorStateList(R.styleable.VoxeetUsersView_speaking_user_color);
        if (null != configuration.speaking_user_color)
            setSelectedUserColor(configuration.speaking_user_color);
        else if (color != null)
            setSelectedUserColor(color.getColorForState(getDrawableState(), 0));

        setNamesEnabled(nameEnabled);

        attributes.recycle();
    }

    /**
     * Mehtod to call when a User Added Event has been fired externally
     *
     * @param conference the conference
     * @param user       the user
     */
    @Override
    public void onUserAddedEvent(@NonNull Conference conference, @NonNull User user) {
        super.onUserAddedEvent(conference, user);

        adapter.setUsers(filter(conference.getUsers()));
        adapter.updateUsers();
    }

    /**
     * Mehtod to call when a User Updated Event has been fired externally
     *
     * @param conference the conference
     * @param user       the user
     */
    @Override
    public void onUserUpdatedEvent(@NonNull Conference conference, @NonNull User user) {
        super.onUserUpdatedEvent(conference, user);

        postOnUi(new Runnable() {
            @Override
            public void run() {
                adapter.setUsers(filter(conference.getUsers()));
                adapter.updateUsers();
                recyclerView.setLayoutManager(horizontalLayout);
            }
        });
    }

    private List<User> filter(List<User> users) {
        List<User> filter = new ArrayList<>();
        for (User user : users) {
            boolean had = isDisplaySelf() || !VoxeetSdk.user().isLocalUser(user);
            if (had) had = ConferenceUserStatus.ON_AIR.equals(user) || isDisplayNonAir();

            if (had) filter.add(user);

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
    public void onStreamAddedEvent(@NonNull Conference conference, @NonNull User user, @NonNull MediaStream mediaStream) {
        super.onStreamAddedEvent(conference, user, mediaStream);
        postOnUi(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.updateUsers();
                }
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
    public void onStreamUpdatedEvent(@NonNull Conference conference, @NonNull User user, @NonNull MediaStream mediaStream) {
        super.onStreamUpdatedEvent(conference, user, mediaStream);
        postOnUi(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.updateUsers();
                }
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
    public void onStreamRemovedEvent(@NonNull Conference conference, @NonNull User user, @NonNull MediaStream mediaStream) {
        super.onStreamRemovedEvent(conference, user, mediaStream);
        postOnUi(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.updateUsers();
                }
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

    @NoDocumentation
    @Override
    public void init() {
        if (adapter == null)
            adapter = new ParticipantViewAdapter(getContext());

        horizontalLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(horizontalLayout);

        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.conference_view_avatar_size));
    }

    @NoDocumentation
    @Override
    protected int layout() {
        return R.layout.voxeet_participant_view;
    }

    @NoDocumentation
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

    @NoDocumentation
    public void notifyDatasetChanged() {
        if (null != adapter) {
            adapter.updateUsers();
            adapter.notifyDataSetChanged();
        }
    }
}
