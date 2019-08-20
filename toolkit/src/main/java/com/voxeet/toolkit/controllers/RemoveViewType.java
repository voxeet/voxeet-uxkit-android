package com.voxeet.toolkit.controllers;


/**
 * Simple enum to manage the different ways to request for view removal, if any
 * <p>
 * FROM_HUD = a graphical interaction occured : pause, kill etc...
 * FROM_EVENT = the conference emitted an/- event-s, management requires a removal from thi.o.se
 */

public enum RemoveViewType {
    FROM_HUD,
    FROM_EVENT,
    FROM_HUD_BUT_KEEP_TIMEOUT
}
