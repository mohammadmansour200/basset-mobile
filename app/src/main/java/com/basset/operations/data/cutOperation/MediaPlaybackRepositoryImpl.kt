package com.basset.operations.data.cutOperation

import android.net.Uri
import com.basset.operations.data.android.MediaPlaybackManager
import com.basset.operations.domain.MediaPlaybackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaPlaybackRepositoryImpl(
    private val mediaPlaybackManager: MediaPlaybackManager
) : MediaPlaybackRepository {

    override val events: Flow<MediaPlaybackRepository.MediaPlaybackEvent> =
        mediaPlaybackManager.events.map { event ->
            when (event) {
                is MediaPlaybackManager.Event.PositionChanged ->
                    MediaPlaybackRepository.MediaPlaybackEvent.PositionChanged(event.position)
            }
        }

    override fun loadMedia(uri: Uri) {
        mediaPlaybackManager.loadMedia(uri)
    }

    override fun releaseMedia() {
        mediaPlaybackManager.releaseMedia()
    }
}