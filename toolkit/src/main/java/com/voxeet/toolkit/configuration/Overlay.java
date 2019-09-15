package com.voxeet.toolkit.configuration;

import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;

/**
 * Hold the Configuration for the Overlay displayed to the user
 */
@Annotate
public class Overlay {

    @NoDocumentation
    public Overlay() {

    }

    /**
     * Set the background color when the overlay is maximized
     */
    public Integer background_maximized_color = null;

    /**
     * Set the background color when the overlay is minized
     */
    public Integer background_minimized_color = null;
}
