package com.voxeet.uxkit.youtube;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.voxeet.uxkit.youtube.tests.TestUtils;
import com.voxeet.uxkit.youtube.tests.YoutubeConstants;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class YoutubeMediaPresentationViewTests {

    @Test
    public void testGetVideoId() throws InvocationTargetException, IllegalAccessException {
        Context context = ApplicationProvider.getApplicationContext();
        YoutubeMediaPresentationView instance = new YoutubeMediaPresentationView(context);
        Method getVideoId = TestUtils.getMethod(instance.getClass(), "getVideoId", String.class);

        Assert.assertNotNull("getVideoId doesn't exist", getVideoId);

        for (String url : YoutubeConstants.URLS) {
            String computed = (String) getVideoId.invoke(instance, url);
            Assert.assertEquals("values are different for url " + url, YoutubeConstants.VIDEO_ID, computed);
        }
    }
}
