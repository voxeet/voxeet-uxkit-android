package fr.voxeet.sdk.sample.activities;

import android.os.Bundle;

import sdk.voxeet.com.toolkit.activities.notification.AbstractIncomingCallActivity;

/**
 * Minimalist Activity receiving incoming calls from the SDK
 */

public class IncomingCallActivity extends AbstractIncomingCallActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //specific Voxeet implement is done here
        super.onCreate(savedInstanceState);

        //no need to implement setContentView when using AbstractIncomingCallActivity
    }
}
