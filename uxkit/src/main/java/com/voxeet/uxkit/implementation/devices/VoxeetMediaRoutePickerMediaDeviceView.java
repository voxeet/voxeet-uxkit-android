package com.voxeet.uxkit.implementation.devices;

import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.voxeet.VoxeetSDK;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.promise.solve.ThenVoid;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.R;

import java.util.List;

public class VoxeetMediaRoutePickerMediaDeviceView extends LinearLayout {
    private ImageView media_icon;
    private TextView media_device_name;

    @Nullable
    private MediaDevice device;

    @Nullable
    private OnClickListener onClickListener;

    public VoxeetMediaRoutePickerMediaDeviceView(Context context) {
        super(context);

        init();
    }

    public VoxeetMediaRoutePickerMediaDeviceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public VoxeetMediaRoutePickerMediaDeviceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VoxeetMediaRoutePickerMediaDeviceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.voxeet_devices_picker_device_view, this, false);

        media_device_name = view.findViewById(R.id.media_device_name);
        media_icon = view.findViewById(R.id.media_icon);

        view.setOnClickListener(v -> {
            if (null != device) {
                if (null != onClickListener) onClickListener.onClick(view);

                VoxeetSDK.audio().connect(device).then(aBoolean -> {
                }).error(Throwable::printStackTrace);
            }
        });

        addView(view);
    }

    public void setDevice(@NonNull MediaDevice device, OnClickListener onClickListener) {
        this.device = device;
        this.onClickListener = onClickListener;

        media_icon.setImageResource(getIcon());
        media_device_name.setText(getDeviceName());
    }

    public void remote() {
        this.device = null;
    }

    private String getDeviceName() {
        switch (Opt.of(device).then(MediaDevice::deviceType).or(DeviceType.INTERNAL_SPEAKER)) {
            case BLUETOOTH:
                return Opt.of(device).then(MediaDevice::name).or(getContext().getString(R.string.bluetooth));
            case WIRED_HEADSET:
                return getContext().getString(R.string.wired_headset);
            case EXTERNAL_SPEAKER:
                return getContext().getString(R.string.external_speaker);
            case NORMAL_MEDIA:
            case USB:
            case INTERNAL_SPEAKER:
            default:
                return getContext().getString(R.string.internal_speaker);
        }
    }

    @DrawableRes
    private int getIcon() {
        switch (Opt.of(device).then(MediaDevice::deviceType).or(DeviceType.INTERNAL_SPEAKER)) {
            case BLUETOOTH:
                return R.drawable.media_bluetooth_audio;
            case WIRED_HEADSET:
                return R.drawable.media_headset_mic;
            case EXTERNAL_SPEAKER:
                return R.drawable.media_speaker_phone;
            case NORMAL_MEDIA:
            case USB:
            case INTERNAL_SPEAKER:
            default:
                return R.drawable.media_smartphone;
        }
    }
}
