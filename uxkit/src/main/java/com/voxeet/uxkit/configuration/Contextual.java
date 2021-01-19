package com.voxeet.uxkit.configuration;

import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;

/**
 * Hold the Configuration for the Contextual interactions when entering conferences
 */
@Annotate
public class Contextual {

    @NoDocumentation
    public Contextual() {

    }

    /**
     * Set the default output to the speakers when entering the conference
     */
    public Boolean default_speaker_on = true;
}
