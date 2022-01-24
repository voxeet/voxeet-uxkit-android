package com.voxeet.uxkit.screenshare.utils;

import android.content.Context;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PinchGestureProvider {

    private final static int MAXIMUM_SCALE_FACTORS = 5;

    /**
     * Create an instance of ScaleGestureDetector which will warn when a zoom/dezoom needs to be performed
     *
     * @param context  the context to use
     * @param onZoom   callback for zooming
     * @param onDezoom callback for dezoom
     * @return a non null instance
     */
    @NonNull
    public static ScaleGestureDetector create(@NonNull Context context,
                                              @NonNull Runnable onZoom,
                                              @NonNull Runnable onDezoom) {
        return new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            List<Float> mItems = new ArrayList<>();

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                while (mItems.size() > MAXIMUM_SCALE_FACTORS) mItems.remove(0);
                mItems.add(scaleFactor);

                return super.onScale(detector);
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mItems.clear();
                return super.onScaleBegin(detector);
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                int up = 0;
                int down = 0;
                for (int i = 0, j = 1; i < mItems.size() && j < mItems.size(); i++, j++) {
                    if (mItems.get(i) < mItems.get(j)) up++;
                    else down++;
                }

                if (up > down) {
                    onZoom.run();
                } else {
                    onDezoom.run();
                }

                super.onScaleEnd(detector);
            }
        });
    }
}
