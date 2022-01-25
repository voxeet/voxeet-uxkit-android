package com.voxeet.uxkit.common.activity.bundle;

import android.os.Bundle;

import androidx.annotation.Nullable;

public interface IExtraBundleFillerListener {

    @Nullable
    Bundle createExtraBundle();
}
