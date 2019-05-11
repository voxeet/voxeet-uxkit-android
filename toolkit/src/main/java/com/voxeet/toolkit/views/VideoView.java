package com.voxeet.toolkit.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.voxeet.android.media.EglBaseRefreshEvent;
import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.views.internal.rounded.RoundedFrameLayout;
import com.voxeet.toolkit.views.video.VoxeetRenderer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.EglBaseMethods;
import org.webrtc.RendererCommon;

import java.util.ArrayList;
import java.util.List;

/**
 * VideoView implementation
 */
public class VideoView extends FrameLayout implements RendererCommon.RendererEvents {
    private static final int SCALE_FIT = 0;
    private static final int SCALE_FILL = 1;
    private static final int SCALE_BALANCED = 2;

    private final String TAG = VideoView.class.getSimpleName();

    private boolean mIsAttached = false;

    private List<RendererCommon.RendererEvents> mEventsListeners;
    /**
     * The Voxeet renderer.
     */
    //protected SurfaceViewRenderer mRenderer;
    protected VoxeetRenderer mRenderer;

    private String mPeerId;

    private MediaStream mMediaStream;

    private boolean autoUnAttach = false;

    //private EglBase eglBase;

    private boolean shouldMirror = false;
    private boolean showFlip = false;
    private int mScaleType = SCALE_FIT;
    private Handler mHandler;

    // this view holds the reference to the renderer AND the flip image
    private View mInternalVideoView;
    private View mFlip;
    private RoundedFrameLayout mCornerRadiusView;
    private boolean mIsCircle;
    private float mCornerRadius;
    private boolean enableRefreshEglBase = false;

    private boolean layoutOnAttached = false;

    /**
     * Instantiates a new Video view.
     *
     * @param context the context
     */
    public VideoView(Context context) {
        super(context);

        init();
    }

    /**
     * Instantiates a new Video view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        updateAttrs(attrs);

        init();
    }

    @Override
    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this);

        layoutOnAttached = false;

        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        EventBus.getDefault().unregister(this);
    }

    private void init() {
        mHandler = new Handler();
        mEventsListeners = new ArrayList<>();
    }


    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VideoView);
        shouldMirror = attributes.getBoolean(R.styleable.VideoView_mirrored, false);
        showFlip = attributes.getBoolean(R.styleable.VideoView_showFlip, false);

        mScaleType = attributes.getInteger(R.styleable.VideoView_scaleType, SCALE_FIT);

        mIsCircle = attributes.getBoolean(R.styleable.VideoView_circle, false);
        mCornerRadius = attributes.getDimension(R.styleable.VideoView_cornerRadius, 0);

        attributes.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        createRendererIfNeeded();
        setSurfaceViewRenderer();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        updateFlip();
    }

    /**
     * Add a new RendererEvents listener to the current VideoView
     * <p>
     * Caution : no catch are made when listeners are called !
     *
     * @param listener a non null listener
     */
    public void addListener(@NonNull RendererCommon.RendererEvents listener) {
        synchronized (listener) {
            if (!mEventsListeners.contains(listener)) {
                mEventsListeners.add(listener);
            }
        }
    }

    /**
     * Remove a specified listener from the list of internal RendererEvents listener
     *
     * @param listener a non null listener to remove
     */
    public void removeListener(@NonNull RendererCommon.RendererEvents listener) {
        synchronized (listener) {
            if (mEventsListeners.contains(listener)) {
                mEventsListeners.remove(listener);
            }
        }
    }

    /**
     * Clear the list of listener from this view
     * <p>
     * to be called when cleaning it
     */
    public void clearListeners() {
        synchronized (mEventsListeners) {
            mEventsListeners.clear();
        }
    }

    @MainThread
    public void setVideoFit() {
        mScaleType = SCALE_FIT;
        setSurfaceViewRenderer();
        requestLayout();
    }

    @MainThread
    public void setVideoFill() {
        mScaleType = SCALE_FILL;
        setSurfaceViewRenderer();
        requestLayout();
    }

    @MainThread
    public void setVideoBalanced() {
        mScaleType = SCALE_BALANCED;
        setSurfaceViewRenderer();
        requestLayout();
    }

    @MainThread
    public void setFlip(boolean flip) {
        showFlip = flip;

        updateFlip();
    }

    @MainThread
    public void setMirror(boolean mirror) {
        shouldMirror = mirror;
        if(null != mRenderer) {
            mRenderer.setMirror(mirror);
        }
    }

    private void updateFlip() {
        if (null != mFlip && null != mRenderer) {
            if (showFlip && isAttached() && mRenderer.isFirstFrameRendered()) {
                mFlip.setVisibility(View.VISIBLE);
            } else {
                mFlip.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Sets surface view renderer.
     */
    public void setSurfaceViewRenderer() {
        RendererCommon.ScalingType type = getScalingType();

        if (null != mRenderer) {
            boolean update = shouldMirror != mRenderer.isMirror();
            update |= !type.equals(mRenderer.getScalingType());

            if (update) {
                this.mRenderer.setEnableHardwareScaler(true);
                this.mRenderer.setScalingType(type);

                this.mRenderer.setMirror(shouldMirror);
            }
        }
    }

    @NonNull
    public RendererCommon.ScalingType getScalingType() {
        switch (mScaleType) {
            case SCALE_BALANCED:
                return RendererCommon.ScalingType.SCALE_ASPECT_BALANCED;
            case SCALE_FILL:
                return RendererCommon.ScalingType.SCALE_ASPECT_FILL;
            default:
                return RendererCommon.ScalingType.SCALE_ASPECT_FIT;
        }
    }

    private void setNextScalingType() {
        switch (mScaleType) {
            case SCALE_BALANCED:
                setVideoFill();
                break;
            case SCALE_FILL:
                setVideoFit();
                break;
            default:
                setVideoFill();
                break;
        }
    }

    /**
     * Is auto un attach boolean.
     *
     * @return the boolean
     */
    public boolean isAutoUnAttach() {
        return autoUnAttach;
    }

    /**
     * Sets the videoview's behavior when already attached. Should it auto attach or just
     * stay attached to the old stream.
     *
     * @param autoUnAttach the auto un attach
     */
    public void setAutoUnAttach(boolean autoUnAttach) {
        this.autoUnAttach = autoUnAttach;
    }

    /**
     * Releases the renderer.
     */
    public void release() {
        if (null != mRenderer) {
            this.mRenderer.release();
        }
    }

    /**
     * Try to reinit the view
     * <p>
     * When returning false, warns that something wrong happened. Common case is the fast the
     * renderer's init was already called without a proper release call
     *
     * @return if the view was reinit
     */
    public boolean reinit() {
        try {
            if (null != mRenderer && VoxeetSdk.getInstance().getMediaService().hasMedia()) {
                this.mRenderer.init(VoxeetSdk.getInstance().getMediaService().getEglContext(), this);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the renderer.
     *
     * @return the renderer
     */
    public VoxeetRenderer getRenderer() {
        return mRenderer;
    }

    private void setAttached(boolean attached) {
        mIsAttached = attached;
    }

    /**
     * Is attached boolean.
     *
     * @return the boolean
     */
    public boolean isAttached() {
        return mIsAttached;
    }

    /**
     * Attach the stream associated with the peerId to the videoView.
     *
     * @param peerId      the peer id
     * @param mediaStream the media stream
     */
    public void attach(String peerId, MediaStream mediaStream) {
        attach(peerId, mediaStream, false);
    }

    /**
     * Attach the stream associated with the peerId to the videoView.
     *
     * @param peerId      the peer id
     * @param mediaStream the media stream
     * @param force       force the update
     */
    public void attach(String peerId, MediaStream mediaStream, boolean force) {
        if (isAttached() && mPeerId != null && mPeerId.equals(peerId)) {// this user is already attached.
            if (force) {
                //nothing, go on
                if (mediaStream != mMediaStream || mediaStream == null) {
                    unAttach();
                } else {
                    return;
                }
            } else if (null != mMediaStream && null != mediaStream && !force) {
                return;
            } else if (null == mediaStream) {
                unAttach();
                return;
            }
        }

        if (autoUnAttach && isAttached())
            unAttach();

        if (!isAttached() && peerId != null && mediaStream != null && (mediaStream.videoTracks().size() > 0 || mediaStream.isScreenShare())) {
            setAttached(true);
            //reinit();

            mPeerId = peerId;

            mMediaStream = mediaStream;

            createRendererIfNeeded();

            if (null != mRenderer) {
                mRenderer.setVisibility(View.VISIBLE);
                boolean result = VoxeetSdk.getInstance().getMediaService().attachMediaStream(mediaStream, mRenderer);

                setVisibility(View.VISIBLE);

                forceLayout();
                mRenderer.forceLayout();
                requestLayout();
                mRenderer.requestLayout();
            }

            updateFlip();
        }
    }

    /**
     * Un attach the stream from the videoView.
     */
    public void unAttach() {
        createRendererIfNeeded();

        if (isAttached() && mPeerId != null) {
            if (mMediaStream != null) {
                VoxeetSdk.getInstance().getMediaService().unAttachMediaStream(mMediaStream, mRenderer);
            }

            mPeerId = null;
            mMediaStream = null;

            setAttached(false);
        }

        if (null != mRenderer) {
            mRenderer.setVisibility(View.GONE);
            updateFlip();
        }
    }

    /**
     * Gets the currently attached conference user's peer id.
     *
     * @return the peer id
     */
    public String getPeerId() {
        return mPeerId;
    }

    /**
     * Simple getter to get the current type of the screenshare :
     * screenshare or not
     *
     * @return true of false
     */
    public boolean isScreenShare() {
        return mMediaStream != null && mMediaStream.isScreenShare();
    }

    /**
     * Simple getter to get the current type of the screenshare :
     * video or not
     *
     * @return true of false
     */
    public boolean hasVideo() {
        return mMediaStream != null && mMediaStream.videoTracks().size() > 0;
    }

    @Override
    public void onFirstFrameRendered() {
        //make sure we are sending event on the main looper
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setSurfaceViewRenderer();

                updateFlip();

                synchronized (mEventsListeners) {
                    for (RendererCommon.RendererEvents events_listener : mEventsListeners) {
                        events_listener.onFirstFrameRendered();
                    }
                }
            }
        });
    }

    @Override
    public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
        synchronized (mEventsListeners) {
            for (RendererCommon.RendererEvents events_listener : mEventsListeners) {
                events_listener.onFrameResolutionChanged(videoWidth, videoHeight, rotation);
            }
        }
    }

    public MediaStreamType getCurrentMediaStreamType() {
        if (isScreenShare()) return MediaStreamType.SCREEN_SHARE;
        if (hasVideo()) return MediaStreamType.VIDEO;
        return MediaStreamType.NONE;
    }

    public enum MediaStreamType {
        NONE,
        VIDEO,
        SCREEN_SHARE
    }


    private void createRendererIfNeeded() {
        if (null == mRenderer && VoxeetSdk.getInstance().getMediaService().hasMedia()) {
            EglBaseMethods.Context context = VoxeetSdk.getInstance().getMediaService().getEglContext();

            //don't setup if no context
            if (null != context) {

                mInternalVideoView = LayoutInflater.from(getContext())
                        .inflate(R.layout.voxeet_internal_videoview, this, false);
                addView(mInternalVideoView);

                mCornerRadiusView = mInternalVideoView.findViewById(R.id.voxeet_videoview_cornerradius);
                mRenderer = mInternalVideoView.findViewById(R.id.voxeet_videoview_renderer);//new VoxeetRenderer(getContext());
                mFlip = mInternalVideoView.findViewById(R.id.voxeet_videoview_flip);

                mCornerRadiusView.setIsCircle(mIsCircle);
                mCornerRadiusView.setCornerRadius(mCornerRadius);

                updateFlip();
                mRenderer.init(context, this);

                mRenderer.setScalingType(getScalingType());
            }
        } else if (null != mRenderer) {
            LayoutParams param = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER);

            //post new layout params just to prevent issues
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != mRenderer && !layoutOnAttached) {
                        layoutOnAttached = true;
                        mRenderer.setLayoutParams(param);
                    }
                }
            });
        }
    }

    private void removeRender() {
        if (mRenderer != null) {
            try {
                mRenderer.release();
            } catch (Exception e) {

            }

            removeView(mRenderer);
            mRenderer = null;
        }
    }

    @Nullable
    public Bitmap getBitmap() {
        if (null == mRenderer) return null;
        return mRenderer.getBitmap();
    }

    @Nullable
    public Bitmap getBitmap(@NonNull Bitmap bitmap) {
        if (null == mRenderer) return null;
        return mRenderer.getBitmap(bitmap);
    }

    @Nullable
    public Bitmap getBitmap(int width, int height) {
        if (null == mRenderer) return null;
        return mRenderer.getBitmap(width, height);
    }

    public VideoView setIsCircle(boolean isCircle) {
        mIsCircle = isCircle;
        if (null != mCornerRadiusView) mCornerRadiusView.setIsCircle(isCircle);
        return this;
    }

    public VideoView setCornerRadius(float cornerRadius) {
        mCornerRadius = cornerRadius;
        if (null != mCornerRadiusView) mCornerRadiusView.setCornerRadius(mCornerRadius);
        return this;
    }

    public void activateSafeRefreshEglBase(boolean state) {
        this.enableRefreshEglBase = state;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EglBaseRefreshEvent event) {
        if (!enableRefreshEglBase || null == VoxeetSdk.getInstance() || null == mRenderer) {
            return;
        }

        if (VoxeetSdk.getInstance().getMediaService().hasMedia()) {
            //release the surface
            removeRender();
            String peerId = null;
            MediaStream stream = mMediaStream;

            //is was attached, save and release
            if (isAttached()) {
                peerId = getPeerId();
                stream = mMediaStream;
                unAttach();
            }

            //if was attached, reattach
            if (!TextUtils.isEmpty(peerId) && null != stream) {
                attach(peerId, stream, true);
            }
        }
    }
}