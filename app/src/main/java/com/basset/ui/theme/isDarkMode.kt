package com.basset.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
fun isDarkMode(theme: String): Boolean {
    return when (theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
}
