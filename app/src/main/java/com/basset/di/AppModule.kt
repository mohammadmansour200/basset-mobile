package com.basset.di

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.basset.home.data.preferences.LocalThemePreferencesDataSource
import com.basset.home.data.preferences.ThemePreferencesRepositoryImpl
import com.basset.home.domain.ThemePreferencesRepository
import com.basset.home.presentation.ThemeViewModel
import com.basset.operations.data.android.MediaPlaybackManager
import com.basset.operations.data.cutOperation.LocalMediaDataSource
import com.basset.operations.data.cutOperation.MediaPlaybackRepositoryImpl
import com.basset.operations.domain.MediaDataSource
import com.basset.operations.domain.MediaPlaybackRepository
import com.basset.operations.presentation.OperationScreenViewModel
import com.basset.operations.presentation.cut_operation.CutOperationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<Player> { ExoPlayer.Builder(get()).build() }

    single<MediaPlaybackManager> {
        MediaPlaybackManager(player = get())
    }

    single<MediaPlaybackRepository> {
        MediaPlaybackRepositoryImpl(mediaPlaybackManager = get())
    }

    single<MediaDataSource> {
        LocalMediaDataSource(appContext = androidContext())
    }
    single<LocalThemePreferencesDataSource> {
        LocalThemePreferencesDataSource(context = androidContext())
    }

    single<ThemePreferencesRepository> {
        ThemePreferencesRepositoryImpl(themePreferencesDataSource = get())
    }

    viewModel {
        OperationScreenViewModel(context = androidContext())
    }

    viewModel {
        CutOperationViewModel(
            player = get(),
            mediaPlaybackRepository = get(),
            mediaDataSource = get()
        )
    }

    viewModel {
        ThemeViewModel(
            themePreferencesRepository = get(),
            context = androidContext()
        )
    }
}