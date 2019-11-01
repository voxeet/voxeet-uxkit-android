package com.voxeet.toolkit.service;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.voxeet.sdk.utils.Annotate;

@Annotate
public class VoxeetSystemService extends AbstractSDKService<VoxeetSystemService.VoxeetSystemBinder> {

    public static class VoxeetSystemBinder extends SDKBinder<VoxeetSystemService> {

        private VoxeetSystemService instance;

        public VoxeetSystemBinder(@NonNull VoxeetSystemService instance) {
            this.instance = instance;
        }

        @NonNull
        @Override
        public VoxeetSystemService getService() {
            return instance;
        }
    }

    @NonNull
    @Override
    public VoxeetSystemBinder onBind(@NonNull Intent intent) {
        return new VoxeetSystemBinder(this);
    }
}
