package com.cast.tv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 颜色定义 - 与PC端保持一致
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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF667eea),
    onPrimary = Color.White,
    secondary = Color(0xFF00d4ff),
    onSecondary = Color.Black,
    background = Color(0xFF0a0a0f),
    onBackground = Color.White,
    surface = Color(0xFF14141e),
    onSurface = Color.White
)

@Composable
fun CastTVTheme(
    darkTheme: Boolean = true,  // TV默认使用深色主题
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
