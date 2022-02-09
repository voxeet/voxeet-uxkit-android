#!/bin/bash

rm -rf build */build

let failed=0

# test with only youtube to speed up UT for now
modules=`./gradlew tasks --all | grep ":assemble" | cut -d: -f1 | sort -u | grep youtube`
for x in $modules
do
  echo "module " $x
  ./gradlew :$x:createDebugCoverageReport || (echo "createDebugCoverageReport FAILED"; let failed=1)
  ./gradlew :$x:testDebugUnitTest || (echo "testDebugUnitTest FAILED"; let failed=1)
  ./gradlew :$x:jacocoTestReport || (echo "jacocoTestReport FAILED"; let failed=1)
done

if [ $failed -eq 1 ]; then
  echo "failed to run the tests, at least 1 failed"
  exit 1
fi

echo "success"