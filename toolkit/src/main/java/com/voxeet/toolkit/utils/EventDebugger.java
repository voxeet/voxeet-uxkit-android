package com.voxeet.toolkit.utils;

import android.util.Log;

import com.voxeet.sdk.events.sdk.ConferenceTimeoutUserJoinedEvent;
import com.voxeet.sdk.events.sdk.ConferenceUserCallDeclinedEvent;
import com.voxeet.sdk.events.success.ConferenceUpdated;
import com.voxeet.sdk.events.websocket.RenegociationEndedEvent;
import com.voxeet.sdk.json.BadgeUpdatedEvent;
import com.voxeet.sdk.json.ConferenceDestroyedPush;
import com.voxeet.sdk.json.ConferenceEnded;
import com.voxeet.sdk.json.ConferenceUserAdded;
import com.voxeet.sdk.json.ConferenceUserSwitch;
import com.voxeet.sdk.json.ContactAdded;
import com.voxeet.sdk.json.FileAddedEvent;
import com.voxeet.sdk.json.FileConverted;
import com.voxeet.sdk.json.FileDeletedEvent;
import com.voxeet.sdk.json.InvitationReceivedEvent;
import com.voxeet.sdk.json.MeetingAddedOrUpdatedEvent;
import com.voxeet.sdk.json.MeetingDeletedEvent;
import com.voxeet.sdk.json.OfferCreated;
import com.voxeet.sdk.json.OwnConferenceCreated;
import com.voxeet.sdk.json.OwnConferenceUserSwitch;
import com.voxeet.sdk.json.OwnContactRemovedEvent;
import com.voxeet.sdk.json.OwnExternalInvitationSentEvent;
import com.voxeet.sdk.json.OwnProfileUpdated;
import com.voxeet.sdk.json.OwnUserInvited;
import com.voxeet.sdk.json.ParticipantUpdated;
import com.voxeet.sdk.json.PeerConnectionStatusUpdated;
import com.voxeet.sdk.json.ProfileUpdatedEvent;
import com.voxeet.sdk.json.QualityUpdated;
import com.voxeet.sdk.json.RecordingStatusUpdateEvent;
import com.voxeet.sdk.json.UserInvited;
import com.voxeet.sdk.json.WhisperLeft;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Simple class with the sole purpose of tracing the different events
 * sent through the default event bus
 */

public class EventDebugger {

    private static final String TAG = EventDebugger.class.getSimpleName();

    public void register() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    public void unregister() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OfferCreated offer) {
        //Log.d(TAG, "onEvent: OfferCreatedEvent " + offer.getUserId() + " " + offer.getExternalId() + " " + offer.getType() + " " + offer.getDescription().getType() + " " + offer.getCandidates().size());

        Log.d(TAG, "onEvent: time := " + System.currentTimeMillis());
        Log.d(TAG, "onEvent: userId := " + offer.getUserId());
        Log.d(TAG, "onEvent: sdp := " + offer.getDescription().sdp);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPush event) {
        Log.d(TAG, "onEvent: ConferenceDestroyedPushEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ProfileUpdatedEvent event) {
        Log.d(TAG, "onEvent: ProfileUpdated " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ParticipantUpdated event) {
        Log.d(TAG, "onEvent: ParticipantUpdatedEvent " + event + " " + event.userId + " " + event.status + " " + event.conferenceId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnConferenceCreated event) {
        Log.d(TAG, "onEvent: OwnConferenceCreated " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnProfileUpdated event) {
        Log.d(TAG, "onEvent: OwnProfileUpdatedEvent " + event.getType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnExternalInvitationSentEvent event) {
        Log.d(TAG, "onEvent: OwnExternalInvitationSentEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnContactRemovedEvent event) {
        Log.d(TAG, "onEvent: OwnContactRemovedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeetingAddedOrUpdatedEvent event) {
        Log.d(TAG, "onEvent: MeetingAddedOrUpdated " + event.getType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeetingDeletedEvent event) {
        Log.d(TAG, "onEvent: MeetingDeletedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BadgeUpdatedEvent event) {
        Log.d(TAG, "onEvent: BadgeUpdatedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RenegociationEndedEvent event) {
        Log.d(TAG, "onEvent: RenegociationEndedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RecordingStatusUpdateEvent event) {
        Log.d(TAG, "onEvent: RecordingStatusUpdateEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InvitationReceivedEvent event) {
        Log.d(TAG, "onEvent: InvitationReceived " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FileDeletedEvent event) {
        Log.d(TAG, "onEvent: FileDeletedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FileAddedEvent event) {
        Log.d(TAG, "onEvent: FileAddedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QualityUpdated event) {
        Log.d(TAG, "onEvent: QualityUpdatedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WhisperLeft event) {
        Log.d(TAG, "onEvent: WhisperLeft " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnUserInvited event) {
        Log.d(TAG, "onEvent: OwnUserInvited " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserInvited event) {
        Log.d(TAG, "onEvent: UserInvited " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceUserSwitch event) {
        Log.d(TAG, "onEvent: ConferenceUserSwitch " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceUserAdded event) {
        Log.d(TAG, "onEvent: ConferenceUserAddedEvent " + event.userId + " " + event.externalId + " " + event.status);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnConferenceUserSwitch event) {
        Log.d(TAG, "onEvent: OwnConferenceUserSwitch " + event.getType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEnded event) {
        Log.d(TAG, "onEvent: ConferenceEnded " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceUpdated event) {
        Log.d(TAG, "onEvent: ConferenceUpdated " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FileConverted event) {
        Log.d(TAG, "onEvent: FileConverted " + event.getType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ContactAdded event) {
        Log.d(TAG, "onEvent: ContactAdded " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PeerConnectionStatusUpdated event) {
        Log.d(TAG, "onEvent: PeerConnectionStatusUpdated " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceTimeoutUserJoinedEvent event) {
        Log.d(TAG, "onEvent: ConferenceTimeoutUserJoinedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceUserCallDeclinedEvent event) {
        Log.d(TAG, "onEvent: ConferenceUserCallDeclinedEvent " + event);
    }
}
