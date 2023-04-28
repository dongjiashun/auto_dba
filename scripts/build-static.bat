@echo off
set STATIC_PATH=src/main/resources/public/static

echo [INFO] static resources path: %STATIC_PATH%
echo [INFO] build static resources starting ...

cd %STATIC_PATH%
gulp default & echo [INFO] build static resources success