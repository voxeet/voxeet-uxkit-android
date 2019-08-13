package com.voxeet.toolkit.utils;

import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.models.User;

/**
 * Participants selection callbacks
 */

public interface IParticipantViewListener {

    /**
     * A conference user has been selected.
     *
     * @param user the user
     */
    void onParticipantSelected(User user, MediaStream requested_mediaStream);

    /**
     * A conference user has been unselected.
     *
     * @param user the user
     */
    void onParticipantUnselected(User user);
}
