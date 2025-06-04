package com.basset.home.presentation

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.basset.R
import com.basset.home.domain.ThemePreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ThemeViewModel(
    context: Context,
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {
    private val appContext: Context = context

    private val _state = MutableStateFlow(ThemeState())
    val state: StateFlow<ThemeState> = _state

    init {
        loadThemePrefs()
    }

    fun onAction(action: ThemeAction) {
        when (action) {
            is ThemeAction.OnDynamicColorChange -> {
                themePreferencesRepository.setDynamicColorsEnabled(action.enabled)
                _state.update { it.copy(dynamicColorsEnabled = action.enabled) }
            }

            is ThemeAction.OnThemeChange -> {
                themePreferencesRepository.setTheme(action.theme)
                _state.update { it.copy(theme = action.theme) }
            }
        }

        Toast.makeText(
            appContext,
            appContext.getString(R.string.settings_action_toast),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun loadThemePrefs() {
        _state.update {
            it.copy(
                theme = themePreferencesRepository.getTheme() ?: "system",
                dynamicColorsEnabled = themePreferencesRepository.getDynamicColorsEnabled()
            )
        }
    }
}