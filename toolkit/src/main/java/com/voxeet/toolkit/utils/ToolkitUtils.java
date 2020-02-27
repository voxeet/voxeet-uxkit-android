package com.voxeet.toolkit.utils;

import android.support.annotation.Nullable;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;

import java.util.List;

public class ToolkitUtils {

    public static boolean hasVideo(@Nullable MediaStream mediaStream) {
        return null != mediaStream && mediaStream.videoTracks().size() > 0;
    }

    public static boolean hasParticipants() {
        String ownUserId = VoxeetSDK.session().getParticipantId();
        if (null == ownUserId) ownUserId = "";
        List<Participant> users = VoxeetSDK.conference().getParticipants();

        for (Participant user : users) {
            if (ConferenceParticipantStatus.ON_AIR.equals(user.getStatus()) && !ownUserId.equals(user.getId()))
                return true;
            if (ConferenceParticipantStatus.CONNECTING.equals(user.getStatus()) && !ownUserId.equals(user.getId()) && user.streams().size() > 0)
                return true;
        }
        return false;
    }
}
