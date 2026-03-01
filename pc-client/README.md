# 💻 投屏助手 - PC 客户端

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-green?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge)
![Platform](https://img.shields.io/badge/platform-Windows%20%7C%20macOS%20%7C%20Linux-blue?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)

**电脑屏幕无线投射到安卓 TV 的发送端程序**

[安装](#-安装) • [使用](#-使用方法) • [配置](#-配置参数) • [故障排除](#-故障排除)

</div>

---

## 📋 目录

- [系统要求](#-系统要求)
- [安装](#-安装)
- [使用方法](#-使用方法)
- [功能特性](#-功能特性)
- [配置参数](#-配置参数)
- [打包 EXE](#-打包-exe)
- [故障排除](#-故障排除)
- [开发者指南](#-开发者指南)

---

## 🖥️ 系统要求

### 最低配置

| 项目 | 要求 |
|:---|:---|
| **操作系统** | Windows 10 / macOS 10.14 / Linux |
| **Java 版本** | Java 17 或更高版本 |
| **内存** | 4GB RAM |
| **网络** | 局域网连接（与 TV 同一网络） |
| **存储** | 500MB 可用空间 |

### 推荐配置

| 项目 | 要求 |
|:---|:---|
| **操作系统** | Windows 11 / macOS 12+ / Ubuntu 20.04+ |
| **Java 版本** | Java 21 LTS |
| **内存** | 8GB RAM |
| **CPU** | 4 核心以上，支持硬件编码 |
| **网络** | 千兆以太网或 5GHz WiFi |

---

## 📦 安装

### 方式一：使用 EXE 安装包（推荐 - 仅 Windows）

```bash
# 1. 编译项目
mvn clean package -Pbuild-exe

# 2. 创建分发文件夹
cd pc-client
if (Test-Path dist) { Remove-Item -Recurse -Force dist }
New-Item -ItemType Directory -Path dist\lib | Out-Null
Copy-Item target\投屏助手.exe dist\
Copy-Item target\pc-client-1.0.0.jar dist\
Copy-Item target\lib\*.jar dist\lib\
Copy-Item start.bat dist\

# 3. 运行
dist\投屏助手.exe
```

### 方式二：直接运行 JAR

```bash
# 1. 编译
mvn clean package

# 2. 运行
java -jar target/pc-client-1.0.0.jar

# 或使用启动脚本（Windows）
start.bat
```

### 方式三：IDE 中运行

```bash
# 1. 克隆项目
git clone https://github.com/yourusername/screencast-pro.git
cd screencast-pro/pc-client

# 2. 在 IDE 中打开项目并运行 Main 类
```

---

## 🚀 使用方法

### 快速开始

1. **连接网络**
   - 确保电脑和安卓 TV 连接到**同一个 WiFi 网络**

2. **启动 TV 端**
   - 在 TV 上打开"投屏接收端"应用
   - 记下 TV 显示的 IP 地址（如：`192.168.1.100`）

3. **启动 PC 端**
   - 运行 `投屏助手.exe` 或 `java -jar pc-client-1.0.0.jar`

4. **连接设备**
   - 程序会自动搜索局域网内的 TV 设备
   - 在设备列表中选择你的 TV
   - 或手动输入 TV 的 IP 地址

5. **开始投屏**
   - 点击"开始投屏"按钮
   - 调整分辨率和画质（可选）
   - 享受大屏体验！

### 界面说明

<div align="center">
<img src="screenshots/Screenshot-PC.png" height="400" alt="PC 客户端界面">
</div>

| 区域 | 功能 |
|:---|:---|
| **设备列表** | 显示自动发现的 TV 设备 |
| **IP 输入框** | 手动输入 TV 的 IP 地址 |
| **屏幕选择** | 选择要投射的显示器（多屏支持） |
| **分辨率设置** | 调整输出分辨率 |
| **开始/停止按钮** | 控制投屏状态 |
| **状态栏** | 显示连接状态和性能指标 |

---

## ✨ 功能特性

### 核心功能

| 功能 | 描述 | 状态 |
|:---|:---|:---:|
| 🔍 **自动发现** | UDP 广播自动发现同一网络下的 TV 设备 | ✅ |
| 📺 **多分辨率** | 支持 720p/1080p/2K/4K 分辨率 | ✅ |
| 🎥 **硬件编码** | H.264 硬件加速编码，高效稳定 | ✅ |
| ⚡ **低延迟** | 端到端延迟 < 100ms | ✅ |
| 🔊 **音频传输** | 音视频同步传输 | ✅ |
| 🖥️ **多屏支持** | 支持多显示器选择 | ✅ |
| 🎨 **玻璃拟态 UI** | 现代化设计风格 | ✅ |
| 🔄 **自适应码率** | 根据网络状况自动调整 | ✅ |

### 高级功能

- **动态帧率调整**：根据性能自动调整帧率
- **网络重连机制**：断开后自动尝试重连
- **性能监控**：实时显示 FPS、延迟、码率
- **快捷键支持**：快速开始/停止投屏
- **日志记录**：详细的运行日志便于调试

---

## ⚙️ 配置参数

### 配置文件位置

```
# Windows
C:\Users\<用户名>\.screencast-pro\config.properties

# macOS
~/.screencast-pro/config.properties

# Linux
~/.screencast-pro/config.properties
```

### 配置项说明

```properties
# ====================================
# 视频编码配置
# ====================================

# 视频码率 (bps)
# 推荐值：720p=2000000, 1080p=4000000, 2K=8000000, 4K=15000000
video.bitrate=4000000

# 帧率 (fps)
# 推荐值：24/30/60
video.framerate=30

# 视频分辨率
video.width=1920
video.height=1080

# H.264 编码预设
# 可选值：ultrafast/fast/medium/slow/slower
# 越快延迟越高，画质越低
encoder.preset=medium

# ====================================
# 音频配置
# ====================================

# 是否启用音频传输
audio.enabled=true

# 音频码率 (bps)
audio.bitrate=128000

# 音频采样率 (Hz)
audio.sample_rate=44100

# ====================================
# 网络配置
# ====================================

# TCP 服务器端口（视频流）
server.port=8888

# UDP 发现端口
discovery.port=8889

# 发现超时时间 (毫秒)
discovery.timeout=3000

# 连接超时时间 (毫秒)
connection.timeout=5000

# ====================================
# 高级配置
# ====================================

# 最大帧队列大小
queue.max_size=5

# 是否启用日志
log.enabled=true

# 日志级别：DEBUG/INFO/WARN/ERROR
log.level=INFO

# 日志文件路径
log.path=logs/screencast.log
```

---

## 📦 打包 EXE

### 生成 EXE 可执行文件

```bash
# 1. 进入项目目录
cd pc-client

# 2. 执行打包命令（包含 EXE 生成）
mvn clean package -Pbuild-exe

# 3. 编译完成后，生成的文件在 target 目录：
#    - target/投屏助手.exe          (EXE 启动器)
#    - target/pc-client-1.0.0.jar   (主程序 JAR)
#    - target/lib/                  (依赖库文件夹)
```

### 创建分发文件夹

```powershell
# PowerShell 命令
if (Test-Path dist) { Remove-Item -Recurse -Force dist }
New-Item -ItemType Directory -Path dist\lib | Out-Null
Copy-Item target\投屏助手.exe dist\
Copy-Item target\pc-client-1.0.0.jar dist\
Copy-Item target\lib\*.jar dist\lib\
Copy-Item start.bat dist\
```

### 分发文件夹结构

```
dist/
├── 投屏助手.exe          # EXE 可执行文件（用户双击运行）
├── pc-client-1.0.0.jar   # 主程序 JAR
├── start.bat             # Windows 启动脚本
└── lib/                  # 依赖库文件夹
    ├── javacv-platform-1.5.9.jar
    ├── netty-all-4.1.100.Final.jar
    ├── gson-2.10.1.jar
    └── ... (170+ 个依赖 JAR)
```

### 注意事项

⚠️ **重要提示：**

1. **Java 环境**：EXE 需要系统已安装 Java 17 或更高版本
2. **环境变量**：确保 `JAVA_HOME` 环境变量已正确设置
3. **依赖完整**：分发时必须包含整个 `lib` 文件夹
4. **图标文件**：EXE 图标来自 `icon/ay-cast.ico`

---

## 🔧 故障排除

### ❓ 无法发现设备

**症状**：设备列表为空，无法自动发现 TV

**解决方案：**

```bash
# 1. 检查网络连接
ping <TV-IP>

# 2. 检查防火墙设置
# Windows: 允许 Java 通过防火墙
# 控制面板 > Windows Defender 防火墙 > 允许应用通过防火墙

# 3. 检查端口是否可用
netstat -ano | findstr 8889

# 4. 尝试手动输入 IP 地址连接
```

### ❓ 连接失败

**症状**：点击连接后提示"连接超时"或"连接被拒绝"

**解决方案：**

```bash
# 1. 确认 TV 端应用已启动
# 2. 检查端口是否被占用
netstat -ano | findstr 8888

# 3. 重启两端应用
# 4. 检查 TV 的 IP 地址是否变化
# 5. 尝试关闭杀毒软件
```

### ❓ 画面卡顿或延迟高

**症状**：画面不流畅，延迟明显

**解决方案：**

```properties
# 修改配置文件，降低视频参数：
video.bitrate=2000000      # 降低码率
video.framerate=24         # 降低帧率
video.width=1280           # 降低分辨率
video.height=720
encoder.preset=ultrafast   # 使用更快的编码预设
```

### ❓ 没有声音

**症状**：画面正常但没有声音

**解决方案：**

1. 检查配置文件中 `audio.enabled=true`
2. 确保系统允许屏幕录制和音频捕获
   - Windows: 设置 > 隐私 > 麦克风 > 允许应用访问
3. 更新声卡驱动
4. 检查音频输出设备是否正确

### ❓ 程序崩溃

**症状**：启动后立即崩溃或运行中闪退

**解决方案：**

```bash
# 1. 查看日志文件
# Windows: C:\Users\<用户名>\.screencast-pro\logs\
# macOS/Linux: ~/.screencast-pro/logs/

# 2. 增加 JVM 内存
java -Xmx2G -jar target/pc-client-1.0.0.jar

# 3. 更新显卡驱动
# 4. 检查 Java 版本
java -version
```

### ❓ EXE 无法启动

**症状**：双击 EXE 无反应或提示错误

**解决方案：**

```bash
# 1. 检查 Java 是否安装
java -version

# 2. 检查 JAVA_HOME 环境变量
echo %JAVA_HOME%

# 3. 使用 JAR 方式运行
java -jar pc-client-1.0.0.jar

# 4. 检查 lib 文件夹是否完整
```

---

## 👨‍💻 开发者指南

### 项目结构

```
pc-client/
├── src/main/
│   ├── java/com/cast/pc/
│   │   ├── Main.java              # 程序入口
│   │   ├── ui/MainWindow.java     # 主窗口 UI
│   │   ├── capture/ScreenCapture.java  # 屏幕捕获
│   │   ├── network/CastClient.java     # 网络客户端
│   │   ├── discovery/DeviceDiscovery.java  # 设备发现
│   │   └── config/AppConfig.java   # 配置管理
│   └── resources/
│       ├── style.css              # UI 样式
│       └── config.properties      # 默认配置
├── icon/
│   └── ay-cast.ico                # 应用图标
├── pom.xml                        # Maven 配置
└── start.bat                      # 启动脚本
```

### 构建命令

```bash
# 清理编译
mvn clean

# 编译项目
mvn compile

# 运行测试
mvn test

# 打包 JAR
mvn package

# 打包 EXE
mvn package -Pbuild-exe

# 跳过测试
mvn package -DskipTests

# 安装到本地仓库
mvn install
```

### 依赖说明

主要依赖：

- **JavaCV** (`javacv-platform`): 视频捕获和编码
- **JavaFX** (`javafx-controls`): UI 框架
- **Netty** (`netty-all`): 网络通信
- **Gson** (`gson`): JSON 处理
- **SLF4J** (`slf4j-simple`): 日志框架

---

## 📄 开源协议

本项目采用 [MIT 协议](../LICENSE) 开源。

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

<div align="center">

**Made with ❤️ by AnYang**

如有问题，请查看 [主项目 README](../README.md) 或提交 Issue

</div>
