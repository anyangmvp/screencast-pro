# 电脑端投屏程序

## 系统要求

- Windows 10/11、macOS 或 Linux
- Java 17 或更高版本
- 4GB 内存
- 网络连接（与安卓TV在同一局域网）

## 编译

```bash
mvn clean package
```

编译完成后，可执行文件位于 `target/pc-client-1.0.0.jar`

## 运行

```bash
java -jar target/pc-client-1.0.0.jar
```

## 使用方法

1. 确保电脑和安卓TV连接到同一个WiFi网络
2. 在安卓TV上启动"投屏接收端"应用
3. 在电脑上运行此程序
4. 程序会自动搜索局域网内的TV设备
5. 选择要连接的TV设备，点击"开始投屏"

## 功能特性

- 自动发现局域网内的安卓TV设备
- 实时屏幕捕获（最高1080p@30fps）
- H.264硬件编码
- 低延迟传输（< 100ms）
- 支持多屏幕选择

## 配置参数

可在 `config.properties` 中修改以下参数：

```properties
# 视频码率 (bps)
video.bitrate=4000000

# 帧率 (fps)
video.framerate=30

# 视频分辨率
video.width=1920
video.height=1080

# 服务器端口
server.port=8888
```

## 故障排除

### 无法发现设备
- 检查电脑和TV是否在同一网络
- 检查防火墙设置，允许UDP广播
- 尝试手动输入IP地址连接

### 画面卡顿
- 降低视频码率
- 降低帧率
- 检查网络连接质量

### 连接失败
- 检查TV端应用是否正在运行
- 检查端口是否被占用
- 重启两端应用
