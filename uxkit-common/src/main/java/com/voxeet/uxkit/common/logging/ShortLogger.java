package com.voxeet.uxkit.common.logging;

import androidx.annotation.NonNull;

/**
 * Interface describing what should be able to be logged
 */
public interface ShortLogger {

    /**
     * Send a debug message
     *
     * @param text the debug message
     */
    void d(@NonNull String text);

    /**
     * Send a warning message
     *
     * @param text the debug message
     */
    void w(@NonNull String text);

    /**
     * Send an information message
     *
     * @param text the debug message
     */
    void i(@NonNull String text);

    /**
     * Send an exception with the appropriate message
     *
     * @param text      the debug message
     * @param throwable the exception
     */
    void e(@NonNull String text, @NonNull Throwable throwable);

    /**
     * Send an exception with the appropriate message
     *
     * @param throwable the exception
     */
    void e(@NonNull Throwable throwable);

}
