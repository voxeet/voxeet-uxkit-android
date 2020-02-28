package com.voxeet.toolkit.youtube.activities;

/**
 * VoxeetYoutubeAppCompatActivity manages the call state
 * This class is to be used in the context of any requirement to be able to play Youtube URL
 * <p>
 * In the current merged state, this class is not used
 * <p>
 * However, it is extremely easy to use this class now :
 * - manages automatically the bundles to join conferences when "resumed"
 * - automatically registers its subclasses's extra info to propagate to "recreated" instances
 * <p>
 * Few things to consider :
 * - singleTop / singleInstance
 */
@Deprecated
public class VoxeetYoutubeAppCompatActivity extends com.voxeet.uxkit.youtube.activities.VoxeetYoutubeAppCompatActivity {

}
