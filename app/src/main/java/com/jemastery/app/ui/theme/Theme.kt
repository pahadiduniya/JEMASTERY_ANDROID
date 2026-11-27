package com.jemastery.app.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class BgPreset(val name: String, val top: Color, val bottom: Color)

val bgPresets = listOf(
    BgPreset("Default", Color(0xFFF8FAFC), Color(0xFFF8FAFC)),
    BgPreset("Ocean", Color(0xFFE0F2FE), Color(0xFFF8FAFC)),
    BgPreset("Mint", Color(0xFFDCFCE7), Color(0xFFF8FAFC)),
    BgPreset("Sunset", Color(0xFFFFE4E6), Color(0xFFFFF7ED)),
    BgPreset("Lavender", Color(0xFFEDE9FE), Color(0xFFF8FAFC))
)

object ThemeState {
    var dark by mutableStateOf(false)
        private set
    var preset by mutableIntStateOf(0)
        private set

    fun load(ctx: Context) {
        val p = ctx.getSharedPreferences("jemastery_theme", Context.MODE_PRIVATE)
        dark = p.getBoolean("dark", false)
        preset = p.getInt("preset", 0)
    }

    fun toggleDark(ctx: Context) {
        dark = !dark
        ctx.getSharedPreferences("jemastery_theme", Context.MODE_PRIVATE)
            .edit().putBoolean("dark", dark).apply()
    }

    fun setPreset(ctx: Context, index: Int) {
        preset = index
        ctx.getSharedPreferences("jemastery_theme", Context.MODE_PRIVATE)
            .edit().putInt("preset", index).apply()
    }
}

private val Light = lightColorScheme(
    primary = Color(0xFF1E3A8A),
    onPrimary = Color.White,
    secondary = Color(0xFF3B82F6),
    onSecondary = Color.White,
    tertiary = Color(0xFF10B981),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    error = Color(0xFFEF4444)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF0F172A),
    tertiary = Color(0xFF34D399),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = Color(0xFFEF4444)
)

@Composable
fun JEMasteryTheme(
    dark: Boolean = ThemeState.dark,
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}

@Composable
fun systemIsDark(): Boolean = isSystemInDarkTheme()
