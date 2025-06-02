package com.basset.di

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.basset.home.presentation.ThemeViewModel
import com.basset.operations.presentation.CutOperationMediaPlayerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<Player> { ExoPlayer.Builder(get()).build() }
    viewModelOf(::ThemeViewModel)
    viewModel { CutOperationMediaPlayerViewModel(get(), get()) }
}