package sdk.voxeet.com.toolkit.views.uitookit.sdk.listeners;

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
    void onParticipantSelected(DefaultConferenceUser user);

    /**
     * A conference user has been unselected.
     *
     * @param user the user
     */
    void onParticipantUnselected(DefaultConferenceUser user);
}
