package com.basset

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.basset.core.navigation.HomeRoute
import com.basset.core.navigation.OperationRoute
import com.basset.home.presentation.HomeScreen
import com.basset.home.presentation.ThemeViewModel
import com.basset.operations.presentation.OperationScreen
import com.basset.ui.theme.AppTheme
import com.basset.ui.theme.isDarkMode
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoinContext {
                val themeViewModel = koinViewModel<ThemeViewModel>()

                val themeState by themeViewModel.state.collectAsStateWithLifecycle()

                val insetsController = WindowInsetsControllerCompat(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isDarkMode(themeState.theme)

                AppTheme(
                    darkTheme = isDarkMode(themeState.theme),
                    dynamicColor = themeState.dynamicColorsEnabled
                ) {
                    val backStack = rememberNavBackStack(HomeRoute)
                    NavDisplay(
                        backStack = backStack,
                        entryDecorators = listOf(
                            rememberSavedStateNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                            rememberSceneSetupNavEntryDecorator()
                        ),
                        entryProvider = { key ->
                            when (key) {
                                is HomeRoute -> {
                                    NavEntry(key = key) {
                                        HomeScreen(onGoToOperation = { it ->
                                            backStack.add(it)
                                        })
                                    }
                                }

                                is OperationRoute -> {
                                    NavEntry(key = key) {
                                        OperationScreen(
                                            pickedFile = key,
                                            onGoBack = {
                                                backStack.removeLastOrNull()
                                            })
                                    }
                                }

                                else -> throw RuntimeException("Unknown route")
                            }
                        },
                    )
                }
            }
        }
    }

}
