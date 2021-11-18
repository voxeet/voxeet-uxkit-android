package com.voxeet.uxkit.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.sdk.models.v2.ParticipantType;
import com.voxeet.sdk.utils.Map;
import com.voxeet.sdk.utils.MapFilter;
import com.voxeet.sdk.utils.Opt;

import java.util.List;

public class ToolkitUtils {

    public static boolean hasVideo(@Nullable MediaStream mediaStream) {
        return null != mediaStream && mediaStream.videoTracks().size() > 0;
    }

    public static boolean isParticipant(@NonNull Participant participant) {
        String ownUserId = VoxeetSDK.session().getParticipantId();
        ParticipantType type = Opt.of(participant.participantType()).or(ParticipantType.NONE);

        if ("00000000-0000-0000-0000-000000000000".equals(participant.getId())) return false;
        if (!(type.equals(ParticipantType.DVC) || type.equals(ParticipantType.USER) || type.equals(ParticipantType.PSTN))) {
            return false;
        }

        if (ConferenceParticipantStatus.ON_AIR.equals(participant.getStatus()) && !ownUserId.equals(participant.getId()))
            return true;
        if (ConferenceParticipantStatus.CONNECTING.equals(participant.getStatus()) && !ownUserId.equals(participant.getId()) && participant.streams().size() > 0)
            return true;

        return false;
    }

    public static List<Participant> filterParticipants(@NonNull List<Participant> participants) {
        return Map.filter(participants, ToolkitUtils::isParticipant);
    }

    public static boolean hasParticipants() {
        String ownUserId = VoxeetSDK.session().getParticipantId();
        if (null == ownUserId) ownUserId = "";
        List<Participant> users = VoxeetSDK.conference().getParticipants();

        for (Participant user : users) {
            if (isParticipant(user)) return true;
        }
        return false;
    }
}
