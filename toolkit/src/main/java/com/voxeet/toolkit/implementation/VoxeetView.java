package com.voxeet.toolkit.implementation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation for voxeet views
 *
 * Those classes will evolve quickly in the future to reflect much more flexibility
 */
public abstract class VoxeetView extends FrameLayout
        implements IVoxeetView {

    @NonNull
    private List<VoxeetView> mListeners;

    private final String TAG = VoxeetView.class.getSimpleName();

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
     * @param conference_id the conference id
     */
    public void onConferenceJoined(@NonNull String conference_id) {
        for (VoxeetView child : mListeners) {
            child.onConferenceJoined(conference_id);
        }
    }

    /**
     * On conference updated.
     *
     * @param conference_users the conference id
     */
    public void onConferenceUpdated(@NonNull List<User> conference_users) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUpdated(conference_users);
        }
    }

    /**
     * On conference creating.
     */
    public void onConferenceCreating() {
        for (VoxeetView child : mListeners) {
            child.onConferenceCreating();
        }
    }

    /**
     * On conference creation.
     */
    public void onConferenceCreation(@NonNull String conferenceId) {
        for (VoxeetView child : mListeners) {
            child.onConferenceCreation(conferenceId);
        }
    }

    /**
     * On conference creation.
     *
     * @param conference_id the conference id
     */
    public void onConferenceJoining(@NonNull String conference_id) {
        for (VoxeetView child : mListeners) {
            child.onConferenceJoining(conference_id);
        }
    }

    @Override
    public void onConferenceFromNoOneToOneUser() {
        for (VoxeetView child : mListeners) {
            child.onConferenceFromNoOneToOneUser();
        }
    }

    @Override
    public void onConferenceNoMoreUser() {
        for (VoxeetView child : mListeners) {
            child.onConferenceNoMoreUser();
        }
    }

    /**
     * On conference user joined.
     *
     * @param conference_user the conference user
     */
    @Override
    public void onConferenceUserJoined(@NonNull User conference_user) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUserJoined(conference_user);
        }
    }

    /**
     * On conference user updated.
     *
     * @param conference_user the conference user
     */
    @Override
    public void onConferenceUserUpdated(@NonNull User conference_user) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUserUpdated(conference_user);
        }
    }

    /**
     * On conference user left.
     *
     * @param conference_user the conference user
     */
    @Override
    public void onConferenceUserLeft(@NonNull User conference_user) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUserLeft(conference_user);
        }
    }

    /**
     * An user declined the call
     *
     * @param userId the declined-user id
     */
    @Override
    public void onConferenceUserDeclined(String userId) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUserDeclined(userId);
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
     * @param userId        the user id
     * @param media_streams the list of media streams
     */
    public void onMediaStreamUpdated(@NonNull String userId,
                                     @NonNull Map<String, MediaStream> media_streams) {
        for (VoxeetView child : mListeners) {
            child.onMediaStreamUpdated(userId, media_streams);
        }
    }

    /**
     * On Screen Share media stream updated
     *
     * @param userId                     the user id
     * @param screen_share_media_streams the list of screen shares media streams
     */
    public void onScreenShareMediaStreamUpdated(@NonNull String userId,
                                                @NonNull Map<String, MediaStream> screen_share_media_streams) {
        for (VoxeetView child : mListeners) {
            child.onScreenShareMediaStreamUpdated(userId, screen_share_media_streams);
        }
    }

    /**
     * @param screenShareMediaStreams the new list of screen share media streams
     */
    @Override
    public void onScreenShareMediaStreamUpdated(Map<String, MediaStream> screenShareMediaStreams) {
        for (VoxeetView child : mListeners) {
            child.onScreenShareMediaStreamUpdated(screenShareMediaStreams);
        }
    }

    /**
     * @param conference_users the new list of users
     */
    @Override
    public void onConferenceUsersListUpdate(List<User> conference_users) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUsersListUpdate(conference_users);
        }
    }

    @Override
    public void onMediaStreamsListUpdated(Map<String, MediaStream> mediaStreams) {
        for (VoxeetView child : mListeners) {
            child.onMediaStreamsListUpdated(mediaStreams);
        }
    }

    /**
     * @param mediaStreams the new list of mMediaStreams
     */
    @Override
    public void onMediaStreamsUpdated(Map<String, MediaStream> mediaStreams) {
        for (VoxeetView child : mListeners) {
            child.onMediaStreamsUpdated(mediaStreams);
        }
    }

    /**
     * On conference leaving from this user.
     */
    @Override
    public void onConferenceLeaving() {
        for (VoxeetView child : mListeners) {
            child.onConferenceLeaving();
        }
    }

    /**
     * On conference destroyed.
     */
    @Override
    public void onConferenceDestroyed() {
        for (VoxeetView child : mListeners) {
            child.onConferenceDestroyed();
        }
    }

    /**
     * On conference left.
     */
    @Override
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
    public void onStop() {
        for (VoxeetView child : mListeners) {
            child.onStop();
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
        if (mListeners.indexOf(voxeetView) < 0) {
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
