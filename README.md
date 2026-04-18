# SensorDiary (传感器情绪日记)

SensorDiary 是一款基于 Android 的创新应用，旨在通过结合环境传感器数据（如光照、信号强度等）来记录和分析用户的每日情绪。

## 📱 真机效果

<div align="center">
  <img src="image/effect.png" width="80%" alt="CiLin App Effect"/>
</div>

## 📺 项目地址

> 代码地址：https://github.com/luoxjcode/SensorDiary

> 体验地址：https://share.feijipan.com/s/7n3kQLEg

## 🌟 主要功能

- **传感器数据采集**：实时监测环境光照强度和移动网络信号强度，为情绪记录提供背景上下文。
- **情绪记录系统**：简单直观的界面，允许用户记录当前心情，并自动关联当前的传感器数据。
- **数据分析与可视化**：提供情绪趋势图表，帮助用户发现环境因素与心理状态之间的潜在联系。
- **存储安全**：检测数据基于本地存储，保护您的隐私日记安全。
- **数据管理**：支持历史记录的查看、单条/批量删除，以及数据导出与分享。

## 🛠️ 技术栈

- **开发语言**：Kotlin
- **UI 框架**：Jetpack Compose (声明式 UI)
- **数据库**：Room (本地持久化存储)
- **架构组件**：ViewModel, Coroutines, Flow, Navigation Compose
- **设计规范**：Material 3 (Material Design)
- **依赖管理**：Gradle (Kotlin DSL)

## 🚀 快速开始

1. **环境要求**：
   - Android Studio Koala | 2024.1.1 或更高版本
   - JDK 21
   - Android 设备/模拟器 (API 24+)

2. **构建步骤**：
   - 克隆项目到本地。
   - 在 Android Studio 中打开项目。
   - 等待 Gradle 同步完成。
   - 点击 `Run` 按钮安装到设备。

## 📂 项目结构

- `app/src/main/java`: 包含核心业务逻辑、UI 组件、数据层和视图模型。
- `app/src/main/res`: 包含图片资源、字符串和主题配置。
- `app/src/main/icon_source`: 存放原始应用图标。

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可。
