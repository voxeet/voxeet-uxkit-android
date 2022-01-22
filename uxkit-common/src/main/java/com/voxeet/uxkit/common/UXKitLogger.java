package com.voxeet.uxkit.common;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.common.logging.Logger;
import com.voxeet.uxkit.common.logging.LoggerDefaultOverride;
import com.voxeet.uxkit.common.logging.LoggerDefaultWrapper;
import com.voxeet.uxkit.common.logging.LoggerWrapper;
import com.voxeet.uxkit.common.logging.ShortLogger;

/**
 * Global instance which by default includes the following output to logcat :
 * <p>
 * - sends messages with tag to "tag" (adb logcat -s tag) - filter to specific tags
 * - sends messages to UXKitLogger tag (adb logcat -s UXKitLogger) - broader output
 */
public class UXKitLogger {

    private final static String TAG = UXKitLogger.class.getSimpleName();

    private final static Logger logger = new Logger(new LoggerDefaultOverride(TAG), new LoggerDefaultWrapper());

    public static boolean enabled = false;

    // during UXKit local development, enable the 2 default logcat's loggers
    // while developing a project using the UXKit, this needs to be set to true/false depending on the requirements
    // for instance UXKitLogger = BuildConfig.DEBUG (where BuildConfig is in this case the projet's BuildConfig, not UXKit's
    public static boolean logcatEnabled = BuildConfig.DEBUG;


    /**
     * Register a new Logger to the list of loggers
     *
     * @param wrapper Add a new logger wrapper. No check is done for possible clones
     */
    public void add(@NonNull LoggerWrapper wrapper) {
        logger.add(wrapper);
    }

    /**
     * Send a debug message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    public static void d(@NonNull String tag, @NonNull String text) {
        if (enabled) logger.d(tag, text);
    }

    /**
     * Send a warning message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    public static void w(@NonNull String tag, @NonNull String text) {
        if (enabled) logger.w(tag, text);
    }

    /**
     * Send an information message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    public static void i(@NonNull String tag, @NonNull String text) {
        if (enabled) logger.i(tag, text);
    }

    /**
     * Send an exception with the appropriate message
     *
     * @param tag       the tag to use
     * @param text      the debug message
     * @param throwable the exception
     */
    public static void e(@NonNull String tag, @NonNull String text, @NonNull Throwable throwable) {
        if (enabled) logger.e(tag, text, throwable);
    }

    public static ShortLogger createLogger(@NonNull Class<?> klass) {
        return createLogger(klass.getSimpleName());
    }

    public static ShortLogger createLogger(@NonNull String tag) {
        return new ShortLogger() {
            @Override
            public void d(@NonNull String text) {
                UXKitLogger.d(tag, text);
            }

            @Override
            public void w(@NonNull String text) {
                UXKitLogger.w(tag, text);
            }

            @Override
            public void i(@NonNull String text) {
                UXKitLogger.i(tag, text);
            }

            @Override
            public void e(@NonNull Throwable throwable) {
                UXKitLogger.e(tag, "Exception reported", throwable);
            }

            @Override
            public void e(@NonNull String text, @NonNull Throwable throwable) {
                UXKitLogger.e(tag, text, throwable);
            }
        };
    }

}
