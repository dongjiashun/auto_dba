#!/usr/bin/env bash
STATIC_PATH=src/main/resources/public/static

echo [INFO] static resources path: ${STATIC_PATH}
echo [INFO] build static resources starting ...

cd ${STATIC_PATH}
npm install  --registry=https://registry.npm.taobao.org
gulp default
echo [INFO] build static resources success