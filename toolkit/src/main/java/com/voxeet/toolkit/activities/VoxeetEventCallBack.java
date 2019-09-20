package com.voxeet.toolkit.activities;

public interface VoxeetEventCallBack {
    /**
     * On conference mute from this user.
     */
    void onConferenceMute(Boolean isMuted);

    /**
     * On conference turn on video from this user.
     */
    void onConferenceVideo(Boolean isVideoEnabled);

    /**
     * On conference call end from this user.
     */
    void onConferenceCallEnded();

    /**
     * On conference minimized from this user.
     */
    void onConferenceMinimized();

    /**
     * On conference Speaker On from this user.
     */
    void onConferenceSpeakerOn(Boolean isSpeakerOn);
}
