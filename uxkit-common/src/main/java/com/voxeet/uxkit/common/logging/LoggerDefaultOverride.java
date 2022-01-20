package com.voxeet.uxkit.common.logging;

import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Send log messages to the logcat, the tags will be the one set when being built but messages will be :
 * "tag :: message"
 */
public class LoggerDefaultOverride extends LoggerDefaultWrapper {

    private final String overrideTag;

    public LoggerDefaultOverride(@NonNull String tag) {
        overrideTag = tag;
    }

    /**
     * Send a debug message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    @Override
    public void d(@NonNull String tag, @NonNull String text) {
        Log.d(overrideTag, tag + " :: " + text);
    }

    /**
     * Send a warning message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    @Override
    public void w(@NonNull String tag, @NonNull String text) {
        Log.w(overrideTag, tag + " :: " + text);
    }

    /**
     * Send an information message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    @Override
    public void i(@NonNull String tag, @NonNull String text) {
        Log.i(overrideTag, tag + " :: " + text);
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
        Log.e(overrideTag, tag + " :: " + text, throwable);
    }
}
