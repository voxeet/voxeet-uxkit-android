package fr.voxeet.sdk.sample.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.voxeet.sdk.sample.R;
import com.voxeet.toolkit.activities.VoxeetAppCompatActivity;

public class ActivityToTestOverlay extends VoxeetAppCompatActivity {

    private Button launch_one;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, ActivityToTestOverlay.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_to_test_overlay);

        launch_one = findViewById(R.id.launch_one);

        launch_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(ActivityToTestOverlay.this);
            }
        });
    }
}
