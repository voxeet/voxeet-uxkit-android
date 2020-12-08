package com.voxeet.uxkit.implementation.devices;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.voxeet.VoxeetSDK;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.sdk.utils.Filter;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.R;

import java.util.ArrayList;
import java.util.List;

public class VoxeetMediaRoutePickerView extends LinearLayout implements IMediaDeviceControlListener {

    private View devices_picker_background;
    private LinearLayout devices_picker_list;

    private List<MediaDevice> know_devices = new ArrayList<>();
    private boolean attached = false;

    public VoxeetMediaRoutePickerView(Context context) {
        super(context);

        init();
    }

    public VoxeetMediaRoutePickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public VoxeetMediaRoutePickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VoxeetMediaRoutePickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.voxeet_devices_picker_view, this, false);

        devices_picker_background = view.findViewById(R.id.devices_picker_background);
        devices_picker_list = view.findViewById(R.id.devices_picker_list);

        devices_picker_background.setOnClickListener(v -> setVisibility(View.GONE));

        addView(view);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        VoxeetSDK.audio().registerUpdateDevices(this::onDevice);
        attached = true;

        VoxeetSDK.audio().enumerateDevices().then(this::onDevice).error(Throwable::printStackTrace);
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        VoxeetSDK.audio().unregisterUpdateDevices(this::onDevice);

        super.onDetachedFromWindow();
    }

    private void onDevice(List<MediaDevice> mediaDevices) {
        if (!attached) return;

        this.know_devices = Filter.filter(Opt.of(mediaDevices).or(new ArrayList<>()), mediaDevice -> null != mediaDevice && mediaDevice.platformConnectionState() == ConnectionState.CONNECTED && !DeviceType.NORMAL_MEDIA.equals(mediaDevice.deviceType()));
        this.refresh();
    }

    private void refresh() {
        int array_length = know_devices.size();
        int in_length = devices_picker_list.getChildCount();

        while (in_length > array_length) {
            devices_picker_list.removeViewAt(in_length - 1);

            in_length--;
        }

        while (in_length < array_length) {
            LinearLayout new_view = new VoxeetMediaRoutePickerMediaDeviceView(getContext());

            devices_picker_list.addView(new_view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            in_length++;
        }

        int i = 0;
        for (MediaDevice device : know_devices) {
            View child = devices_picker_list.getChildAt(i);

            if (child instanceof VoxeetMediaRoutePickerMediaDeviceView) {
                VoxeetMediaRoutePickerMediaDeviceView view = (VoxeetMediaRoutePickerMediaDeviceView) child;
                view.setDevice(device, listener);
            }
            i++;
        }
    }

    private OnClickListener listener = v -> setVisibility(View.GONE);

    @Override
    public void onMediaRouteButtonInteraction() {
        setVisibility(View.VISIBLE);
    }
}
