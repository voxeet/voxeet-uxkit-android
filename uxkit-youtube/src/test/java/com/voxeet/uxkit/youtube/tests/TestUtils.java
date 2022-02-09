package com.voxeet.uxkit.youtube.tests;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

public class TestUtils {

    public static Method getMethod(@NonNull Class<?> klass, @NonNull String name, @NonNull Class<?> ...params) {
        try {
            Method method = klass.getDeclaredMethod(name, params);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}
