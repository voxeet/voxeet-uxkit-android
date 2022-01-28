package com.voxeet.uxkit.incoming.implementation;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.incoming.AbstractIncomingNotificationService;

public class DefaultIncomingNotificationService extends AbstractIncomingNotificationService<DefaultIncomingNotificationIntentProvider> {
    @NonNull
    @Override
    protected DefaultIncomingNotificationIntentProvider createIncomingNotificationIntentProvider() {
        return new DefaultIncomingNotificationIntentProvider(this);
    }
}
