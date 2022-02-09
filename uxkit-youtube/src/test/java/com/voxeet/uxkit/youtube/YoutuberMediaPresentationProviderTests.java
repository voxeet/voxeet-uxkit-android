package com.voxeet.uxkit.youtube;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.voxeet.uxkit.youtube.tests.YoutubeConstants;

import org.junit.Test;

import java.util.UUID;

public class YoutuberMediaPresentationProviderTests {

    @Test
    public void testValidYoutubeURLs() {
        YoutubeMediaPresentationProvider instance = new YoutubeMediaPresentationProvider();

        for (String url : YoutubeConstants.URLS) {
            assertTrue("URL needs to be valid " + url, instance.isUrlCompatible(url));
        }
    }

    @Test
    public void testInvalidYoutubeURLs() {
        YoutubeMediaPresentationProvider instance = new YoutubeMediaPresentationProvider();

        for (String url : YoutubeConstants.URLS) {
            url = url.replace("youtu", UUID.randomUUID().toString().replaceAll("-", ""));
            assertFalse("URL needs to be invalid " + url, instance.isUrlCompatible(url));
        }
    }
}