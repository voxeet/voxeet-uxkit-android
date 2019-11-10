package com.voxeet.toolkit.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.MediaStreamType;
import com.voxeet.sdk.VoxeetSdk;
import com.voxeet.sdk.media.camera.CameraContext;
import com.voxeet.sdk.models.User;
import com.voxeet.sdk.views.VideoView;
import com.voxeet.toolkit.implementation.VoxeetConferenceView;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;

public class ConferenceViewRendererControl {

    private static final String TAG = ConferenceViewRendererControl.class.getSimpleName();
    @NonNull
    private WeakReference<VoxeetConferenceView> parent;

    @NonNull
    private WeakReference<VideoView> selfVideoView;

    @NonNull
    private WeakReference<VideoView> otherVideoView;

    private View.OnClickListener selectedFromSelf = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            VideoView view = otherVideoView.get();

            if (null != view) {
                switchCamera();
            }
        }
    };
    private boolean clickEnabled;


    private ConferenceViewRendererControl() {
        parent = new WeakReference<>(null);
        selfVideoView = new WeakReference<>(null);
        otherVideoView = new WeakReference<>(null);
    }

    public ConferenceViewRendererControl(@NonNull VoxeetConferenceView parent,
                                         @NonNull VideoView selfVideoView,
                                         @NonNull VideoView otherVideoView) {
        this.parent = new WeakReference<>(parent);
        this.selfVideoView = new WeakReference<>(selfVideoView);
        this.otherVideoView = new WeakReference<>(otherVideoView);
    }

    @Nullable
    private VoxeetConferenceView getParent() {
        return parent.get();
    }

    @NonNull
    private VideoView getSelfVideoView() {
        return selfVideoView.get();
    }

    @NonNull
    private VideoView getOtherVideoView() {
        return otherVideoView.get();
    }

    public void attachStreamToSelected(@NonNull String peerId,
                                       @NonNull MediaStream stream) {
        VideoView selectedView = getOtherVideoView();

        String ownUserId = VoxeetSdk.session().getUserId();
        if (null == ownUserId) ownUserId = "";

        if (ownUserId.equals(peerId)) {
            attachStreamToSelf(stream);
        } else if (selectedView.isAttached() && ownUserId.equals(selectedView.getPeerId())) {
            attachStreamToSelf(stream);

            if (!ownUserId.equals(peerId)) {
                selectedView.setOnClickListener(null);
                selectedView.setClickable(false);
                selectedView.setMirror(false);
                selectedView.unAttach();
                selectedView.setVisibility(View.VISIBLE);
                selectedView.attach(peerId, stream);
            } else {
                selectedView.unAttach();
                selectedView.setVisibility(View.GONE);
            }
        } else {
            selectedView.setOnClickListener(null);
            selectedView.setClickable(false);
            selectedView.setMirror(false);
            selectedView.unAttach();
            selectedView.setVisibility(View.VISIBLE);
            selectedView.attach(peerId, stream);
        }
    }

    public void detachStreamFromSelected() {
        VideoView selectedView = getOtherVideoView();
        VideoView selfVideoView = getSelfVideoView();

        String ownUserId = VoxeetSdk.session().getUserId();
        User user = VoxeetSdk.conference().findUserById(ownUserId);

        selectedView.unAttach();

        MediaStream stream = null != user ? user.streamsHandler().getFirst(MediaStreamType.Camera) : null;

        if (!ToolkitUtils.hasParticipants() && null != stream && stream.videoTracks().size() > 0) {
            attachStreamToSelf(stream);
        } else {
            selectedView.setVisibility(View.GONE);
            getParent().showSpeakerView();
        }
    }

    public void attachStreamToSelf(@android.support.annotation.Nullable MediaStream stream) {
        VideoView selectedView = getOtherVideoView();
        VideoView selfView = getSelfVideoView();

        CameraContext provider = VoxeetSdk.mediaDevice().getCameraContext();

        if (null != stream && stream.videoTracks().size() > 0) {
            String ownUserId = VoxeetSdk.session().getUserId();
            if (!ToolkitUtils.hasParticipants()) {
                selfView.unAttach();
                selfView.setVisibility(View.GONE);

                selectedView.setVideoFill();
                selectedView.setMirror(provider.isDefaultFrontFacing());
                selectedView.setVisibility(View.VISIBLE);
                selectedView.attach(ownUserId, stream);
                setClickForSelectedIfNecessary();
                getParent().hideSpeakerView();
            } else {
                if (selectedView.isAttached() && ownUserId.equals(selectedView.getPeerId())) {
                    selectedView.setOnClickListener(null);
                    selectedView.setClickable(false);
                    selectedView.unAttach();
                    selectedView.setVisibility(View.GONE);
                    getParent().showSpeakerView();
                }
                selfView.setMirror(provider.isDefaultFrontFacing());
                selfView.attach(VoxeetSdk.session().getUserId(), stream);
                selfView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setClickForSelectedIfNecessary() {
        VideoView selectedView = getOtherVideoView();
        String ownUserId = VoxeetSdk.session().getUserId();
        if (null == ownUserId) ownUserId = "";

        if (clickEnabled && ownUserId.equals(selectedView.getPeerId())) {
            selectedView.setOnClickListener(selectedFromSelf);
        } else {
            selectedView.setOnClickListener(null);
            selectedView.setClickable(false);
        }
    }

    public void detachStreamFromSelf() {
        VideoView selectedView = getOtherVideoView();
        VideoView selfView = getSelfVideoView();

        if (selfView.isAttached()) {
            selfView.unAttach();
            selfView.setVisibility(View.GONE);
        }

        String ownUserId = VoxeetSdk.session().getUserId();
        if (selectedView.isAttached() && ownUserId.equals(selectedView.getPeerId())) {
            selectedView.setOnClickListener(null);
            selectedView.setClickable(false);
            selectedView.unAttach();
            selectedView.setVisibility(View.GONE);
            getParent().showSpeakerView();
        }
    }

    public void switchCamera() {
        String ownUserId = VoxeetSdk.session().getUserId();
        if (null == ownUserId) ownUserId = "";

        VideoView self = selfVideoView.get();
        VideoView other = otherVideoView.get();

        VideoView finalVideoView = null;

        if (null != self && ownUserId.equals(self.getPeerId())) {
            finalVideoView = self;
        } else if (null != other && ownUserId.equals(other.getPeerId())) {
            finalVideoView = other;
        }

        if (null != finalVideoView && finalVideoView == self) { //force for now the effect only in the SELF VIDEO VIEW
            //until a proper animation is found

            //switchCamera should not trigger crash since it is only possible
            //to click when already capturing and ... rendering the camera

            ObjectAnimator animationFlip = ObjectAnimator.ofFloat(finalVideoView, View.ROTATION_Y, -180f, -360f);
            animationFlip.setInterpolator(new AccelerateDecelerateInterpolator());

            ObjectAnimator animationGrow = ObjectAnimator.ofFloat(finalVideoView, View.SCALE_Y, 1f, 1.15f, 1f);
            animationGrow.setInterpolator(new AccelerateDecelerateInterpolator());

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(450).setStartDelay(450);
            animatorSet.playTogether(animationFlip, animationGrow);
            animatorSet.start();
        }

        VoxeetSdk.mediaDevice().switchCamera()
                .then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@android.support.annotation.Nullable Boolean result, @NonNull Solver<Object> solver) {
                        CameraContext provider = VoxeetSdk.mediaDevice().getCameraContext();
                        updateMirror(provider.isDefaultFrontFacing());
                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        error.printStackTrace();
                    }
                });
    }

    public void updateMirror(boolean isFrontCamera) {
        String ownUserId = VoxeetSdk.session().getUserId();
        VideoView selectedView = getOtherVideoView();
        VideoView selfView = getSelfVideoView();

        if (null != ownUserId) {
            if (null != selectedView && ownUserId.equals(selectedView.getPeerId())) {
                //only mirror the view in case of camera stream
                MediaStreamType type = selectedView.current();
                if (MediaStreamType.Camera.equals(type)) {
                    selectedView.setMirror(isFrontCamera);
                }
            } else if (null != selfView && ownUserId.equals(selfView.getPeerId())) {
                selfView.setMirror(isFrontCamera);
            }
        }
    }

    public void enableClick(boolean state) {
        clickEnabled = state;

        setClickForSelectedIfNecessary();
    }
}
