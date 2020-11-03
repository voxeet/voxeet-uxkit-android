package com.voxeet.uxkit.utils;

import android.support.annotation.Nullable;
import android.util.Log;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.sdk.models.v2.ParticipantType;
import com.voxeet.sdk.utils.Opt;

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
            ParticipantType type = Opt.of(user.participantType()).or(ParticipantType.NONE);

            if ("00000000-0000-0000-0000-000000000000".equals(user.getId())) continue;
            if (!(type.equals(ParticipantType.DVC) || type.equals(ParticipantType.USER) || type.equals(ParticipantType.PSTN))) {
                continue;
            }

            if (ConferenceParticipantStatus.ON_AIR.equals(user.getStatus()) && !ownUserId.equals(user.getId()))
                return true;
            if (ConferenceParticipantStatus.CONNECTING.equals(user.getStatus()) && !ownUserId.equals(user.getId()) && user.streams().size() > 0)
                return true;
        }
        return false;
    }
}
