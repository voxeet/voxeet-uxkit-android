# Voxeet UXKit for Android

## Integrating the UXKit

### Maven Repository

After accepting the [Dolby Software License Agreement](https://github.com/voxeet/voxeet-sdk-android/blob/main/LICENSE), update the main `build.gradle` file with the following repository:

```gradle
allprojects {
    repositories {
        maven { url "https://android-sdk.voxeet.com/release" }
        ...
    }
}
```

### App's dependencies

Then update the app's dependencies with:

```gradle
dependencies {
  compile ('com.voxeet.sdk:uxkit:3.+') { transitive = true }
}
```

See the main process and the various articles on our [website](https://dolby.io/developers/interactivity-apis/client-ux-kit/uxkit-voxeet-java)
