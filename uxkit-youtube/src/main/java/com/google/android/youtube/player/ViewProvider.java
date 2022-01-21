package com.google.android.youtube.player;

import android.content.Context;

public class ViewProvider {

    public static YouTubePlayerView createView(Context context) {
        return new YouTubePlayerView(context, null, 0, new a());
    }


    private static final class a implements YouTubePlayerView.b {
        private a() {
        }

        @Override
        public void a(YouTubePlayerView youTubePlayerView, String s, YouTubePlayer.OnInitializedListener onInitializedListener) {

        }

        @Override
        public void a(YouTubePlayerView youTubePlayerView) {

        }
    }
}
