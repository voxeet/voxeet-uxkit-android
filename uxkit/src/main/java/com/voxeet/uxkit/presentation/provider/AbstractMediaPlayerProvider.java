package com.voxeet.uxkit.presentation.provider;

import com.voxeet.uxkit.common.presentation.view.AbstractMediaPlayerView;

/**
 * Abstract class to be extended by developers to create their own management of incoming Video Presentation information
 * @param <MEDIA_PLAYER_VIEW>
 */
@Deprecated
public abstract class AbstractMediaPlayerProvider<MEDIA_PLAYER_VIEW extends AbstractMediaPlayerView> extends com.voxeet.uxkit.common.presentation.provider.AbstractMediaPlayerProvider<MEDIA_PLAYER_VIEW> {

    protected AbstractMediaPlayerProvider() {
        super();
    }
}
