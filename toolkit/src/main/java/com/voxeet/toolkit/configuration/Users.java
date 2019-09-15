package com.voxeet.toolkit.configuration;

import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;

/**
 * Hold the Configuration for the Users displayed by default on top
 */
@Annotate
public class Users {

    @NoDocumentation
    public Users() {

    }

    /**
     * Set the color of the surrounding circle when any users are speaking.
     */
    public Integer speaking_user_color = null;

    /**
     * Set the color for the selected user. This variable is not currently used. it's currently a TODO
     */
    public Integer selected_user_color = null;
}
