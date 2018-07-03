package sdk.voxeet.com.toolkit.utils;

import org.webrtc.MediaStream;

import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Participants selection callbacks
 */

public interface IParticipantViewListener {

    /**
     * A conference user has been selected.
     *
     * @param user the user
     */
    void onParticipantSelected(DefaultConferenceUser user, MediaStream requested_mediaStream);

    /**
     * A conference user has been unselected.
     *
     * @param user the user
     */
    void onParticipantUnselected(DefaultConferenceUser user);
}
