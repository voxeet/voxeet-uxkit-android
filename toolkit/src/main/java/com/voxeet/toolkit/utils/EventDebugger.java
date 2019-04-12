package com.voxeet.toolkit.utils;

import android.util.Log;

import com.voxeet.sdk.events.ConferenceTimeoutUserJoinedEvent;
import com.voxeet.sdk.events.success.BadgeUpdate;
import com.voxeet.sdk.events.success.ConferenceDestroyedPushEvent;
import com.voxeet.sdk.events.success.ConferenceEndedEvent;
import com.voxeet.sdk.events.success.ConferenceUpdatedEvent;
import com.voxeet.sdk.events.success.ConferenceUserAddedEvent;
import com.voxeet.sdk.events.success.ConferenceUserCallDeclinedEvent;
import com.voxeet.sdk.events.success.ConferenceUserSwitchEvent;
import com.voxeet.sdk.events.success.ContactAddedEvent;
import com.voxeet.sdk.events.success.FileConvertedEvent;
import com.voxeet.sdk.events.success.InvitationReceived;
import com.voxeet.sdk.events.success.MeetingActivityAdded;
import com.voxeet.sdk.events.success.MeetingActivityDeleted;
import com.voxeet.sdk.events.success.MeetingActivityUpdated;
import com.voxeet.sdk.events.success.MeetingDeleted;
import com.voxeet.sdk.events.success.MeetingEventAddedOrUpdated;
import com.voxeet.sdk.events.success.MeetingReadTimeStampUpdated;
import com.voxeet.sdk.events.success.OfferCreatedEvent;
import com.voxeet.sdk.events.success.OwnConferenceStartedEvent;
import com.voxeet.sdk.events.success.OwnConferenceUserSwitchEvent;
import com.voxeet.sdk.events.success.OwnContactRemoved;
import com.voxeet.sdk.events.success.OwnExternalInvitationSent;
import com.voxeet.sdk.events.success.OwnProfileUpdatedEvent;
import com.voxeet.sdk.events.success.OwnUserInvitedEvent;
import com.voxeet.sdk.events.success.ParticipantUpdatedEvent;
import com.voxeet.sdk.events.success.PeerConnectionStatusUpdatedEvent;
import com.voxeet.sdk.events.success.ProfileUpdated;
import com.voxeet.sdk.events.success.QualityUpdatedEvent;
import com.voxeet.sdk.events.success.RecordingStatusUpdate;
import com.voxeet.sdk.events.success.RenegociationUpdate;
import com.voxeet.sdk.events.success.UserInvitedEvent;
import com.voxeet.sdk.events.success.WhisperInviteAcceptedEvent;
import com.voxeet.sdk.events.success.WhisperInviteDeclinedEvent;
import com.voxeet.sdk.events.success.WhisperInviteReceivedEvent;
import com.voxeet.sdk.events.success.WhisperLeftEvent;
import com.voxeet.sdk.json.ConferenceDestroyedPush;
import com.voxeet.sdk.json.FileAdded;
import com.voxeet.sdk.json.FileDeleted;
import com.voxeet.sdk.json.MeetingAddedOrUpdatedEvent;
import com.voxeet.sdk.json.OfferCreated;

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
    public void onEvent(OfferCreatedEvent event) {
        OfferCreated offer = event.offer();
        //Log.d(TAG, "onEvent: OfferCreatedEvent " + offer.getUserId() + " " + offer.getExternalId() + " " + offer.getType() + " " + offer.getDescription().getType() + " " + offer.getCandidates().size());

        Log.d(TAG, "onEvent: time := " + System.currentTimeMillis());
        Log.d(TAG, "onEvent: userId := " + offer.getUserId());
        Log.d(TAG, "onEvent: sdp := " + offer.getDescription().sdp);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPushEvent event) {
        Log.d(TAG, "onEvent: ConferenceDestroyedPushEvent " + event + " internal call !");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPush event) {
        Log.d(TAG, "onEvent: ConferenceDestroyedPushEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ProfileUpdated event) {
        Log.d(TAG, "onEvent: ProfileUpdated " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ParticipantUpdatedEvent event) {
        Log.d(TAG, "onEvent: ParticipantUpdatedEvent " + event + " " + event.getUserId() + " " + event.getStatus() + " " + event.getConfId());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnConferenceStartedEvent event) {
        Log.d(TAG, "onEvent: OwnConferenceStartedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnProfileUpdatedEvent event) {
        Log.d(TAG, "onEvent: OwnProfileUpdatedEvent " + event.getEvent().getType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnExternalInvitationSent event) {
        Log.d(TAG, "onEvent: OwnExternalInvitationSent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnContactRemoved event) {
        Log.d(TAG, "onEvent: OwnContactRemoved " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeetingAddedOrUpdatedEvent event) {
        Log.d(TAG, "onEvent: MeetingAddedOrUpdated " + event.getType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeetingDeleted event) {
        Log.d(TAG, "onEvent: MeetingDeleted " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BadgeUpdate event) {
        Log.d(TAG, "onEvent: BadgeUpdate " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RenegociationUpdate event) {
        Log.d(TAG, "onEvent: RenegociationUpdate " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RecordingStatusUpdate event) {
        Log.d(TAG, "onEvent: RecordingStatusUpdate " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeetingActivityDeleted event) {
        Log.d(TAG, "onEvent: MeetingActivityDeleted " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeetingActivityUpdated event) {
        Log.d(TAG, "onEvent: MeetingActivityUpdated " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeetingActivityAdded event) {
        Log.d(TAG, "onEvent: MeetingActivityAdded " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeetingReadTimeStampUpdated event) {
        Log.d(TAG, "onEvent: MeetingReadTimeStampUpdated " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InvitationReceived event) {
        Log.d(TAG, "onEvent: InvitationReceived " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeetingEventAddedOrUpdated event) {
        Log.d(TAG, "onEvent: MeetingEventAddedOrUpdated " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FileDeleted event) {
        Log.d(TAG, "onEvent: FileDeleted " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FileAdded event) {
        Log.d(TAG, "onEvent: FileAdded " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QualityUpdatedEvent event) {
        Log.d(TAG, "onEvent: QualityUpdatedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WhisperInviteReceivedEvent event) {
        Log.d(TAG, "onEvent: WhisperInviteReceivedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WhisperInviteAcceptedEvent event) {
        Log.d(TAG, "onEvent: WhisperInviteAcceptedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WhisperInviteDeclinedEvent event) {
        Log.d(TAG, "onEvent: WhisperInviteDeclinedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WhisperLeftEvent event) {
        Log.d(TAG, "onEvent: WhisperLeftEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnUserInvitedEvent event) {
        Log.d(TAG, "onEvent: OwnUserInvitedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserInvitedEvent event) {
        Log.d(TAG, "onEvent: UserInvitedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceUserSwitchEvent event) {
        Log.d(TAG, "onEvent: ConferenceUserSwitchEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceUserAddedEvent event) {
        Log.d(TAG, "onEvent: ConferenceUserAddedEvent " + event.getEvent().getUserId() + " " + event.getEvent().getExternalId() + " " + event.getEvent().getStatus());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OwnConferenceUserSwitchEvent event) {
        Log.d(TAG, "onEvent: OwnConferenceUserSwitchEvent " + event.getEvent().getType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEndedEvent event) {
        Log.d(TAG, "onEvent: ConferenceEnded " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceUpdatedEvent event) {
        Log.d(TAG, "onEvent: ConferenceUpdated " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FileConvertedEvent event) {
        Log.d(TAG, "onEvent: FileConvertedEvent " + event.getEvent().getType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ContactAddedEvent event) {
        Log.d(TAG, "onEvent: ContactAddedEvent " + event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PeerConnectionStatusUpdatedEvent event) {
        Log.d(TAG, "onEvent: PeerConnectionStatusUpdatedEvent " + event);
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
