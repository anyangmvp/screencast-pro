package com.cast.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 深色主题颜色方案（保留但不再使用）
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF667eea),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF764ba2),
    onPrimaryContainer = Color.White,
    
    secondary = Color(0xFF00d4ff),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00d4ff).copy(alpha = 0.1f),
    onSecondaryContainer = Color(0xFF00d4ff),
    
    tertiary = Color(0xFFa855f7),
    onTertiary = Color.White,
    
    background = Color(0xFF0a0a0f),
    onBackground = Color.White,
    
    surface = Color(0xFF14141e),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1a1a2e),
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    
    error = Color(0xFFef4444),
    onError = Color.White,
    
    outline = Color.White.copy(alpha = 0.1f),
    outlineVariant = Color.White.copy(alpha = 0.08f)
)

// 浅色主题颜色方案 - 蓝紫色主题（与 PC 端保持一致）
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),  // 靛蓝
    onPrimary = Color.White,
    primaryContainer = Color(0xFF6366F1).copy(alpha = 0.1f),
    onPrimaryContainer = Color(0xFF6366F1),
    
    secondary = Color(0xFF8B5CF6),  // 紫罗兰
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8B5CF6).copy(alpha = 0.1f),
    onSecondaryContainer = Color(0xFF8B5CF6),
    
    tertiary = Color(0xFF06B6D4),  // 青色
    onTertiary = Color.White,
    
    background = Color(0xFFF5F3FF),  // 浅紫背景
    onBackground = Color(0xFF1E293B),  // 深灰文字
    
    surface = Color(0xFFFFFFFF),  // 白色卡片
    onSurface = Color(0xFF1E293B),  // 深灰文字
    surfaceVariant = Color(0xFFEDE9FE),  // 浅紫变体
    onSurfaceVariant = Color(0xFF64748B),  // 中灰文字
    
    error = Color(0xFFef4444),
    onError = Color.White,
    
    outline = Color(0xFFE2E8F0),  // 浅灰边框
    outlineVariant = Color(0xFFF1F5F9)  // 更浅的边框
)

@Composable
fun CastTVTheme(
    darkTheme: Boolean = false,  // 默认使用浅色主题
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
