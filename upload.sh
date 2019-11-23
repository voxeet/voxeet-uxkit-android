#!/bin/bash

rm -rf build */build

./gradlew :toolkit:assembleRelease :toolkit:bintrayUpload
./gradlew :toolkit-exoplayer-support:assembleRelease :toolkit-exoplayer-support:bintrayUpload
./gradlew :toolkit-firebase:assembleRelease :toolkit-firebase:bintrayUpload
./gradlew :toolkit-incoming-call:assembleRelease :toolkit-incoming-call:bintrayUpload
./gradlew :toolkit-self-managed-call:assembleRelease :toolkit-self-managed-call:bintrayUpload
./gradlew :toolkit-system-service:assembleRelease :toolkit-system-service:bintrayUpload
./gradlew :toolkit-youtube:assembleRelease :toolkit-youtube:bintrayUpload
