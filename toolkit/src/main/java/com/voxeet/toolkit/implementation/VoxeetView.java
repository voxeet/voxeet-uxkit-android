package com.voxeet.toolkit.implementation;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.Participant;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation for voxeet views
 * <p>
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
     * @param conference the conference involved
     */
    public void onConferenceJoined(@NonNull Conference conference) {
        for (VoxeetView child : mListeners) {
            child.onConferenceJoined(conference);
        }
    }

    /**
     * On conference updated.
     *
     * @param conference_users the conference id
     */
    public void onConferenceUpdated(@NonNull List<Participant> conference_users) {
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
     * @param conference the conference involved
     */
    public void onConferenceCreation(@NonNull Conference conference) {
        for (VoxeetView child : mListeners) {
            child.onConferenceCreation(conference);
        }
    }

    /**
     * On conference creation.
     *
     * @param conference the conference
     */
    public void onConferenceJoining(@NonNull Conference conference) {
        for (VoxeetView child : mListeners) {
            child.onConferenceJoining(conference);
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


    @Override
    public void onUserAddedEvent(@NonNull Conference conference, @NonNull Participant user) {
        for (VoxeetView child : mListeners) {
            child.onUserAddedEvent(conference, user);
        }
    }

    @Override
    public void onUserUpdatedEvent(@NonNull Conference conference, @NonNull Participant user) {
        for (VoxeetView child : mListeners) {
            child.onUserUpdatedEvent(conference, user);
        }
    }


    @Override
    public void onStreamAddedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        for (VoxeetView child : mListeners) {
            child.onStreamAddedEvent(conference, user, mediaStream);
        }
    }

    @Override
    public void onStreamUpdatedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        for (VoxeetView child : mListeners) {
            child.onStreamUpdatedEvent(conference, user, mediaStream);
        }
    }

    @Override
    public void onStreamRemovedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream) {
        for (VoxeetView child : mListeners) {
            child.onStreamRemovedEvent(conference, user, mediaStream);
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
     * @param conference_users the new list of users
     */
    @Override
    public void onConferenceUsersListUpdate(List<Participant> conference_users) {
        for (VoxeetView child : mListeners) {
            child.onConferenceUsersListUpdate(conference_users);
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
