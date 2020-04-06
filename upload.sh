#!/bin/bash

rm -rf build */build

./gradlew -PCICD_FROM_BUILD_ID="$1" -PCICD_BUILD_ID="$1" :cleanAll :licenseAll

sleep 5

(bash license_check.sh) || exit 1

./gradlew -PCICD_FROM_BUILD_ID="$1" -PCICD_BUILD_ID="$1" :cleanAll :assembleAll :uploadAll
