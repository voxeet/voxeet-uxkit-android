#TODOs


When trying to un/-mute a conference, no effect (sound still played) Logcat show the given lines :
```
11-20 16:04:09.720 26222 26222 I VoxeetSDK :: VoxeetSdk: Conference unmuted
11-20 16:04:09.720 26222 26222 E libjingle: (webrtcvoiceengine.cc:993): webrtc: Unable to Find Master Peer
```
```
11-20 16:04:09.871 26222 26222 I VoxeetSDK :: VoxeetSdk: Conference muted
11-20 16:04:09.871 26222 26222 E libjingle: (webrtcvoiceengine.cc:993): webrtc: Unable to Find Master Peer
```