package com.voxeet.uxkit.common.permissions;

import androidx.annotation.NonNull;

import com.voxeet.audio.utils.__Call;

import java.util.List;
import java.util.Map;

public interface IRequestPermissions {

    void requestPermissions(@NonNull List<String> permissions, @NonNull __Call<Map<String, Boolean>> callback);

    boolean isPermissionNeverAskAgain(@NonNull String permission);

    boolean shouldShowRequestPermissionRationale(@NonNull String permission);

}
