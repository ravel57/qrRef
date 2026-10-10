#!/bin/bash

path=$(pwd)
cd ../../WebstormProjects/qrref-front || exit

#npm install
vite build

if [ -d "./dist/" ]; then
    cp -r "./dist/static/"* "$path/src/main/resources/static"
else
    echo "Copping error" >&2
    exit 1
fi