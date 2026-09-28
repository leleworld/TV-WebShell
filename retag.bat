@echo off
cd /d "%~dp0"

echo === 提交权限修复 ===
git add -A
git commit -m "fix: add contents write permission for release creation"

echo === 删除旧 tag ===
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0

echo === 推送代码 ===
git push

echo === 重新打 tag ===
git tag -a v1.0.0 -m "v1.0.0 - 首个发布版本"
git push origin v1.0.0

echo === DONE ===
