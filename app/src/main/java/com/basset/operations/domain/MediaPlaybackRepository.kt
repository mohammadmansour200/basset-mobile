package com.basset.operations.domain

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface MediaPlaybackRepository {
    val events: Flow<MediaPlaybackEvent>
    fun loadMedia(uri: Uri)
    fun releaseMedia()

    sealed interface MediaPlaybackEvent {
        data class PositionChanged(val position: Long) : MediaPlaybackEvent
    }
}