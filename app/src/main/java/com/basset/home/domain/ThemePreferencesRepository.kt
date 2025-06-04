package com.basset.home.domain

interface ThemePreferencesRepository {
    fun setTheme(
        theme: String
    )

    fun getTheme(): String?

    fun setDynamicColorsEnabled(
        enabled: Boolean
    )

    fun getDynamicColorsEnabled(): Boolean
}