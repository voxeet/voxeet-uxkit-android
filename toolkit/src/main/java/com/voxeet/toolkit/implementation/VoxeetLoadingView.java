package com.voxeet.toolkit.implementation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.voxeet.toolkit.R;
import com.voxeet.toolkit.views.internal.rounded.RoundedImageView;

/**
 * Created by romainbenmansour on 29/03/2017.
 */
public class VoxeetLoadingView extends VoxeetView {

    private static final long FADE_DURATION = 750;

    private RoundedImageView images[];

    public VoxeetLoadingView(Context context) {
        super(context);
    }

    public VoxeetLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        updateAttrs(attrs);
    }

    public VoxeetLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        updateAttrs(attrs);
    }

    /**
     * Sets the meter color.
     *
     * @param color the color
     */
    public void setLoadingColor(int color) {
        for (RoundedImageView imageView : images)
            imageView.setImageDrawable(new ColorDrawable(color));

        invalidate();
    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetLoadingView);
        ColorStateList color = attributes.getColorStateList(R.styleable.VoxeetLoadingView_loading_color);
        attributes.recycle();

        if (color != null)
            setLoadingColor(color.getColorForState(getDrawableState(), 0));
    }

    public void onStop() {
        for (RoundedImageView imageView : images)
            if (imageView.getAnimation() != null)
                imageView.getAnimation().cancel();
    }

    @Override
    public void init() {
        ObjectAnimator first = ObjectAnimator.ofFloat(images[0], View.ALPHA, 1);
        first.setRepeatCount(ValueAnimator.INFINITE);
        first.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator second = ObjectAnimator.ofFloat(images[1], View.ALPHA, 1);
        second.setRepeatCount(ValueAnimator.INFINITE);
        second.setStartDelay(FADE_DURATION / 3);
        second.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator third = ObjectAnimator.ofFloat(images[2], View.ALPHA, 1);
        third.setRepeatCount(ValueAnimator.INFINITE);
        third.setStartDelay((2 * FADE_DURATION) / 3);
        third.setRepeatMode(ValueAnimator.REVERSE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(FADE_DURATION);
        animatorSet.playTogether(first, second, third);
        animatorSet.start();
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_loading_view;
    }

    @Override
    protected void bindView(View view) {
        images = new RoundedImageView[3];
        images[0] = view.findViewById(R.id.first);
        images[1] = view.findViewById(R.id.second);
        images[2] = view.findViewById(R.id.third);
    }
}
