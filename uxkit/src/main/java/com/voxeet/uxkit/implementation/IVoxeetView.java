package com.voxeet.uxkit.implementation;

import androidx.annotation.NonNull;

import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.Participant;

import java.util.List;

public interface IVoxeetView {
    /**
     * On conference joined.
     *
     * @param conference the conference id
     */
    void onConferenceJoined(@NonNull Conference conference);

    /**
     * On conference updated.
     *
     * @param conferenceId the conference id
     */
    void onConferenceUpdated(@NonNull List<Participant> conferenceId);

    /**
     * On conference creation.
     *
     * @param conference the conference id
     */
    void onConferenceCreation(@NonNull Conference conference);

    /**
     * On conference for user joined
     */
    void onConferenceFromNoOneToOneUser();

    /**
     * On conference no more users.
     */
    void onConferenceNoMoreUser();

    void onUserAddedEvent(@NonNull Conference conference, @NonNull Participant user);

    void onUserUpdatedEvent(@NonNull Conference conference, @NonNull Participant user);

    void onStreamAddedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream);

    void onStreamUpdatedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream);

    void onStreamRemovedEvent(@NonNull Conference conference, @NonNull Participant user, @NonNull MediaStream mediaStream);

    /**
     * An user declined the call
     *
     * @param userId the declined-user id
     */
    void onConferenceUserDeclined(@NonNull String userId);

    /**
     * On recording status updated.
     *
     * @param recording the recording
     */
    void onRecordingStatusUpdated(boolean recording);

    /**
     * @param conferenceUsers the new list of users
     */
    void onConferenceUsersListUpdate(List<Participant> conferenceUsers);

    /**
     * On conference leaving from this user.
     */
    void onConferenceLeaving();

    /**
     * On conference destroyed.
     */
    void onConferenceDestroyed();

    /**
     * On conference left.
     */

    void onConferenceLeft();

    /**
     * On conference error.
     */

    void onConferenceError(Throwable error);

    /**
     * View resumed
     */
    void onResume();

    /**
     * View stopped
     * typically when conference stopped
     */
    void onStop();

    /**
     * View destroyed
     */
    void onDestroy();

    /**
     * After init
     */
    void onInit();
}
