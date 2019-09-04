package com.voxeet.toolkit.implementation.overlays.abs;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.voxeet.sdk.exceptions.ExceptionManager;
import com.voxeet.sdk.utils.ScreenHelper;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.configuration.Overlay;
import com.voxeet.toolkit.controllers.VoxeetToolkit;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.providers.logics.IVoxeetSubViewProvider;
import com.voxeet.toolkit.utils.CornerHelper;
import com.voxeet.toolkit.utils.WindowHelper;

import java.util.ArrayList;

/**
 * Abbstract view used to managed the overlay state of its content. It does not manage the calls, leaving users etc...
 * It is only here to help and ensure the current overlay behaviour
 */
public abstract class AbstractVoxeetOverlayView extends AbstractVoxeetExpandableView {

    private ArrayList<AnimatorSet> mCurrentAnimations;

    private final String TAG = AbstractVoxeetOverlayView.class.getSimpleName();

    private final int defaultWidth = getResources().getDimensionPixelSize(R.dimen.conference_view_width);

    private final int defaultHeight = getResources().getDimensionPixelSize(R.dimen.conference_view_height);
    private final IVoxeetSubViewProvider mSubViewProvider;
    private IExpandableViewProviderListener mListener;

    //private boolean isMaxedOut = false;
    private OverlayState overlayState;

    private View action_button;

    private AnimationHandler animationHandler;

    private com.voxeet.sdk.views.RoundedFrameLayout container;
    private FrameLayout background_container;

    private GestureDetector gestureDetector;

    private DisplayMetrics dm;

    private WindowManager windowManager;
    private AbstractVoxeetExpandableView mSubView;
    private ViewGroup sub_container;
    private boolean mRemainExpanded;
    private boolean mCanBeMinizedByTouch;

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param listener the listener used to create the sub view
     * @param context  the context
     */
    public AbstractVoxeetOverlayView(@NonNull IExpandableViewProviderListener listener,
                                     @NonNull IVoxeetSubViewProvider provider,
                                     @NonNull Context context,
                                     @NonNull final OverlayState overlay) {
        super(context);

        mCurrentAnimations = new ArrayList<>();

        mCanBeMinizedByTouch = true;
        mRemainExpanded = false;
        //isMaxedOut = OverlayState.EXPANDED.equals(overlay);
        overlayState = overlay;

        mListener = listener;
        mSubViewProvider = provider;
        afterConstructorInit();

        final boolean[] done = {false};

        mSubView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d("VoxeetConferenceView ", "onGlobalLayout: " + overlayState);
                if (!done[0]) {
                    done[0] = true;
                    if (OverlayState.EXPANDED.equals(overlayState)) {
                        expand();
                    } else {
                        minimize();
                    }
                }
            }
        });

        sub_container.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                Log.d("VoxeetConferenceView", "onViewAttachedToWindow: " + overlayState);
                if (OverlayState.EXPANDED.equals(overlayState)) {
                    expand();
                } else {
                    minimize();
                }
            }

            @Override
            public void onViewDetachedFromWindow(View view) {

            }
        });
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);

        if (child == mSubView) {
            if (OverlayState.EXPANDED.equals(overlayState)) {
                expand();
            } else {
                minimize();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        WindowHelper.hideKeyboard(this);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int previousWidth = dm.widthPixels;
        int previousHeight = dm.heightPixels;

        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);

        if (isExpanded())
            animationHandler.toLandScape(250, previousWidth, dm.widthPixels, previousHeight, dm.heightPixels);
        else
            CornerHelper.sendToCorner(this, windowManager, getContext());

    }

    /**
     * Inform the current view wether it can be minized when the user touches it
     * Useful in custom environment
     * Note that the current default behaviour of the view is to be minizable
     *
     * @param can_be_minized_by_touch wether the can be minized when it is expanded
     */
    public void setCanBeMinizedByTouch(boolean can_be_minized_by_touch) {
        mCanBeMinizedByTouch = can_be_minized_by_touch;
    }

    @Override
    public void init() {
        animationHandler = new AnimationHandler();

        dm = new DisplayMetrics();

        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);

        gestureDetector = new GestureDetector(getContext(), new SingleTapConfirm());

        setOnTouchListener(new OnTouchListener() {
            private float dX;

            private float dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if the view can be minized by touch or it is not expanded
                if ((mCanBeMinizedByTouch || !isExpanded()) && gestureDetector.onTouchEvent(event)) {
                    toggleSize();
                } else if (!isExpanded()) { // drag n drop only when minimized
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            dX = getX() - event.getRawX();
                            dY = getY() - event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float x = event.getRawX() + dX;
                            float y = event.getRawY() + dY;
                            if (x < 0)
                                x = 0;
                            if (y < ScreenHelper.getStatusBarHeight(getContext()))
                                y = ScreenHelper.getStatusBarHeight(getContext());

                            animate().x(event.getRawX() + dX).y(event.getRawY() + dY)
                                    .setDuration(0).start();
                            break;
                        case MotionEvent.ACTION_UP:
                            CornerHelper.sendToCorner(AbstractVoxeetOverlayView.this, windowManager, getContext());
                        default:
                            return false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Toggles view's size to full screen or default size.
     */
    protected void toggleSize() {
        //isMaxedOut = getWidth() > defaultWidth && getHeight() > defaultHeight;
        if (getWidth() > defaultWidth && getHeight() > defaultHeight) {
            overlayState = OverlayState.EXPANDED;
        } else {
            overlayState = OverlayState.MINIMIZED;
        }

        if (!isExpanded()) { // maximize
            expand();
        } else { // minimize
            minimize();
        }
    }

    public void expand() {
        //isMaxedOut = true;
        overlayState = OverlayState.EXPANDED;

        WindowHelper.hideKeyboard(this);

        onPreExpandedView();
        expandView();
    }

    public void minimize() {
        if (!mRemainExpanded) {
            //isMaxedOut = false;
            overlayState = OverlayState.MINIMIZED;

            onPreMinizedView();
            minizeView();
            Intent intent = new Intent();
            intent.setAction("OnCallReceive");
            intent.putExtra("isMinimized", true);
			getContext().sendBroadcast(intent);
        }
    }

    private void onViewToggled() {
        //isMaxedOut = !isMaxedOut;
        /*if (OverlayState.EXPANDED.equals(overlayState)) {
            overlayState = OverlayState.MINIMIZED;
        } else {
            overlayState = OverlayState.EXPANDED;
        }*/

        toggleBackground();

        if (isExpanded()) {
            onExpandedView();
        } else {
            onMinizedView();
        }
    }

    protected void expandView() {
        action_button.setVisibility(View.VISIBLE);
        ViewGroup view = (ViewGroup) getParent();
        if (view != null)
            animationHandler.expand(1000, view.getWidth(), view.getHeight());
    }

    protected void minizeView() {
        if (!mRemainExpanded) {
            action_button.setVisibility(View.GONE);
            animationHandler.collapse(1000, defaultWidth, defaultHeight);
        }
    }

    protected void toggleBackground() {
        int background = 0;
        Integer color = null;
        Overlay overlay = VoxeetToolkit.getInstance().getConferenceToolkit().Configuration.Overlay;
        Resources resources = getContext().getResources();

        if (isExpanded()) {
            background = R.drawable.background_maximized_color;
            color = overlay.background_minimized_color;
            if(null != container) container.setCornerRadius(0f);
        } else {
            background = R.drawable.background_minimized_color;
            color = overlay.background_minimized_color;
            float dimension = getContext().getResources().getDimension(R.dimen.voxeet_overlay_minized_corner);
            if(null != container) container.setCornerRadius(dimension);
        }

        if(null != color) {
            background_container.setBackgroundColor(color);
        } else {
            background_container.setBackgroundResource(background);
        }

        background_container.setBackgroundResource(background);
    }


    @Override
    public void onPreExpandedView() {
        mSubView.onPreExpandedView();
    }

    @Override
    public void onExpandedView() {
        mSubView.onExpandedView();
    }

    @Override
    public void onPreMinizedView() {
        mSubView.onPreMinizedView();
    }

    @Override
    public void onMinizedView() {
        mSubView.onMinizedView();
    }


    @Override
    protected void bindView(View view) {
        container = view.findViewById(R.id.overlay_main_container);
        background_container = view.findViewById(R.id.overlay_background_container);
        sub_container = view.findViewById(R.id.container);
        action_button = view.findViewById(R.id.action_button);

        action_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onActionButtonClicked();
            }
        });

        toggleBackground();

    }


    private void afterConstructorInit() {
        mSubView = mSubViewProvider.createView(getContext(), overlayState);

        sub_container.addView(mSubView);
        //now add the subview as a listener of the current view
        addListener(mSubView);
    }

    protected abstract void onActionButtonClicked();

    /**
     * Set how long will it should take to close the current view
     * <p>
     * Every values <= 0 will be considered as being equals to "now"
     *
     * @return timeoutin milliseconds
     */
    public long getCloseTimeoutInMilliseconds() {
        return 0;
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }


    public class AnimationHandler {

        private final long animatonDuration = 200;

        /**
         * Animation when orientation changed to landscape.
         *
         * @param duration       the duration
         * @param previousWidth  the previous width
         * @param targetWidth    the target width
         * @param previousHeight the previous height
         * @param targetHeight   the target height
         */
        void toLandScape(int duration, final int previousWidth, final int targetWidth, final int previousHeight, final int targetHeight) {
            cancelAnimations();

            animate().x(0).y(0).setDuration(0).start();

            ValueAnimator height = ValueAnimator.ofInt(previousHeight, targetHeight);
            height.setDuration(duration);
            height.addUpdateListener(HEIGHT_LISTENER);

            ValueAnimator width = ValueAnimator.ofInt(previousWidth, targetWidth);
            width.setDuration(duration);
            width.addUpdateListener(WIDTH_LISTENER);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.addListener(ANIMATOR_LISTENER);
            animatorSet.setDuration(animatonDuration);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(width, height);
            appendAndStart(animatorSet);
        }

        /**
         * Expand animation.
         *
         * @param duration     the duration
         * @param targetWidth  the target width
         * @param targetHeight the target height
         */
        void expand(int duration, final int targetWidth, final int targetHeight) {
            cancelAnimations();

            animate().x(0).y(0).setDuration(300).start();

            ValueAnimator height = ValueAnimator.ofInt(getHeight(), targetHeight);
            height.setDuration(duration);
            height.addUpdateListener(HEIGHT_LISTENER);

            ValueAnimator width = ValueAnimator.ofInt(getWidth(), targetWidth);
            width.setDuration(duration);
            width.addUpdateListener(WIDTH_LISTENER);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.addListener(ANIMATOR_LISTENER);
            animatorSet.setDuration(animatonDuration);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(width, height);
            appendAndStart(animatorSet);
        }

        /**
         * Collapse the view to default size.
         *
         * @param duration     the duration
         * @param targetWidth  the target width
         * @param targetHeight the target height
         */
        void collapse(int duration, final int targetWidth, final int targetHeight) {
            cancelAnimations();

            if (isOverlay()) {
                animate().x(dm.widthPixels - defaultWidth).y(ScreenHelper.actionBar(getContext()) + ScreenHelper.getStatusBarHeight(getContext())).setDuration(300).start();
            } else if (getParent() != null) {
                ViewGroup view = (ViewGroup) getParent();
                animate().x(dm.widthPixels - defaultWidth - view.getPaddingRight()).y(view.getPaddingTop()).setDuration(200).start();
            }

            ValueAnimator height = ValueAnimator.ofInt(getHeight(), targetHeight);
            height.setDuration(duration);
            height.addUpdateListener(HEIGHT_LISTENER);

            ValueAnimator width = ValueAnimator.ofInt(getWidth(), targetWidth);
            width.setDuration(duration);
            width.addUpdateListener(WIDTH_LISTENER);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.addListener(ANIMATOR_LISTENER);
            animatorSet.setDuration(animatonDuration);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(width, height);

            appendAndStart(animatorSet);
        }

    }

    protected IExpandableViewProviderListener getExpandableViewProviderListener() {
        return mListener;
    }

    public void lockExpanded(boolean remain_expanded) {
        mRemainExpanded = remain_expanded;
    }

    protected boolean isOverlay() {
        return getParent() != null && getParent() == getRootView();
    }

    protected boolean isExpanded() {
        /*DisplayMetrics metrics = new DisplayMetrics();

        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);


        boolean expanded = getWidth() > metrics.widthPixels * 0.8 || getHeight() > metrics.heightPixels * 0.8;

        Log.d(TAG, "isExpanded: " + expanded + " " + getWidth() + " " + metrics.widthPixels + " " + getHeight() + " " + metrics.heightPixels);
        return expanded;*/

        return OverlayState.EXPANDED.equals(overlayState);
    }

    private ValueAnimator.AnimatorUpdateListener HEIGHT_LISTENER = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            ViewGroup.LayoutParams params = getLayoutParams();
            if (null != params) {
                int value = (int) animation.getAnimatedValue();
                params.height = value;
                if (null != container && null != container.getLayoutParams()) {
                    container.getLayoutParams().height = value;
                }

                requestLayout();
                Log.d(TAG, "onAnimationUpdate: height " + value);
            }
        }
    };

    private ValueAnimator.AnimatorUpdateListener WIDTH_LISTENER = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = getLayoutParams();
            if (null != params) {
                params.width = value;
                if (null != container && null != container.getLayoutParams()) {
                    container.getLayoutParams().width = value;
                }

                requestLayout();
                Log.d(TAG, "onAnimationUpdate: width " + value);
            }
        }
    };

    private Animator.AnimatorListener ANIMATOR_LISTENER = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            onViewToggled();

            if (!isExpanded()) {
                CornerHelper.sendToCorner(AbstractVoxeetOverlayView.this, windowManager, getContext());
            } else {
                animate().x(0).y(0).setDuration(0).start();
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    /**
     * Cancel any animations which could have been put in motion
     * <p>
     * It is normally - at most - with 1 animation, but this function is here to deal
     * with possibly more than 1
     * <p>
     * note that it is not done to deal with non-ui thread calls
     */
    private void cancelAnimations() {
        try {
            for (AnimatorSet animator : mCurrentAnimations) {
                if (animator.isStarted() && animator.isRunning()) {
                    animator.cancel();
                }
            }
        } catch (Exception e) {
            //nothing particular to do here, print crash just in case
            //one could happen
            e.printStackTrace();
            ExceptionManager.sendException(e);
        }

        mCurrentAnimations.clear();
    }

    /**
     * Start a given animator. Append it to the list of animations before actually
     * starting it
     *
     * @param animator a given valid animator, ready
     */
    private void appendAndStart(@NonNull AnimatorSet animator) {
        mCurrentAnimations.add(animator);

        animator.start();
    }


}
