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
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.InetAddress;
import java.util.List;

/**
 * 主窗口界面 - 现代化深色玻璃拟态设计
 * 
 * 设计风格: Dark Glassmorphism
 * 主色调: 深蓝紫渐变 + 青色点缀
 */
public class MainWindow extends Application {
    
    private CastClient castClient;
    private ScreenCapture screenCapture;
    private DeviceDiscovery deviceDiscovery;
    private AppConfig appConfig;
    
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
    private Text titleText;
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

    // 颜色定义
    private static final Color COLOR_BG_DARK = Color.web("#0a0a0f");
    private static final Color COLOR_BG_CARD = Color.web("#14141e", 0.6);
    private static final Color COLOR_ACCENT_CYAN = Color.web("#00d4ff");
    private static final Color COLOR_ACCENT_PURPLE = Color.web("#a855f7");
    private static final Color COLOR_TEXT_PRIMARY = Color.WHITE;
    private static final Color COLOR_TEXT_SECONDARY = Color.web("#ffffff", 0.7);
    private static final Color COLOR_TEXT_MUTED = Color.web("#ffffff", 0.5);
    private static final Color COLOR_STATUS_CONNECTED = Color.web("#10b981");
    private static final Color COLOR_STATUS_DISCONNECTED = Color.web("#ef4444");
    private static final Color COLOR_STATUS_WAITING = Color.web("#f59e0b");
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Screen Cast Pro");
        
        // 创建主容器
        mainContainer = createMainContainer();
        
        // 创建场景 - 增加高度确保内容完整显示
        Scene scene = new Scene(mainContainer, 1200, 900);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // 设置窗口样式
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(1000);
        primaryStage.setOnCloseRequest(e -> onClose());
        
        // 添加启动动画
        addStartupAnimation();
        
        primaryStage.show();
        
        // 初始化服务
        initializeServices();
    }
    
    /**
     * 创建主容器 - 带有渐变背景
     */
    private VBox createMainContainer() {
        VBox container = new VBox(0);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(0));
        
        // 创建渐变背景
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, null,
            new Stop(0, COLOR_BG_DARK),
            new Stop(0.5, Color.web("#1a1a2e")),
            new Stop(1, Color.web("#16213e"))
        );
        container.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        
        // 创建内容区域
        VBox content = new VBox(24);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(40, 48, 48, 48));
        content.setMaxWidth(1400);

        // 标题区域
        content.getChildren().add(createHeader());

        // 主体内容 - 三列分栏
        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.TOP_CENTER);

        // 第一列：设备列表
        VBox leftPanel = createLeftPanel();
        leftPanel.setPrefWidth(320);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        // 第二列：连接状态和本机信息
        VBox middlePanel = createMiddlePanel();
        middlePanel.setPrefWidth(320);
        HBox.setHgrow(middlePanel, Priority.ALWAYS);

        // 第三列：视频设置
        VBox rightPanel = createRightPanel();
        rightPanel.setPrefWidth(360);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        mainContent.getChildren().addAll(leftPanel, middlePanel, rightPanel);
        content.getChildren().add(mainContent);

        // 日志区域
        content.getChildren().add(createLogPanel());

        container.getChildren().add(content);
        
        return container;
    }
    
    /**
     * 创建标题区域
     */
    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        
        // 渐变标题
        titleText = new Text("Screen Cast Pro");
        titleText.setFont(Font.font("Inter", FontWeight.BOLD, 36));
        
        LinearGradient titleGradient = new LinearGradient(
            0, 0, 1, 0, true, null,
            new Stop(0, COLOR_ACCENT_CYAN),
            new Stop(1, COLOR_ACCENT_PURPLE)
        );
        titleText.setFill(titleGradient);
        
        // 添加发光效果
        DropShadow glow = new DropShadow();
        glow.setColor(COLOR_ACCENT_CYAN);
        glow.setRadius(20);
        glow.setSpread(0.2);
        titleText.setEffect(glow);
        
        // 副标题
        Label subtitle = new Label("无线投屏到安卓TV");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.5);");
        
        header.getChildren().addAll(titleText, subtitle);
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
        
        // 设备图标
        Circle icon = new Circle(6, COLOR_ACCENT_CYAN);
        
        Label title = new Label("可用设备");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: white;");
        
        // 网络模式标签
        networkModeLabel = new Label("📡 广播模式");
        networkModeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.4);");
        
        HBox.setHgrow(networkModeLabel, Priority.ALWAYS);
        networkModeLabel.setAlignment(Pos.CENTER_RIGHT);
        
        // 设置按钮
        settingsButton = new Button("⚙");
        settingsButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: rgba(255,255,255,0.6);" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 4px 8px;" +
            "-fx-cursor: hand;"
        );
        settingsButton.setOnAction(e -> showNetworkSettings());
        
        // 悬停效果
        settingsButton.setOnMouseEntered(e -> {
            settingsButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-padding: 4px 8px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 8px;"
            );
        });
        settingsButton.setOnMouseExited(e -> {
            settingsButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: rgba(255,255,255,0.6);" +
                "-fx-font-size: 16px;" +
                "-fx-padding: 4px 8px;" +
                "-fx-cursor: hand;"
            );
        });
        
        titleBar.getChildren().addAll(icon, title, networkModeLabel, settingsButton);
        
        // 设备列表
        deviceListView = new ListView<>();
        deviceListView.setPrefHeight(280);
        deviceListView.getStyleClass().add("device-list-view");
        
        // 设置空列表提示样式
        Label placeholderLabel = new Label("正在搜索设备...");
        placeholderLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 14px;");
        deviceListView.setPlaceholder(placeholderLabel);
        
        // 刷新按钮
        refreshButton = createStyledButton("🔄 刷新设备", false);
        refreshButton.setOnAction(e -> refreshDevices());
        
        panel.getChildren().addAll(titleBar, deviceListView, refreshButton);
        return panel;
    }
    
    /**
     * 创建中间面板 - 连接状态和本机信息
     */
    private VBox createMiddlePanel() {
        VBox panel = createGlassCard();
        panel.setSpacing(20);

        // 连接状态卡片
        VBox statusCard = createStatusCard();

        // 本机信息
        VBox infoCard = createInfoCard();

        // 控制按钮
        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);

        startButton = createStyledButton("▶ 开始投屏", false);
        startButton.setOnAction(e -> startCasting());

        stopButton = createStyledButton("⏹ 停止投屏", true);
        stopButton.setDisable(true);
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
        card.setStyle(
            "-fx-background-color: rgba(15,15,25,0.6);" +
            "-fx-background-radius: 16px;" +
            "-fx-padding: 20px;"
        );
        
        Label title = new Label("连接状态");
        title.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.5);");
        
        // 状态显示
        HBox statusBox = new HBox(12);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        statusIndicator = new Circle(8, COLOR_STATUS_WAITING);
        addPulseAnimation(statusIndicator);
        
        statusLabel = new Label("等待连接...");
        statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #f59e0b;");
        
        statusBox.getChildren().addAll(statusIndicator, statusLabel);
        
        deviceLabel = new Label("未选择设备");
        deviceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.5);");
        
        card.getChildren().addAll(title, statusBox, deviceLabel);
        return card;
    }
    
    /**
     * 创建信息卡片
     */
    private VBox createInfoCard() {
        VBox card = new VBox(12);
        card.setStyle(
            "-fx-background-color: rgba(15,15,25,0.6);" +
            "-fx-background-radius: 16px;" +
            "-fx-padding: 20px;"
        );
        
        Label title = new Label("本机信息");
        title.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.5);");
        
        // IP地址
        VBox ipBox = new VBox(4);
        Label ipLabel = new Label("本机IP地址");
        ipLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.4);");
        
        Label ipValue = new Label(getLocalIpAddress());
        ipValue.setStyle(
            "-fx-font-family: 'JetBrains Mono', monospace;" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #00d4ff;"
        );
        ipBox.getChildren().addAll(ipLabel, ipValue);
        
        // 端口
        VBox portBox = new VBox(4);
        Label portLabel = new Label("服务端口");
        portLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.4);");
        
        Label portValue = new Label("8888");
        portValue.setStyle(
            "-fx-font-family: 'JetBrains Mono', monospace;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: 500;" +
            "-fx-text-fill: white;"
        );
        portBox.getChildren().addAll(portLabel, portValue);
        
        card.getChildren().addAll(title, ipBox, portBox);
        return card;
    }

    /**
     * 创建视频设置卡片 - 包含分辨率、码率、帧率
     */
    private VBox createResolutionCard() {
        VBox card = new VBox(20);
        card.setStyle(
            "-fx-background-color: rgba(15,15,25,0.6);" +
            "-fx-background-radius: 16px;" +
            "-fx-padding: 20px;"
        );

        // 标题栏
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("📺 视频设置");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: white;");

        // 重置按钮
        resetSettingsButton = new Button("🔄 重置默认");
        resetSettingsButton.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: rgba(255,255,255,0.7);" +
            "-fx-font-size: 12px;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 6px 12px;"
        );
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
                "24 Mbps (2K画质)",
                "32 Mbps (2K超清)",
                "50 Mbps (4K画质)",
                "80 Mbps (4K超清)",
                "100 Mbps (8K画质)"
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
        Label defaultTip = new Label("💡 默认方案: 电脑分辨率 + 16Mbps + 30fps | 适合85寸电视超清播放");
        defaultTip.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(0,212,255,0.7); -fx-font-style: italic;");

        // 投屏中提示
        settingsStatusLabel = new Label("");
        settingsStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.5);");
        settingsStatusLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(titleBar, resolutionBox, bitrateBox, fpsBox, defaultTip, settingsStatusLabel);
        return card;
    }

    /**
     * 创建设置行
     */
    private VBox createSettingRow(String label, String description, String[] options, String defaultValue) {
        VBox box = new VBox(8);

        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: rgba(255,255,255,0.8);");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(options);
        comboBox.setValue(defaultValue);
        comboBox.setPrefWidth(320);
        // 设置ComboBox样式 - 文字颜色通过CSS类设置
        comboBox.setStyle(
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 8px;"
        );
        comboBox.getStyleClass().add("white-text-combobox");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.4);");

        box.getChildren().addAll(titleLabel, comboBox, descLabel);
        return box;
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
            resolutionLabel.setText("⚠️ 4K分辨率建议使用50Mbps以上码率");
            resolutionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b;");
        } else if (resolution.contains("8K") && !bitrate.contains("100")) {
            resolutionLabel.setText("⚠️ 8K分辨率建议使用100Mbps码率");
            resolutionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444;");
        } else {
            resolutionLabel.setText("✅ 当前设置适合大多数场景");
            resolutionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #10b981;");
        }
    }

    /**
     * 重置为默认设置
     */
    private void resetToDefaultSettings() {
        resolutionComboBox.setValue("自动 (跟随电脑屏幕)");
        bitrateComboBox.setValue("16 Mbps (超清) ⭐推荐");
        fpsComboBox.setValue("30 fps (标准)");
        updateSettingsInfo();
        log("🔄 已重置为默认设置: 电脑分辨率 + 16Mbps + 30fps", "info");
    }

    /**
     * 立即应用视频设置（无需断开重连）
     */
    private void applySettingsImmediately() {
        if (!castClient.isConnected()) {
            settingsStatusLabel.setText("⚠️ 请先连接设备");
            settingsStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f59e0b;");
            return;
        }

        // 防止频繁切换导致崩溃
        if (isApplyingSettings) {
            return;
        }
        isApplyingSettings = true;

        // 获取当前设置（在主线程中获取）
        String selectedResolution = resolutionComboBox.getValue();
        String selectedBitrate = bitrateComboBox.getValue();
        String selectedFps = fpsComboBox.getValue();

        // 解析分辨率
        boolean useNativeResolution = selectedResolution.contains("自动");
        int targetWidth = 0, targetHeight = 0;
        if (!useNativeResolution) {
            if (selectedResolution.contains("720")) { targetWidth = 1280; targetHeight = 720; }
            else if (selectedResolution.contains("1080")) { targetWidth = 1920; targetHeight = 1080; }
            else if (selectedResolution.contains("2K")) { targetWidth = 2560; targetHeight = 1440; }
            else if (selectedResolution.contains("4K")) { targetWidth = 3840; targetHeight = 2160; }
            else if (selectedResolution.contains("8K")) { targetWidth = 7680; targetHeight = 4320; }
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

        // 在后台线程中执行设置更新
        final int finalBitrate = bitrate;
        final int finalFps = fps;
        final boolean finalUseNativeResolution = useNativeResolution;
        final int finalTargetWidth = targetWidth;
        final int finalTargetHeight = targetHeight;
        final String finalSelectedResolution = selectedResolution;

        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    settingsStatusLabel.setText("⏳ 正在应用设置...");
                    settingsStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00d4ff;");
                });

                // 停止当前捕获
                screenCapture.stop();

                // 等待更长时间确保FFmpeg资源完全释放
                Thread.sleep(1000);

                // 应用新设置
                screenCapture.setUseNativeResolution(finalUseNativeResolution);
                screenCapture.setBitrate(finalBitrate);
                screenCapture.setFrameRate(finalFps);
                if (!finalUseNativeResolution) {
                    screenCapture.setTargetResolution(finalTargetWidth, finalTargetHeight);
                }

                // 重新启动捕获
                screenCapture.start();

                // 等待捕获初始化完成
                Thread.sleep(500);

                // 获取新分辨率
                String newResolution = screenCapture.getCurrentResolution();
                String[] parts = newResolution.split("x");
                int width = Integer.parseInt(parts[0]);
                int height = Integer.parseInt(parts[1]);

                // 发送新的视频参数到TV
                castClient.setVideoParams(width, height, finalFps);

                // 更新UI
                Platform.runLater(() -> {
                    deviceLabel.setText(deviceLabel.getText().split(" ")[0] + " (" + newResolution + ")");
                    settingsStatusLabel.setText("✅ 设置已生效: " + newResolution + " @ " + (finalBitrate/1000000) + "Mbps, " + finalFps + "fps");
                    settingsStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981;");
                    log("✅ 视频设置已更新: " + finalSelectedResolution + ", " + (finalBitrate/1000000) + "Mbps, " + finalFps + "fps", "success");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    settingsStatusLabel.setText("❌ 应用失败: " + e.getMessage());
                    settingsStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
                    log("❌ 应用设置失败: " + e.getMessage(), "error");
                });
            } finally {
                isApplyingSettings = false;
            }
        }).start();
    }

    /**
     * 创建日志面板
     */
    private VBox createLogPanel() {
        VBox panel = createGlassCard();
        panel.setSpacing(12);

        Label title = new Label("📋 运行日志");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: white;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setMinHeight(180);
        logArea.setWrapText(true);
        
        // 使用CSS类来设置样式，确保背景色和文字颜色正确
        logArea.getStyleClass().add("log-text-area");
        
        // 同时设置内联样式作为后备
        logArea.setStyle(
            "-fx-control-inner-background: #0f0f19;" +
            "-fx-background-color: #0f0f19;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-radius: 12px;" +
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-width: 1px;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-family: 'JetBrains Mono', monospace;" +
            "-fx-font-size: 12px;" +
            "-fx-highlight-fill: #667eea;" +
            "-fx-highlight-text-fill: #ffffff;"
        );

        panel.getChildren().addAll(title, logArea);
        return panel;
    }
    
    /**
     * 创建玻璃拟态卡片
     */
    private VBox createGlassCard() {
        VBox card = new VBox();
        card.setStyle(
            "-fx-background-color: rgba(20,20,30,0.6);" +
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;" +
            "-fx-border-color: rgba(255,255,255,0.1);" +
            "-fx-border-width: 1px;" +
            "-fx-padding: 24px;"
        );
        
        // 添加阴影效果
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.4));
        shadow.setRadius(20);
        shadow.setOffsetY(8);
        card.setEffect(shadow);
        
        return card;
    }
    
    /**
     * 创建样式化按钮
     */
    private Button createStyledButton(String text, boolean isDanger) {
        Button button = new Button(text);
        button.setPrefHeight(48);
        button.setPrefWidth(140);

        if (isDanger) {
            // 停止按钮 - 红色
            button.setStyle(
                "-fx-background-color: #dc2626;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 12px;" +
                "-fx-cursor: hand;"
            );
        } else {
            // 开始/刷新按钮 - 蓝色
            button.setStyle(
                "-fx-background-color: #3b82f6;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-radius: 12px;" +
                "-fx-cursor: hand;"
            );
        }

        // 添加悬停效果
        button.setOnMouseEntered(e -> {
            if (isDanger) {
                button.setStyle(
                    "-fx-background-color: #ef4444;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 12px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: #60a5fa;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 12px;" +
                    "-fx-cursor: hand;"
                );
            }
            button.setScaleX(1.02);
            button.setScaleY(1.02);
        });

        button.setOnMouseExited(e -> {
            if (isDanger) {
                button.setStyle(
                    "-fx-background-color: #dc2626;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 12px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: #3b82f6;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 12px;" +
                    "-fx-cursor: hand;"
                );
            }
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        return button;
    }
    
    /**
     * 添加启动动画
     */
    private void addStartupAnimation() {
        // 标题淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), titleText);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // 卡片依次进入
        int delay = 0;
        for (var node : mainContainer.getChildren()) {
            if (node instanceof VBox) {
                TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), node);
                slideUp.setFromY(30);
                slideUp.setToY(0);
                slideUp.setDelay(Duration.millis(delay));
                
                FadeTransition fade = new FadeTransition(Duration.millis(600), node);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.setDelay(Duration.millis(delay));
                
                slideUp.play();
                fade.play();
                delay += 100;
            }
        }
    }
    
    /**
     * 添加脉冲动画
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
     * 初始化服务
     */
    private void initializeServices() {
        log("🚀 正在初始化服务...", "info");
        
        // 加载配置
        appConfig = new AppConfig();
        
        // 初始化设备发现
        deviceDiscovery = new DeviceDiscovery();
        deviceDiscovery.setOnDeviceFound(this::onDeviceFound);
        
        // 应用网段配置
        String segment = appConfig.getNetworkSegment();
        if (!segment.isEmpty()) {
            deviceDiscovery.setNetworkSegment(segment);
            log("🌐 使用指定网段: " + segment + ".x", "info");
        } else {
            log("📡 使用广播模式发现设备", "info");
        }
        
        updateNetworkModeLabel();
        
        deviceDiscovery.start();
        
        // 初始化网络客户端
        castClient = new CastClient();
        castClient.setOnConnected(() -> Platform.runLater(() -> {
            updateStatus("已连接", "connected");
            log("✅ 连接成功！", "success");
        }));
        castClient.setOnDisconnected(() -> Platform.runLater(() -> {
            updateStatus("已断开", "disconnected");
            log("❌ 连接已断开", "warning");
        }));
        castClient.setOnError(msg -> Platform.runLater(() -> {
            log("❌ 错误: " + msg, "error");
            updateStatus("连接错误", "error");
        }));
        
        // 初始化屏幕捕获
        screenCapture = new ScreenCapture();
        screenCapture.setOnFrameCaptured(frame -> {
            if (castClient.isConnected()) {
                castClient.sendFrame(frame);
            }
        });
        
        log("✨ 服务初始化完成", "success");
        log("📍 本机IP: " + getLocalIpAddress(), "info");
    }
    
    /**
     * 更新网络模式显示
     */
    private void updateNetworkModeLabel() {
        Platform.runLater(() -> {
            if (networkModeLabel != null) {
                String segment = appConfig.getNetworkSegment();
                if (segment.isEmpty()) {
                    networkModeLabel.setText("📡 广播模式");
                    networkModeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.4);");
                } else {
                    networkModeLabel.setText("🌐 网段: " + segment + ".x");
                    networkModeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00d4ff;");
                }
            }
        });
    }
    
    /**
     * 显示网段设置对话框
     */
    private void showNetworkSettings() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("网络设置");
        dialog.setHeaderText("配置设备发现网段");
        
        // 设置对话框样式
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #0a0a0f;" +
            "-fx-text-fill: white;"
        );
        
        // 创建选项
        ToggleGroup group = new ToggleGroup();
        
        RadioButton broadcastRadio = new RadioButton("广播模式（自动发现所有网段）");
        broadcastRadio.setToggleGroup(group);
        broadcastRadio.setTextFill(Color.WHITE);
        broadcastRadio.setSelected(appConfig.isUseBroadcast());
        
        RadioButton segmentRadio = new RadioButton("指定网段");
        segmentRadio.setToggleGroup(group);
        segmentRadio.setTextFill(Color.WHITE);
        segmentRadio.setSelected(!appConfig.isUseBroadcast());
        
        // 网段输入
        TextField segmentField = new TextField(appConfig.getNetworkSegment());
        segmentField.setPromptText("如: 192.168.1");
        segmentField.setDisable(appConfig.isUseBroadcast());
        segmentField.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.3);"
        );
        
        // 建议网段下拉框
        ComboBox<String> suggestedCombo = new ComboBox<>();
        suggestedCombo.setPromptText("选择建议网段");
        suggestedCombo.setDisable(appConfig.isUseBroadcast());
        suggestedCombo.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);"
        );
        
        // 获取建议网段
        List<String> suggestedSegments = DeviceDiscovery.getSuggestedSegments();
        suggestedCombo.getItems().addAll(suggestedSegments);
        suggestedCombo.setOnAction(e -> {
            segmentField.setText(suggestedCombo.getValue());
        });
        
        // 启用/禁用输入框
        broadcastRadio.setOnAction(e -> {
            segmentField.setDisable(true);
            suggestedCombo.setDisable(true);
        });
        segmentRadio.setOnAction(e -> {
            segmentField.setDisable(false);
            suggestedCombo.setDisable(false);
        });
        
        // 布局
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #0a0a0f;");
        
        content.getChildren().addAll(
            broadcastRadio,
            segmentRadio,
            new Label("网段地址（如: 192.168.1）:") {{
                setTextFill(Color.WHITE);
            }},
            segmentField,
            new Label("或选择建议网段:") {{
                setTextFill(Color.WHITE);
            }},
            suggestedCombo
        );
        
        dialogPane.setContent(content);
        
        // 添加按钮
        ButtonType saveButton = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButton, cancelButton);
        
        // 设置按钮样式
        dialogPane.lookupButton(saveButton).setStyle(
            "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
            "-fx-text-fill: white;"
        );
        dialogPane.lookupButton(cancelButton).setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;"
        );
        
        // 处理结果
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButton) {
                if (broadcastRadio.isSelected()) {
                    return "";
                } else {
                    return segmentField.getText().trim();
                }
            }
            return null;
        });
        
        // 显示对话框并处理结果
        dialog.showAndWait().ifPresent(result -> {
            if (result != null) {
                if (result.isEmpty()) {
                    // 广播模式
                    appConfig.setUseBroadcast(true);
                    deviceDiscovery.setNetworkSegment(null);
                    log("📡 切换到广播模式", "info");
                } else {
                    // 验证网段格式
                    if (DeviceDiscovery.isValidNetworkSegment(result)) {
                        appConfig.setNetworkSegment(result);
                        deviceDiscovery.setNetworkSegment(result);
                        log("🌐 切换到网段模式: " + result + ".x", "info");
                    } else {
                        showAlert("网段格式错误！\n正确格式如: 192.168.1");
                        return;
                    }
                }
                appConfig.save();
                updateNetworkModeLabel();
                
                // 重新扫描
                refreshDevices();
            }
        });
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
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #10b981;");
                    break;
                case "disconnected":
                    statusIndicator.setFill(COLOR_STATUS_DISCONNECTED);
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #ef4444;");
                    break;
                case "error":
                    statusIndicator.setFill(COLOR_STATUS_DISCONNECTED);
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #ef4444;");
                    break;
                default:
                    statusIndicator.setFill(COLOR_STATUS_WAITING);
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #f59e0b;");
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
     * 发现新设备
     */
    private void onDeviceFound(String deviceName, String ipAddress) {
        Platform.runLater(() -> {
            String item = String.format("📺 %s\n   %s", deviceName, ipAddress);
            if (!deviceListView.getItems().contains(item)) {
                deviceListView.getItems().add(item);
                log("📱 发现设备: " + deviceName + " @ " + ipAddress, "success");
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

        // 解析IP地址
        String ip = selected.substring(selected.indexOf("   ") + 3).trim();
        String deviceName = selected.substring(2, selected.indexOf("\n")).trim();

        try {
            log("🔗 正在连接到: " + ip, "info");

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
            int bitrate = 16000000; // 默认16Mbps
            if (selectedBitrate.contains("4")) bitrate = 4000000;
            else if (selectedBitrate.contains("8")) bitrate = 8000000;
            else if (selectedBitrate.contains("16")) bitrate = 16000000;
            else if (selectedBitrate.contains("24")) bitrate = 24000000;
            else if (selectedBitrate.contains("32")) bitrate = 32000000;
            else if (selectedBitrate.contains("50")) bitrate = 50000000;
            else if (selectedBitrate.contains("80")) bitrate = 80000000;
            else if (selectedBitrate.contains("100")) bitrate = 100000000;

            // 解析帧率
            int fps = 30; // 默认30fps
            if (selectedFps.contains("24")) fps = 24;
            else if (selectedFps.contains("30")) fps = 30;
            else if (selectedFps.contains("60")) fps = 60;
            else if (selectedFps.contains("120")) fps = 120;

            log("⚙️ 视频设置: 分辨率=" + selectedResolution + ", 码率=" + (bitrate/1000000) + "Mbps, 帧率=" + fps + "fps", "info");

            // 配置屏幕捕获
            screenCapture.setConfig(appConfig);
            screenCapture.setUseNativeResolution(useNativeResolution);
            screenCapture.setBitrate(bitrate);
            screenCapture.setFrameRate(fps);

            // 如果不是原生分辨率，设置目标分辨率
            if (!useNativeResolution) {
                screenCapture.setTargetResolution(targetWidth, targetHeight);
                log("📺 设置投屏分辨率: " + targetWidth + "x" + targetHeight, "info");
            }

            // 先启动屏幕捕获，获取实际分辨率
            screenCapture.start();

            // 等待一小段时间让捕获器初始化
            Thread.sleep(500);

            // 获取实际编码分辨率
            String resolution = screenCapture.getCurrentResolution();
            log("📺 实际投屏分辨率: " + resolution, "info");

            // 解析分辨率
            String[] parts = resolution.split("x");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);

            // 设置客户端视频参数
            castClient.setVideoParams(width, height, fps);

            // 设置连接成功回调
            castClient.setOnConnected(() -> {
                Platform.runLater(() -> {
                    deviceLabel.setText(deviceName + " (" + resolution + ")");
                    startButton.setDisable(true);
                    stopButton.setDisable(false);
                    updateStatus("已连接", "connected");
                    log("▶️ 投屏已开始", "success");

                    // 连接成功后停止扫描设备列表
                    if (deviceDiscovery != null) {
                        deviceDiscovery.stop();
                        log("🔍 已停止扫描设备", "info");
                    }
                });
            });

            // 设置连接断开回调
            castClient.setOnDisconnected(() -> {
                Platform.runLater(() -> {
                    log("⚠️ 连接已断开", "warning");
                });
            });

            // 异步连接
            try {
                castClient.connect(ip, 8888);
                log("⏳ 正在连接...", "info");
            } catch (Exception e) {
                log("❌ 连接失败: " + e.getMessage(), "error");
                showAlert("连接失败: " + e.getMessage());
                screenCapture.stop();
            }

        } catch (Exception e) {
            log("❌ 连接失败: " + e.getMessage(), "error");
            showAlert("连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 停止投屏
     */
    private void stopCasting() {
        screenCapture.stop();
        castClient.disconnect();

        startButton.setDisable(false);
        stopButton.setDisable(true);
        deviceLabel.setText("未选择设备");
        updateStatus("等待连接...", "waiting");

        // 停止投屏后重新启动设备发现
        if (deviceDiscovery != null) {
            deviceDiscovery.start();
            log("🔍 已重新开始扫描设备", "info");
        }

        // 清空设置状态
        if (settingsStatusLabel != null) {
            settingsStatusLabel.setText("");
        }

        log("⏹️ 投屏已停止", "warning");
    }
    
    /**
     * 窗口关闭处理
     */
    private void onClose() {
        log("🚪 正在关闭应用...", "info");

        // 停止屏幕捕获
        if (screenCapture != null) {
            screenCapture.stop();
        }

        // 断开投屏连接
        if (castClient != null) {
            castClient.disconnect();
        }

        // 停止设备发现服务
        if (deviceDiscovery != null) {
            deviceDiscovery.stop();
        }

        // 关闭JavaFX平台
        Platform.exit();

        // 强制终止所有线程并退出JVM
        System.exit(0);
    }
    
    /**
     * 添加日志
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
     * 显示警告对话框
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 设置对话框样式
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #0a0a0f;" +
            "-fx-text-fill: white;"
        );
        
        alert.showAndWait();
    }
    
    /**
     * 获取本机IP地址
     */
    private String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "未知";
        }
    }
}
