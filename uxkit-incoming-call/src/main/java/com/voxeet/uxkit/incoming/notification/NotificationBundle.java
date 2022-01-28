package com.voxeet.uxkit.incoming.notification;

import android.app.Notification;

import androidx.annotation.NonNull;

public class NotificationBundle {
    @NonNull
    public int notificationId;

    @NonNull
    public Notification notification;

    public NotificationBundle(@NonNull int notificationId, @NonNull Notification notification) {
        this.notificationId = notificationId;
        this.notification = notification;
    }
}
