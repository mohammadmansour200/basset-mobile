package com.basset.home.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.basset.home.domain.ThemePreferences

class LocalThemePreferencesDataSource(context: Context) : ThemePreferences {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_DYNAMIC_COLORS = "dynamic_colors"
    }

    override fun setTheme(theme: String) {
        sharedPreferences.edit().putString(KEY_THEME, theme).apply()
    }

    override fun getTheme(): String? {
        return sharedPreferences.getString(KEY_THEME, "system") // Default to light theme
    }

    override fun setDynamicColorsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DYNAMIC_COLORS, enabled).apply()
    }

    // Retrieve whether dynamic colors are enabled
    override fun getDynamicColorsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DYNAMIC_COLORS, false) // Default is false
    }
}
