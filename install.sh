#!/bin/bash

rm -rf build */build

./gradlew :toolkit:licenseReleaseReport
cp toolkit/src/main/assets/open_source_licenses.json ./

./gradlew :toolkit:assembleRelease :toolkit:install
./gradlew :toolkit-exoplayer-support:assembleRelease :toolkit-exoplayer-support:install
./gradlew :toolkit-firebase:assembleRelease :toolkit-firebase:install
./gradlew :toolkit-incoming-call:assembleRelease :toolkit-incoming-call:install
./gradlew :toolkit-self-managed-call:assembleRelease :toolkit-self-managed-call:install
./gradlew :toolkit-system-service:assembleRelease :toolkit-system-service:install
./gradlew :toolkit-youtube:assembleRelease :toolkit-youtube:install
