#!/bin/bash
cd ../../WebstormProjects/ITdesk-Front/
yarn run install
yarn build
pwd
mkdir ../../IdeaProjects/qrRef/src/main/webapp/js/
mkdir ../../IdeaProjects/qrRef/src/main/webapp/css/
cp ./dist/js/app*.js           ../../IdeaProjects/ITdesk/src/main/webapp/js/app.js
cp ./dist/js/chunk-vendors*.js ../../IdeaProjects/ITdesk/src/main/webapp/js/chunk-vendors.js
cp ./dist/css/app*.css         ../../IdeaProjects/ITdesk/src/main/webapp/css/app.css
