package com.cast.tv.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cast.tv.MainActivity
import com.cast.tv.R
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * 投屏接收服务
 * 
 * 在后台运行，监听TCP连接，接收并解码视频流
 */
class CastServerService : Service() {
    
    companion object {
        const val PORT = 8888
        const val NOTIFICATION_CHANNEL_ID = "cast_server_channel"
        const val NOTIFICATION_ID = 1
        
        // 连接状态
        private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
        val connectionState: StateFlow<ConnectionState> = _connectionState
        
        fun start(context: Context) {
            val intent = Intent(context, CastServerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, CastServerService::class.java))
        }
    }
    
    // 连接状态密封类
    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        data class Connected(
            val deviceName: String,
            val width: Int = 1920,
            val height: Int = 1080,
            val fps: Int = 30
        ) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var bossGroup: EventLoopGroup? = null
    private var workerGroup: EventLoopGroup? = null
    private var serverChannel: Channel? = null
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("CastServerService 已创建")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification())
        
        // 启动服务器
        startServer()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopServer()
        serviceScope.cancel()
        Timber.d("CastServerService 已销毁")
    }
    
    /**
     * 启动TCP服务器
     */
    private fun startServer() {
        serviceScope.launch {
            try {
                bossGroup = NioEventLoopGroup(1)
                workerGroup = NioEventLoopGroup()
                
                val bootstrap = ServerBootstrap()
                bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel) {
                            val pipeline = ch.pipeline()
                            
                            // 添加长度字段编解码器
                            pipeline.addLast(LengthFieldBasedFrameDecoder(
                                10 * 1024 * 1024, 0, 4, 0, 4))
                            pipeline.addLast(LengthFieldPrepender(4))
                            
                            // 添加业务处理器
                            pipeline.addLast(CastServerHandler())
                        }
                    })
                
                // 绑定端口
                val future = bootstrap.bind(PORT).sync()
                serverChannel = future.channel()
                
                Timber.d("投屏服务器已启动，端口: $PORT")
                
                // 等待服务器关闭
                serverChannel?.closeFuture()?.sync()
                
            } catch (e: Exception) {
                Timber.e(e, "服务器启动失败")
                _connectionState.value = ConnectionState.Error(e.message ?: "未知错误")
            } finally {
                stopServer()
            }
        }
    }
    
    /**
     * 停止服务器
     */
    private fun stopServer() {
        serverChannel?.close()?.syncUninterruptibly()
        bossGroup?.shutdownGracefully()
        workerGroup?.shutdownGracefully()
        
        serverChannel = null
        bossGroup = null
        workerGroup = null
        
        _connectionState.value = ConnectionState.Disconnected
        
        Timber.d("投屏服务器已停止")
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "投屏服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持投屏接收服务运行"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建前台通知
     */
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("投屏接收服务")
            .setContentText("等待设备连接...")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    /**
     * 服务器处理器
     */
    private inner class CastServerHandler : SimpleChannelInboundHandler<ByteBuf>() {
        
        private var clientAddress: String? = null
        
        override fun channelActive(ctx: ChannelHandlerContext) {
            clientAddress = ctx.channel().remoteAddress().toString()
            Timber.d("客户端连接: $clientAddress")
        }
        
        override fun channelInactive(ctx: ChannelHandlerContext) {
            Timber.d("客户端断开: $clientAddress")
            _connectionState.value = ConnectionState.Disconnected
        }
        
        override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
            if (msg.readableBytes() < 1) return
            
            val type = msg.readByte()
            
            when (type.toInt()) {
                0x00 -> handleHandshake(ctx, msg)  // 握手消息
                0x01 -> handleVideoFrame(msg)      // 视频帧
                0x02 -> handleHeartbeat(ctx)       // 心跳
                else -> Timber.w("未知消息类型: $type")
            }
        }
        
        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            Timber.e(cause, "连接异常")
            _connectionState.value = ConnectionState.Error(cause.message ?: "连接异常")
            ctx.close()
        }
        
        /**
         * 处理握手消息
         */
        private fun handleHandshake(ctx: ChannelHandlerContext, msg: ByteBuf) {
            try {
                val version = msg.readInt()
                val width = msg.readInt()
                val height = msg.readInt()
                val fps = msg.readInt()
                
                Timber.d("握手信息 - 版本: $version, 分辨率: ${width}x$height, 帧率: $fps")
                
                // 发送握手响应
                val response = ctx.alloc().buffer()
                response.writeByte(0x00)
                ctx.writeAndFlush(response)
                
                _connectionState.value = ConnectionState.Connected(
                    deviceName = clientAddress ?: "未知设备",
                    width = width,
                    height = height,
                    fps = fps
                )
                
            } catch (e: Exception) {
                Timber.e(e, "处理握手消息失败")
                sendError(ctx, "握手失败: ${e.message}")
            }
        }
        
        /**
         * 处理视频帧
         */
        private fun handleVideoFrame(msg: ByteBuf) {
            try {
                val timestamp = msg.readInt()
                val data = ByteArray(msg.readableBytes())
                msg.readBytes(data)
                
                Timber.d("收到视频帧，大小: ${data.size} bytes, 时间戳: $timestamp")
                
                // 将视频帧传递给解码器
                val callback = MainActivity.onVideoFrameReceived
                if (callback != null) {
                    callback.invoke(data, timestamp.toLong())
                    Timber.d("视频帧已传递给解码器")
                } else {
                    Timber.w("视频帧回调未设置，无法传递给解码器")
                }
                
            } catch (e: Exception) {
                Timber.e(e, "处理视频帧失败")
            }
        }
        
        /**
         * 处理心跳
         */
        private fun handleHeartbeat(ctx: ChannelHandlerContext) {
            val response = ctx.alloc().buffer()
            response.writeByte(0x02)
            ctx.writeAndFlush(response)
        }
        
        /**
         * 发送错误响应
         */
        private fun sendError(ctx: ChannelHandlerContext, message: String) {
            val errorBytes = message.toByteArray(Charsets.UTF_8)
            val response = ctx.alloc().buffer(5 + errorBytes.size)
            response.writeByte(0xFF)
            response.writeInt(errorBytes.size)
            response.writeBytes(errorBytes)
            ctx.writeAndFlush(response)
        }
    }
}
