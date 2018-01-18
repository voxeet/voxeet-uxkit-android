package sdk.voxeet.com.toolkit.views.uitookit.sdk;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.voxeet.android.media.MediaStream;
import com.voxeet.toolkit.R;

import java.util.Map;

import sdk.voxeet.com.toolkit.utils.IParticipantViewListener;
import sdk.voxeet.com.toolkit.utils.ParticipantViewAdapter;
import voxeet.com.sdk.core.VoxeetPreferences;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Created by ROMMM on 9/29/15.
 *
 * @in
 */
public class VoxeetParticipantView extends VoxeetView {

    private static final int USER_THRESHOLD = 4;

    private final String TAG = VoxeetParticipantView.class.getSimpleName();

    private RecyclerView recyclerView;

    private ParticipantViewAdapter adapter;

    private RecyclerView.LayoutManager horizontalLayout;

    private RecyclerView.LayoutManager gridLayout;

    private boolean nameEnabled = true;

    private boolean displaySelf = false;

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context the context
     */
    public VoxeetParticipantView(Context context) {
        super(context);
    }

    /**
     * Instantiates a new Voxeet participant view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetParticipantView(Context context, AttributeSet attrs) {
        super(context, attrs);

        updateAttrs(attrs);
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

        nameEnabled = attributes.getBoolean(R.styleable.VoxeetParticipantView_name_enabled, true);

        displaySelf = attributes.getBoolean(R.styleable.VoxeetParticipantView_display_self, false);

        ColorStateList color = attributes.getColorStateList(R.styleable.VoxeetParticipantView_overlay_color);
        if (color != null)
            setOverlayColor(color.getColorForState(getDrawableState(), 0));

        setNamesEnabled(nameEnabled);

        attributes.recycle();
    }

    @Override
    public void onConferenceUserJoined(DefaultConferenceUser conferenceUser) {
        super.onConferenceUserJoined(conferenceUser);

        boolean isMe = conferenceUser.getUserId().equalsIgnoreCase(VoxeetPreferences.id());
        if (!isMe || displaySelf) {
            adapter.addUser(conferenceUser);

            if (adapter.getItemCount() > USER_THRESHOLD)
                recyclerView.setLayoutManager(gridLayout);

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConferenceUserLeft(DefaultConferenceUser conferenceUser) {
        super.onConferenceUserLeft(conferenceUser);

        adapter.removeUser(conferenceUser);

        if (adapter.getItemCount() > USER_THRESHOLD)
            recyclerView.setLayoutManager(gridLayout);
        else recyclerView.setLayoutManager(horizontalLayout);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMediaStreamUpdated(String userId, Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamUpdated(userId, mediaStreams);

        adapter.onMediaStreamUpdated(mediaStreams);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConferenceDestroyed() {
        super.onConferenceDestroyed();

        adapter.clearParticipants();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConferenceLeft() {
        super.onConferenceLeft();

        adapter.clearParticipants();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void init() {
        if (adapter == null)
            adapter = new ParticipantViewAdapter(getContext());

        gridLayout = new GridLayoutManager(getContext(), 2, LinearLayoutManager.HORIZONTAL, false);

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
        recyclerView = (RecyclerView) view.findViewById(R.id.participant_recycler_view);
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
}
