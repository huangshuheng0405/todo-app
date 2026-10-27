package com.huangshuheng.todo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = TodoRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCEBEB),
    onPrimaryContainer = TodoRedPressed,
    secondary = LightTextSecondary,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightChipBackground,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainer = LightSurfaceSunken,
    surfaceContainerHigh = Color(0xFFF7F7F7),
    outline = LightOutline,
    outlineVariant = LightDivider,
    scrim = Color(0x66000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = TodoRedDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A221E),
    onPrimaryContainer = Color(0xFFFFD9D4),
    secondary = DarkTextSecondary,
    onSecondary = Color(0xFF1F1F1F),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkChipBackground,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainer = DarkSurfaceSunken,
    surfaceContainerHigh = Color(0xFF2C2C2C),
    outline = DarkOutline,
    outlineVariant = DarkDivider,
    scrim = Color(0x99000000)
)

/**
 * Material3 的色彩槽位覆盖不到的 Todoist 专属 token。
 */
@Immutable
data class TodoPalette(
    val brand: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val outline: Color,
    val drawer: Color,
    val sunken: Color,
    val chip: Color,
    val checkboxRing: Color,
    val ripple: Color,
    val priority1: Color,
    val priority2: Color,
    val priority3: Color,
    val priority4: Color,
    val isDark: Boolean
)

private val LightPalette = TodoPalette(
    brand = TodoRed,
    textPrimary = LightOnBackground,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    divider = LightDivider,
    outline = LightOutline,
    drawer = LightSurfaceDrawer,
    sunken = LightSurfaceSunken,
    chip = LightChipBackground,
    checkboxRing = LightCheckboxRing,
    ripple = LightRipple,
    priority1 = PriorityP1,
    priority2 = PriorityP2,
    priority3 = PriorityP3,
    priority4 = PriorityP4,
    isDark = false
)

private val DarkPalette = TodoPalette(
    brand = TodoRedDark,
    textPrimary = DarkOnBackground,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    divider = DarkDivider,
    outline = DarkOutline,
    drawer = DarkSurfaceDrawer,
    sunken = DarkSurfaceSunken,
    chip = DarkChipBackground,
    checkboxRing = DarkCheckboxRing,
    ripple = DarkRipple,
    priority1 = PriorityP1Dark,
    priority2 = PriorityP2Dark,
    priority3 = PriorityP3Dark,
    priority4 = PriorityP4Dark,
    isDark = true
)

val LocalTodoPalette = staticCompositionLocalOf { LightPalette }

object TodoTheme {
    val palette: TodoPalette
        @Composable get() = LocalTodoPalette.current
}

private val TodoShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

/** 取任务优先级对应的颜色。 */
@Composable
fun priorityColor(priority: Int): Color {
    val p = TodoTheme.palette
    return when (priority) {
        1 -> p.priority1
        2 -> p.priority2
        3 -> p.priority3
        else -> p.priority4
    }
}

@Composable
fun TodoistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 刻意不启用动态取色：动态色会把品牌红换成壁纸色，Todoist 的辨识度就没了
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val palette = if (darkTheme) DarkPalette else LightPalette
    androidx.compose.runtime.CompositionLocalProvider(LocalTodoPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = TodoShapes,
            content = content
        )
    }
}
