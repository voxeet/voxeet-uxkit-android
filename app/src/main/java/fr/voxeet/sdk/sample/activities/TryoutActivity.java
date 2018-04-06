package fr.voxeet.sdk.sample.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import fr.voxeet.sdk.sample.R;
import sdk.voxeet.com.toolkit.activities.workflow.VoxeetAppCompatActivity;

public class TryoutActivity extends VoxeetAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tryout);
    }
}
