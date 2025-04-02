package com.basset.di

import com.basset.home.presentation.ThemeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::ThemeViewModel)
}