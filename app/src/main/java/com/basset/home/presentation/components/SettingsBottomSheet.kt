package com.basset.home.presentation.components

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.basset.R
import com.basset.core.presentation.utils.findActivity
import com.basset.home.presentation.ThemeAction
import com.basset.home.presentation.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    state: ThemeState,
    onAction: (ThemeAction) -> Unit
) {
    val context = LocalContext.current
    Column {
        //Theme settings
        var showDialog by remember { mutableStateOf(false) }
        val localizedRadioLabel = { label: String ->
            when (label) {
                "dark" -> R.string.settings_theme_dark_option
                "light" -> R.string.settings_theme_light_option
                else -> R.string.settings_theme_system_option
            }
        }
        TextButton(
            onClick = { showDialog = true },
            shape = MaterialTheme.shapes.extraSmall,
            contentPadding = PaddingValues()
        ) {
            ListItem(
                headlineContent = { Text(text = stringResource(R.string.settings_theme_label)) },
                supportingContent = {
                    Text(
                        text = stringResource(localizedRadioLabel(state.theme)),
                        modifier = Modifier.alpha(0.8f)
                    )
                },
                leadingContent = {
                    Icon(
                        ImageVector.vectorResource(R.drawable.sun),
                        null,
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
        if (showDialog) {
            BasicAlertDialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = AlertDialogDefaults.shape,
                    color = AlertDialogDefaults.containerColor,
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.settings_theme_alert_title),
                            style = MaterialTheme.typography.titleLarge
                        )

                        val radioOptions = listOf("system", "light", "dark")

                        Column(modifier = Modifier.selectableGroup()) {
                            radioOptions.forEach { text ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .height(56.dp)
                                        .selectable(
                                            selected = (text == state.theme),
                                            onClick = {
                                                onAction(
                                                    ThemeAction.OnThemeChange(
                                                        text
                                                    )
                                                )
                                            },
                                            role = Role.RadioButton
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        modifier = Modifier.padding(start = 16.dp),
                                        selected = (text == state.theme),
                                        onClick = null
                                    )
                                    Text(
                                        text = stringResource(localizedRadioLabel(text)),
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        //Dynamic color settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            TextButton(
                onClick = { onAction(ThemeAction.OnDynamicColorChange(!state.dynamicColorsEnabled)) },
                shape = MaterialTheme.shapes.extraSmall,
                contentPadding = PaddingValues()
            ) {
                ListItem(
                    headlineContent = { Text(text = stringResource(R.string.settings_dynamic_color_label)) },
                    trailingContent = {
                        Switch(
                            checked = state.dynamicColorsEnabled,
                            onCheckedChange = { onAction(ThemeAction.OnDynamicColorChange(!state.dynamicColorsEnabled)) },
                        )
                    },
                    leadingContent = {
                        Icon(
                            ImageVector.vectorResource(R.drawable.palette),
                            null,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            HorizontalDivider(modifier = Modifier.alpha(0.8f))
        }

        //Language settings
        var showLanguageBottomSheet by remember { mutableStateOf(false) }
        val appLocales = listOf("ar", "en")
        val onLocaleClick = { lang: String ->
            context.findActivity()?.runOnUiThread {
                val appLocale =
                    LocaleListCompat.forLanguageTags(lang)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
        }
        TextButton(
            onClick = { showLanguageBottomSheet = true },
            shape = MaterialTheme.shapes.extraSmall,
            contentPadding = PaddingValues()
        ) {
            ListItem(
                headlineContent = { Text(text = stringResource(R.string.settings_lang_label)) },
                supportingContent = {
                    Text(
                        text = java.util.Locale(Locale.current.language).displayLanguage,
                        modifier = Modifier.alpha(0.8f)
                    )
                },
                leadingContent = {
                    Icon(
                        ImageVector.vectorResource(R.drawable.language),
                        null,
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
        if (showLanguageBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showLanguageBottomSheet = false
                },
            ) {

                LazyColumn(
                    modifier = Modifier.selectableGroup(),
                ) {

                    appLocales.forEach { locale ->
                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (locale == Locale.current.language),
                                        onClick = { onLocaleClick(locale) },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    modifier = Modifier.padding(start = 16.dp),
                                    selected = (locale == Locale.current.language),
                                    onClick = null
                                )
                                Text(
                                    text = java.util.Locale(locale).displayLanguage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
