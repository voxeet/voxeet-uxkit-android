package com.voxeet.toolkit.utils;

import android.support.annotation.Nullable;

import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.core.preferences.VoxeetPreferences;
import com.voxeet.sdk.models.User;
import com.voxeet.sdk.models.v1.ConferenceUserStatus;

import java.util.List;

public class ToolkitUtils {

    public static boolean hasVideo(@Nullable MediaStream mediaStream) {
        return null != mediaStream && mediaStream.videoTracks().size() > 0;
    }

    public static boolean hasParticipants() {
        String ownUserId = VoxeetPreferences.id();
        List<User> users = VoxeetSdk.conference().getConferenceUsers();

        for (User user : users) {
            if (ConferenceUserStatus.ON_AIR.equals(user.getStatus())
                    && null != user.getId()
                    && !user.getId().equals(ownUserId))
                return true;
        }
        return false;
    }
}
