package com.basset.di

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.basset.core.navigation.OperationRoute
import com.basset.home.data.preferences.LocalThemePreferencesDataSource
import com.basset.home.domain.ThemePreferences
import com.basset.home.presentation.ThemeViewModel
import com.basset.operations.data.ai.TFLiteBackgroundRemover
import com.basset.operations.data.media.LocalCuttingTimelineDataSource
import com.basset.operations.data.media.LocalMediaMetadataDataSource
import com.basset.operations.data.media.LocalMediaPlaybackManager
import com.basset.operations.data.media.LocalMediaStoreManager
import com.basset.operations.domain.BackgroundRemover
import com.basset.operations.domain.MediaMetadataDataSource
import com.basset.operations.domain.MediaStoreManager
import com.basset.operations.domain.cut_operation.CuttingTimelineDataSource
import com.basset.operations.domain.cut_operation.MediaPlaybackManager
import com.basset.operations.presentation.OperationScreenViewModel
import com.basset.operations.presentation.cut_operation.CutOperationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<Player> { ExoPlayer.Builder(get()).build() }

    single<MediaPlaybackManager> {
        LocalMediaPlaybackManager(player = get())
    }

    single<CuttingTimelineDataSource> {
        LocalCuttingTimelineDataSource(appContext = androidContext())
    }

    single<ThemePreferences> {
        LocalThemePreferencesDataSource(context = androidContext())
    }

    single<MediaStoreManager> {
        LocalMediaStoreManager(context = androidContext())
    }

    single<MediaMetadataDataSource> {
        LocalMediaMetadataDataSource(context = androidContext())
    }

    single<BackgroundRemover> {
        TFLiteBackgroundRemover(context = androidContext())
    }

    viewModel { (pickedFile: OperationRoute) ->
        OperationScreenViewModel(
            context = get(),
            mediaStoreManager = get(),
            metadataDataSource = get(),
            backgroundRemover = get(),
            pickedFile = pickedFile
        )
    }

    viewModel {
        CutOperationViewModel(
            player = get(),
            mediaPlaybackManager = get(),
            cuttingTimelineDataSource = get()
        )
    }

    viewModel {
        ThemeViewModel(
            themePreferences = get(),
            context = androidContext()
        )
    }
}