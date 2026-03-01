# 📺 投屏接收端 - TV 版

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-green?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-purple?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-7.0%2B-green?style=for-the-badge)
![API](https://img.shields.io/badge/API-24%2B-blue?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)

**安卓 TV 端投屏接收程序 - 将手机/电脑屏幕投射到大屏电视**

[安装](#-安装) • [使用](#-使用方法) • [配置](#-配置参数) • [故障排除](#-故障排除)

</div>

---

## 📋 目录

- [系统要求](#-系统要求)
- [安装](#-安装)
- [使用方法](#-使用方法)
- [功能特性](#-功能特性)
- [配置参数](#-配置参数)
- [权限说明](#-权限说明)
- [故障排除](#-故障排除)
- [开发者指南](#-开发者指南)

---

## 📱 系统要求

### 最低要求

| 项目 | 要求 |
|:---|:---|
| **操作系统** | Android 7.0 (API 24) 或更高版本 |
| **设备类型** | 安卓 TV 盒子 / 智能电视 / TV 棒 |
| **网络** | WiFi 或以太网连接 |
| **解码** | 硬件 H.264 解码支持 |
| **存储** | 100MB 可用空间 |

### 推荐配置

| 项目 | 要求 |
|:---|:---|
| **操作系统** | Android TV 9.0+ (API 28+) |
| **设备类型** | 支持 Leanback 的安卓 TV |
| **网络** | 5GHz WiFi 或千兆以太网 |
| **内存** | 2GB RAM 以上 |
| **CPU** | 四核处理器，支持硬件解码 |

### 支持的设备

✅ **支持的设备类型：**
- 小米电视/盒子
- 当贝投影/盒子
- 坚果投影
- TCL/海信/创维等智能电视
- NVIDIA Shield TV
- Chromecast with Google TV
- 其他安卓 TV 设备

❌ **不支持的设备：**
- iOS 设备（Apple TV）
- 非安卓系统的智能电视
- 手机/平板（需要单独适配）

---

## 📦 安装

### 方式一：ADB 无线安装（推荐）

```bash
# 1. 启用 TV 的开发者模式
# 设置 > 关于 > 版本号（连续点击 7 次）

# 2. 启用 USB 调试
# 设置 > 开发者选项 > USB 调试

# 3. 获取 TV 的 IP 地址
# 设置 > 网络 > 查看当前连接的 IP
# 例如：192.168.1.100

# 4. 连接 TV
adb connect 192.168.1.100:5555

# 5. 安装 APK
adb install app/build/outputs/apk/release/app-release.apk

# 6. 验证安装
adb shell pm list packages | grep cast
```

### 方式二：ADB USB 安装

```bash
# 1. 使用 USB 双公头线连接电脑和 TV

# 2. 启用 USB 调试（同上）

# 3. 检查设备连接
adb devices

# 4. 安装 APK
adb install app/build/outputs/apk/release/app-release.apk

# 5. 启动应用
adb shell am start -n com.cast.tv/.MainActivity
```

### 方式三：直接安装

```bash
# 1. 下载 APK 到 U 盘

# 2. 将 U 盘插入 TV

# 3. 使用 TV 的文件管理器找到 APK

# 4. 点击安装（可能需要允许"未知来源"）
```

### 编译 APK

```bash
# 使用 Android Studio
# 1. 打开项目
# 2. Build > Generate Signed Bundle/APK
# 3. 选择 APK
# 4. 创建或选择密钥
# 5. 选择 release 构建类型

# 或使用命令行
cd tv-server
./gradlew assembleRelease

# APK 位置：
# app/build/outputs/apk/release/app-release.apk
```

---

## 🚀 使用方法

### 快速开始

1. **安装应用**
   - 使用上述任一方式安装 APK

2. **启动应用**
   - 在 TV 的应用列表中找到"投屏接收端"
   - 点击启动

3. **查看 IP 地址**
   - 应用启动后会显示本机 IP 地址
   - 例如：`192.168.1.100:8888`

4. **电脑端连接**
   - 在电脑上运行投屏助手
   - 选择 TV 设备或手动输入 IP
   - 点击"开始投屏"

5. **享受大屏**
   - 连接成功后，电脑屏幕会实时显示在 TV 上
   - 使用遥控器可返回主界面

### 界面说明

<div align="center">
<img src="screenshots/Screenshot-TV.png" height="400" alt="TV 接收端界面">
</div>

| 区域 | 功能 |
|:---|:---|
| **状态显示** | 显示当前连接状态（等待/已连接） |
| **IP 地址** | 显示 TV 的 IP 地址和端口 |
| **设备信息** | 显示设备型号和系统版本 |
| **网络信息** | 显示当前网络连接类型 |
| **退出按钮** | 退出应用 |

### 状态说明

| 状态 | 说明 |
|:---|:---|
| 🟢 **等待连接** | 应用已启动，等待电脑端连接 |
| 🔵 **已连接** | 电脑端已连接，正在接收画面 |
| 🟡 **连接中** | 正在与电脑端建立连接 |
| 🔴 **连接失败** | 连接失败，请检查网络 |

---

## ✨ 功能特性

### 核心功能

| 功能 | 描述 | 状态 |
|:---|:---|:---:|
| 📡 **自动响应** | 自动响应设备发现请求 | ✅ |
| 🎥 **硬件解码** | 使用 MediaCodec 硬件 H.264 解码 | ✅ |
| 🔄 **自适应帧率** | 根据网络状况自动调整帧率 | ✅ |
| 🔌 **网络重连** | 网络断开后自动重连 | ✅ |
| 📺 **Leanback 支持** | 专为 TV 优化的界面 | ✅ |
| 🎨 **Jetpack Compose** | 现代化 UI 框架 | ✅ |
| 🔇 **静音模式** | 后台运行时自动静音 | ✅ |
| 📊 **性能监控** | 实时显示解码性能 | ✅ |

### 高级功能

- **低功耗模式**：待机时降低资源占用
- **自动启动**：开机自动启动（需授权）
- **遥控器支持**：完整遥控器按键映射
- **多端口监听**：同时支持多个连接请求
- **错误恢复**：解码错误自动恢复

---

## ⚙️ 配置参数

### 配置文件位置

```
/data/data/com.cast.tv/shared_prefs/config.xml
```

### 默认配置

```xml
<!-- res/values/config.xml -->
<resources>
    <!-- 服务器端口（TCP 视频流） -->
    <integer name="server_port">8888</integer>
    
    <!-- 发现服务端口（UDP） -->
    <integer name="discovery_port">8889</integer>
    
    <!-- 最大帧队列大小 -->
    <integer name="max_frame_queue">5</integer>
    
    <!-- 连接超时时间（毫秒） -->
    <integer name="connection_timeout">5000</integer>
    
    <!-- 自动重连间隔（毫秒） -->
    <integer name="reconnect_interval">3000</integer>
    
    <!-- 是否启用日志 -->
    <bool name="log_enabled">true</bool>
    
    <!-- 日志级别 -->
    <string name="log_level">INFO</string>
</resources>
```

### 修改配置

**方式一：修改源码重新编译**

```xml
<!-- 编辑 app/src/main/res/values/config.xml -->
<resources>
    <integer name="server_port">9999</integer>
</resources>
```

**方式二：使用 ADB 修改**

```bash
# 修改服务器端口
adb shell settings put global cast_server_port 9999

# 查看当前配置
adb shell settings get global cast_server_port
```

---

## 🔐 权限说明

### 应用权限

应用需要以下权限：

| 权限 | 用途 | 必需 |
|:---|:---|:---:|
| `INTERNET` | 网络通信，接收视频流 | ✅ |
| `ACCESS_NETWORK_STATE` | 检查网络状态 | ✅ |
| `ACCESS_WIFI_STATE` | 获取 WiFi 信息和 IP 地址 | ✅ |
| `CHANGE_WIFI_MULTICAST_STATE` | 设备发现多播 | ✅ |
| `RECEIVE_BOOT_COMPLETED` | 开机自动启动 | ❌ |
| `FOREGROUND_SERVICE` | 后台服务运行 | ✅ |

### 权限说明

- **网络权限**：用于接收电脑端发送的视频流
- **WiFi 权限**：用于获取 TV 的 IP 地址并显示
- **多播权限**：用于响应电脑端的设备发现请求
- **前台服务**：确保应用后台持续运行

---

## 🔧 故障排除

### ❓ 无法被发现

**症状**：电脑端无法自动发现 TV 设备

**解决方案：**

```bash
# 1. 检查网络连接
# 确保 TV 和电脑在同一 WiFi 网络

# 2. 检查应用是否运行
adb shell pm list packages | grep cast

# 3. 检查端口是否监听
adb shell netstat -tuln | grep 8889

# 4. 重启应用
adb shell am force-stop com.cast.tv
adb shell am start -n com.cast.tv/.MainActivity

# 5. 检查路由器设置
# 确保路由器未阻止 UDP 广播
```

### ❓ 连接失败

**症状**：电脑端连接时提示"连接超时"

**解决方案：**

```bash
# 1. 确认应用已启动并显示 IP 地址

# 2. 检查端口是否被占用
adb shell netstat -tuln | grep 8888

# 3. 检查防火墙设置
# 某些 TV 系统可能有防火墙，需要允许端口

# 4. 手动输入 IP 地址连接
# 在电脑端输入 TV 显示的 IP 地址

# 5. 重启路由器和 TV
```

### ❓ 画面不显示

**症状**：连接成功但没有画面

**解决方案：**

```bash
# 1. 检查 TV 是否支持 H.264 解码
# 大多数安卓 TV 都支持，但需要确认

# 2. 查看日志
adb logcat | grep cast

# 3. 降低电脑端的分辨率和码率
# 在电脑端配置文件中调整：
# video.bitrate=2000000
# video.width=1280
# video.height=720

# 4. 清除应用数据
adb shell pm clear com.cast.tv

# 5. 重新安装应用
```

### ❓ 应用崩溃

**症状**：应用启动后立即崩溃

**解决方案：**

```bash
# 1. 检查系统内存
adb shell dumpsys meminfo | head -20

# 2. 查看崩溃日志
adb logcat -d > crash.log

# 3. 清除应用数据
adb shell pm clear com.cast.tv

# 4. 重新安装
adb uninstall com.cast.tv
adb install app/build/outputs/apk/release/app-release.apk

# 5. 检查系统版本
# 确保 Android 版本 >= 7.0
```

### ❓ 画面卡顿

**症状**：画面不流畅，频繁缓冲

**解决方案：**

```bash
# 1. 检查网络质量
# 使用 TV 浏览器测试网速

# 2. 切换到 5GHz WiFi
# 5GHz 干扰少，速度更快

# 3. 降低电脑端码率
# video.bitrate=2000000

# 4. 检查 TV CPU 占用
adb shell top -m 10

# 5. 关闭其他后台应用
```

### ❓ 没有声音

**症状**：画面正常但没有声音

**解决方案：**

1. 检查 TV 音量设置
2. 检查电脑端音频是否启用
3. 确认 TV 支持音频解码
4. 尝试重启应用

---

## 👨‍💻 开发者指南

### 项目结构

```
tv-server/
├── app/
│   ├── src/main/
│   │   ├── java/com/cast/tv/
│   │   │   ├── MainActivity.kt         # 主界面
│   │   │   ├── service/CastService.kt  # 投屏服务
│   │   │   ├── decoder/VideoDecoder.kt # 视频解码器
│   │   │   ├── network/Server.kt       # 网络服务器
│   │   │   └── ui/theme/               # UI 主题
│   │   ├── res/
│   │   │   ├── values/config.xml       # 配置文件
│   │   │   ├── drawable/               # 图标资源
│   │   │   └── layout/                 # 布局文件
│   │   └── AndroidManifest.xml         # 应用清单
│   └── build.gradle                    # 构建配置
├── build.gradle                        # 项目构建
└── README.md                           # 本文档
```

### 技术栈

| 组件 | 技术 |
|:---|:---|
| **编程语言** | Kotlin 1.9+ |
| **UI 框架** | Jetpack Compose for TV |
| **网络库** | Netty 4.1.x |
| **视频解码** | Android MediaCodec |
| **架构模式** | MVVM |
| **依赖注入** | Hilt (可选) |

### 构建命令

```bash
# 清理项目
./gradlew clean

# 编译 Debug 版
./gradlew assembleDebug

# 编译 Release 版
./gradlew assembleRelease

# 安装到 TV
./gradlew installDebug

# 运行测试
./gradlew test

# 生成签名 APK
./gradlew assembleRelease
```

### 依赖说明

主要依赖：

- **Netty** (`io.netty:netty-all`): 网络通信
- **Kotlinx Coroutines** (`org.jetbrains.kotlinx:kotlinx-coroutines-android`): 协程
- **Jetpack Compose** (`androidx.compose.ui`): UI 框架
- **AndroidX Core** (`androidx.core:core-ktx`): Android 核心库
- **Lifecycle** (`androidx.lifecycle`): 生命周期管理

### 调试技巧

```bash
# 查看日志
adb logcat | grep cast

# 查看网络状态
adb shell netstat -tuln

# 查看内存使用
adb shell dumpsys meminfo com.cast.tv

# 查看 CPU 使用
adb shell top -m 10 | grep cast

# 远程调试
# 在 Chrome 浏览器中输入：chrome://inspect
```

---

## 📄 开源协议

本项目采用 [MIT 协议](../LICENSE) 开源。

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📞 联系方式

如有问题，请：

1. 查看 [主项目 README](../README.md)
2. 查看 [PC 客户端文档](../pc-client/README.md)
3. 提交 Issue
4. 联系开发者

---

<div align="center">

**Made with ❤️ by AnYang**

让大屏投屏更简单！

</div>
