package com.bodik.words.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.bodik.words.utils.AppTheme
import com.bodik.words.utils.ThemeManager

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    // ПРИМЕНЯЕМ ТУТ:
    background = AppBackgroundLight,
    surface = AppBackgroundLight,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSecondary = ButtonLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,


    // Для темной темы:
    background = Color.Black,
    surface = AppBackgroundDark,
    onBackground = Color.White,
    onSurface = Color.White,
    onSecondary = ButtonDark,
    surfaceContainerLowest = AppBackgroundDark
)

//@Composable
//fun WordsTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = false,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        content = content
//    )
//}

@Composable
fun WordsTheme(
    themeManager: ThemeManager, // Добавляем параметр
    content: @Composable () -> Unit
) {
    val currentTheme = themeManager.theme.value
    val isDark = when (currentTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.AUTO -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}