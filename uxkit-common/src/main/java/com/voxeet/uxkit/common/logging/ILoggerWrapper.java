package com.voxeet.uxkit.common.logging;

import androidx.annotation.NonNull;

/**
 * Interface describing what should be able to be logged
 */
public interface ILoggerWrapper {

    /**
     * Send a debug message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    void d(@NonNull String tag, @NonNull String text);

    /**
     * Send a warning message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    void w(@NonNull String tag, @NonNull String text);

    /**
     * Send an information message
     *
     * @param tag  the tag to use
     * @param text the debug message
     */
    void i(@NonNull String tag, @NonNull String text);

    /**
     * Send an exception with the appropriate message
     *
     * @param tag       the tag to use
     * @param text      the debug message
     * @param throwable the exception
     */
    void e(@NonNull String tag, @NonNull String text, @NonNull Throwable throwable);

}
