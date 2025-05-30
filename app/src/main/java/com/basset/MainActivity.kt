package com.basset

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.basset.core.navigation.HomeRoute
import com.basset.core.navigation.OperationRoute
import com.basset.home.presentation.HomeScreen
import com.basset.home.presentation.ThemeViewModel
import com.basset.operations.presentation.OperationScreen
import com.basset.ui.theme.AppTheme
import com.basset.ui.theme.isDarkMode
import org.koin.androidx.compose.koinViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = koinViewModel<ThemeViewModel>()

            val state by viewModel.state.collectAsStateWithLifecycle()

            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = !isDarkMode(state.theme)

            AppTheme(
                darkTheme = isDarkMode(state.theme),
                dynamicColor = state.dynamicColorsEnabled
            ) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = HomeRoute) {
                    composable<HomeRoute> {
                        HomeScreen(onGoToOperation = { it -> navController.navigate(it) })
                    }
                    composable<OperationRoute> { it ->
                        val pickedFile: OperationRoute = it.toRoute()
                        OperationScreen(
                            pickedFile = pickedFile,
                            themeState = state,
                            onGoBack = {
                                navController.navigate(HomeRoute) {
                                    popUpTo(HomeRoute)
                                }
                            })
                    }
                }
            }
        }
    }
}

