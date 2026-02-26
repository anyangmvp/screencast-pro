# 安卓TV端投屏接收程序

## 系统要求

- Android 7.0 (API 24) 或更高版本
- 支持Leanback的安卓TV设备
- 网络连接（与电脑在同一局域网）
- 硬件H.264解码支持

## 编译

使用 Android Studio 打开此项目，点击 "Build > Generate Signed Bundle/APK" 生成APK。

或者使用命令行：

```bash
./gradlew assembleRelease
```

APK文件位于 `app/build/outputs/apk/release/`

## 安装

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

## 使用方法

1. 在安卓TV上安装并启动此应用
2. 应用会显示本机IP地址（例如：192.168.1.100）
3. 在电脑端运行发送端程序
4. 电脑端会自动发现TV设备，或手动输入IP地址连接
5. 连接成功后，电脑屏幕会实时显示在TV上

## 功能特性

- 自动响应设备发现请求
- 后台服务持续运行
- 硬件H.264解码（低CPU占用）
- 自适应帧率
- 网络重连机制

## 权限说明

应用需要以下权限：

- `INTERNET` - 网络通信
- `ACCESS_NETWORK_STATE` - 检查网络状态
- `ACCESS_WIFI_STATE` - 获取WiFi信息
- `CHANGE_WIFI_MULTICAST_STATE` - 设备发现多播

## 配置参数

可在 `res/values/config.xml` 中修改：

```xml
<!-- 服务器端口 -->
<integer name="server_port">8888</integer>

<!-- 发现服务端口 -->
<integer name="discovery_port">8889</integer>

<!-- 最大帧队列大小 -->
<integer name="max_frame_queue">5</integer>
```

## 故障排除

### 无法被发现
- 检查TV和电脑是否在同一WiFi网络
- 检查路由器是否阻止UDP广播
- 重启应用

### 画面不显示
- 检查TV是否支持H.264硬件解码
- 降低电脑端的分辨率和码率
- 检查网络连接质量

### 应用崩溃
- 检查系统内存是否充足
- 更新到最新版本
- 清除应用数据后重试

## 开发者信息

- 最低SDK: 24 (Android 7.0)
- 目标SDK: 34 (Android 14)
- 编程语言: Kotlin
- UI框架: Jetpack Compose for TV
