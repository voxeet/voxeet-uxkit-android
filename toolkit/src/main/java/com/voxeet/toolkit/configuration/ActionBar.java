package com.voxeet.toolkit.configuration;

import com.voxeet.sdk.utils.Annotate;

@Annotate
public class ActionBar {
    public boolean displayMute = true;
    public boolean displaySpeaker = true;
    public boolean displayCamera = true;
    public boolean displayScreenShare = true;
    public boolean displayLeave = true;

    public Integer mic_off = null;
    public Integer mic_on = null;

    public Integer speaker_off = null;
    public Integer speaker_on = null;

    public Integer camera_off = null;
    public Integer camera_on = null;

    public Integer screenshare_off = null;
    public Integer screenshare_on = null;

    public Integer hangup = null;
    public Integer hangup_pressed = null;
}
