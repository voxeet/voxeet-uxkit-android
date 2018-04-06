package fr.voxeet.sdk.sample.activities;

import android.support.annotation.NonNull;

import sdk.voxeet.com.toolkit.activities.notification.AbstractIncomingCallActivity;
import sdk.voxeet.com.toolkit.activities.notification.IncomingCallFactory;
import sdk.voxeet.com.toolkit.activities.workflow.VoxeetAppCompatActivity;

/**
 * Created by kevinleperf on 06/04/2018.
 */

public class IncomingCallActivity extends AbstractIncomingCallActivity {
    @NonNull
    @Override
    protected Class<? extends VoxeetAppCompatActivity> getActivityClassToCall() {
        return MainActivity.class;
    }
}
