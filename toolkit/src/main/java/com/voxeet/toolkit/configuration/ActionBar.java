package com.voxeet.toolkit.configuration;

import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;

/**
 * Configuration the default VoxeetActionBarView instances
 */
@Annotate
public class ActionBar {

    @NoDocumentation
    public ActionBar() {

    }

    /**
     * Should the Mute/unmute button be displayed. _null_ to use the default one.
     */
    public boolean displayMute = true;
    /**
     * Should the internal/external speaker be displayed. _null_ to use the default one.
     */
    public boolean displaySpeaker = true;
    /**
     * Should the camera on/off button be displayed. _null_ to use the default one.
     */
    public boolean displayCamera = true;
    /**
     * Should the start/stop own screenshare be displayed. _null_ to use the default one.
     */
    public boolean displayScreenShare = true;
    /**
     * Should the hangup button be displayed. _null_ to use the default one.
     */
    public boolean displayLeave = true;

    /**
     * Set the R.drawable reference to show when the mic is muted. _null_ to use the default one.
     */
    public Integer mic_off = null;
    /**
     * Set the R.drawable reference to show when the mic is unmuted. _null_ to use the default one.
     */
    public Integer mic_on = null;

    /**
     * Set the R.drawable reference to show when the speakers used are the internal ones. _null_ to use the default one.
     */
    public Integer speaker_off = null;
    /**
     * Set the R.drawable reference to show when the speakers used are the external ones. _null_ to use the default one.
     */
    public Integer speaker_on = null;

    /**
     * Set the R.drawable reference to show when the camera is off. _null_ to use the default one.
     */
    public Integer camera_off = null;
    /**
     * Set the R.drawable reference to show when the camera is on. _null_ to use the default one.
     */
    public Integer camera_on = null;

    /**
     * Set the R.drawable reference to show when no local screenshare is streamed. _null_ to use the default one.
     */
    public Integer screenshare_off = null;
    /**
     * Set the R.drawable reference to show when a local screenshare is streamed. _null_ to use the default one.
     */
    public Integer screenshare_on = null;

    /**
     * Set the R.drawable reference to show when the hangup button is not pressed. _null_ to use the default one.
     */
    public Integer hangup = null;
    /**
     * Set the R.drawable reference to show when the hangup button is being pressed. _null_ to use the default one.
     */
    public Integer hangup_pressed = null;
}
