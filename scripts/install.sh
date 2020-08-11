echo "installing with $1 suffix"

./gradlew -PCICD_FROM_BUILD_ID="$1" -PCICD_BUILD_ID="$1" :installAll
