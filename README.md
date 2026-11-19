# 有数记账（Youshu Ledger）

一款 Android 原生手机记账软件。授予「通知使用权」后，自动抓取微信支付、支付宝、银行卡的收支通知并记账，数据仅保存在本机，不上传云端。应用内名称、桌面图标名称均为「有数记账」。

## 功能

- **自动记账**：监听通知栏，解析微信支付/支付宝/银行卡通知，自动提取金额、商户、收支方向并归类
- **智能分类**：内置餐饮、交通、购物、生活缴费、娱乐、医疗、教育、工资、理财等分类规则
- **账单明细**：按日分组展示，左滑删除，右下角 + 手动补记
- **统计报表**：本月收支概览、近 6 个月趋势柱状图、支出分类占比
- **来源开关**：可分别启用/停用微信、支付宝、银行卡的自动记账
- **版本记录**：每次发布递增版本号，设置页可查看更新内容，APK 文件名带版本号
- **隐私优先**：所有数据存储在本地 SQLite，无需登录，无网络上传

## 技术栈

- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite) 本地数据库
- NotificationListenerService 通知监听
- MVVM + Coroutines/Flow
- AGP 8.7.3 / Kotlin 2.0.21 / Gradle 8.11.1

## 构建与安装

本机未安装 Android SDK，项目已配置 GitHub Actions 云端编译，无需本地环境。

### 步骤

1. 在 GitHub 新建一个仓库（如 `AutoLedger`）
2. 把本项目所有文件上传到该仓库的 `main` 分支
3. 进入仓库的 **Actions** 标签页，等待 `Build Android APK` 工作流自动运行（首次推送会自动触发）
4. 工作流完成后，点击该次运行记录，在页面底部 **Artifacts** 处下载 `YoushuLedger-debug-apk`
5. 解压得到 `app-debug.apk`，重命名为带版本号的文件（如 `有数记账-v1.1.0-code2.apk`）后传到手机安装即可（需允许安装未知来源应用）

也可以手动触发：Actions → Build Android APK → Run workflow。

### 本地构建（可选）

若本机已安装 Android Studio：

1. 用 Android Studio 打开项目目录，等待 Gradle 同步
2. 菜单栏 Build → Build Bundle(s)/APK(s) → Build APK(s)
3. 产物在 `app/build/outputs/apk/debug/app-debug.apk`

> 若使用命令行且缺少 `gradle-wrapper.jar`，在项目根目录执行 `gradle wrapper --gradle-version 8.11.1` 生成后再用 `./gradlew assembleDebug`。

## 首次使用

1. 安装后打开 App，进入「我的」页
2. 点击「通知读取权限」卡片，在系统设置中开启本 App 的通知使用权
3. 返回 App，用微信/支付宝或银行卡完成一笔支付，稍等即可在「账单」页看到自动入账的记录

## 注意事项

- **仅限 Android**：iOS 系统禁止第三方 App 读取其他 App 的通知，无法自动记账，本项目为 Android 原生实现
- **通知格式差异**：各支付 App 的通知文案会随版本更新变化，若发现某类通知未入账，可能需要更新 `NotificationParser.kt` 中的正则规则
- **安卓厂商保活**：部分国产 ROM 可能限制后台服务，需在系统设置中将本 App 设为「自启动」并允许后台运行
- **去重机制**：以「来源+金额+商户+分钟」生成去重键，同一笔支付重复通知不会重复入账

## 目录结构

```
AutoLedger/
├── app/src/main/java/com/autoledger/app/
│   ├── data/          # Room 实体、DAO、数据库、分类规则
│   ├── parser/        # 支付通知解析引擎（纯 Kotlin，可单测）
│   ├── service/       # 通知监听服务、开机自启、来源开关
│   ├── ui/            # Compose 页面（账单/统计/设置/补记）
│   ├── viewmodel/     # 状态管理
│   ├── util/          # 格式化工具、应用内版本记录（AppChangelog.kt）
│   ├── MainActivity.kt
│   └── AutoLedgerApp.kt
├── app/src/test/      # 解析器单元测试
├── .github/workflows/build.yml   # 云端编译配置
├── CHANGELOG.md       # 每次发布的版本号与变更记录
└── gradlew            # Gradle wrapper 启动脚本
```
