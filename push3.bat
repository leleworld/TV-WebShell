@echo off
cd /d "%~dp0"
git add -A
git commit -m "fix: correct settings.gradle syntax + pin Gradle 8.0 in CI"
git push
