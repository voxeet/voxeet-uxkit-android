package com.voxeet.uxkit.service;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;

public class VoxeetSystemService extends AbstractSDKService<VoxeetSystemService.VoxeetSystemBinder> {

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

    @Override
    protected Class<? extends Activity> getActivityClass() {
        return SystemServiceFactory.getAppCompatActivity();
    }

}
