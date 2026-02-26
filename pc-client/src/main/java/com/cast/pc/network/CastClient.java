package com.cast.pc.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 投屏网络客户端
 * 
 * 使用Netty实现TCP连接，传输视频流数据
 */
public class CastClient {
    
    private EventLoopGroup workerGroup;
    private Channel channel;
    private AtomicBoolean connected = new AtomicBoolean(false);
    
    private Runnable onConnected;
    private Runnable onDisconnected;
    private Consumer<String> onError;
    
    // 视频参数
    private int videoWidth = 1920;
    private int videoHeight = 1080;
    private int frameRate = 30;
    
    // 连接超时时间（秒）
    private static final int CONNECT_TIMEOUT = 5;
    
    /**
     * 设置连接成功回调
     */
    public void setOnConnected(Runnable callback) {
        this.onConnected = callback;
    }
    
    /**
     * 设置断开连接回调
     */
    public void setOnDisconnected(Runnable callback) {
        this.onDisconnected = callback;
    }
    /**
     * 设置错误回调
     */
    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
    }
    
    /**
     * 设置视频参数
     */
    public void setVideoParams(int width, int height, int fps) {
        this.videoWidth = width;
        this.videoHeight = height;
        this.frameRate = fps;
    }
    
    /**
     * 连接到服务器
     * 
     * @param host 服务器地址
     * @param port 服务器端口
     */
    public void connect(String host, int port) throws Exception {
        if (connected.get()) {
            return;
        }
        
        System.out.println("正在连接到 " + host + ":" + port + "...");
        
        workerGroup = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT * 1000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // 添加长度字段编解码器（解决粘包问题）
                            // 格式: [4字节长度][数据体]
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                    10 * 1024 * 1024,  // 最大帧大小 10MB
                                    0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            
                            // 添加业务处理器
                            pipeline.addLast(new CastClientHandler());
                        }
                    });
            
            // 连接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
            
            connected.set(true);
            System.out.println("已连接到服务器: " + host + ":" + port);
            
            if (onConnected != null) {
                onConnected.run();
            }
            
            // 发送握手消息
            sendHandshake();
            
            // 等待连接关闭
            channel.closeFuture().sync();
            
        } finally {
            disconnect();
        }
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        if (!connected.get()) {
            return;
        }
        
        connected.set(false);
        
        if (channel != null && channel.isActive()) {
            channel.close();
        }
        
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        
        System.out.println("已断开连接");
        
        if (onDisconnected != null) {
            onDisconnected.run();
        }
    }
    
    /**
     * 发送视频帧
     * 
     * @param frameData H.264编码后的帧数据
     */
    public void sendFrame(byte[] frameData) {
        if (!connected.get() || channel == null || !channel.isActive()) {
            return;
        }
        
        try {
            // 构造数据包: [1字节类型][4字节时间戳][视频数据]
            ByteBuf buffer = Unpooled.buffer(5 + frameData.length);
            buffer.writeByte(0x01);  // 类型: 视频帧
            buffer.writeInt((int) System.currentTimeMillis());  // 时间戳
            buffer.writeBytes(frameData);
            
            channel.writeAndFlush(buffer);
        } catch (Exception e) {
            System.err.println("发送帧失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送握手消息
     */
    private void sendHandshake() {
        if (channel == null || !channel.isActive()) {
            return;
        }
        
        // 构造握手包
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(0x00);  // 类型: 握手
        buffer.writeInt(1);      // 版本号
        buffer.writeInt(videoWidth);   // 屏幕宽度
        buffer.writeInt(videoHeight);  // 屏幕高度
        buffer.writeInt(frameRate);    // 帧率
        
        channel.writeAndFlush(buffer);
        
        System.out.println("发送握手信息: " + videoWidth + "x" + videoHeight + " @ " + frameRate + "fps");
    }
    
    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return connected.get() && channel != null && channel.isActive();
    }
    
    /**
     * 客户端处理器
     */
    private class CastClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            // 处理服务器响应
            if (msg.readableBytes() < 1) {
                return;
            }
            
            byte type = msg.readByte();
            
            switch (type) {
                case 0x00:  // 握手响应
                    System.out.println("收到握手响应");
                    break;
                case 0x02:  // 心跳响应
                    break;
                case (byte) 0xFF:  // 错误
                    int errorLen = msg.readInt();
                    byte[] errorBytes = new byte[errorLen];
                    msg.readBytes(errorBytes);
                    String error = new String(errorBytes, "UTF-8");
                    System.err.println("服务器错误: " + error);
                    if (onError != null) {
                        onError.accept(error);
                    }
                    break;
                default:
                    System.out.println("收到未知消息类型: " + type);
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.err.println("连接异常: " + cause.getMessage());
            if (onError != null) {
                onError.accept(cause.getMessage());
            }
            ctx.close();
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            connected.set(false);
            if (onDisconnected != null) {
                onDisconnected.run();
            }
        }
    }
}
