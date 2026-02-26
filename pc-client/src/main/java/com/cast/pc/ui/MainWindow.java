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
        Scene scene = new Scene(mainContainer, 1000, 900);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // 设置窗口样式
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(800);
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
        content.setMaxWidth(800);
        
        // 标题区域
        content.getChildren().add(createHeader());
        
        // 主体内容 - 左右分栏
        HBox mainContent = new HBox(24);
        mainContent.setAlignment(Pos.TOP_CENTER);
        
        // 左侧：设备列表
        VBox leftPanel = createLeftPanel();
        leftPanel.setPrefWidth(380);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        
        // 右侧：状态和控制
        VBox rightPanel = createRightPanel();
        rightPanel.setPrefWidth(380);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(leftPanel, rightPanel);
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
     * 创建右侧面板 - 状态和控制
     */
    private VBox createRightPanel() {
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
                "-fx-background-color: #667eea;" +
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
                    "-fx-background-color: #f87171;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 12px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: #7c8ce5;" +
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
                    "-fx-background-color: #ef4444;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 12px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: #667eea;" +
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
            
            // 配置屏幕捕获
            screenCapture.setConfig(appConfig);
            screenCapture.setUseNativeResolution(true); // 使用屏幕原生分辨率
            
            // 先启动屏幕捕获，获取实际分辨率
            screenCapture.start();
            
            // 等待一小段时间让捕获器初始化
            Thread.sleep(500);
            
            // 获取实际编码分辨率
            String resolution = screenCapture.getCurrentResolution();
            log("📺 投屏分辨率: " + resolution, "info");
            
            // 解析分辨率
            String[] parts = resolution.split("x");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            int fps = appConfig.getFrameRate();
            
            // 设置客户端视频参数
            castClient.setVideoParams(width, height, fps);
            
            // 在后台线程中连接
            new Thread(() -> {
                try {
                    castClient.connect(ip, 8888);
                    
                    Platform.runLater(() -> {
                        deviceLabel.setText(deviceName + " (" + resolution + ")");
                        startButton.setDisable(true);
                        stopButton.setDisable(false);
                        
                        log("▶️ 投屏已开始", "success");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        log("❌ 连接失败: " + e.getMessage(), "error");
                        showAlert("连接失败: " + e.getMessage());
                        screenCapture.stop();
                    });
                }
            }).start();
            
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
        
        log("⏹️ 投屏已停止", "warning");
    }
    
    /**
     * 窗口关闭处理
     */
    private void onClose() {
        if (screenCapture != null) {
            screenCapture.stop();
        }
        if (castClient != null) {
            castClient.disconnect();
        }
        if (deviceDiscovery != null) {
            deviceDiscovery.stop();
        }
        Platform.exit();
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
