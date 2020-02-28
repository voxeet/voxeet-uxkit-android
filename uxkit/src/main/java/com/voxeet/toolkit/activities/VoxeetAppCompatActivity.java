package com.voxeet.toolkit.activities;

/**
 * VoxeetAppCompatActivity manages the call state
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
public class VoxeetAppCompatActivity extends com.voxeet.uxkit.activities.VoxeetAppCompatActivity {
}

