package com.voxeet.uxkit.screenshare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.sdk.json.VideoPresentationStarted;
import com.voxeet.sdk.json.VideoPresentationStopped;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.utils.Opt;

public class StreamEventToPresentationConverter {

    @Nullable
    public static VideoPresentationStarted onStreamAddedEvent(@NonNull Conference conference, @NonNull Participant participant, @NonNull MediaStream mediaStream) {
        boolean isScreenShare = Opt.of(mediaStream).then(stream -> MediaStreamType.ScreenShare.equals(stream.getType())).or(false);

        if (isScreenShare) {
            VideoPresentationStarted started = new VideoPresentationStarted();
            started.url = "screenshare://";
            started.key = "screenshare";
            started.conferenceId = conference.getId();
            started.participantId = participant.getId();
            return started;
        }

        return null;
    }

    @Nullable
    public static VideoPresentationStopped onStreamRemovedEvent(@NonNull Conference conference, @NonNull Participant participant, @NonNull MediaStream mediaStream) {
        boolean isScreenShare = Opt.of(mediaStream).then(stream -> MediaStreamType.ScreenShare.equals(stream.getType())).or(false);
        if (isScreenShare) {
            VideoPresentationStopped stopped = new VideoPresentationStopped();
            stopped.key = "screenshare";
            stopped.conferenceId = conference.getId();
            stopped.participantId = participant.getId();
            return stopped;
        }
        return null;
    }
}
