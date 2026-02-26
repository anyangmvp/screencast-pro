package com.cast.pc.discovery;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * 设备发现服务
 * 
 * 使用UDP广播发现局域网内的安卓TV设备
 * 支持指定网段扫描
 */
public class DeviceDiscovery {
    
    private static final int DISCOVERY_PORT = 8889;      // 发现服务端口
    private static final String DISCOVERY_MESSAGE = "CAST_DISCOVER";  // 发现消息
    private static final int BROADCAST_INTERVAL = 3000;  // 广播间隔（毫秒）
    private static final int SCAN_TIMEOUT = 200;         // 单个IP扫描超时（毫秒）
    
    private DatagramSocket socket;
    private ExecutorService executor;
    private ScheduledExecutorService scheduledExecutor;
    private ExecutorService scanExecutor;                // 网段扫描线程池
    private BiConsumer<String, String> onDeviceFound;    // 回调: (设备名, IP地址)
    
    private volatile boolean running = false;
    
    // 网段配置
    private String networkSegment = null;                // 指定网段，如 "192.168.1"
    private boolean useBroadcast = true;                 // 是否使用广播模式
    
    /**
     * 设置设备发现回调
     */
    public void setOnDeviceFound(BiConsumer<String, String> callback) {
        this.onDeviceFound = callback;
    }
    
    /**
     * 设置指定网段
     * @param segment 网段，如 "192.168.1" 或 "192.168.0"
     */
    public void setNetworkSegment(String segment) {
        this.networkSegment = segment;
        this.useBroadcast = (segment == null || segment.isEmpty());
    }
    
    /**
     * 获取当前网段配置
     */
    public String getNetworkSegment() {
        return networkSegment;
    }
    
    /**
     * 是否使用广播模式
     */
    public boolean isUseBroadcast() {
        return useBroadcast;
    }
    
    /**
     * 启动发现服务
     */
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(1000);  // 1秒超时
            
            // 启动接收线程
            executor = Executors.newSingleThreadExecutor();
            executor.submit(this::receiveLoop);
            
            // 启动扫描线程池
            scanExecutor = Executors.newFixedThreadPool(50);
            
            // 启动定时扫描
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutor.scheduleAtFixedRate(
                    this::discover, 
                    0, 
                    BROADCAST_INTERVAL, 
                    TimeUnit.MILLISECONDS
            );
            
            System.out.println("设备发现服务已启动" + 
                (useBroadcast ? "（广播模式）" : "（网段: " + networkSegment + ".x）"));
            
        } catch (Exception e) {
            System.err.println("启动发现服务失败: " + e.getMessage());
            stop();
        }
    }
    
    /**
     * 停止发现服务
     */
    public void stop() {
        running = false;
        
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            scheduledExecutor = null;
        }
        
        if (scanExecutor != null) {
            scanExecutor.shutdown();
            scanExecutor = null;
        }
        
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        
        if (socket != null && !socket.isClosed()) {
            socket.close();
            socket = null;
        }
        
        System.out.println("设备发现服务已停止");
    }
    
    /**
     * 立即执行一次设备发现
     */
    public void discover() {
        if (useBroadcast) {
            broadcastDiscovery();
        } else {
            scanNetworkSegment();
        }
    }
    
    /**
     * 广播发现模式
     */
    private void broadcastDiscovery() {
        try {
            byte[] message = DISCOVERY_MESSAGE.getBytes("UTF-8");
            
            // 获取所有网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // 跳过回环接口和未启用的接口
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                // 遍历接口的所有地址
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    
                    if (broadcast != null) {
                        // 发送广播消息
                        DatagramPacket packet = new DatagramPacket(
                                message, 
                                message.length, 
                                broadcast, 
                                DISCOVERY_PORT
                        );
                        socket.send(packet);
                        System.out.println("发送发现广播到: " + broadcast.getHostAddress());
                    }
                }
            }
            
        } catch (Exception e) {
            if (running) {
                System.err.println("广播发现消息失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 扫描指定网段
     * 逐个IP发送发现请求
     */
    private void scanNetworkSegment() {
        if (networkSegment == null || networkSegment.isEmpty()) {
            return;
        }
        
        System.out.println("开始扫描网段: " + networkSegment + ".1-254");
        
        // 扫描 .1 到 .254
        for (int i = 1; i <= 254; i++) {
            final int ipSuffix = i;
            scanExecutor.submit(() -> {
                try {
                    String ip = networkSegment + "." + ipSuffix;
                    InetAddress address = InetAddress.getByName(ip);
                    
                    // 先检查IP是否可达（快速过滤）
                    if (address.isReachable(SCAN_TIMEOUT)) {
                        byte[] message = DISCOVERY_MESSAGE.getBytes("UTF-8");
                        DatagramPacket packet = new DatagramPacket(
                                message,
                                message.length,
                                address,
                                DISCOVERY_PORT
                        );
                        socket.send(packet);
                        System.out.println("发送发现请求到: " + ip);
                    }
                } catch (Exception e) {
                    // 忽略单个IP的错误
                }
            });
        }
    }
    
    /**
     * 接收响应循环
     */
    private void receiveLoop() {
        byte[] buffer = new byte[1024];
        
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String response = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                String senderIp = packet.getAddress().getHostAddress();
                
                // 解析响应: "CAST_RESPONSE:设备名"
                if (response.startsWith("CAST_RESPONSE:")) {
                    String deviceName = response.substring(14);
                    System.out.println("发现设备: " + deviceName + " @ " + senderIp);
                    
                    if (onDeviceFound != null) {
                        onDeviceFound.accept(deviceName, senderIp);
                    }
                }
                
            } catch (SocketTimeoutException e) {
                // 超时，继续循环
            } catch (Exception e) {
                if (running) {
                    System.err.println("接收响应出错: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 验证网段格式是否正确
     * @param segment 网段字符串，如 "192.168.1"
     * @return 是否有效
     */
    public static boolean isValidNetworkSegment(String segment) {
        if (segment == null || segment.isEmpty()) {
            return true;  // 空值表示使用广播模式
        }
        
        String[] parts = segment.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 获取建议的网段列表
     * 基于本机IP地址
     */
    public static List<String> getSuggestedSegments() {
        List<String> segments = new ArrayList<>();
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress address = interfaceAddress.getAddress();
                    
                    if (address instanceof Inet4Address) {
                        String ip = address.getHostAddress();
                        String[] parts = ip.split("\\.");
                        if (parts.length == 4) {
                            String segment = parts[0] + "." + parts[1] + "." + parts[2];
                            if (!segments.contains(segment)) {
                                segments.add(segment);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取建议网段失败: " + e.getMessage());
        }
        
        // 添加常用网段
        if (!segments.contains("192.168.0")) {
            segments.add(0, "192.168.0");
        }
        if (!segments.contains("192.168.1")) {
            segments.add(0, "192.168.1");
        }
        
        return segments;
    }
}
