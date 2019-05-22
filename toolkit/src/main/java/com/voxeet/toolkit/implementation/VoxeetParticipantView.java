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
import com.voxeet.sdk.core.preferences.VoxeetPreferences;
import com.voxeet.sdk.models.abs.ConferenceUser;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.utils.IParticipantViewListener;
import com.voxeet.toolkit.utils.ParticipantViewAdapter;

import java.util.Iterator;
import java.util.Map;

public class VoxeetParticipantView extends VoxeetView {

    private RecyclerView recyclerView;

    private ParticipantViewAdapter adapter;

    private RecyclerView.LayoutManager horizontalLayout;

    private boolean displaySelf = false;
    private Handler mHandler;

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context the context
     */
    public VoxeetParticipantView(Context context) {
        super(context);

        internalInit();
    }

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetParticipantView(Context context, AttributeSet attrs) {
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
    public VoxeetParticipantView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    public void setOverlayColor(int color) {
        adapter.setOverlayColor(color);
        adapter.notifyDataSetChanged();
    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetParticipantView);

        boolean nameEnabled = attributes.getBoolean(R.styleable.VoxeetParticipantView_name_enabled, true);

        displaySelf = attributes.getBoolean(R.styleable.VoxeetParticipantView_display_self, false);

        ColorStateList color = attributes.getColorStateList(R.styleable.VoxeetParticipantView_overlay_color);
        if (color != null)
            setOverlayColor(color.getColorForState(getDrawableState(), 0));

        setNamesEnabled(nameEnabled);

        attributes.recycle();
    }

    @Override
    public void onConferenceUserJoined(@NonNull ConferenceUser conferenceUser) {
        super.onConferenceUserJoined(conferenceUser);

        boolean isMe = conferenceUser.getUserId().equalsIgnoreCase(VoxeetPreferences.id());
        if (!isMe || displaySelf) {
            adapter.addUser(conferenceUser);

            adapter.updateUsers();
        }
    }

    @Override
    public void onConferenceUserLeft(@NonNull ConferenceUser conferenceUser) {
        super.onConferenceUserLeft(conferenceUser);

        adapter.removeUser(conferenceUser);
        recyclerView.setLayoutManager(horizontalLayout);

        adapter.updateUsers();
    }

    @Override
    public void onConferenceUserUpdated(@NonNull final ConferenceUser conference_user) {
        super.onConferenceUserUpdated(conference_user);


        postOnUi(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.updateUsers();
                }
            }
        });
    }

    @Override
    public void onMediaStreamUpdated(@NonNull final String userId, @NonNull final Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamUpdated(userId, mediaStreams);

        postOnUi(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.onMediaStreamUpdated(userId, mediaStreams);
                    adapter.updateUsers();
                }
            }
        });
    }

    @Override
    public void onScreenShareMediaStreamUpdated(@NonNull final String userId, @NonNull final Map<String, MediaStream> mediaStreams) {
        super.onScreenShareMediaStreamUpdated(userId, mediaStreams);

        postOnUi(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.onScreenShareMediaStreamUpdated(userId, mediaStreams);
                    adapter.updateUsers();
                }
            }
        });
    }

    @Override
    public void onScreenShareMediaStreamUpdated(final Map<String, MediaStream> screenShareMediaStreams) {
        super.onScreenShareMediaStreamUpdated(screenShareMediaStreams);

        postOnUi(new Runnable() {
            @Override
            public void run() {
                String userId = null;
                Iterator<String> keys = screenShareMediaStreams.keySet().iterator();
                while (null == userId && keys.hasNext()) {
                    userId = keys.next();
                    if (null == screenShareMediaStreams.get(userId))
                        userId = null; //reset if invalid stream
                }

                adapter.onMediaStreamUpdated(userId, screenShareMediaStreams);
                adapter.updateUsers();
            }
        });
    }

    @Override
    public void onConferenceDestroyed() {
        super.onConferenceDestroyed();

        adapter.clearParticipants();
        adapter.updateUsers();
    }

    @Override
    public void onConferenceLeft() {
        super.onConferenceLeft();

        adapter.clearParticipants();
        adapter.updateUsers();
    }

    @Override
    public void init() {
        if (adapter == null)
            adapter = new ParticipantViewAdapter(getContext());

        horizontalLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(horizontalLayout);

        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.conference_view_avatar_size));
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
            adapter.notifyDataSetChanged();
        }
    }
}
