package com.cast.pc.capture;

import com.cast.pc.config.AppConfig;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.ffmpeg.global.swscale;
import org.bytedeco.ffmpeg.swscale.SwsContext;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.global.swscale.*;

/**
 * 屏幕捕获器
 * 
 * 使用Java Robot捕获屏幕，FFmpeg编码为H.264
 * 支持自定义分辨率或自动获取屏幕原生分辨率
 */
public class ScreenCapture {
    
    // 视频参数
    private static final int FRAME_RATE = 30;           // 帧率
    private static final int VIDEO_BITRATE = 8000000;   // 视频码率 8Mbps（高清）
    
    private ExecutorService executor;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private Robot robot;
    private AppConfig config;
    
    // FFmpeg编码器
    private AVCodecContext codecContext;
    private SwsContext swsContext;
    private AVFrame frame;
    private AVFrame yuvFrame;
    
    // 实际使用的分辨率
    private int captureWidth;
    private int captureHeight;
    private int encodeWidth;
    private int encodeHeight;
    
    // 是否使用屏幕原生分辨率
    private boolean useNativeResolution = true;
    
    private Consumer<byte[]> onFrameCaptured;
    private Consumer<Exception> onError;
    
    /**
     * 设置帧捕获回调
     */
    public void setOnFrameCaptured(Consumer<byte[]> callback) {
        this.onFrameCaptured = callback;
    }
    
    /**
     * 设置错误回调
     */
    public void setOnError(Consumer<Exception> callback) {
        this.onError = callback;
    }
    
    /**
     * 设置是否使用屏幕原生分辨率
     */
    public void setUseNativeResolution(boolean useNative) {
        this.useNativeResolution = useNative;
    }
    
    /**
     * 设置配置
     */
    public void setConfig(AppConfig config) {
        this.config = config;
    }
    
    /**
     * 开始捕获屏幕
     */
    public void start() {
        if (isRunning.get()) {
            return;
        }
        
        executor = Executors.newSingleThreadExecutor();
        executor.submit(this::captureLoop);
    }
    
    /**
     * 停止捕获
     */
    public void stop() {
        isRunning.set(false);
        
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        
        // 释放FFmpeg资源
        if (frame != null) {
            av_frame_free(frame);
            frame = null;
        }
        if (yuvFrame != null) {
            av_frame_free(yuvFrame);
            yuvFrame = null;
        }
        if (swsContext != null) {
            sws_freeContext(swsContext);
            swsContext = null;
        }
        if (codecContext != null) {
            avcodec_free_context(codecContext);
            codecContext = null;
        }
    }
    
    /**
     * 捕获循环
     */
    private void captureLoop() {
        try {
            // 获取屏幕尺寸
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            captureWidth = (int) screenSize.getWidth();
            captureHeight = (int) screenSize.getHeight();
            
            // 确定编码分辨率
            if (useNativeResolution) {
                // 使用屏幕原生分辨率
                encodeWidth = captureWidth;
                encodeHeight = captureHeight;
            } else if (config != null) {
                // 使用配置的分辨率
                encodeWidth = config.getVideoWidth();
                encodeHeight = config.getVideoHeight();
            } else {
                // 默认使用屏幕分辨率
                encodeWidth = captureWidth;
                encodeHeight = captureHeight;
            }
            
            System.out.println("屏幕分辨率: " + captureWidth + "x" + captureHeight);
            System.out.println("编码分辨率: " + encodeWidth + "x" + encodeHeight);
            
            // 初始化Robot
            robot = new Robot();
            Rectangle screenRect = new Rectangle(captureWidth, captureHeight);
            
            // 初始化FFmpeg编码器
            initializeEncoder(encodeWidth, encodeHeight);
            
            isRunning.set(true);
            System.out.println("屏幕捕获已启动");
            
            long startTime = System.currentTimeMillis();
            int frameCount = 0;
            long frameInterval = 1000 / FRAME_RATE;
            long nextFrameTime = System.currentTimeMillis();
            
            while (isRunning.get()) {
                long currentTime = System.currentTimeMillis();
                
                if (currentTime >= nextFrameTime) {
                    // 捕获屏幕
                    BufferedImage screenshot = robot.createScreenCapture(screenRect);
                    
                    // 如果需要缩放，进行缩放处理
                    BufferedImage processedImage = screenshot;
                    if (captureWidth != encodeWidth || captureHeight != encodeHeight) {
                        processedImage = resizeImage(screenshot, encodeWidth, encodeHeight);
                    }
                    
                    // 编码并发送
                    byte[] encodedData = encodeFrame(processedImage);
                    if (encodedData != null && encodedData.length > 0) {
                        if (onFrameCaptured != null) {
                            onFrameCaptured.accept(encodedData);
                        }
                    }
                    
                    frameCount++;
                    
                    // 计算帧率
                    long elapsed = currentTime - startTime;
                    if (elapsed >= 1000) {
                        System.out.println("捕获帧率: " + frameCount + " fps");
                        frameCount = 0;
                        startTime = currentTime;
                    }
                    
                    nextFrameTime = currentTime + frameInterval;
                } else {
                    // 等待下一帧
                    Thread.sleep(1);
                }
            }
            
        } catch (Exception e) {
            System.err.println("捕获出错: " + e.getMessage());
            e.printStackTrace();
            if (onError != null) {
                onError.accept(e);
            }
        } finally {
            isRunning.set(false);
        }
    }
    
    /**
     * 缩放图像
     */
    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = resized.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                          java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }
    
    /**
     * 初始化H.264编码器
     */
    private void initializeEncoder(int width, int height) throws Exception {
        // 查找H.264编码器
        AVCodec codec = avcodec_find_encoder(AV_CODEC_ID_H264);
        if (codec == null) {
            throw new RuntimeException("找不到H.264编码器");
        }
        
        // 创建编码器上下文
        codecContext = avcodec_alloc_context3(codec);
        if (codecContext == null) {
            throw new RuntimeException("无法创建编码器上下文");
        }
        
        // 计算码率（根据分辨率自适应）
        int bitrate = calculateBitrate(width, height);
        
        // 设置编码参数
        codecContext.width(width);
        codecContext.height(height);
        codecContext.time_base(av_make_q(1, FRAME_RATE));
        codecContext.framerate(av_make_q(FRAME_RATE, 1));
        codecContext.pix_fmt(AV_PIX_FMT_YUV420P);
        codecContext.bit_rate(bitrate);
        codecContext.gop_size(FRAME_RATE * 2);  // 2秒一个关键帧
        codecContext.max_b_frames(0);  // 不使用B帧，降低延迟
        
        // 设置编码器选项
        av_opt_set(codecContext.priv_data(), "preset", "ultrafast", 0);
        av_opt_set(codecContext.priv_data(), "tune", "zerolatency", 0);
        
        // 打开编码器
        int ret = avcodec_open2(codecContext, codec, (org.bytedeco.ffmpeg.avutil.AVDictionary) null);
        if (ret < 0) {
            throw new RuntimeException("无法打开编码器: " + ret);
        }
        
        // 创建转换上下文 (RGB -> YUV420P)
        swsContext = sws_getContext(
            width, height, AV_PIX_FMT_RGB24,
            width, height, AV_PIX_FMT_YUV420P,
            SWS_FAST_BILINEAR, null, null, (double[]) null
        );
        
        // 创建帧
        frame = av_frame_alloc();
        frame.width(width);
        frame.height(height);
        frame.format(AV_PIX_FMT_RGB24);
        av_frame_get_buffer(frame, 0);
        
        yuvFrame = av_frame_alloc();
        yuvFrame.width(width);
        yuvFrame.height(height);
        yuvFrame.format(AV_PIX_FMT_YUV420P);
        av_frame_get_buffer(yuvFrame, 0);
        
        System.out.println("H.264编码器已初始化: " + width + "x" + height + " @ " + (bitrate / 1000000) + "Mbps");
    }
    
    /**
     * 根据分辨率计算合适的码率
     */
    private int calculateBitrate(int width, int height) {
        int pixels = width * height;
        
        if (pixels <= 1280 * 720) {
            // 720p
            return 4000000;  // 4Mbps
        } else if (pixels <= 1920 * 1080) {
            // 1080p
            return 8000000;  // 8Mbps
        } else if (pixels <= 2560 * 1440) {
            // 2K
            return 12000000; // 12Mbps
        } else if (pixels <= 3840 * 2160) {
            // 4K
            return 25000000; // 25Mbps
        } else {
            // 更高分辨率
            return 35000000; // 35Mbps
        }
    }
    
    /**
     * 编码帧
     */
    private byte[] encodeFrame(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            // 获取RGB数据
            int[] rgbData = image.getRGB(0, 0, width, height, null, 0, width);
            
            // 填充RGB帧
            byte[] rgbBytes = new byte[width * height * 3];
            for (int i = 0; i < rgbData.length; i++) {
                int pixel = rgbData[i];
                rgbBytes[i * 3] = (byte) ((pixel >> 16) & 0xFF);     // R
                rgbBytes[i * 3 + 1] = (byte) ((pixel >> 8) & 0xFF);  // G
                rgbBytes[i * 3 + 2] = (byte) (pixel & 0xFF);         // B
            }
            
            // 将RGB数据复制到AVFrame
            org.bytedeco.javacpp.BytePointer frameData = new org.bytedeco.javacpp.BytePointer(rgbBytes);
            av_image_fill_arrays(frame.data(), frame.linesize(), frameData, AV_PIX_FMT_RGB24, width, height, 1);
            
            // 转换到YUV420P
            sws_scale(swsContext, frame.data(), frame.linesize(), 0, height, yuvFrame.data(), yuvFrame.linesize());
            
            // 设置帧时间戳
            yuvFrame.pts(av_rescale_q(codecContext.frame_number(), codecContext.time_base(), codecContext.time_base()));
            
            // 发送帧到编码器
            int ret = avcodec_send_frame(codecContext, yuvFrame);
            if (ret < 0) {
                System.err.println("发送帧到编码器失败: " + ret);
                return null;
            }
            
            // 接收编码后的数据
            AVPacket packet = av_packet_alloc();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            while (ret >= 0) {
                ret = avcodec_receive_packet(codecContext, packet);
                if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) {
                    break;
                } else if (ret < 0) {
                    System.err.println("编码失败: " + ret);
                    break;
                }
                
                // 复制编码数据
                byte[] data = new byte[packet.size()];
                packet.data().get(data);
                outputStream.write(data);
                
                av_packet_unref(packet);
            }
            
            av_packet_free(packet);
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            System.err.println("编码帧失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 检查是否正在运行
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * 获取当前编码分辨率
     */
    public String getCurrentResolution() {
        return encodeWidth + "x" + encodeHeight;
    }
}
