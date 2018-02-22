package sdk.voxeet.com.toolkit.views.uitookit.sdk;

import com.voxeet.android.media.MediaStream;

import java.util.List;
import java.util.Map;

import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public interface IVoxeetView {
    /**
     * On conference joined.
     *
     * @param conferenceId the conference id
     */
    void onConferenceJoined(String conferenceId);

    /**
     * On conference updated.
     *
     * @param conferenceId the conference id
     */
    void onConferenceUpdated(List<DefaultConferenceUser> conferenceId);

    /**
     * On conference creation.
     *
     * @param conferenceId the conference id
     */
    void onConferenceCreation(String conferenceId);

    /**
     * On conference user joined.
     *
     * @param conferenceUser the conference user
     */
    void onConferenceUserJoined(DefaultConferenceUser conferenceUser);

    /**
     * On conference user updated.
     *
     * @param conferenceUser the conference user
     */
    void onConferenceUserUpdated(DefaultConferenceUser conferenceUser);

    /**
     * On conference user left.
     *
     * @param conferenceUser the conference user
     */
    void onConferenceUserLeft(DefaultConferenceUser conferenceUser);

    /**
     * An user declined the call
     *
     * @param userId the declined-user id
     */
    void onConferenceUserDeclined(String userId);

    /**
     * On recording status updated.
     *
     * @param recording the recording
     */
    void onRecordingStatusUpdated(boolean recording);

    /**
     * On media stream updated.
     *
     * @param userId the user id
     * @param mediaStreams
     */
    void onMediaStreamUpdated(String userId, Map<String, MediaStream> mediaStreams);

    /**
     *
     * @param conferenceUsers the new list of users
     */
    void onConferenceUsersListUpdate(List<DefaultConferenceUser> conferenceUsers);

    /**
     * @param mediaStreams the new list of media streams
     */
    void onMediaStreamsListUpdated(Map<String, MediaStream> mediaStreams);

    /**
     *
     * @param mediaStreams the new list of mediaStreams
     */
    void onMediaStreamsUpdated(Map<String, MediaStream> mediaStreams);

    /**
     * On conference destroyed.
     */
    void onConferenceDestroyed();

    /**
     * On conference left.
     */
    void onConferenceLeft();

    /**
     * View resumed
     */
    void onResume();

    /**
     * View destroyed
     */
    void onDestroy();

    /**
     * After init
     */
    void onInit();
}
