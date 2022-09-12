package com.voxeet.uxkit.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.sdk.models.v2.ParticipantType;
import com.voxeet.sdk.utils.Map;
import com.voxeet.sdk.utils.Opt;

import java.util.Arrays;
import java.util.List;

public class ToolkitUtils {

    public static boolean hasVideo(@Nullable MediaStream mediaStream) {
        return null != mediaStream && mediaStream.videoTracks().size() > 0;
    }

    public static boolean isParticipant(@NonNull Participant participant) {
        String ownUserId = VoxeetSDK.session().getParticipantId();
        ParticipantType type = Opt.of(participant.participantType()).or(ParticipantType.NONE);

        if ("00000000-0000-0000-0000-000000000000".equals(participant.getId())) return false;

        List<ParticipantType> valids = Arrays.asList(ParticipantType.DVC,
                ParticipantType.USER,
                ParticipantType.PSTN,
                ParticipantType.ROBOT_PSTN,
                ParticipantType.ROBOT_SPEAKER,
                ParticipantType.ROBOT);
        ParticipantType found = Map.find(valids, participantType -> participantType.equals(type));

        if (null == found) {
            return false;
        }

        if (Opt.of(participant.getId()).or("").equals(ownUserId)) {
            //prevent own user to be "active speaker"
            return false;
        }
        if (ConferenceParticipantStatus.ON_AIR == participant.getStatus()) return true;
        return ConferenceParticipantStatus.CONNECTING == participant.getStatus() && null != participant.streamsHandler().getFirst(MediaStreamType.Camera);
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

    public static List<Participant> getOnAirParticipants() {
        return Filter.filter(VoxeetSDK.conference().getParticipants(), participant -> {
            if (participant.getId().equals(VoxeetSDK.session().getParticipantId())) return false;
            if (com.voxeet.sdk.utils.ParticipantUtils.isMixer(participant)) return false;

            if (ConferenceParticipantStatus.ON_AIR.equals(participant.getStatus())) {
                return true;
            }
            if (ConferenceParticipantStatus.CONNECTING.equals(participant.getStatus()) && participant.streams().size() > 0) {
                return true;
            }

            //default, nope
            return false;
        });
    }

    public static boolean hasParticipantsOnline() {
        return getOnAirParticipants().size() > 0;
    }
}
