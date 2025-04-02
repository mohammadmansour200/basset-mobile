package com.basset.home.presentation.components

import androidx.compose.runtime.Composable
import com.basset.home.presentation.ThemeAction
import com.basset.home.presentation.ThemeState

enum class BottomSheetType {
    ABOUT, SETTINGS
}

@Composable
fun SheetContent(
    bottomSheetType: BottomSheetType,
    settingsState: ThemeState,
    settingsOnAction: (ThemeAction) -> Unit,
) {
    when (bottomSheetType) {
        BottomSheetType.ABOUT -> AboutBottomSheet()
        BottomSheetType.SETTINGS -> SettingsBottomSheet(
            state = settingsState,
            onAction = settingsOnAction
        )
    }
}
