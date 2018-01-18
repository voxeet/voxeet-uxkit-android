package sdk.voxeet.com.toolkit.views.uitookit.sdk;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.voxeet.android.media.MediaStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Created by romainbenmansour on 20/02/2017.
 */
public abstract class VoxeetView extends FrameLayout
implements IVoxeetView {

    private List<VoxeetView> mListeners;

    private final String TAG = VoxeetView.class.getSimpleName();

    protected boolean builderMode = false;

    /**
     * Instantiates a new Voxeet view.
     *
     * @param context the context
     */
    public VoxeetView(Context context) {
        super(context);

        onInit();
    }

    /**
     * Instantiates a new Voxeet view.
     *
     * @param context     the context
     * @param builderMode inflating the layout will differ depending on the value
     */
    public VoxeetView(Context context, boolean builderMode) {
        super(context);

        this.builderMode = builderMode;

        onInit();
    }

    /**
     * Instantiates a new Voxeet view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetView(Context context, AttributeSet attrs) {
        super(context, attrs);

        onInit();
    }

    /**
     * Instantiates a new Voxeet view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public VoxeetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        onInit();
    }

    /**
     * On conference joined.
     *
     * @param conferenceId the conference id
     */
    public void onConferenceJoined(String conferenceId) {
        for (VoxeetView child : mListeners) {
            child.onConferenceJoined(conferenceId);
        }
    }

    /**
     * On conference updated.
     *
     * @param conferenceId the conference id
     */
    public void onConferenceUpdated(List<DefaultConferenceUser> conferenceId) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUpdated(conferenceId);
        }
    }

    /**
     * On conference creation.
     *
     * @param conferenceId the conference id
     */
    public void onConferenceCreation(String conferenceId) {
        for (VoxeetView child : mListeners) {
            child.onConferenceCreation(conferenceId);
        }
    }

    /**
     * On conference user joined.
     *
     * @param conferenceUser the conference user
     */
    public void onConferenceUserJoined(DefaultConferenceUser conferenceUser) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUserJoined(conferenceUser);
        }
    }

    /**
     * On conference user updated.
     *
     * @param conferenceUser the conference user
     */
    public void onConferenceUserUpdated(DefaultConferenceUser conferenceUser) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUserUpdated(conferenceUser);
        }
    }

    /**
     * On conference user left.
     *
     * @param conferenceUser the conference user
     */
    public void onConferenceUserLeft(DefaultConferenceUser conferenceUser) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUserLeft(conferenceUser);
        }
    }

    /**
     * On recording status updated.
     *
     * @param recording the recording
     */
    public void onRecordingStatusUpdated(boolean recording) {
        for (VoxeetView child : mListeners) {
            child.onRecordingStatusUpdated(recording);
        }
    }

    /**
     * On media stream updated.
     *
     * @param userId the user id
     * @param mediaStreams
     */
    public void onMediaStreamUpdated(String userId, Map<String, MediaStream> mediaStreams) {
        for (VoxeetView child : mListeners) {
            child.onMediaStreamUpdated(userId, mediaStreams);
        }
    }

    /**
     *
     * @param conferenceUsers the new list of users
     */
    public void onConferenceUsersListUpdate(List<DefaultConferenceUser> conferenceUsers) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUsersListUpdate(conferenceUsers);
        }
    }

    @Override
    public void onMediaStreamsListUpdated(Map<String, MediaStream> mediaStreams) {
        for(VoxeetView child: mListeners) {
            child.onMediaStreamsListUpdated(mediaStreams);
        }
    }

    /**
     *
     * @param mediaStreams the new list of mediaStreams
     */
    public void onMediaStreamsUpdated(Map<String, MediaStream> mediaStreams) {
        for (VoxeetView child : mListeners) {
            child.onMediaStreamsUpdated(mediaStreams);
        }
    }

    /**
     * On conference destroyed.
     */
    public void onConferenceDestroyed() {
        for (VoxeetView child : mListeners) {
            child.onConferenceDestroyed();
        }
    }

    /**
     * On conference left.
     */
    public void onConferenceLeft() {
        for (VoxeetView child : mListeners) {
            child.onConferenceLeft();
        }
    }

    @Override
    public void onResume() {
        for (VoxeetView child : mListeners) {
            child.onResume();
        }
    }

    @Override
    public void onDestroy() {
        for (VoxeetView child : mListeners) {
            child.onDestroy();
        }
    }

    /**
     * On init.
     */
    @Override
    public void onInit() {
        mListeners = new ArrayList<>();

        inflateLayout();

        bindView(this);

        init();
    }

    protected void addListener(@NonNull VoxeetView voxeetView) {
        if(mListeners.indexOf(voxeetView) < 0) {
            Log.d(TAG, "addListener: " + voxeetView.getClass().getSimpleName());
            mListeners.add(voxeetView);
        }
    }

    private void inflateLayout() {
        inflate(getContext(), layout(), this);
    }

    /**
     * Inflate layout.
     */
    @LayoutRes
    protected abstract int layout();

    /**
     * Init.
     */
    public abstract void init();

    /**
     * Bind view.
     *
     * @param view the view
     */
    protected abstract void bindView(View view);
}
