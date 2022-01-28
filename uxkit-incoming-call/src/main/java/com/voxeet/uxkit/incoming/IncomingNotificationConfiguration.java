package com.voxeet.uxkit.incoming;

public class IncomingNotificationConfiguration {

    /**
     * When setting to true, the notification will act as an ongoing notification
     * <p>
     * if this field is set to true, to cancel, a call to the method onInvitationCanceled will be required
     * <p>
     * Default false, was previously true by default
     */
    public boolean IsOnGoing = false;

    /**
     * Set the notification as auto cancel. The behaviour is the same as the system one
     * <p>
     * Default true
     */
    public boolean IsAutoCancel = true;
}
