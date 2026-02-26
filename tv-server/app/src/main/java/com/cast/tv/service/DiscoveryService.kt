package com.cast.tv.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cast.tv.MainActivity
import com.cast.tv.R
import kotlinx.coroutines.*
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * 设备发现服务
 * 
 * 响应电脑的UDP广播发现请求
 */
class DiscoveryService : Service() {
    
    companion object {
        const val DISCOVERY_PORT = 8889
        const val DISCOVERY_MESSAGE = "CAST_DISCOVER"
        const val RESPONSE_PREFIX = "CAST_RESPONSE:"
        const val NOTIFICATION_CHANNEL_ID = "discovery_channel"
        const val NOTIFICATION_ID = 2
        
        fun start(context: Context) {
            val intent = Intent(context, DiscoveryService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, DiscoveryService::class.java))
        }
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: DatagramSocket? = null
    private var isRunning = false
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("DiscoveryService 已创建")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification())
        
        startDiscovery()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopDiscovery()
        serviceScope.cancel()
        Timber.d("DiscoveryService 已销毁")
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "设备发现服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于响应电脑的发现请求"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建通知
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
            .setContentText("正在等待投屏连接...")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    /**
     * 启动发现服务
     */
    private fun startDiscovery() {
        if (isRunning) return
        
        isRunning = true
        
        serviceScope.launch {
            try {
                // 获取多播锁
                val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
                val multicastLock = wifiManager.createMulticastLock("DiscoveryService")
                multicastLock.acquire()
                
                socket = DatagramSocket(DISCOVERY_PORT)
                socket?.broadcast = true
                
                Timber.d("设备发现服务已启动，端口: $DISCOVERY_PORT")
                
                val buffer = ByteArray(1024)
                
                while (isRunning) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket?.receive(packet)
                        
                        val message = String(packet.data, 0, packet.length, Charsets.UTF_8)
                        val senderAddress = packet.address
                        val senderPort = packet.port
                        
                        if (message == DISCOVERY_MESSAGE) {
                            Timber.d("收到发现请求来自: $senderAddress")
                            respondToDiscovery(senderAddress, senderPort)
                        }
                        
                    } catch (e: Exception) {
                        if (isRunning) {
                            Timber.e(e, "接收发现请求出错")
                        }
                    }
                }
                
                multicastLock.release()
                
            } catch (e: Exception) {
                Timber.e(e, "启动发现服务失败")
            }
        }
    }
    
    /**
     * 停止发现服务
     */
    private fun stopDiscovery() {
        isRunning = false
        socket?.close()
        socket = null
        Timber.d("设备发现服务已停止")
    }
    
    /**
     * 响应发现请求
     */
    private fun respondToDiscovery(address: InetAddress, port: Int) {
        try {
            val deviceName = Build.MODEL ?: "Android TV"
            val response = "$RESPONSE_PREFIX$deviceName"
            val data = response.toByteArray(Charsets.UTF_8)
            
            val packet = DatagramPacket(data, data.size, address, port)
            socket?.send(packet)
            
            Timber.d("已响应发现请求: $deviceName")
            
        } catch (e: Exception) {
            Timber.e(e, "响应发现请求失败")
        }
    }
}
