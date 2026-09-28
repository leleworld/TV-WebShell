# 剧迷TV - Android TV 遥控器版

纯 WebView 壳子，直接加载 gimytw.cc，零数据存储/解析，仅增加遥控器 D-pad 导航支持。

## 🎮 遥控器按键映射

| 遥控器按键 | 功能 |
| --- | --- |
| ↑ ↓ ← → 方向键 | 网页元素焦点导航（智能空间寻路） |
| 确认键 / OK | 点击当前高亮元素 |
| 返回键 | 网页后退，无历史时双击退出 |
| 菜单键 | 回到首页 |
| 频道+/- | 整页快速滚动 |

## 🔧 构建

```bash
# 用 Android Studio 打开项目目录，或命令行构建：
./gradlew assembleDebug

# APK 输出位置：
# app/build/outputs/apk/debug/app-debug.apk

```

## 📦 安装到电视

```bash
adb connect <电视IP>:5555
adb install app-debug.apk

```

## 核心设计

- **零数据解析**：纯 WebView 直接访问网站，不抓取/存储任何数据
- **遥控器适配**：通过注入 JS 实现 D-pad 空间导航，金色高亮框标识当前焦点
- **全屏沉浸**：横屏全屏，隐藏系统栏，适配大屏体验
- **视频友好**：关闭手势播放限制，支持网页内嵌播放器

