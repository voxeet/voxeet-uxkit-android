package com.voxeet.uxkit.common;

import com.voxeet.sdk.media.constraints.Constraints;
import com.voxeet.uxkit.common.activity.bundle.OnAcceptCallback;

public class DefaultConfiguration {
    /**
     * Change the value of defaultVideo to react to
     * - IncomingBundleChecker.onAccept
     * - DefaultIncomingBundleChecker.onAccept
     * <p>
     * Note that the uxkit will try to configure the internal call to onAccept given those values
     * but external (cross platform) lib using it won't probably, if such case happen,
     * don't hesitate to contact maintainers or us
     */
    public static boolean defaultVideo = false;

    /**
     * Change the value of defaultAudio to react to
     * - IncomingBundleChecker.onAccept
     * - DefaultIncomingBundleChecker.onAccept
     * <p>
     * Note that the uxkit will try to configure the internal call to onAccept given those values
     * but external (cross platform) lib using it won't probably, if such case happen,
     * don't hesitate to contact maintainers or us
     */
    public static boolean defaultAudio = true;

    public final static OnAcceptCallback onAcceptCallback = builder ->
            builder.setConstraints(new Constraints(defaultAudio, defaultVideo));
}
