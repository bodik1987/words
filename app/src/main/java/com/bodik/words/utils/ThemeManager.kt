package com.bodik.words.utils

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

enum class AppTheme { LIGHT, DARK, AUTO }

class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var theme = mutableStateOf(
        AppTheme.valueOf(prefs.getString("app_theme", AppTheme.AUTO.name) ?: AppTheme.AUTO.name)
    )

    fun setTheme(newTheme: AppTheme) {
        theme.value = newTheme
        prefs.edit { putString("app_theme", newTheme.name) }
    }
}