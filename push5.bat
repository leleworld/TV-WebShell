@echo off
cd /d "%~dp0"
git add -A
git commit -m "fix: remove deprecated setAppCachePath + hardcode version string"
git push
