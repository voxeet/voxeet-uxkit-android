package com.voxeet.uxkit.incoming.implementation;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

public class DefaultAndroid12BounceActivity extends AppCompatActivity {

    private final ShortLogger Log = UXKitLogger.createLogger(DefaultAndroid12BounceActivity.class);
    public final static String FULLY_QUALIFIED_NAME = "FULLY_QUALIFIED_NAME";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String fullyQualifiedName = Opt.of(getIntent()).then(Intent::getExtras)
                .then(b -> b.getString(FULLY_QUALIFIED_NAME, null)).orNull();

        if(null != fullyQualifiedName) {
            try {
                Class broadcast = Class.forName(fullyQualifiedName);
                BroadcastReceiver receiver = (BroadcastReceiver) broadcast.newInstance();
                receiver.onReceive(this, getIntent());
            } catch(Throwable throwable) {
                Log.e("Error", throwable);
            }
        } else {
            Log.d("can't start activity : no FULLY_QUALIFIED_NAME");
        }
        
        finish();
    }
}
