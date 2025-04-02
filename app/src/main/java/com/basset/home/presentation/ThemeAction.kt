package com.basset.home.presentation

sealed interface ThemeAction {
    data class OnThemeChange(val theme: String) : ThemeAction
    data class OnDynamicColorChange(val enabled: Boolean) : ThemeAction
}