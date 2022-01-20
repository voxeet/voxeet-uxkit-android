package com.voxeet.uxkit.common;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.common.logging.ILoggerWrapper;
import com.voxeet.uxkit.common.logging.Logger;
import com.voxeet.uxkit.common.logging.LoggerDefaultOverride;
import com.voxeet.uxkit.common.logging.LoggerDefaultWrapper;

/**
 * Global instance which by default includes the following output to logcat :
 *
 * - sends messages with tag to "tag" (adb logcat -s tag) - filter to specific tags
 * - sends messages to UXKitLogger tag (adb logcat -s UXKitLogger) - broader output
 */
public class UXKitLogger {

    private final static String TAG = UXKitLogger.class.getSimpleName();

    private final static Logger logger = new Logger(new LoggerDefaultOverride(TAG), new LoggerDefaultWrapper());

    public static boolean enabled = false;

    /**
     * Register a new Logger to the list of loggers
     *
     * @param wrapper Add a new logger wrapper. No check is done for possible clones
     */
    public void add(@NonNull ILoggerWrapper wrapper) {
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

}
