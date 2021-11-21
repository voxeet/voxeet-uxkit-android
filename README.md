# Voxeet Android UXKit

## Integrating the UXKit

### SDK 3.0 Repository

After accepting the [Dolby Software License Agreement](https://github.com/voxeet/voxeet-sdk-android/blob/main/LICENSE), update the main build.gradle file with the following repository :

```
allprojects {
    repositories {
        maven { url "https://android-sdk.voxeet.com/release" }
        ...
    }
}
```

**Note for Beta releases of the SDK: ** When using a beta release, add as well `"https://android-sdk.voxeet.com/beta"` after the release line. With this, you will be able to use the beta versions of the UXKit which are using beta versions of the SDK.

### Available stable versions

The list will only feature new most recents versions

#### v3.3.0

- Use SDK 3.3.0
- Set build tools/compile and target to 31.0.0
- Fix texts in the translations

#### v3.2.3

- Use SDK 3.2.3
- Fix an issue where the invitation notification could be dismissed without active interactions

#### v3.2.2

- Use SDK 3.2.2

#### v3.2.1

- The SDK 3.2.0 add a missing dependency block ; this fix adds the expected SoLoader dependency

#### v3.2.0

- Official compatibiltiy with SDK 3.2+
- Removed deprecated toolkit classes (replaced by uxkit classes)
- Fix issue where the mute button could be disabled even with microphone permission granted

#### v3.1.4

Compatibility for SDK 3.2+ where the documentation annotations changed

#### v3.1.3

use SDK version 3.1.4 which updates device database

#### v3.1.2

use SDK version 3.1.2

#### v3.0.4

use SDK version v3.0.3

- Add enhanced action buttons
- add maximize and minimize methods

#### v3.0.3

use SDK version v3.0.3

- VoxeetSDK.instance() is always valid
- VoxeetSDK.<services()> are always instantiated
- VoxeetSDK.initialize(...) still needs to be called to enable network-related calls
- The speaker action refreshes the devices on top of displaying those media devices
- Media output shows properly the list's text color
- Add static IncomingNotification.Configuration to override some fields

### Available beta versions

#### v3.0.3-BETA2

use beta version of the SDK, v3.0.3-BETA2012071606

- VoxeetSDK.instance() is always valid
- VoxeetSDK.<services()> are always instantiated
- VoxeetSDK.initialize(...) still needs to be called to enable network-related calls
- The speaker action refreshes the devices on top of displaying those media devices
- Media output shows properly the list's text color
- Add static IncomingNotification.Configuration to override some fields

### App's dependencies

Then update the app's dependencies with :

```gradle
dependencies {
  compile ('com.voxeet.sdk:uxkit:3.+') { transitive = true }
}
```

See the main process and our various articles on our [website](https://dolby.io/developers/interactivity-apis/client-ux-kit/uxkit-voxeet-java)

## License

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://choosealicense.com/licenses/mit/)

MIT License

Copyright (c) 2020 Voxeet

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
