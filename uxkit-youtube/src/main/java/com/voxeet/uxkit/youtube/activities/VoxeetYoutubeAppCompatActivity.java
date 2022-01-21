package com.voxeet.uxkit.youtube.activities;

import android.os.Bundle;

import com.voxeet.uxkit.common.activity.VoxeetCommonAppCompatActivity;
import com.voxeet.uxkit.common.service.AbstractSDKService;
import com.voxeet.uxkit.common.service.SDKBinder;

@Deprecated
public class VoxeetYoutubeAppCompatActivity<T extends AbstractSDKService<? extends SDKBinder<T>>> extends VoxeetCommonAppCompatActivity<T> {

    public VoxeetYoutubeAppCompatActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
