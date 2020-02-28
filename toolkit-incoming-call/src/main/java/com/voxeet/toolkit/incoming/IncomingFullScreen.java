package com.voxeet.toolkit.incoming;

import android.app.Activity;
import android.support.annotation.NonNull;

@Deprecated
public class IncomingFullScreen extends com.voxeet.uxkit.incoming.IncomingFullScreen {

    public static final String[] DEFAULT_NOTIFICATION_KEYS = IncomingFullScreen.DEFAULT_NOTIFICATION_KEYS;

    public IncomingFullScreen(@NonNull Class<? extends Activity> incomingCallClass) {
        super(incomingCallClass);
    }
}
