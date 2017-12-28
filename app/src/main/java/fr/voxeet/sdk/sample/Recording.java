package fr.voxeet.sdk.sample;

/**
 * Created by romainbenmansour on 24/02/2017.
 */

public class Recording {

    public String conferenceId;

    public long timestamp;

    public Recording(String conferenceId, long timeStamp) {
        this.conferenceId = conferenceId;

        this.timestamp = timeStamp;
    }
}
