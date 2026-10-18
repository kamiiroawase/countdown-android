# 红叶倒计时

[![Build](https://github.com/kamiiroawase/countdown-android/actions/workflows/build.yml/badge.svg)](https://github.com/kamiiroawase/countdown-android/actions/workflows/build.yml)
[![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](LICENSE)

Android 倒计时应用：记录每一个值得期待的日子。公历与农历、灵活的重复规则、子事件层级、桌面小组件与到点提醒，Jetpack Compose 渲染。

[English version](README.en.md) · [版本发布说明](https://github.com/kamiiroawase/countdown-android/releases)

## 功能

- **公历 / 农历**：目标日两种历法可选，农历支持「闰月参与」计数
- **重复规则**：每 N 天 / 周 / 月 / 年 / 小时重复，可选重复结束日
- **精确到秒**：目标时间记录时 / 分 / 秒，倒计时按年、月、天、小时、分、秒展示，过期后显示「已经……」
- **天数口径**：目标日当天整天显示「还有0天」；开启「不足一天算一天」时按日历天计数，此前每天整天显示「还有N天」，关闭则按剩余时长显示
- **子事件**：主事件下可挂子事件，删除主事件连带删除；详情页可全屏展示（含 / 隐藏子事件两种）
- **视图与排序**：列表 / 网格两种视图，事件可设排序权重（越大越靠前）
- **桌面小组件**：单事件卡片与事件列表两种样式，可绑定指定事件或跟随 App 列表（全部）
- **到点提醒**：通知 + 精确闹钟，开机自动恢复调度
- **事件背景**：详情页可自定义背景图，一键恢复默认
- **时区**：倒计时按所选时区计算，默认跟随系统（时区选择来自同作者的 [ZonePicker](https://github.com/kamiiroawase/zonepicker)）
- **数据备份**：事件数据与个人偏好适配系统备份，恢复备份或换机迁移无需手动操作
- 首次启动预置示例事件：「遇见你」（记录首次启动时刻）与春节、重阳节（农历，每年重复）

倒计时的核心计算由同作者的 [nexttime](https://central.sonatype.com/artifact/io.github.kamiiroawase/nexttime-android) 库完成，农历历法由 tyme 经其传递提供。

## 下载

APK 随 [GitHub Releases](https://github.com/kamiiroawase/countdown-android/releases) 发布，要求 Android 12 及以上。

## 开发

```bash
git clone https://github.com/kamiiroawase/countdown-android.git
cd countdown-android
./gradlew build              # 构建、lint、Spotless —— 与 CI 同一道门禁
./gradlew :app:installDebug  # 安装到设备
```

本地 release 签名读取仓库根目录的 `keystore.properties`（已 gitignore；字段：`storeFile` / `storePassword` / `keyAlias` / `keyPassword`），文件不存在时产出未签名 APK。

### 发布

推送 `v*` git tag 触发 Release 工作流：完整构建门禁通过后，用仓库 secrets 中的签名密钥构建签名 APK（`countdown-<tag>.apk`）附到对应的 GitHub Release。

## 许可

[The Unlicense](LICENSE) —— 公共领域，随意使用。
