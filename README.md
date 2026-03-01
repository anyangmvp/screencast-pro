# 🖥️ ScreenCast Pro

<div align="center">

![GitHub stars](https://img.shields.io/github/stars/yourusername/screencast-pro?style=for-the-badge&color=yellow)
![GitHub forks](https://img.shields.io/github/forks/yourusername/screencast-pro?style=for-the-badge&color=blue)
![License](https://img.shields.io/github/license/yourusername/screencast-pro?style=for-the-badge&color=green)
![Platform](https://img.shields.io/badge/platform-Windows%20%7C%20Android%20TV-blue?style=for-the-badge)
![Version](https://img.shields.io/badge/version-1.0.0-green?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-purple?style=for-the-badge)

**✨ 高清无线投屏解决方案 | 电脑到安卓 TV**

将电脑屏幕无线投射到安卓智能电视，支持高清画质和低延迟传输

[功能特性](#-功能特性) • [快速开始](#-快速开始) • [技术架构](#-技术架构) • [常见问题](#-常见问题)

</div>

---

## 📱 效果展示

<div align="center">
<table>
<tr>
<td align="center"><b>💻 电脑客户端</b></td>
<td align="center"><b>📺 TV 接收端</b></td>
</tr>
<tr>
<td align="center"><img src="screenshots/Screenshot-PC.png" height="300" alt="PC 客户端界面"></td>
<td align="center"><img src="screenshots/Screenshot-TV.png" height="300" alt="TV 接收端界面"></td>
</tr>
</table>
</div>

---

## ✨ 功能特性

| 功能 | 描述 | 支持 |
|:---|:---|:---:|
| 🔍 **自动发现** | UDP 广播自动发现同一网络下的 TV 设备 | ✅ |
| 📺 **高清画质** | 支持 720p/1080p/2K/4K 分辨率，自适应屏幕 | ✅ |
| 🎥 **H.264 硬编码** | 使用 FFmpeg 硬件加速编码，高效稳定 | ✅ |
| ⚡ **低延迟** | 端到端延迟 < 100ms | ✅ |
| 🔊 **音频同步** | 音视频同步传输 | ✅ |
| 🎨 **现代化 UI** | 玻璃拟态设计风格 | ✅ |
| 🔄 **动态分辨率** | 自动适配电脑屏幕分辨率 | ✅ |
| 🌐 **跨平台** | 支持 Windows/macOS/Linux | ✅ |

---

## 🏗️ 技术架构

```
┌─────────────────────────┐         ┌─────────────────────────┐
│   💻 PC 客户端           │  TCP    │   📺 TV 接收端          │
│   (发送端)              │◄───────►│   (接收端)              │
│                         │  8888   │                         │
│  • ScreenCapture        │         │  • VideoDecoder         │
│  • H.264 Encoder        │         │  • MediaCodec           │
│  • Netty Client         │         │  • Netty Server         │
│  • JavaFX UI            │         │  • Jetpack Compose      │
└───────────┬─────────────┘         └───────────┬─────────────┘
            │                                   │
            │ UDP Broadcast (8889)              │
            └──────────────┬────────────────────┘
                           │
                    ┌──────▼──────┐
                    │  设备发现    │
                    └─────────────┘
```

### 技术栈对比

| 端 | 编程语言 | UI 框架 | 网络 | 视频处理 |
|:---|:---|:---|:---|:---|
| **💻 PC 端** | Java 17 | JavaFX | Netty | JavaCV + FFmpeg |
| **📺 TV 端** | Kotlin | Jetpack Compose | Netty | MediaCodec |
| **🌐 协议** | TCP (视频流) + UDP (设备发现) |

---

## 🚀 快速开始

### 📋 环境要求

| 端 | 系统要求 | 其他要求 |
|:---|:---|:---|
| **💻 PC 端** | Windows 10/11, macOS, Linux | Java 17+, 4GB 内存 |
| **📺 TV 端** | Android TV 7.0+ (API 24+) | 已开启 ADB 调试 |

### 1️⃣ 编译 PC 端

```bash
# 克隆项目
git clone https://github.com/yourusername/screencast-pro.git
cd screencast-pro/pc-client

# 编译打包（包含 EXE 生成）
mvn clean package -Pbuild-exe

# 运行 JAR
java -jar target/pc-client-1.0.0.jar

# 或直接运行 EXE（Windows）
target\投屏助手.exe
```

> 💡 **提示**: 使用 `-Pbuild-exe` 参数会生成独立的 EXE 可执行文件

### 2️⃣ 安装 TV 端

```bash
# 连接 TV 设备（替换为你的 TV IP）
adb connect 192.168.1.100:5555

# 安装 APK
adb install tv-server/app/build/outputs/apk/release/app-release.apk

# 验证安装
adb shell pm list packages | grep cast
```

### 3️⃣ 开始投屏

1. ✅ 确保电脑和 TV 连接到**同一个 WiFi 网络**
2. ✅ 在 TV 上启动**投屏接收端**应用
3. ✅ 在电脑上运行**投屏助手**
4. ✅ 程序会自动搜索设备，选择并点击**开始投屏**

---

## 📂 项目结构

```
screencast-pro/
├── 📄 README.md                 # 项目总览（本文件）
├── pc-client/                   # 💻 PC 客户端
│   ├── README.md               # PC 端详细文档
│   ├── pom.xml                 # Maven 配置
│   ├── src/                    # 源代码
│   ├── icon/                   # 应用图标
│   └── start.bat               # Windows 启动脚本
├── tv-server/                   # 📺 TV 接收端
│   ├── README.md               # TV 端详细文档
│   ├── app/                    # Android 应用源码
│   └── build.gradle            # Gradle 配置
└── screenshots/                 # 📸 截图文件夹
```

---

## ⚙️ 配置说明

### PC 端配置

配置文件：`pc-client/src/main/resources/config.properties`

```properties
# 视频编码配置
video.bitrate=4000000          # 视频码率 (bps)
video.framerate=30             # 帧率 (fps)
video.width=1920               # 分辨率宽度
video.height=1080              # 分辨率高度

# 网络配置
server.port=8888               # TCP 服务器端口
discovery.port=8889            # UDP 发现端口

# 高级配置
encoder.preset=medium          # 编码预设：ultrafast/fast/medium/slow
audio.enabled=true             # 是否启用音频
```

### TV 端配置

配置文件：`tv-server/app/src/main/res/values/config.xml`

```xml
<resources>
    <integer name="server_port">8888</integer>
    <integer name="discovery_port">8889</integer>
    <integer name="max_frame_queue">5</integer>
    <string name="app_name">投屏接收端</string>
</resources>
```

---

## 🔧 故障排除

### ❓ 无法发现设备

**可能原因：**
- ❌ 电脑和 TV 不在同一网络
- ❌ 防火墙阻止 UDP 广播
- ❌ TV 端应用未启动

**解决方案：**
1. 检查网络连接，确保在同一 WiFi
2. 关闭防火墙或添加例外规则
3. 重启 TV 端应用
4. 尝试手动输入 TV 的 IP 地址

### ❓ 画面卡顿或延迟高

**可能原因：**
- ❌ 网络带宽不足
- ❌ 视频码率过高
- ❌ CPU 占用过高

**解决方案：**
1. 降低视频码率（建议 2-4 Mbps）
2. 降低帧率（建议 24-30 fps）
3. 降低分辨率（尝试 720p）
4. 关闭其他占用带宽的应用

### ❓ 连接失败

**可能原因：**
- ❌ 端口被占用
- ❌ TV 端未启动
- ❌ 网络不稳定

**解决方案：**
1. 检查端口是否被占用（`netstat -ano | findstr 8888`）
2. 重启 TV 端和 PC 端应用
3. 检查网络连接质量
4. 重启路由器

### ❓ 没有声音

**可能原因：**
- ❌ 音频未启用
- ❌ 系统音频捕获权限

**解决方案：**
1. 检查配置文件中 `audio.enabled=true`
2. 确保系统允许屏幕录制和音频捕获
3. 更新声卡驱动

---

## 📊 性能指标

| 指标 | 数值 | 测试环境 |
|:---|:---|:---|
| 端到端延迟 | < 100ms | 千兆局域网 |
| 视频分辨率 | 最高 4K | 取决于网络 |
| 帧率 | 最高 60fps | 取决于硬件 |
| CPU 占用 | 15-25% | Intel i5, 1080p |
| 内存占用 | ~200MB | PC 客户端 |

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 开源协议

本项目采用 [MIT 协议](LICENSE) 开源。

---

## 👨‍💻 开发者

- **作者**: AnYang
- **GitHub**: [@anyangmvp](https://github.com/anyangmvp)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个 Star 支持一下！⭐**

Made with ❤️ by AnYang

</div>
