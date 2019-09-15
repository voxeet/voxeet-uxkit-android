package com.voxeet.toolkit.configuration;

import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;

/**
 * Hold the various sub-Configuration objects for the given Configuration instance.
 * It's mandatory for each Controllers to have one instance of this Class
 */
@Annotate
public class Configuration {

    @NoDocumentation
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
}
