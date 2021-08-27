# Voxeet Android UXKit

The Android UXKit is the standard SDK extension which helps to integrate additional features enhancing the UX without UI burden.

## Maven Repository

After accepting the [Dolby Software License Agreement](https://github.com/voxeet/voxeet-sdk-android/blob/main/LICENSE), update the main `build.gradle` file with the following repository :

```gradle
allprojects {
    repositories {
        maven { url "https://android-sdk.voxeet.com/release" }
        ...
    }
}
```

**Note for Beta releases of the SDK: ** When using a beta release, add as well `"https://android-sdk.voxeet.com/beta"` after the release line. With this, you will be able to use the beta versions of the UXKit which are using beta versions of the SDK.

## Dependencies

The integration of the Android UXKit into an application is as simple as the following build.gradle.

```gradle
dependencies {
    ...
    api ("com.voxeet.sdk:toolkit:${rootProject.ext.voxeetSdkToolkitVersion}") {
        transitive = true
    }
}
```

It is recommended to use [_Gradle tips and recipes_](https://developer.android.com/studio/build/gradle-tips#configure-project-wide-properties). The recommended properties are in the example below.

```gradle
ext {
    buildToolsVersion = "28.0.3"
    minSdkVersion = 14
    compileSdkVersion = 28
    targetSdkVersion = 28
    supportLibVersion = "28.0.0"
    voxeetSdkVersion = "2.0.72.1"
    voxeetSdkToolkitVersion = "2.0.72.2"
}
```

## Conflict management

_Note: adding support libraries with versions 28.0.+ can lead to issues with outdated libraries._

The UXKit requires the following:

- com.android.support:support-compat:28.0.0
- com.android.support:appcompat-v7:28.0.0
- com.android.support:recyclerview-v7:28.0.0
- com.squareup.picasso:picasso:2.71828
- org.apache.commons:commons-collections4:4.0
- com.android.support:multidex:1.0.3
- junit:junit:4.12

In a case of issues with the UI (for example the application uses the support libraries version _27_), you can set three supported libraries as dependent on your application and UXKit, as in the example below.

```gradle
dependencies {
    ...
    api ("com.voxeet.sdk:toolkit:${rootProject.ext.voxeetSdkToolkitVersion}") {
        transitive = true
    }

    // forcefully set the toolkit's mandatory dependencies
    api "com.android.support:support-compat:${rootProject.ext. supportLibVersion}"
    api "com.android.support:appcompat-v7:${rootProject.ext. supportLibVersion}"
    api "com.android.support:recyclerview-v7:${rootProject.ext. supportLibVersion}"
}
```

## Initialization

Create the UXKit right after the `VoxeetSDK.initialize` call, as in the following example.

```java
public class SomeApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();


        VoxeetSDK.initialize(
                getString(R.string.api_key),
                getString(R.string.api_secret));

        VoxeetToolkit.initialize(this, VoxeetSDK.getInstance().getEventBus());
    }
}
```

## Usage of extended capabilities

Set proper overlay settings by changing the value of the `enableOverlay`. Set a value to true to enable it, false to disable.
Set proper screen sharing settings by changing the value of the `setScreenShareEnabled`. Set a value to true to enable it, false to disable.
An exemplary setting is presented in the example below.

```java
VoxeetToolkit.initialize(this, VoxeetSDK.getInstance().getEventBus());
VoxeetToolkit.getInstance().enableOverlay(false);
VoxeetToolkit.getInstance().getConferenceToolkit().setScreenShareEnabled(true);
```

## Available stable versions

The list will only feature new most recents versions

### v3.2.1

- The SDK 3.2.0 add a missing dependency block ; this fix adds the expected SoLoader dependency

### v3.2.0

- Official compatibility with SDK 3.2+
- Removed deprecated toolkit classes (replaced by uxkit classes)
- Fix issue where the mute button could be disabled even with microphone permission granted

### v3.1.4

Compatibility for SDK 3.2+ where the documentation annotations changed

### v3.1.3

use SDK version 3.1.4 which updates device database

### v3.1.2

use SDK version 3.1.2

### v3.0.4

use SDK version v3.0.3

- Add enhanced action buttons
- add maximize and minimize methods

### v3.0.3

use SDK version v3.0.3

- `VoxeetSDK.instance()` is always valid
- `VoxeetSDK.<services()>` are always instantiated
- `VoxeetSDK.initialize(...)` still needs to be called to enable network-related calls
- The speaker action refreshes the devices on top of displaying those media devices
- Media output shows properly the list's text color
- Add static `IncomingNotification.Configuration` to override some fields

## Beta versions

### v3.0.3-BETA2

use beta version of the SDK, v3.0.3-BETA2012071606

- `VoxeetSDK.instance()` is always valid
- `VoxeetSDK.<services()>` are always instantiated
- `VoxeetSDK.initialize(...)` still needs to be called to enable network-related calls
- The speaker action refreshes the devices on top of displaying those media devices
- Media output shows properly the list's text color
- Add static `IncomingNotification.Configuration` to override some fields
