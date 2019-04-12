package com.voxeet.toolkit.views.video;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.TextureView;
import android.view.WindowManager;

import org.webrtc.EglBaseMethods;
import org.webrtc.GlRectDrawer;
import org.webrtc.Logging;
import org.webrtc.RendererCommon;
import org.webrtc.SafeRenderFrameEglRenderer;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.util.concurrent.CountDownLatch;

/**
 * Implements org.webrtc.VideoRenderer.Callbacks by displaying the video stream on a SurfaceView.
 * renderFrame() is asynchronous to avoid blocking the calling thread.
 * This class is thread safe and handles access from potentially four different threads:
 * Interaction from the main app in init, release, setMirror, and setScalingtype.
 * Interaction from C++ rtc::VideoSinkInterface in renderFrame.
 * Interaction from the Activity lifecycle in surfaceCreated, surfaceChanged, and surfaceDestroyed.
 * Interaction with the layout framework in onMeasure and onSizeChanged.
 */
public class VoxeetRenderer extends TextureView
        implements TextureView.SurfaceTextureListener, VideoSink {
    private static final String TAG = "VoxeetRenderer";

    private Point size = new Point();

    // Cached resource name.
    private final String resourceName;
    private final RendererCommon.VideoLayoutMeasure videoLayoutMeasure =
            new RendererCommon.VideoLayoutMeasure();
    private final SafeRenderFrameEglRenderer eglRenderer;
    private Handler mHandler;
    private boolean pendingLayout = false;

    // Callback for reporting renderer events. Read-only after initilization so no lock required.
    private RendererCommon.RendererEvents rendererEvents;

    private final Object layoutLock = new Object();
    private boolean isRenderingPaused = false;
    private boolean isFirstFrameRendered;
    private int rotatedFrameWidth;
    private int rotatedFrameHeight;
    private int frameRotation;

    // Accessed only on the main thread.
    private boolean enableFixedSize;
    private int surfaceWidth;
    private int surfaceHeight;
    private boolean isEglRendererInitialized;

    private RendererCommon.ScalingType setScalingType;

    /**
     * Standard View constructor. In order to render something, you must first call init().
     */
    public VoxeetRenderer(Context context) {
        super(context);
        isEglRendererInitialized = false;
        this.resourceName = getResourceName();
        eglRenderer = new SafeRenderFrameEglRenderer(resourceName);
        setSurfaceTextureListener(this);

        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Standard View constructor. In order to render something, you must first call init().
     */
    public VoxeetRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
        isEglRendererInitialized = false;
        this.resourceName = getResourceName();
        eglRenderer = new SafeRenderFrameEglRenderer(resourceName);
        setSurfaceTextureListener(this);

        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Initialize this class, sharing resources with |sharedContext|. It is allowed to call init() to
     * reinitialize the renderer after a previous init()/release() cycle.
     */
    public void init(EglBaseMethods.Context sharedContext, RendererCommon.RendererEvents rendererEvents) {
        init(sharedContext, rendererEvents, EglBaseMethods.CONFIG_RGBA, new GlRectDrawer());
    }

    /**
     * Initialize this class, sharing resources with |sharedContext|. The custom |drawer| will be used
     * for drawing frames on the EGLSurface. This class is responsible for calling release() on
     * |drawer|. It is allowed to call init() to reinitialize the renderer after a previous
     * init()/release() cycle.
     */
    public void init(final EglBaseMethods.Context sharedContext,
                     RendererCommon.RendererEvents rendererEvents, final int[] configAttributes,
                     RendererCommon.GlDrawer drawer) {
        synchronized (eglRenderer) {
            if (isEglRendererInitialized) return;

            ThreadUtils.checkIsOnMainThread();
            this.rendererEvents = rendererEvents;
            synchronized (layoutLock) {
                isFirstFrameRendered = false;
                rotatedFrameWidth = 0;
                rotatedFrameHeight = 0;
                frameRotation = 0;
            }
            eglRenderer.init(sharedContext, configAttributes, drawer);
            isEglRendererInitialized = true;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(size);
            } else {
                display.getSize(size);
            }
        }
    }

    /**
     * Block until any pending frame is returned and all GL resources released, even if an interrupt
     * occurs. If an interrupt occurs during release(), the interrupt flag will be set. This function
     * should be called before the Activity is destroyed and the EGLContext is still valid. If you
     * don't call this function, the GL resources might leak.
     */
    public void release() {
        synchronized (eglRenderer) {
            if (!isEglRendererInitialized) return;

            isEglRendererInitialized = false;
            setSurfaceTextureListener(null);
            eglRenderer.releaseEglSurface(new Runnable() {
                @Override
                public void run() {
                    //nothing to do
                }
            });
            eglRenderer.release();

            mHandler = null;
        }
    }

    /**
     * Register a callback to be invoked when a new video frame has been received.
     *
     * @param listener    The callback to be invoked. The callback will be invoked on the render thread.
     *                    It should be lightweight and must not call removeFrameListener.
     * @param scale       The scale of the Bitmap passed to the callback, or 0 if no Bitmap is
     *                    required.
     * @param drawerParam Custom drawer to use for this frame listener.
     */
    public void addFrameListener(
            SafeRenderFrameEglRenderer.FrameListener listener, float scale, RendererCommon.GlDrawer drawerParam) {
        eglRenderer.addFrameListener(listener, scale, drawerParam);
    }

    /**
     * Register a callback to be invoked when a new video frame has been received. This version uses
     * the drawer of the EglRenderer that was passed in init.
     *
     * @param listener The callback to be invoked. The callback will be invoked on the render thread.
     *                 It should be lightweight and must not call removeFrameListener.
     * @param scale    The scale of the Bitmap passed to the callback, or 0 if no Bitmap is
     *                 required.
     */
    public void addFrameListener(SafeRenderFrameEglRenderer.FrameListener listener, float scale) {
        eglRenderer.addFrameListener(listener, scale);
    }

    public void removeFrameListener(SafeRenderFrameEglRenderer.FrameListener listener) {
        eglRenderer.removeFrameListener(listener);
    }

    /**
     * Enables fixed size for the surface. This provides better performance but might be buggy on some
     * devices. By default this is turned off.
     */
    public void setEnableHardwareScaler(boolean enabled) {
        ThreadUtils.checkIsOnMainThread();
        enableFixedSize = enabled;
        updateSurfaceSize(false);
    }

    /**
     * Set if the video stream should be mirrored or not.
     */
    public void setMirror(final boolean mirror) {
        eglRenderer.setMirror(mirror);
    }

    public boolean isMirror() {
        return eglRenderer.isMirror();
    }

    public boolean isFirstFrameRendered() {
        return isFirstFrameRendered;
    }

    /**
     * Set how the video will fill the allowed layout area.
     */
    public void setScalingType(RendererCommon.ScalingType scalingType) {
        ThreadUtils.checkIsOnMainThread();
        surfaceHeight = 0;
        surfaceWidth = 0;

        setScalingType = scalingType;
        setScalingType(scalingType, scalingType);
    }

    public void setScalingType(RendererCommon.ScalingType scalingTypeMatchOrientation,
                               RendererCommon.ScalingType scalingTypeMismatchOrientation) {
        ThreadUtils.checkIsOnMainThread();
        videoLayoutMeasure.setScalingType(scalingTypeMatchOrientation, scalingTypeMismatchOrientation);

        eglRenderer.setScalingType(scalingTypeMatchOrientation);
    }

    /**
     * TODO implement a way to get both types if set
     *
     * @return
     */
    @Nullable
    public RendererCommon.ScalingType getScalingType() {
        return setScalingType;
    }

    /**
     * Limit render framerate.
     *
     * @param fps Limit render framerate to this value, or use Float.POSITIVE_INFINITY to disable fps
     *            reduction.
     */
    public void setFpsReduction(float fps) {
        synchronized (layoutLock) {
            isRenderingPaused = fps == 0f;
        }
        eglRenderer.setFpsReduction(fps);
    }

    public void disableFpsReduction() {
        synchronized (layoutLock) {
            isRenderingPaused = false;
        }
        eglRenderer.disableFpsReduction();
    }

    public void pauseVideo() {
        synchronized (layoutLock) {
            isRenderingPaused = true;
        }
        eglRenderer.pauseVideo();
    }

    @Override
    public void onFrame(VideoFrame frame) {
        try {
            updateFrameDimensionsAndReportEvents(frame);
            eglRenderer.onFrame(frame);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // View layout interface.
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        ThreadUtils.checkIsOnMainThread();
        final Point size;
        synchronized (layoutLock) {
            size = videoLayoutMeasure.measure(widthSpec, heightSpec, rotatedFrameWidth, rotatedFrameHeight);
        }

        if (size.y > getScreenHeight()) size.y = getScreenHeight();
        if (size.x > getScreenWidth()) size.x = getScreenWidth();

        setMeasuredDimension(size.x, size.y);
        Log.d(TAG, "onMeasure: " + widthSpec + " " + heightSpec);

        pendingLayout = false;
        posting = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        ThreadUtils.checkIsOnMainThread();
        Log.d(TAG, "onLayout: " + left + " " + top + " " + right + " " + bottom);

        eglRenderer.setLayoutAspectRatio((right - left) / (float) (bottom - top));
        updateSurfaceSize(false);
    }

    private void updateSurfaceSize() {
        updateSurfaceSize(true);
    }

    private void updateSurfaceSize(boolean sendLayout) {
        ThreadUtils.checkIsOnMainThread();
        synchronized (layoutLock) {

            if (enableFixedSize && rotatedFrameWidth != 0 && rotatedFrameHeight != 0 && getWidth() != 0
                    && getHeight() != 0) {
                final float layoutAspectRatio = getWidth() / (float) getHeight();
                final float frameAspectRatio = rotatedFrameWidth / (float) rotatedFrameHeight;
                final int drawnFrameWidth;
                final int drawnFrameHeight;
                if (frameAspectRatio > layoutAspectRatio) {
                    drawnFrameWidth = (int) (rotatedFrameHeight * layoutAspectRatio);
                    drawnFrameHeight = rotatedFrameHeight;
                } else {
                    drawnFrameWidth = rotatedFrameWidth;
                    drawnFrameHeight = (int) (rotatedFrameWidth / layoutAspectRatio);
                }
                // Aspect ratio of the drawn frame and the view is the same.
                final int width = Math.min(getWidth(), drawnFrameWidth);
                final int height = Math.min(getHeight(), drawnFrameHeight);
                if (width != surfaceWidth || height != surfaceHeight) {
                    /*logD("updateSurfaceSize. Layout size: " + getWidth() + "x" + getHeight() + ", frame size: "
                            + rotatedFrameWidth + "x" + rotatedFrameHeight + ",  requested surface size: " + width
                            + "x" + height + ", old surface size: " + surfaceWidth + "x" + surfaceHeight);*/

                    surfaceWidth = width;
                    surfaceHeight = height;
                    //getHolder().setFixedSize(width, height);
                    if (sendLayout && surfaceWidth != 0 && surfaceHeight != 0)
                        requestLayoutIfNotPending();
                }
            } else {
                surfaceWidth = surfaceHeight = 0;
                //getHolder().setSizeFromLayout();
                //requestLayoutIfNotPending();
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        ThreadUtils.checkIsOnMainThread();
        eglRenderer.createEglSurface(surface);
        surfaceWidth = surfaceHeight = 0;

        updateSurfaceSize();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        ThreadUtils.checkIsOnMainThread();
        logD("surfaceChanged: size: " + width + "x" + height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        ThreadUtils.checkIsOnMainThread();
        final CountDownLatch completionLatch = new CountDownLatch(1);
        eglRenderer.releaseEglSurface(new Runnable() {
            @Override
            public void run() {
                completionLatch.countDown();
            }
        });
        ThreadUtils.awaitUninterruptibly(completionLatch);

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        ThreadUtils.checkIsOnMainThread();
        surfaceWidth = surfaceHeight = 0;
        updateSurfaceSize(false);
    }

    private String getResourceName() {
        try {
            return getResources().getResourceEntryName(getId()) + ": ";
        } catch (Resources.NotFoundException e) {
            return "";
        }
    }

    /**
     * Post a task to clear the SurfaceView to a transparent uniform color.
     */
    public void clearImage() {
        eglRenderer.clearImage();
    }

    // Update frame dimensions and report any changes to |rendererEvents|.
    private void updateFrameDimensionsAndReportEvents(VideoFrame frame) {
        synchronized (layoutLock) {
            if (isRenderingPaused) {
                return;
            }
            if (!isFirstFrameRendered) {
                isFirstFrameRendered = true;
                logD("Reporting first rendered frame.");
                if (rendererEvents != null) {
                    rendererEvents.onFirstFrameRendered();
                }
            }
            if (rotatedFrameWidth != frame.getRotatedWidth()
                    || rotatedFrameHeight != frame.getRotatedHeight()
                    || frameRotation != frame.getRotation()) {
                logD("Reporting frame resolution changed to " + frame.getBuffer().getWidth() + "x"
                        + frame.getBuffer().getHeight() + " with rotation " + frame.getRotation());
                if (rendererEvents != null) {
                    rendererEvents.onFrameResolutionChanged(
                            frame.getBuffer().getWidth(), frame.getBuffer().getHeight(), frame.getRotation());
                }
                rotatedFrameWidth = frame.getRotatedWidth();
                rotatedFrameHeight = frame.getRotatedHeight();
                frameRotation = frame.getRotation();
                post(new Runnable() {
                    @Override
                    public void run() {
                        updateSurfaceSize();
                        requestLayoutIfNotPending();
                    }
                });
            }
        }
    }

    private void logD(String string) {
        Logging.d(TAG, resourceName + string);
    }

    private boolean posting = false;
    private boolean lock = false;

    private final Runnable post = new Runnable() {
        @Override
        public void run() {
            lock = false;
            mHandler.removeCallbacks(this);
            requestLayout();
        }
    };

    private void requestLayoutIfNotPending() {

        if (posting || (null == mHandler)) return;
        if(lock) return;

        lock = true;
        posting = true;
        mHandler.postDelayed(post, 1000);
        /*if(!pendingLayout) {
            pendingLayout = true;
            requestLayout();
        }*/
    }

    public int getScreenWidth() {
        return size.x;
    }

    public int getScreenHeight() {
        return size.y;
    }
}
