package com.voxeet.uxkit.common.logging;

import android.util.Log;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.common.UXKitLogger;

/**
 * Send log messages to the logcat
 */
public class LoggerDefaultWrapper implements LoggerWrapper {

    /**
     * Send a debug message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    @Override
    public void d(@NonNull String tag, @NonNull String text) {
        if(UXKitLogger.logcatEnabled) Log.d(tag, text);
    }

    /**
     * Send a warning message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    @Override
    public void w(@NonNull String tag, @NonNull String text) {
        if(UXKitLogger.logcatEnabled) Log.w(tag, text);
    }

    /**
     * Send an information message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    @Override
    public void i(@NonNull String tag, @NonNull String text) {
        if(UXKitLogger.logcatEnabled) Log.i(tag, text);
    }

    /**
     * Send an exception with the appropriate message
     *
     * @param tag       the tag to use
     * @param text      the debug message
     * @param throwable the exception
     */
    @Override
    public void e(@NonNull String tag, @NonNull String text, @NonNull Throwable throwable) {
        if(UXKitLogger.logcatEnabled) Log.e(tag, text, throwable);
    }
}
