package com.voxeet.uxkit.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.voxeet.VoxeetSDK;
import com.voxeet.sdk.events.sdk.ConferenceStatusUpdatedEvent;
import com.voxeet.sdk.events.v2.AudioStateEvent;
import com.voxeet.sdk.events.v3.MicrophoneStateEvent;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.services.conference.information.ConferenceStatus;
import com.voxeet.sdk.services.media.MediaState;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.common.service.AbstractSDKService;
import com.voxeet.uxkit.common.service.SDKBinder;
import com.voxeet.uxkit.common.service.SystemServiceFactory;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class VoxeetSystemService extends AbstractSDKService<VoxeetSystemService.VoxeetSystemBinder> {

    public static boolean enableActions = false;
    private final static ShortLogger Log = UXKitLogger.createLogger(VoxeetSystemService.class);

    public static class VoxeetSystemBinder extends SDKBinder<VoxeetSystemService> {

        private VoxeetSystemService instance;

        public VoxeetSystemBinder(@NonNull VoxeetSystemService instance) {
            this.instance = instance;
        }

        @NonNull
        @Override
        public VoxeetSystemService getService() {
            return instance;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        VoxeetSDK.instance().register(this);
    }

    @NonNull
    @Override
    public VoxeetSystemBinder onBind(@NonNull Intent intent) {
        return new VoxeetSystemBinder(this);
    }

    @Override
    protected int getConferenceStateCreating() {
        return R.string.voxeet_foreground_conference_state_creating;
    }

    @Override
    protected int getConferenceStateCreated() {
        return R.string.voxeet_foreground_conference_state_created;
    }

    @Override
    protected int getConferenceStateJoining() {
        return R.string.voxeet_foreground_conference_state_joining;
    }

    @Override
    protected int getConferenceStateJoined() {
        return R.string.voxeet_foreground_conference_state_joined;
    }

    @Override
    protected int getConferenceStateJoinedError() {
        return R.string.voxeet_foreground_conference_state_join_error;
    }

    @Override
    protected int getConferenceStateLeft() {
        return R.string.voxeet_foreground_conference_state_left;
    }

    @Override
    protected int getConferenceStateEnd() {
        return R.string.voxeet_foreground_conference_state_ended;
    }

    @Override
    protected int getNotificationId() {
        return 342;
    }

    @Override
    protected int getSmallIcon() {
        return R.drawable.ic_stat_conference;
    }

    @NonNull
    @Override
    protected String getContentTitle() {
        return getString(R.string.voxeet_foreground_content_title);
    }

    protected void afterNotificationBuild(NotificationCompat.Builder builder,
                                          ConferenceStatus status) {
        // if we don't have support for the actions set by developer, just do nothing
        if (!VoxeetSystemService.enableActions) return;

        Log.d("afterNotificationBuild status " + status);
        if (!ConferenceStatus.JOINED.equals(status)) return;

        Intent leaveCall = new Intent(this, OnLeaveActionBroadcastReceiver.class);

        int flag = PendingIntent.FLAG_UPDATE_CURRENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flag |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntentLeave = PendingIntent.getBroadcast(this, 24, leaveCall, flag);

        ConferenceInformation conference = VoxeetSDK.conference().getCurrentConference();
        if (null != conference) {
            MediaState state = conference.getAudioState();

            if (state == MediaState.STARTED) {
                if (VoxeetSDK.conference().isMuted()) {
                    Intent intent = new Intent(this, OnUnMuteActionBroadcastReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 23, intent, flag);
                    builder.addAction(R.drawable.ic_action_unmute, getString(R.string.notification_unmute_mic), pendingIntent);
                } else {
                    Intent intent = new Intent(this, OnMuteActionBroadcastReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 22, intent, flag);
                    builder.addAction(R.drawable.ic_action_mute, getString(R.string.notification_mute_mic), pendingIntent);
                }
            }
        }
        builder.addAction(R.drawable.ic_action_leave_call, getString(R.string.leave_call), pendingIntentLeave);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull MicrophoneStateEvent event) {
        //retrigger a conference state to force update of the notification
        Conference conference = VoxeetSDK.conference().getConference();
        if (null == conference) return;

        onEvent(new ConferenceStatusUpdatedEvent(conference, conference.getState()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull AudioStateEvent event) {
        //retrigger a conference state to force update of the notification
        Conference conference = VoxeetSDK.conference().getConference();
        if (null == conference) return;

        if(!conference.getId().equals(Opt.of(event.conference).then(Conference::getId).orNull())) return;

        onEvent(new ConferenceStatusUpdatedEvent(conference, conference.getState()));
    }

    @Override
    protected Class<? extends Activity> getActivityClass() {
        return SystemServiceFactory.getActivityClass();
    }

}
