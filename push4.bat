@echo off
cd /d "%~dp0"
git add -A
git commit -m "fix: replace adaptive-icon with simple shape drawable for launcher icon"
git push
