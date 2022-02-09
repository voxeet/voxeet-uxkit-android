package com.voxeet.uxkit.youtube;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.voxeet.uxkit.youtube.tests.YoutubeConstants;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.UUID;

@Config(manifest= Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class InternalYoutubeHelperTests {

    @Test
    public void testGetVideoId() throws Throwable {
        for (String url : YoutubeConstants.URLS) {
            String computed = (String) InternalYoutubeHelper.getVideoId(url);
            Assert.assertEquals("values are different for url " + url, YoutubeConstants.VIDEO_ID, computed);
        }
    }

    @Test
    public void testValidYoutubeURLs() {
        for (String url : YoutubeConstants.URLS) {
            assertTrue("URL needs to be valid " + url, InternalYoutubeHelper.isUrlCompatible(url));
        }
    }

    @Test
    public void testInvalidYoutubeURLs() {
        for (String url : YoutubeConstants.URLS) {
            url = url.replace("youtu", UUID.randomUUID().toString().replaceAll("-", ""));
            assertFalse("URL needs to be invalid " + url, InternalYoutubeHelper.isUrlCompatible(url));
        }
    }
}
