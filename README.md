# Voxeet Android SDK

The SDK is a Java library allowing users to:

  - Create demo/normal conferences
  - Join conferences
  - Change sounds angle and direction for each conference user
  - Broadcast messages to other participants
  - Mute users/conferences
  - If you use External login like O365, LDAP, or custom login to retrieve contact details, it is now possible to also add your contact ID with the display name and the photo url avatar.
    This allows you to ask guest users to introduce themselves and provide their display name and for your authenticated users in your enterprise or for your clients the ID that can be retrieved from O365 (name, department, etc).

### Installing the Android SDK using Gradle

To install the SDK directly into your Android project using the Grade build system and an IDE like Android Studio, add the following entry: "compile 'com.voxeet.sdk:core:0.7.796'" to your build.gradle file as shown below:

```java
dependencies {
    compile 'com.voxeet.sdk:core:0.7.796'
}
```
### Recommended settings for API compatibility:

```java
apply plugin: 'com.android.application'

android {
    compileSdkVersion 21+
    defaultConfig {
        minSdkVersion 16
        targetSdkVersion 21+
    }
}
```
### Permissions

Add the following permissions to your Android Manifest file:

```java
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />    
    <uses-permission android:name="android.permission.CAMERA" />

    <uses-feature android:name="android.hardware.camera" />

  // Used to change audio routes
  <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

In order to target Android API level 23 or later, you will need to ensure that your application requests runtime permissions for microphone access. To do this, perform the following step:

Request microphone permissions from within your application code:

```java
ActivityCompat.requestPermissions(this,
    new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_REQUEST_CODE);
}
```

See the [Official Android Documentation] for more details.

### Consumer Key & Secret

Add your consumer key & secret to the xml string file of your application.

```java
 <string name="consumer_key">your consumer key</string>
 <string name="consumer_password">your consumer password</string>
```

## Available methods

### Initializing  

```java
// To be called from the application class
VoxeetSdk.sdkInitialize(Context context, String consumerKey, String consumerSecret, null);
```

### Creating a demo conference  

```java
VoxeetSdk.createSdkDemo();
```

### Creating a conference  

```java
VoxeetSdk.createSdkConference();
```

### Joining a conference  

```java
// Used to join someone's conference otherwise joining is automatic
VoxeetSdk.joinSdkConference(String conferenceId);
```

### Leaving a conference  

```java
VoxeetSdk.leaveSdkConference();
```

### Checking if a conference is live  

```java
VoxeetSdk.isSdkConferenceLive();
```

### Changing user position  

```java
// Change user position using an angle and a distance
// Values for x, y are between : x = [-1, 1] and y = [0, 1]
VoxeetSdk.changePeerPosition(String userId, double x, double y);
```

### Sending message in a conference

```java
// Send messages such as JSON commands...
VoxeetSdk.sendSdkBroadcast(String message);
```

### Knowing if a conference is live

```java
VoxeetSdk.isSdkConferenceLive();
```

### Getting current conference users

```java
VoxeetSdk.getConferenceUsers();
```

### Getting microphone state

```java
VoxeetSdk.isSdkMuted();
```

### Muting microphone

```java
VoxeetSdk.muteSdkConference(boolean mute);
```

### Muting user

```java
// Muting or unmmuting an user depending on the boolean value
VoxeetSdk.muteSdkUser(String userId, boolean mute);
```

### Checking if a user is muted

```java
VoxeetSdk.isSdkUserMuted(String userId);
```

### Getting available audio routes

```java
// Get available audio routes
VoxeetSdk.getSdkAvailableRoutes();
```

### Getting current audio route

```java
VoxeetSdk.currentSdkRoute();
```

### Setting audio route

```java
VoxeetSdk.setSdkoutputRoute(AudioRoute route);
```

### Registering the SDK

```java
// Susbcribe to the SDK events
VoxeetSdk.register(Context context);
```

### Unregistering the SDK

```java
// Unsusbcribe from the SDK events
VoxeetSdk.unregister(Context context);
```

### Registering the media stream listener

```java
// Get notified when streams are added/removed
VoxeetSdk.setMediaSdkStreamListener(Media.MediaStreamListener listener);
```

### Attaching the media stream

```java
// Attach the renderer to the media so we can get the rendering working
VoxeetSdk.attachMediaSdkStream(String peerId, MediaStream stream, VideoRenderer.Callbacks render);
```

### Unattaching the media stream

```java
// Unattach the renderers to the media to avoid leaks
VoxeetSdk.unAttachMediaSdkStream(String peerId, MediaStream stream);
```

### Setting up the Video capturer

```java
//Init the video capturer in the onCreate of your activity.
VideoCapturer capturer = VideoCapturerAndroid.create(CameraEnumerationAndroid.getNameOfFrontFacingDevice(), null);

// Use to retrieved the front camera stream
VoxeetSdk.setSdkVideoCapturer(VideoCapturer capturer);
```

## SDK Initialization

Initialize the SDK in the onCreate() method of your application class:

```java
@Override
public void onCreate() {
    super.onCreate();
    VoxeetSdk.sdkInitialize(this, consumerKey, consumerSecret);
}
```

## Activity Structure

In order to work properly, it is necessary to register and unregister the SDK respectively in the onCreate() and onDestroy() methods of your activity/fragment holding the conference.

```java
@Override

private Media.MediaStreamListener listener; // needs to be initialized

protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    VoxeetSdk.register(this);
    
    //Init the video capturer in the onCreate of your activity.
    VideoCapturer capturer = VideoCapturerAndroid.create(CameraEnumerationAndroid.getNameOfFrontFacingDevice(), null);

    // Use to retrieved the front camera stream
    VoxeetSdk.setSdkVideoCapturer(VideoCapturer capturer);
    
    VoxeetSdk.setMediaSdkStreamListener(listener);
}

@Override
protected void onDestroy() {
    super.onDestroy();
    
    VoxeetSdk.unregister(this);  
}   
```

## Media Stream Listener

The media stream listener is a set of callbacks used to let you know when video streams & screen share streams are added / removed. 

```java
private Media.MediaStreamListener mediaStreamListener = new Media.MediaStreamListener() {
                
        @Override
        public void onStreamAdded(String peer, MediaStream stream) {
          // use VoxeetSdk.attachMediaSdkStream(peer, stream, yourRenderer);
        }

        @Override
        public void onStreamRemoved(String peer) {
          // use VoxeetSdk.unAttachMediaSdkStream(peer, stream);
        }

        @Override
        public void onScreenStreamAdded(final String peer, final MediaStream stream) {
          // use VoxeetSdk.attachMediaSdkStream(peer, stream, yourRenderer);
        }

        @Override
        public void onScreenStreamRemoved(String peer) {
          // use VoxeetSdk.unAttachMediaSdkStream(peer, stream);
        }
    };
    
    NB: yourRenderer can be a custom object like the VideoView or the ScreenShareView included in the sample.
```

## ConferenceUser Model

ConferenceUser model now has an userInfo object where infos are stored such as the external user id, the url avatar and display name.

```java
public UserInfo getUserInfo();
```

## Events

The SDK will dispatch events to the suscribed classes such as activities and fragments holding the conferences. To get notified, the only necessary step is to add those methods below:


### Conference joined

```java
@Subscribe
public void onEvent(final ConferenceJoinedSuccessEvent event) {
    // Action to be called when joining successfully the conference
}
```
### Conference joined error

```java
@Subscribe
public void onEvent(final ConferenceJoinedError event) {
    // Action to be called if joining failed
}
```

### Conference left

```java
@Subscribe
public void onEvent(final ConferenceLeftSuccessEvent event) {
    // Action to be called when leaving successfully the conference
}
```

### Conference left error

```java
@Subscribe
public void onEvent(final ConferenceLeftError event) {
    // Action to be called when leaving the conference failed
}
```

### Participant added
```java
@Subscribe
public void onEvent(final ConferenceUserJoinedEvent event) {
    // Action to be called when a new participant joins the conference
}
```

### Participant status updated
```java
@Subscribe
public void onEvent(final ConferenceUserUpdateEvent event) {
    // Action to be called when a participant has left for example
}
```

### Message received
```java
@Subscribe
public void onEvent(MessageReceived event) {
    // Action to be called when a message is received
}
```

## Best practice regarding conferences

Only one instance of a conference is allowed to be live. Leaving the current conference before creating or joining another one is mandatory. Otherwise, a IllegalStateException will be thrown.

## Version
0.7.796

## Tech

The Voxeet Android SDK uses a number of open source projects to work properly:

* [Retrofit2] - A type-safe HTTP client for Android and Java.
* [GreenRobot/EventBus] - Android optimized event bus.
* [Jackson] - Jackson is a suite of data-processing tools for Java.
* [RxAndroid] - RxJava is a Java VM implementation of Reactive Extensions: a library for composing asynchronous and event-based programs by using observable sequences.

## Sample Application

A sample application is available on this [public repository][sample] on GitHub.

© Voxeet, 2016

   [Official Android Documentation]: <http://developer.android.com/training/permissions/requesting.html>
   [sample]: <https://github.com/voxeet/android-sdk-sample.git>
   [GreenRobot/EventBus]: <https://github.com/greenrobot/EventBus>
   [Jackson]: <https://github.com/FasterXML/jackson>
   [RxAndroid]: <https://github.com/ReactiveX/RxAndroid>
   [Retrofit2]: <http://square.github.io/retrofit/>