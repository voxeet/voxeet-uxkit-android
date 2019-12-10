package com.voxeet.toolkit.utils;

import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.models.Participant;

/**
 * Participants selection callbacks
 */

public interface IParticipantViewListener {

    /**
     * A conference user has been selected.
     *
     * @param user the user
     */
    void onParticipantSelected(Participant user, MediaStream requested_mediaStream);

    /**
     * A conference user has been unselected.
     *
     * @param user the user
     */
    void onParticipantUnselected(Participant user);
}
