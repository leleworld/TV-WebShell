@echo off
cd /d "%~dp0"
echo === Step 1: git init ===
git init
echo === Step 2: git add ===
git add -A
echo === Step 3: git status ===
git status
echo === Step 4: git commit ===
git commit -m "feat: Android TV WebView App - D-pad remote navigation + configurable URL"
echo === Step 5: branch main ===
git branch -M main
echo === Step 6: add remote ===
git remote add origin https://github.com/leleworld/TV-WebShell.git
echo === Step 7: push ===
git push -u origin main
echo === DONE ===
