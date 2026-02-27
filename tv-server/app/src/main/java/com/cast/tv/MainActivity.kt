package com.cast.tv

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.cast.tv.decoder.VideoDecoder
import com.cast.tv.service.CastServerService
import com.cast.tv.ui.theme.CastTVTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * TV端主Activity - 现代化深色玻璃拟态设计
 * 
 * 设计风格: Dark Glassmorphism
 * 主色调: 深蓝紫渐变 + 青色点缀
 */
class MainActivity : ComponentActivity() {
    
    private var serverIp by mutableStateOf("正在获取...")
    private var connectionStatus by mutableStateOf(ConnectionStatus.WAITING)
    private var connectedDevice by mutableStateOf<String?>(null)
    private var showVideo by mutableStateOf(false)
    
    // 视频解码器
    private var videoDecoder: VideoDecoder? = null
    private var surfaceView: SurfaceView? = null
    
    // 颜色定义 - 与PC端保持一致
    companion object {
        val ColorBgDark = Color(0xFF0A0A0F)
        val ColorBgCard = Color(0xFF14141E).copy(alpha = 0.6f)
        val ColorAccentCyan = Color(0xFF00D4FF)
        val ColorAccentPurple = Color(0xFFA855F7)
        val ColorTextPrimary = Color.White
        val ColorTextSecondary = Color.White.copy(alpha = 0.7f)
        val ColorTextMuted = Color.White.copy(alpha = 0.5f)
        val ColorStatusConnected = Color(0xFF10B981)
        val ColorStatusDisconnected = Color(0xFFEF4444)
        val ColorStatusWaiting = Color(0xFFF59E0B)
        
        // 用于传递Surface给Service
        var sharedSurface: android.view.Surface? = null
        var onVideoFrameReceived: ((ByteArray, Long) -> Unit)? = null
    }
    
    enum class ConnectionStatus {
        WAITING, CONNECTED, DISCONNECTED, ERROR
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化日志
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // 获取IP地址
        serverIp = getLocalIpAddress()
        Timber.d("本机IP地址: $serverIp")
        
        // 启动服务
        startServices()
        
        setContent {
            CastTVTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 视频显示层
                    if (showVideo) {
                        VideoSurface(
                            onSurfaceCreated = { surface ->
                                Timber.d("Surface已创建，初始化解码器")
                                videoDecoder = VideoDecoder(surface).apply {
                                    initialize()
                                }
                                sharedSurface = surface
                            },
                            onSurfaceDestroyed = {
                                Timber.d("Surface已销毁，释放解码器")
                                videoDecoder?.release()
                                videoDecoder = null
                                sharedSurface = null
                            }
                        )
                    }
                    
                    // UI层
                    if (!showVideo || connectionStatus != ConnectionStatus.CONNECTED) {
                        MainScreen(
                            serverIp = serverIp,
                            connectionStatus = connectionStatus,
                            connectedDevice = connectedDevice
                        )
                    }
                }
            }
        }
        
        // 设置视频帧接收回调
        onVideoFrameReceived = { data, timestamp ->
            videoDecoder?.decodeFrame(data, timestamp)
        }
    }
    
    override fun onResume() {
        super.onResume()
        serverIp = getLocalIpAddress()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        Timber.d("MainActivity销毁，断开所有连接")
        
        // 停止投屏服务，断开所有连接
        CastServerService.stop(this)
        
        // 释放解码器
        videoDecoder?.release()
        videoDecoder = null
        sharedSurface = null
        onVideoFrameReceived = null
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        Timber.d("用户按下返回键，断开连接")
        // 返回键也会触发onDestroy，但这里可以立即断开
        CastServerService.stop(this)
    }
    
    /**
     * 启动后台服务
     */
    private fun startServices() {
        // 启动投屏接收服务
        CastServerService.start(this)
        
        // 监听连接状态
        lifecycleScope.launch {
            CastServerService.connectionState.collect { state ->
                when (state) {
                    is CastServerService.ConnectionState.Connected -> {
                        connectionStatus = ConnectionStatus.CONNECTED
                        connectedDevice = "${state.deviceName} (${state.width}x${state.height})"
                        showVideo = true
                        Timber.d("设备已连接: ${state.deviceName}，分辨率: ${state.width}x${state.height}，切换到视频显示")
                        
                        // 根据连接的分辨率重新初始化解码器
                        sharedSurface?.let { surface ->
                            videoDecoder?.release()
                            videoDecoder = VideoDecoder(surface).apply {
                                initialize(state.width, state.height)
                            }
                            Timber.d("解码器已重新初始化: ${state.width}x${state.height}")
                        }
                    }
                    is CastServerService.ConnectionState.Disconnected -> {
                        connectionStatus = ConnectionStatus.WAITING
                        connectedDevice = null
                        showVideo = false
                        Timber.d("设备已断开，切换到等待界面")
                    }
                    is CastServerService.ConnectionState.Error -> {
                        connectionStatus = ConnectionStatus.ERROR
                        Timber.e("连接错误: ${state.message}")
                    }
                }
            }
        }
    }
    
    /**
     * 获取本机IP地址
     */
    private fun getLocalIpAddress(): String {
        return try {
            val wifiManager = getSystemService(WIFI_SERVICE) as android.net.wifi.WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ip = wifiInfo.ipAddress
            String.format(
                "%d.%d.%d.%d",
                ip and 0xff,
                ip shr 8 and 0xff,
                ip shr 16 and 0xff,
                ip shr 24 and 0xff
            )
        } catch (e: Exception) {
            Timber.e(e, "获取IP地址失败")
            "未知"
        }
    }
}

/**
 * 视频显示Surface
 */
@Composable
fun VideoSurface(
    onSurfaceCreated: (android.view.Surface) -> Unit,
    onSurfaceDestroyed: () -> Unit
) {
    AndroidView(
        factory = { context ->
            SurfaceView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        holder.surface?.let { onSurfaceCreated(it) }
                    }
                    
                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        // Surface大小改变
                    }
                    
                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        onSurfaceDestroyed()
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * 主屏幕界面
 */
@Composable
fun MainScreen(
    serverIp: String,
    connectionStatus: MainActivity.ConnectionStatus,
    connectedDevice: String?
) {
    // 渐变背景
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MainActivity.ColorBgDark,
            Color(0xFF1A1A2E),
            Color(0xFF16213E)
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        // 背景光晕效果
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MainActivity.ColorAccentPurple.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 标题区域
            TitleSection()
            
            // 主内容卡片
            GlassCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.padding(48.dp)
                ) {
                    // IP地址显示
                    IpDisplay(serverIp)
                    
                    // 分隔线
                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                    
                    // 连接状态
                    ConnectionStatusSection(connectionStatus, connectedDevice)
                }
            }
            
            // 使用说明
            InstructionText()
        }
    }
}

/**
 * 标题区域
 */
@Composable
fun TitleSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 渐变标题
        Text(
            text = "Screen Cast Pro",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MainActivity.ColorAccentCyan,
                        MainActivity.ColorAccentPurple
                    )
                )
            ),
            modifier = Modifier
                .shadow(
                    elevation = 20.dp,
                    spotColor = MainActivity.ColorAccentCyan.copy(alpha = glowAlpha),
                    ambientColor = MainActivity.ColorAccentCyan.copy(alpha = glowAlpha)
                )
        )
        
        // 副标题
        Text(
            text = "无线投屏接收端",
            fontSize = 20.sp,
            color = MainActivity.ColorTextMuted,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 玻璃拟态卡片
 */
@Composable
fun GlassCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .background(
                color = MainActivity.ColorBgCard,
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        content()
    }
}

/**
 * IP地址显示
 */
@Composable
fun IpDisplay(ip: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "本机IP地址",
            fontSize = 18.sp,
            color = MainActivity.ColorTextMuted,
            textAlign = TextAlign.Center
        )
        
        // IP地址卡片
        Box(
            modifier = Modifier
                .background(
                    color = MainActivity.ColorAccentCyan.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = MainActivity.ColorAccentCyan.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(
                text = ip,
                fontSize = 42.sp,
                fontWeight = FontWeight.SemiBold,
                color = MainActivity.ColorAccentCyan,
                textAlign = TextAlign.Center,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
        
        // 端口信息
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "端口:",
                fontSize = 16.sp,
                color = MainActivity.ColorTextMuted
            )
            Text(
                text = "8888",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MainActivity.ColorTextSecondary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

/**
 * 连接状态区域
 */
@Composable
fun ConnectionStatusSection(
    status: MainActivity.ConnectionStatus,
    deviceName: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 状态指示器
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 脉冲动画指示器
            PulsingIndicator(status)
            
            // 状态文字
            val (statusText, statusColor) = when (status) {
                MainActivity.ConnectionStatus.WAITING -> "等待连接..." to MainActivity.ColorStatusWaiting
                MainActivity.ConnectionStatus.CONNECTED -> "已连接" to MainActivity.ColorStatusConnected
                MainActivity.ConnectionStatus.DISCONNECTED -> "已断开" to MainActivity.ColorStatusDisconnected
                MainActivity.ConnectionStatus.ERROR -> "连接错误" to MainActivity.ColorStatusDisconnected
            }
            
            Text(
                text = statusText,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor,
                textAlign = TextAlign.Center
            )
        }
        
        // 连接设备信息
        AnimatedVisibility(
            visible = status == MainActivity.ConnectionStatus.CONNECTED && deviceName != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            deviceName?.let {
                Box(
                    modifier = Modifier
                        .background(
                            color = MainActivity.ColorStatusConnected.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "来自: $it",
                        fontSize = 20.sp,
                        color = MainActivity.ColorStatusConnected,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 脉冲动画指示器
 */
@Composable
fun PulsingIndicator(status: MainActivity.ConnectionStatus) {
    val color = when (status) {
        MainActivity.ConnectionStatus.WAITING -> MainActivity.ColorStatusWaiting
        MainActivity.ConnectionStatus.CONNECTED -> MainActivity.ColorStatusConnected
        MainActivity.ConnectionStatus.DISCONNECTED -> MainActivity.ColorStatusDisconnected
        MainActivity.ConnectionStatus.ERROR -> MainActivity.ColorStatusDisconnected
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Box(
        modifier = Modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // 外圈脉冲
        Box(
            modifier = Modifier
                .size(20.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha * 0.3f))
        )
        
        // 内圈实心
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = color
                )
        )
    }
}

/**
 * 使用说明文字
 */
@Composable
fun InstructionText() {
    Text(
        text = "请在电脑端输入上方IP地址进行连接",
        fontSize = 18.sp,
        color = MainActivity.ColorTextMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 16.dp)
    )
}
