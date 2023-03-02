# Dolby.io Android UXKit

[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Fvoxeet%2Fvoxeet-uxkit-android%2Fbadge%3Fref%3Dmain&style=flat)](https://github.com/voxeet/voxeet-uxkit-android)

[![GitHub release](https://img.shields.io/github/release/voxeet/voxeet-uxkit-android.svg)](https://github.com/voxeet/voxeet-uxkit-android/releases/)


## Integrating the UXKit

### SDK 3.0 Repository

After accepting the [Dolby Software License Agreement](https://github.com/voxeet/voxeet-sdk-android/blob/main/LICENSE), update your app's build.gradle :

```gradle
dependencies {
  compile ('io.dolby:uxkit:3.+') { transitive = true }
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

Copyright (c) 2020 - 2022 Dolby.io

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
