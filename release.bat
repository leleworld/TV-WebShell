@echo off
cd /d "%~dp0"

echo === 清理临时 bat 文件 ===
git rm -f push.bat push2.bat push3.bat push4.bat push5.bat cleanup.bat 2>nul

echo === 提交更新 ===
git add -A
git commit -m "ci: add auto-release on tag push + cleanup temp files"

echo === 推送代码 ===
git push

echo === 创建并推送 v1.0.0 tag ===
git tag -a v1.0.0 -m "v1.0.0 - 首个发布版本"
git push origin v1.0.0

echo === DONE ===
echo 请到 GitHub Actions 查看 Release 构建状态
