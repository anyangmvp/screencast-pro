package com.cast.pc.ui;

import com.cast.pc.capture.ScreenCapture;
import com.cast.pc.network.CastClient;
import com.cast.pc.discovery.DeviceDiscovery;
import com.cast.pc.config.AppConfig;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.InetAddress;
import java.util.List;

/**
 * 主窗口界面 - 现代浅色清新主题
 * 设计风格：Modern Light Clean with Glassmorphism
 * 主色调：白色背景 + 蓝色渐变 + 清新点缀
 */
public class MainWindow extends Application {
    
    private CastClient castClient;
    private ScreenCapture screenCapture;
    private DeviceDiscovery deviceDiscovery;
    private AppConfig appConfig;
    
    // UI 组件引用
    private Label statusLabel;
    private Label deviceLabel;
    private Circle statusIndicator;
    private Button startButton;
    private Button stopButton;
    private Button refreshButton;
    private Button settingsButton;
    private ListView<String> deviceListView;
    private TextArea logArea;
    private VBox mainContainer;
    private Label networkModeLabel;
    private ComboBox<String> resolutionComboBox;
    private Label resolutionLabel;
    private ComboBox<String> bitrateComboBox;
    private Label bitrateLabel;
    private ComboBox<String> fpsComboBox;
    private Label fpsLabel;
    private Button resetSettingsButton;
    private Label settingsStatusLabel;
    private volatile boolean isApplyingSettings = false;

    // 状态颜色（仅用于 Circle 填充）
    private static final Color COLOR_STATUS_CONNECTED = Color.web("#10b981");
    private static final Color COLOR_STATUS_DISCONNECTED = Color.web("#ef4444");
    private static final Color COLOR_STATUS_WAITING = Color.web("#f59e0b");
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Screen Cast Pro - 无线投屏");
        
        // 创建主容器
        mainContainer = createMainContainer();
        
        // 创建场景
        Scene scene = new Scene(mainContainer, 1360, 1060);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // 设置窗口样式
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1360);
        primaryStage.setMinHeight(1060);
        primaryStage.setOnCloseRequest(e -> onClose());
        
        primaryStage.show();
        
        // 添加启动动画
        addStartupAnimation();
        
        // 初始化服务
        initializeServices();
    }
    
    /**
     * 创建主容器 - 浅色渐变背景
     */
    private VBox createMainContainer() {
        VBox container = new VBox(0);
        container.setAlignment(Pos.TOP_CENTER);
        container.getStyleClass().add("main-container");
        
        // 创建内容区域
        VBox content = new VBox(16);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20, 32, 32, 32));
        content.setMaxWidth(1400);

        // 标题区域
        content.getChildren().add(createHeader());

        // 主体内容 - 三列布局
        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.TOP_CENTER);

        // 第一列：设备列表
        VBox leftPanel = createLeftPanel();
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        // 第二列：连接状态和控制
        VBox middlePanel = createMiddlePanel();
        HBox.setHgrow(middlePanel, Priority.ALWAYS);

        // 第三列：视频设置
        VBox rightPanel = createRightPanel();
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        mainContent.getChildren().addAll(leftPanel, middlePanel, rightPanel);
        content.getChildren().add(mainContent);

        // 日志区域
        content.getChildren().add(createLogPanel());

        container.getChildren().add(content);
        
        return container;
    }
    
    /**
     * 创建标题区域 - 紧凑设计
     */
    private VBox createHeader() {
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(8, 0, 8, 0));
        
        // 主标题
        Label title = new Label("Screen Cast Pro");
        title.getStyleClass().add("title-main");
        
        // 副标题
        Label subtitle = new Label("无线投屏到安卓 TV");
        subtitle.getStyleClass().add("title-subtitle");
        
        header.getChildren().addAll(title, subtitle);
        return header;
    }
    
    /**
     * 创建左侧面板 - 设备列表
     */
    private VBox createLeftPanel() {
        VBox panel = createGlassCard();
        panel.setSpacing(16);
        
        // 标题栏
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("📱 可用设备");
        title.getStyleClass().add("panel-title");
        
        // 网络模式标签
        networkModeLabel = new Label("📡 广播模式");
        networkModeLabel.getStyleClass().add("network-mode-label");
        HBox.setHgrow(networkModeLabel, Priority.ALWAYS);
        networkModeLabel.setAlignment(Pos.CENTER_RIGHT);
        
        // 设置按钮
        settingsButton = new Button("⚙");
        settingsButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #64748B;" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 4px 8px;" +
            "-fx-cursor: hand;"
        );
        settingsButton.setOnAction(e -> showNetworkSettings());
        
        // 悬停效果
        settingsButton.setOnMouseEntered(e -> {
            settingsButton.setStyle(
                "-fx-background-color: rgba(99, 102, 241, 0.1);" +
                "-fx-text-fill: #6366F1;" +
                "-fx-font-size: 16px;" +
                "-fx-padding: 4px 8px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 8px;"
            );
        });
        settingsButton.setOnMouseExited(e -> {
            settingsButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #64748B;" +
                "-fx-font-size: 16px;" +
                "-fx-padding: 4px 8px;" +
                "-fx-cursor: hand;"
            );
        });
        
        titleBar.getChildren().addAll(title, networkModeLabel, settingsButton);
        
        // 设备列表
        deviceListView = new ListView<>();
        deviceListView.setPrefHeight(280);
        deviceListView.getStyleClass().add("device-list-view");
        
        // 空列表提示
        Label placeholder = new Label("正在搜索设备...");
        placeholder.getStyleClass().add("list-placeholder");
        deviceListView.setPlaceholder(placeholder);
        
        // 刷新按钮
        refreshButton = createStyledButton("🔄 刷新设备", false);
        refreshButton.setOnAction(e -> refreshDevices());
        
        panel.getChildren().addAll(titleBar, deviceListView, refreshButton);
        return panel;
    }
    
    /**
     * 创建中间面板 - 连接状态和控制
     */
    private VBox createMiddlePanel() {
        VBox panel = createGlassCard();
        panel.setSpacing(24);

        // 连接状态卡片
        VBox statusCard = createStatusCard();

        // 本机信息卡片
        VBox infoCard = createInfoCard();

        // 控制按钮
        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);
        
        startButton = createStyledButton("▶ 开始投屏", false);
        startButton.setDisable(false);
        startButton.setOnAction(e -> startCasting());

        stopButton = createStyledButton("⏹ 停止投屏", true);
        stopButton.setDisable(false); // 断开按钮始终可点击
        stopButton.setOnAction(e -> stopCasting());
        
        buttonBox.getChildren().addAll(startButton, stopButton);

        panel.getChildren().addAll(statusCard, infoCard, buttonBox);
        return panel;
    }

    /**
     * 创建右侧面板 - 视频设置
     */
    private VBox createRightPanel() {
        VBox panel = createGlassCard();
        panel.setSpacing(20);

        // 视频设置卡片
        VBox resolutionCard = createResolutionCard();
        panel.getChildren().addAll(resolutionCard);
        
        return panel;
    }
    
    /**
     * 创建状态卡片
     */
    private VBox createStatusCard() {
        VBox card = new VBox(16);
        
        Label title = new Label("连接状态");
        title.getStyleClass().add("section-title");
        
        // 状态显示
        HBox statusBox = new HBox(12);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        statusIndicator = new Circle(10, COLOR_STATUS_WAITING);
        statusIndicator.getStyleClass().add("status-indicator");
        addPulseAnimation(statusIndicator);
        
        statusLabel = new Label("等待连接...");
        statusLabel.getStyleClass().add("status-label-waiting");
        
        statusBox.getChildren().addAll(statusIndicator, statusLabel);
        
        deviceLabel = new Label("未选择设备");
        deviceLabel.getStyleClass().add("label-muted");
        
        card.getChildren().addAll(title, statusBox, deviceLabel);
        return card;
    }
    
    /**
     * 创建信息卡片
     */
    private VBox createInfoCard() {
        VBox card = new VBox(16);
        
        Label title = new Label("本机信息");
        title.getStyleClass().add("section-title");
        
        // IP 地址
        VBox ipBox = new VBox(6);
        Label ipLabel = new Label("本机 IP 地址");
        ipLabel.getStyleClass().add("label-secondary");
        
        Label ipValue = new Label(getLocalIpAddress());
        ipValue.getStyleClass().add("ip-address-label");
        
        ipBox.getChildren().addAll(ipLabel, ipValue);
        
        // 端口
        VBox portBox = new VBox(6);
        Label portLabel = new Label("服务端口");
        portLabel.getStyleClass().add("label-secondary");
        
        Label portValue = new Label("8888");
        portValue.getStyleClass().add("port-label");
        
        portBox.getChildren().addAll(portLabel, portValue);
        
        card.getChildren().addAll(title, ipBox, portBox);
        return card;
    }

    /**
     * 创建视频设置卡片
     */
    private VBox createResolutionCard() {
        VBox card = new VBox(20);

        // 标题栏
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("📺 视频设置");
        title.getStyleClass().add("section-title");

        // 重置按钮
        resetSettingsButton = new Button("🔄 重置");
        resetSettingsButton.getStyleClass().add("button-secondary-small");
        resetSettingsButton.setOnAction(e -> resetToDefaultSettings());

        HBox.setHgrow(title, Priority.ALWAYS);
        titleBar.getChildren().addAll(title, resetSettingsButton);

        // 分辨率设置
        VBox resolutionBox = createSettingRow(
            "分辨率",
            "根据电视尺寸和网络选择合适的分辨率",
            new String[]{
                "自动 (跟随电脑屏幕)",
                "720p (1280×720)",
                "1080p (1920×1080)",
                "2K (2560×1440)",
                "4K (3840×2160)",
                "8K (7680×4320)"
            },
            "自动 (跟随电脑屏幕)"
        );
        resolutionComboBox = (ComboBox<String>) resolutionBox.getChildren().get(1);
        resolutionLabel = (Label) resolutionBox.getChildren().get(2);

        // 码率设置
        VBox bitrateBox = createSettingRow(
            "码率",
            "码率越高画质越好，但需要更强的网络",
            new String[]{
                "自动 (根据分辨率)",
                "4 Mbps (标准)",
                "8 Mbps (高清)",
                "16 Mbps (超清) ⭐推荐",
                "24 Mbps (2K 画质)",
                "32 Mbps (2K 超清)",
                "50 Mbps (4K 画质)",
                "80 Mbps (4K 超清)",
                "100 Mbps (8K 画质)"
            },
            "16 Mbps (超清) ⭐推荐"
        );
        bitrateComboBox = (ComboBox<String>) bitrateBox.getChildren().get(1);
        bitrateLabel = (Label) bitrateBox.getChildren().get(2);

        // 帧率设置
        VBox fpsBox = createSettingRow(
            "帧率",
            "帧率越高画面越流畅",
            new String[]{
                "24 fps (电影)",
                "30 fps (标准)",
                "60 fps (流畅) ⭐推荐",
                "120 fps (极致)"
            },
            "30 fps (标准)"
        );
        fpsComboBox = (ComboBox<String>) fpsBox.getChildren().get(1);
        fpsLabel = (Label) fpsBox.getChildren().get(2);

        // 监听变化自动生效
        resolutionComboBox.setOnAction(e -> {
            updateSettingsInfo();
            applySettingsImmediately();
        });
        bitrateComboBox.setOnAction(e -> {
            updateSettingsInfo();
            applySettingsImmediately();
        });
        fpsComboBox.setOnAction(e -> {
            updateSettingsInfo();
            applySettingsImmediately();
        });

        // 默认设置提示
        Label defaultTip = new Label("💡 默认方案：电脑分辨率 + 16Mbps + 30fps | 适合 85 寸电视超清播放");
        defaultTip.getStyleClass().add("settings-hint");

        // 投屏中提示
        settingsStatusLabel = new Label("");
        settingsStatusLabel.getStyleClass().add("settings-status-info");
        settingsStatusLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(titleBar, resolutionBox, bitrateBox, fpsBox, defaultTip, settingsStatusLabel);
        return card;
    }

    /**
     * 创建设置行
     */
    private VBox createSettingRow(String label, String description, String[] options, String defaultValue) {
        VBox box = new VBox(10);

        Label titleLabel = new Label(label);
        titleLabel.getStyleClass().add("setting-title");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(options);
        comboBox.setValue(defaultValue);
        comboBox.setPrefWidth(320);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.getStyleClass().add("setting-combobox");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("setting-description");

        box.getChildren().addAll(titleLabel, comboBox, descLabel);
        return box;
    }

    /**
     * 创建日志面板
     */
    private VBox createLogPanel() {
        VBox panel = createGlassCard();
        panel.setSpacing(12);

        Label title = new Label("📋 运行日志");
        title.getStyleClass().add("panel-title");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setMinHeight(180);
        logArea.setWrapText(true);
        logArea.getStyleClass().add("log-text-area");

        panel.getChildren().addAll(title, logArea);
        return panel;
    }
    
    /**
     * 创建玻璃卡片
     */
    private VBox createGlassCard() {
        VBox card = new VBox();
        card.getStyleClass().add("glass-card");
        return card;
    }
    
    /**
     * 添加脉冲动画到状态指示器
     */
    private void addPulseAnimation(Circle circle) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), circle);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }
    
    /**
     * 创建样式化按钮
     */
    private Button createStyledButton(String text, boolean isDanger) {
        Button button = new Button(text);
        button.setPrefHeight(48);
        button.setPrefWidth(140);
        
        String baseColor = isDanger ? "#dc2626" : "#3b82f6";
        String hoverColor = isDanger ? "#ef4444" : "#60a5fa";
        
        button.setStyle(
            "-fx-background-color: " + baseColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 12px;" +
            "-fx-cursor: hand;"
        );
        
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: " + hoverColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 12px;" +
                "-fx-cursor: hand;"
            );
            button.setScaleX(1.02);
            button.setScaleY(1.02);
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + baseColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 12px;" +
                "-fx-cursor: hand;"
            );
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        
        return button;
    }
    
    /**
     * 获取本地 IP 地址
     */
    private String getLocalIpAddress() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (Exception e) {
            return "192.168.x.x";
        }
    }
    
    /**
     * 发现新设备
     */
    private void onDeviceFound(String deviceName, String ipAddress) {
        Platform.runLater(() -> {
            String item = String.format("📺 %s\n   %s", deviceName, ipAddress);
            if (!deviceListView.getItems().contains(item)) {
                deviceListView.getItems().add(item);
                log("📱 发现设备：" + deviceName + " @ " + ipAddress, "success");
            }
        });
    }
    
    /**
     * 初始化服务
     */
    private void initializeServices() {
        log("🚀 正在初始化服务...", "info");
        
        appConfig = new AppConfig();
        castClient = new CastClient();
        screenCapture = new ScreenCapture();
        deviceDiscovery = new DeviceDiscovery();
        
        // 应用网段配置
        String segment = appConfig.getNetworkSegment();
        if (!segment.isEmpty()) {
            deviceDiscovery.setNetworkSegment(segment);
            log("🌐 使用指定网段：" + segment + ".x", "info");
        } else {
            log("📡 使用广播模式发现设备", "info");
        }
        
        deviceDiscovery.setOnDeviceFound(this::onDeviceFound);
        deviceDiscovery.start();
        
        // 连接回调
        castClient.setOnConnected(() -> Platform.runLater(() -> {
            updateStatus("已连接", "connected");
            startButton.setDisable(true);
            log("✅ 连接成功！", "success");
        }));
        castClient.setOnDisconnected(() -> Platform.runLater(() -> {
            System.out.println("[DEBUG] onDisconnected 回调被触发");
            log("❌ 连接已断开", "warning");
            // 连接断开时停止屏幕捕获和清理资源（会更新UI状态）
            stopCastingInternal("已断开");
        }));
        castClient.setOnError(msg -> Platform.runLater(() -> {
            System.out.println("[DEBUG] onError 回调被触发: " + msg);
            log("❌ 错误：" + msg, "error");
            // 连接出错时停止屏幕捕获和清理资源
            stopCastingInternal("连接错误");
        }));
        
        // 屏幕捕获回调
        screenCapture.setOnFrameCaptured(frame -> {
            if (castClient.isConnected()) {
                castClient.sendFrame(frame);
            }
        });
        
        updateNetworkModeLabel();
        log("✨ 服务初始化完成", "success");
        log("📍 本机 IP: " + getLocalIpAddress(), "info");
    }
    
    /**
     * 更新网络模式标签
     */
    private void updateNetworkModeLabel() {
        Platform.runLater(() -> {
            if (networkModeLabel != null) {
                String segment = appConfig.getNetworkSegment();
                if (segment.isEmpty()) {
                    networkModeLabel.setText("📡 广播模式");
                    networkModeLabel.getStyleClass().setAll("network-mode-label");
                } else {
                    networkModeLabel.setText("🌐 网段：" + segment + ".x");
                    networkModeLabel.getStyleClass().setAll("network-mode-label-active");
                }
            }
        });
    }
    
    /**
     * 刷新设备列表
     */
    private void refreshDevices() {
        deviceListView.getItems().clear();
        deviceListView.setPlaceholder(new Label("正在搜索设备..."));
        deviceDiscovery.discover();
        log("🔍 正在搜索设备...", "info");
    }
    
    /**
     * 更新状态显示
     */
    private void updateStatus(String status, String type) {
        Platform.runLater(() -> {
            statusLabel.setText(status);
            
            switch (type) {
                case "connected":
                    statusIndicator.setFill(COLOR_STATUS_CONNECTED);
                    statusLabel.getStyleClass().setAll("status-label-connected");
                    break;
                case "disconnected":
                case "error":
                    statusIndicator.setFill(COLOR_STATUS_DISCONNECTED);
                    statusLabel.getStyleClass().setAll("status-label-disconnected");
                    break;
                default:
                    statusIndicator.setFill(COLOR_STATUS_WAITING);
                    statusLabel.getStyleClass().setAll("status-label-waiting");
            }
        });
    }
    
    /**
     * 开始投屏
     */
    private void startCasting() {
        String selected = deviceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("请先选择一个设备");
            return;
        }
        
        // 解析 IP 地址
        String ip = selected.substring(selected.indexOf("   ") + 3).trim();
        String deviceName = selected.substring(2, selected.indexOf("\n")).trim();
        
        try {
            log("🔗 正在连接到：" + ip, "info");
            
            // 获取视频设置
            String selectedResolution = resolutionComboBox.getValue();
            String selectedBitrate = bitrateComboBox.getValue();
            String selectedFps = fpsComboBox.getValue();
            
            // 解析分辨率
            int targetWidth = 0, targetHeight = 0;
            boolean useNativeResolution = false;
            
            switch (selectedResolution) {
                case "自动 (跟随电脑屏幕)":
                    useNativeResolution = true;
                    break;
                case "720p (1280×720)":
                    targetWidth = 1280;
                    targetHeight = 720;
                    break;
                case "1080p (1920×1080)":
                    targetWidth = 1920;
                    targetHeight = 1080;
                    break;
                case "2K (2560×1440)":
                    targetWidth = 2560;
                    targetHeight = 1440;
                    break;
                case "4K (3840×2160)":
                    targetWidth = 3840;
                    targetHeight = 2160;
                    break;
                case "8K (7680×4320)":
                    targetWidth = 7680;
                    targetHeight = 4320;
                    break;
            }
            
            // 解析码率
            int bitrate = 16000000;
            if (selectedBitrate.contains("4")) bitrate = 4000000;
            else if (selectedBitrate.contains("8")) bitrate = 8000000;
            else if (selectedBitrate.contains("16")) bitrate = 16000000;
            else if (selectedBitrate.contains("24")) bitrate = 24000000;
            else if (selectedBitrate.contains("32")) bitrate = 32000000;
            else if (selectedBitrate.contains("50")) bitrate = 50000000;
            else if (selectedBitrate.contains("80")) bitrate = 80000000;
            else if (selectedBitrate.contains("100")) bitrate = 100000000;
            
            // 解析帧率
            int fps = 30;
            if (selectedFps.contains("24")) fps = 24;
            else if (selectedFps.contains("30")) fps = 30;
            else if (selectedFps.contains("60")) fps = 60;
            else if (selectedFps.contains("120")) fps = 120;
            
            log("⚙️ 视频设置：分辨率=" + selectedResolution + ", 码率=" + (bitrate/1000000) + "Mbps, 帧率=" + fps + "fps", "info");
            
            // 配置屏幕捕获
            screenCapture.setConfig(appConfig);
            screenCapture.setUseNativeResolution(useNativeResolution);
            screenCapture.setBitrate(bitrate);
            screenCapture.setFrameRate(fps);
            
            if (!useNativeResolution) {
                screenCapture.setTargetResolution(targetWidth, targetHeight);
                log("📺 设置投屏分辨率：" + targetWidth + "x" + targetHeight, "info");
            }
            
            // 先启动屏幕捕获，获取实际分辨率
            screenCapture.start();
            
            // 等待一小段时间让捕获器初始化
            Thread.sleep(500);
            
            // 获取实际编码分辨率
            String resolution = screenCapture.getCurrentResolution();
            log("📺 实际投屏分辨率：" + resolution, "info");
            
            // 解析分辨率
            String[] parts = resolution.split("x");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            
            // 设置客户端视频参数
            castClient.setVideoParams(width, height, fps);
            
            // 设置连接成功回调（仅用于更新 UI）
            castClient.setOnConnected(() -> {
                Platform.runLater(() -> {
                    deviceLabel.setText(deviceName + " (" + resolution + ")");
                    updateStatus("已连接", "connected");
                    log("▶️ 投屏已开始", "success");

                    // 连接成功后停止扫描设备列表
                    if (deviceDiscovery != null) {
                        deviceDiscovery.stop();
                        log("🔍 已停止扫描设备", "info");
                    }
                });
            });

            // 异步连接
            try {
                castClient.connect(ip, 8888);
                log("⏳ 正在连接...", "info");
                // 连接开始后禁用开始按钮，防止重复点击
                startButton.setDisable(true);
            } catch (Exception e) {
                log("❌ 连接失败：" + e.getMessage(), "error");
                showAlert("连接失败：" + e.getMessage());
                startButton.setDisable(false);
                screenCapture.stop();
            }
            
        } catch (Exception e) {
            log("❌ 连接失败：" + e.getMessage(), "error");
            showAlert("连接失败：" + e.getMessage());
        }
    }
    
    /**
     * 停止投屏 - 无论当前连接状态如何都可以调用
     */
    private void stopCasting() {
        stopCastingInternal("等待连接...");
        log("⏹️ 投屏已停止", "warning");
    }

    /**
     * 内部停止方法 - 执行实际的停止逻辑
     * @param statusText 状态文本（"已断开"或"等待连接..."）
     */
    private void stopCastingInternal(String statusText) {
        log("⏹️ 正在停止投屏...", "info");
        System.out.println("[DEBUG] stopCastingInternal 被调用，状态: " + statusText);

        // 停止屏幕捕获
        try {
            System.out.println("[DEBUG] 正在停止屏幕捕获...");
            screenCapture.stop();
            System.out.println("[DEBUG] 屏幕捕获已停止");
        } catch (Exception e) {
            System.err.println("[DEBUG] 停止屏幕捕获时出错: " + e.getMessage());
            e.printStackTrace();
        }

        // 断开连接（无论是否已连接都可以安全调用）
        try {
            System.out.println("[DEBUG] 正在断开连接...");
            castClient.disconnect();
            System.out.println("[DEBUG] 连接已断开");
        } catch (Exception e) {
            System.err.println("[DEBUG] 断开连接时出错: " + e.getMessage());
            e.printStackTrace();
        }

        // 重置UI状态
        System.out.println("[DEBUG] 正在重置UI状态...");
        startButton.setDisable(false);
        deviceLabel.setText("未选择设备");

        // 根据传入的状态文本更新状态
        if ("已断开".equals(statusText)) {
            updateStatus("已断开", "disconnected");
        } else if ("连接错误".equals(statusText)) {
            updateStatus("连接错误", "error");
        } else {
            updateStatus("等待连接...", "waiting");
        }
        System.out.println("[DEBUG] UI状态已重置为: " + statusText);

        // 停止投屏后重新启动设备发现
        if (deviceDiscovery != null && !deviceDiscovery.isRunning()) {
            deviceDiscovery.start();
            log("🔍 已重新开始扫描设备", "info");
        }

        // 清空设置状态
        if (settingsStatusLabel != null) {
            settingsStatusLabel.setText("");
        }
        System.out.println("[DEBUG] stopCastingInternal 执行完成");
    }
    
    /**
     * 显示网络设置对话框
     */
    private void showNetworkSettings() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("网络设置");
        dialog.setHeaderText("配置设备发现网段");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().addAll("network-dialog-pane", "dialog-pane");
        
        // 确保对话框使用应用的样式表
        Scene scene = dialogPane.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        }
        
        ToggleGroup group = new ToggleGroup();
        
        RadioButton broadcastRadio = new RadioButton("广播模式（自动发现所有网段）");
        broadcastRadio.setToggleGroup(group);
        broadcastRadio.setSelected(appConfig.isUseBroadcast());
        broadcastRadio.getStyleClass().add("radio-button");
        
        RadioButton segmentRadio = new RadioButton("指定网段");
        segmentRadio.setToggleGroup(group);
        segmentRadio.setSelected(!appConfig.isUseBroadcast());
        segmentRadio.getStyleClass().add("radio-button");
        
        TextField segmentField = new TextField(appConfig.getNetworkSegment());
        segmentField.setPromptText("如：192.168.1");
        segmentField.setDisable(appConfig.isUseBroadcast());
        segmentField.getStyleClass().add("text-field");
        
        ComboBox<String> suggestedCombo = new ComboBox<>();
        suggestedCombo.setPromptText("选择建议网段");
        suggestedCombo.setDisable(appConfig.isUseBroadcast());
        suggestedCombo.getStyleClass().add("setting-combobox");
        
        List<String> suggestedSegments = DeviceDiscovery.getSuggestedSegments();
        suggestedCombo.getItems().addAll(suggestedSegments);
        suggestedCombo.setOnAction(e -> segmentField.setText(suggestedCombo.getValue()));
        
        broadcastRadio.setOnAction(e -> {
            segmentField.setDisable(true);
            suggestedCombo.setDisable(true);
        });
        segmentRadio.setOnAction(e -> {
            segmentField.setDisable(false);
            suggestedCombo.setDisable(false);
        });
        
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.getStyleClass().addAll("network-dialog-content", "content");
        
        Label segmentLabel = new Label("网段地址（如：192.168.1）:");
        segmentLabel.getStyleClass().addAll("setting-title", "dialog-label");
        
        Label suggestedLabel = new Label("或选择建议网段:");
        suggestedLabel.getStyleClass().addAll("setting-title", "dialog-label");
        
        content.getChildren().addAll(
            broadcastRadio,
            segmentRadio,
            segmentLabel,
            segmentField,
            suggestedLabel,
            suggestedCombo
        );
        
        dialogPane.setContent(content);
        
        ButtonType saveButton = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButton, cancelButton);
        
        // 设置按钮样式
        dialogPane.lookupButton(saveButton).getStyleClass().addAll("button-primary");
        dialogPane.lookupButton(cancelButton).getStyleClass().addAll("button-secondary");
        
        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                boolean useBroadcast = broadcastRadio.isSelected();
                String segment = segmentField.getText().trim();
                appConfig.setUseBroadcast(useBroadcast);
                if (!useBroadcast && !segment.isEmpty()) {
                    appConfig.setNetworkSegment(segment);
                }
                updateNetworkModeLabel();
                log("✅ 网络设置已保存", "success");
                return "saved";
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    /**
     * 重置为默认设置
     */
    private void resetToDefaultSettings() {
        resolutionComboBox.setValue("自动 (跟随电脑屏幕)");
        bitrateComboBox.setValue("16 Mbps (超清) ⭐推荐");
        fpsComboBox.setValue("30 fps (标准)");
        log("🔄 已重置为默认设置", "info");
    }
    
    /**
     * 更新设置信息显示
     */
    private void updateSettingsInfo() {
        String resolution = resolutionComboBox.getValue();
        String bitrate = bitrateComboBox.getValue();
        String fps = fpsComboBox.getValue();
        
        // 根据选择更新提示
        if (resolution.contains("4K") && !bitrate.contains("50") && !bitrate.contains("80")) {
            resolutionLabel.setText("⚠️ 4K 分辨率建议使用 50Mbps 以上码率");
            resolutionLabel.getStyleClass().setAll("setting-description-warning");
        } else if (resolution.contains("8K") && !bitrate.contains("100")) {
            resolutionLabel.setText("⚠️ 8K 分辨率建议使用 100Mbps 码率");
            resolutionLabel.getStyleClass().setAll("setting-description-error");
        } else {
            resolutionLabel.setText("✅ 当前设置适合大多数场景");
            resolutionLabel.getStyleClass().setAll("setting-description-success");
        }
    }
    
    /**
     * 立即应用设置
     */
    private void applySettingsImmediately() {
        if (isApplyingSettings) return;
        if (!castClient.isConnected()) {
            settingsStatusLabel.setText("⚠️ 请先连接设备");
            settingsStatusLabel.getStyleClass().setAll("settings-status-warning");
            return;
        }
        
        isApplyingSettings = true;
        
        String finalSelectedResolution = resolutionComboBox.getValue();
        String finalSelectedBitrate = bitrateComboBox.getValue();
        String finalSelectedFps = fpsComboBox.getValue();
        
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    settingsStatusLabel.setText("⏳ 正在应用设置...");
                    settingsStatusLabel.getStyleClass().setAll("settings-status-processing");
                });
                
                int finalBitrate = finalSelectedBitrate.contains("4 Mbps") ? 4000000 :
                                   finalSelectedBitrate.contains("8 Mbps") ? 8000000 :
                                   finalSelectedBitrate.contains("16 Mbps") ? 16000000 :
                                   finalSelectedBitrate.contains("24 Mbps") ? 24000000 :
                                   finalSelectedBitrate.contains("32 Mbps") ? 32000000 :
                                   finalSelectedBitrate.contains("50 Mbps") ? 50000000 :
                                   finalSelectedBitrate.contains("80 Mbps") ? 80000000 : 100000000;
                int finalFps = finalSelectedFps.contains("24 fps") ? 24 :
                              finalSelectedFps.contains("30 fps") ? 30 :
                              finalSelectedFps.contains("60 fps") ? 60 : 120;
                
                boolean finalUseNativeResolution = finalSelectedResolution.contains("自动");
                int finalTargetWidth = 1920;
                int finalTargetHeight = 1080;
                
                if (finalSelectedResolution.contains("720p")) { finalTargetWidth = 1280; finalTargetHeight = 720; }
                else if (finalSelectedResolution.contains("1080p")) { finalTargetWidth = 1920; finalTargetHeight = 1080; }
                else if (finalSelectedResolution.contains("2K")) { finalTargetWidth = 2560; finalTargetHeight = 1440; }
                else if (finalSelectedResolution.contains("4K")) { finalTargetWidth = 3840; finalTargetHeight = 2160; }
                else if (finalSelectedResolution.contains("8K")) { finalTargetWidth = 7680; finalTargetHeight = 4320; }
                
                if (screenCapture != null && screenCapture.isRunning()) {
                    screenCapture.stop();
                    // 等待更长时间确保 FFmpeg 资源完全释放
                    Thread.sleep(1500);
                    
                    screenCapture.setUseNativeResolution(finalUseNativeResolution);
                    screenCapture.setBitrate(finalBitrate);
                    screenCapture.setFrameRate(finalFps);
                    if (!finalUseNativeResolution) {
                        screenCapture.setTargetResolution(finalTargetWidth, finalTargetHeight);
                    }
                    
                    screenCapture.start();
                    // 等待捕获初始化完成
                    Thread.sleep(800);
                    
                    String newResolution = screenCapture.getCurrentResolution();
                    String[] parts = newResolution.split("x");
                    int width = Integer.parseInt(parts[0]);
                    int height = Integer.parseInt(parts[1]);
                    
                    castClient.setVideoParams(width, height, finalFps);
                    
                    Platform.runLater(() -> {
                        String deviceName = deviceLabel.getText();
                        if (deviceName.contains(" ")) {
                            deviceName = deviceName.substring(0, deviceName.indexOf(" "));
                        }
                        deviceLabel.setText(deviceName + " (" + newResolution + ")");
                        settingsStatusLabel.setText("✅ 设置已生效：" + newResolution + " @ " + (finalBitrate/1000000) + "Mbps, " + finalFps + "fps");
                        settingsStatusLabel.getStyleClass().setAll("settings-status-success");
                        log("✅ 视频设置已更新：" + finalSelectedResolution + ", " + (finalBitrate/1000000) + "Mbps, " + finalFps + "fps", "success");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    settingsStatusLabel.setText("❌ 应用失败：" + e.getMessage());
                    settingsStatusLabel.getStyleClass().setAll("settings-status-error");
                    log("❌ 应用设置失败：" + e.getMessage(), "error");
                });
            } finally {
                isApplyingSettings = false;
            }
        }).start();
    }
    
    /**
     * 显示提示
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 设置对话框样式
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #F5F3FF;" +
            "-fx-text-fill: #1E293B;" +
            "-fx-font-size: 14px;"
        );
        
        alert.showAndWait();
    }
    
    /**
     * 添加启动动画
     */
    private void addStartupAnimation() {
        // 主容器淡入和上滑动画
        mainContainer.setOpacity(0);
        mainContainer.setTranslateY(20);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), mainContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), mainContainer);
        slideUp.setFromY(30);
        slideUp.setToY(0);
        
        ParallelTransition animation = new ParallelTransition(fadeIn, slideUp);
        animation.play();
    }
    
    /**
     * 记录日志
     */
    private void log(String message, String type) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().withNano(0).toString();
            String prefix;
            
            switch (type) {
                case "success":
                    prefix = "✅";
                    break;
                case "error":
                    prefix = "❌";
                    break;
                case "warning":
                    prefix = "⚠️";
                    break;
                default:
                    prefix = "ℹ️";
            }
            
            logArea.appendText(String.format("[%s] %s %s\n", timestamp, prefix, message));
            // 自动滚动到底部
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    /**
     * 窗口关闭处理
     */
    private void onClose() {
        try {
            log("🚪 正在关闭应用...", "info");
            
            // 1. 先停止屏幕捕获（等待线程退出）
            if (screenCapture != null) {
                log("⏹️ 正在停止屏幕捕获...", "info");
                screenCapture.stop();
                log("✅ 屏幕捕获已停止", "info");
            }
            
            // 2. 断开投屏连接
            if (castClient != null) {
                log("🔌 正在断开连接...", "info");
                castClient.disconnect();
                log("✅ 连接已断开", "info");
            }
            
            // 3. 停止设备发现服务
            if (deviceDiscovery != null) {
                log("🔍 正在停止设备发现...", "info");
                deviceDiscovery.stop();
                log("✅ 设备发现已停止", "info");
            }
            
            // 4. 等待所有资源释放
            Thread.sleep(500);
            
            log("✨ 所有资源已释放", "success");
            
            // 5. 关闭 JavaFX 平台
            Platform.exit();
            
            // 6. 强制终止所有线程并退出 JVM
            System.exit(0);
        } catch (Exception e) {
            System.err.println("关闭时出错：" + e.getMessage());
            e.printStackTrace();
            // 即使出错也要强制退出
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
