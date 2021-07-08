package com.voxeet.uxkit.configuration;

import android.content.Context;

/**
 * Hold the various sub-Configuration objects for the given Configuration instance.
 * It's mandatory for each Controllers to have one instance of this Class
 */
public class Configuration {

    public Configuration() {

    }

    /**
     * Hold the ActionBar configuration instance for this specific Configuration holder
     */
    public final ActionBar ActionBar = new ActionBar();

    /**
     * Hold the Users configuration instance for this specific Configuration holder
     */
    public final Users Users = new Users();

    /**
     * Hold the Overlay configuration instance for this specific Configuration holder
     */
    public final Overlay Overlay = new Overlay();

    /**
     * Hold the Contextual configration instance this specific Configuration holder
     */
    public final Contextual Contextual = new Contextual();
}
