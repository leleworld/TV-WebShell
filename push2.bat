@echo off
cd /d "%~dp0"
echo === git add ===
git add -A
echo === git status ===
git status
echo === git commit ===
git commit -m "ci: add GitHub Actions build workflow + .gitignore"
echo === git push ===
git push
echo === DONE ===
