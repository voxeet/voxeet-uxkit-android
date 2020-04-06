#!/bin/bash

if [[ $(git ls-files -mo | grep src/main/assets | grep .json | wc -l) -gt 0 ]]; then
  echo "There are modifications in the licenses"

  exit 1
else
  echo "No modification, the CI can continue building"
  exit 0
fi