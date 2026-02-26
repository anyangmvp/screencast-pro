package com.cast.pc.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * 应用配置管理
 * 
 * 管理应用的各种配置项，包括网络设置、视频参数等
 */
public class AppConfig {
    
    private static final String CONFIG_FILE = "config.properties";
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.screencast";
    
    private Properties properties;
    private File configFile;
    
    // 默认配置值
    private static final String DEFAULT_VIDEO_BITRATE = "4000000";
    private static final String DEFAULT_FRAME_RATE = "30";
    private static final String DEFAULT_WIDTH = "1920";
    private static final String DEFAULT_HEIGHT = "1080";
    private static final String DEFAULT_SERVER_PORT = "8888";
    private static final String DEFAULT_NETWORK_SEGMENT = "";  // 空表示广播模式
    
    public AppConfig() {
        properties = new Properties();
        
        // 确保配置目录存在
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        configFile = new File(dir, CONFIG_FILE);
        load();
    }
    
    /**
     * 加载配置文件
     */
    public void load() {
        try {
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                    System.out.println("配置文件已加载: " + configFile.getAbsolutePath());
                }
            } else {
                // 使用默认配置
                setDefaults();
                save();
                System.out.println("创建默认配置文件: " + configFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage());
            setDefaults();
        }
    }
    
    /**
     * 保存配置到文件
     */
    public void save() {
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "Screen Cast Pro 配置文件");
            System.out.println("配置已保存");
        } catch (IOException e) {
            System.err.println("保存配置文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置默认值
     */
    private void setDefaults() {
        properties.setProperty("video.bitrate", DEFAULT_VIDEO_BITRATE);
        properties.setProperty("video.framerate", DEFAULT_FRAME_RATE);
        properties.setProperty("video.width", DEFAULT_WIDTH);
        properties.setProperty("video.height", DEFAULT_HEIGHT);
        properties.setProperty("server.port", DEFAULT_SERVER_PORT);
        properties.setProperty("network.segment", DEFAULT_NETWORK_SEGMENT);
        properties.setProperty("network.use_broadcast", "true");
    }
    
    // ==================== 视频配置 ====================
    
    public int getVideoBitrate() {
        return Integer.parseInt(properties.getProperty("video.bitrate", DEFAULT_VIDEO_BITRATE));
    }
    
    public void setVideoBitrate(int bitrate) {
        properties.setProperty("video.bitrate", String.valueOf(bitrate));
    }
    
    public int getFrameRate() {
        return Integer.parseInt(properties.getProperty("video.framerate", DEFAULT_FRAME_RATE));
    }
    
    public void setFrameRate(int fps) {
        properties.setProperty("video.framerate", String.valueOf(fps));
    }
    
    public int getVideoWidth() {
        return Integer.parseInt(properties.getProperty("video.width", DEFAULT_WIDTH));
    }
    
    public void setVideoWidth(int width) {
        properties.setProperty("video.width", String.valueOf(width));
    }
    
    public int getVideoHeight() {
        return Integer.parseInt(properties.getProperty("video.height", DEFAULT_HEIGHT));
    }
    
    public void setVideoHeight(int height) {
        properties.setProperty("video.height", String.valueOf(height));
    }
    
    // ==================== 网络配置 ====================
    
    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", DEFAULT_SERVER_PORT));
    }
    
    public void setServerPort(int port) {
        properties.setProperty("server.port", String.valueOf(port));
    }
    
    /**
     * 获取配置的网段
     * @return 网段字符串，如 "192.168.1"，空字符串表示使用广播模式
     */
    public String getNetworkSegment() {
        return properties.getProperty("network.segment", DEFAULT_NETWORK_SEGMENT);
    }
    
    /**
     * 设置网段
     * @param segment 网段字符串，如 "192.168.1"，null或空字符串表示广播模式
     */
    public void setNetworkSegment(String segment) {
        if (segment == null || segment.trim().isEmpty()) {
            properties.setProperty("network.segment", "");
            properties.setProperty("network.use_broadcast", "true");
        } else {
            properties.setProperty("network.segment", segment.trim());
            properties.setProperty("network.use_broadcast", "false");
        }
    }
    
    /**
     * 是否使用广播模式
     */
    public boolean isUseBroadcast() {
        return "true".equalsIgnoreCase(properties.getProperty("network.use_broadcast", "true"));
    }
    
    /**
     * 设置是否使用广播模式
     */
    public void setUseBroadcast(boolean useBroadcast) {
        properties.setProperty("network.use_broadcast", String.valueOf(useBroadcast));
        if (useBroadcast) {
            properties.setProperty("network.segment", "");
        }
    }
    
    /**
     * 获取配置文件的完整路径
     */
    public String getConfigFilePath() {
        return configFile.getAbsolutePath();
    }
    
    /**
     * 重置为默认配置
     */
    public void resetToDefaults() {
        setDefaults();
        save();
    }
}
