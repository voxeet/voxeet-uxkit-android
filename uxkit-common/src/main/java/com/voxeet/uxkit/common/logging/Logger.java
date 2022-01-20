package com.voxeet.uxkit.common.logging;

import android.util.Log;

import androidx.annotation.NonNull;

import com.voxeet.audio.utils.__Call;
import com.voxeet.uxkit.common.BuildConfig;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Contains and holds logger wrappers, it will receive and propagate messages to underlying instances
 */
public class Logger implements LoggerWrapper {

    private final static String TAG = Logger.class.getSimpleName();

    @NonNull
    private final List<LoggerWrapper> loggers;

    public Logger(@NonNull LoggerWrapper... loggers) {
        this.loggers = new CopyOnWriteArrayList<>();
        this.loggers.addAll(Arrays.asList(loggers));
    }

    /**
     * Add a new logger to the list of known wrappers of this instance
     * @param logger
     */
    public void add(@NonNull LoggerWrapper logger) {
        loggers.add(logger);
    }

    /**
     * Send a debug message
     *
     * @param tag   the tag to use
     * @param text  the debug message
     */
    @Override
    public void d(@NonNull String tag, @NonNull String text) {
        safeRun(logger -> logger.d(tag, text));
    }

    /**
     * Send a warning message
     *
     * @param tag   the tag to use
     * @param text  the debug message
     */
    @Override
    public void w(@NonNull String tag, @NonNull String text) {
        safeRun(logger -> logger.w(tag, text));
    }

    /**
     * Send an information message
     *
     * @param tag   the tag to use
     * @param text  the debug message
     */
    @Override
    public void i(@NonNull String tag, @NonNull String text) {
        safeRun(logger -> logger.i(tag, text));
    }

    /**
     * Send an exception with the appropriate message to the known loggers
     *
     * @param tag       the tag to use
     * @param text      the debug message
     * @param exception the exception
     */
    @Override
    public void e(@NonNull String tag, @NonNull String text, @NonNull Throwable exception) {
        safeRun(logger -> logger.e(tag, text, exception));
    }

    private void safeRun(__Call<LoggerWrapper> runnable) {
        for (LoggerWrapper logger : loggers) {
            try {
                runnable.apply(logger);
            } catch (Throwable throwable) {
                if (BuildConfig.DEBUG) Log.e(TAG, "having exception", throwable);
            }
        }
    }
}
