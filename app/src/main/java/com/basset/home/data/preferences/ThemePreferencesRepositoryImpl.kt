package com.basset.home.data.preferences

import com.basset.home.domain.ThemePreferencesRepository

class ThemePreferencesRepositoryImpl(
    private val themePreferencesDataSource: LocalThemePreferencesDataSource
) : ThemePreferencesRepository {
    override fun setTheme(theme: String) {
        themePreferencesDataSource.setTheme(theme)
    }

    override fun getTheme(): String? {
        return themePreferencesDataSource.getTheme()
    }

    override fun setDynamicColorsEnabled(enabled: Boolean) {
        themePreferencesDataSource.setDynamicColorsEnabled(enabled)
    }

    override fun getDynamicColorsEnabled(): Boolean {
        return themePreferencesDataSource.getDynamicColorsEnabled()
    }
}