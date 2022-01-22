package com.voxeet.uxkit.common.permissions;

import com.voxeet.audio.utils.__Call;

import java.util.List;
import java.util.Map;

public interface IRequestPermissions {

    void requestPermissions(List<String> permissions, __Call<Map<String, Boolean>> callback);

}
