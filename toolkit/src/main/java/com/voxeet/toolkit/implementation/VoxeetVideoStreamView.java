package com.voxeet.toolkit.implementation;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.voxeet.sdk.json.VideoPresentationPaused;
import com.voxeet.sdk.json.VideoPresentationPlay;
import com.voxeet.sdk.json.VideoPresentationSeek;
import com.voxeet.sdk.json.VideoPresentationStarted;
import com.voxeet.sdk.json.VideoPresentationStopped;
import com.voxeet.toolkit.presentation.controller.MediaPlayerProviderController;
import com.voxeet.toolkit.presentation.provider.AbstractMediaPlayerProvider;
import com.voxeet.toolkit.presentation.view.AbstractMediaPlayerView;

public class VoxeetVideoStreamView extends FrameLayout {

    @Nullable
    private AbstractMediaPlayerView view;
    private VideoPresentationStarted started;

    public VoxeetVideoStreamView(@NonNull Context context) {
        super(context);
    }

    public VoxeetVideoStreamView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VoxeetVideoStreamView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VoxeetVideoStreamView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void onEvent(VideoPresentationStarted event) {
        view = createMediaPlayerViewForUrl(event.url);
        started = event;
        attachPlayer();
        if(null != view) view.start(started);
    }

    public void onEvent(VideoPresentationPlay event) {
        attachPlayer();
        if(null != view) view.play(event);
    }

    public void onEvent(VideoPresentationPaused event) {
        attachPlayer();
        if(null != view) view.pause(event);
    }

    public void onEvent(VideoPresentationStopped event) {
        if(null != view) view.stop(event);
        detach();
    }

    public void onEvent(VideoPresentationSeek event) {
        attachPlayer();
        if(null != view) view.seek(event);
    }

    @Nullable
    private AbstractMediaPlayerView createMediaPlayerViewForUrl(@NonNull String url) {
        AbstractMediaPlayerProvider provider = MediaPlayerProviderController.getCompatibleMediaPlayerProvider(url);

        if(null == provider) return null;
        return provider.createMediaPlayerView(getContext());
    }

    private void attachPlayer() {
        if (null != view && null == view.getParent()) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            addView(view, params);
        }
    }

    private void detach() {
        if (null != view && null != view.getParent()) {
            removeView(view);
        }
    }

}
