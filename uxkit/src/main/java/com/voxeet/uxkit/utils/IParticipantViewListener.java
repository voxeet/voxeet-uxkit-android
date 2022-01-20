package com.voxeet.uxkit.utils;

import androidx.annotation.NonNull;

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
    void onParticipantSelected(@NonNull Participant user);

    /**
     * A conference user has been unselected.
     *
     * @param user the user
     */
    void onParticipantUnselected(@NonNull Participant user);
}
