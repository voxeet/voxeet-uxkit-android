# Voxeet Android UXKit

## Integrating the UXKit

### SDK 3.0 Repository

After accepting the [Dolby Software License Agreement](https://github.com/voxeet/voxeet-sdk-android/blob/main/LICENSE), update the main build.gradle file with the following repository :

```
allprojects {
    repositories {
        ...
        maven {
            url "https://android-sdk.voxeet.com/release"
            content {
                includeGroup "com.voxeet.sdk"
                //any other possible group with a new includeGroup line but as far as I remember I only created and set one group
            }
        }

        maven {
            url "https://android-sdk.voxeet.com/beta"
            content {
                includeGroup "com.voxeet.sdk" //same as above
            }
        }
    }
}
```

In your app's build.gradle :

```gradle
dependencies {
  compile ('com.voxeet.sdk:uxkit:3.+') { transitive = true }
}
```

**Note for Beta releases of the SDK: ** When using a beta release, add as well `"https://android-sdk.voxeet.com/beta"` after the release line. With this, you will be able to use the beta versions of the UXKit which are using beta versions of the SDK.

### Available stable versions

[Refer to the releases](https://github.com/voxeet/voxeet-uxkit-android/releases)

### Integration

See the main process and our various articles on our [website](https://dolby.io/developers/interactivity-apis/client-ux-kit/uxkit-voxeet-java)

## License

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://choosealicense.com/licenses/mit/)

MIT License

Copyright (c) 2020 - 2022 Voxeet

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
