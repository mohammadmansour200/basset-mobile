package com.basset.operations.presentation.cut_operation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.basset.core.domain.model.MimeType
import com.basset.operations.domain.MediaDataSource
import com.basset.operations.domain.MediaPlaybackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CutOperationMediaPlayerViewModel(
    val player: Player,
    private val mediaPlaybackRepository: MediaPlaybackRepository,
    private val mediaDataSource: MediaDataSource
) : ViewModel() {
    private val _state = MutableStateFlow(CutOperationMediaPlayerState())
    val state: StateFlow<CutOperationMediaPlayerState> = _state

    override fun onCleared() {
        super.onCleared()
        _state.update { CutOperationMediaPlayerState() }
        mediaPlaybackRepository.releaseMedia()
    }

    fun onAction(action: CutOperationMediaPlayerAction) {
        when (action) {
            is CutOperationMediaPlayerAction.OnLoadMedia -> {
                viewModelScope.launch {
                    mediaPlaybackRepository.loadMedia(uri = action.uri)
                    Log.d("MediaPlayer", "Media item set")
                    
                    launch { observePlaybackEvents() }
                    Log.d("MediaPlayer", "Preview Loaded")

                    when (action.mimeType) {
                        MimeType.AUDIO -> {
                            val result = mediaDataSource.loadAmplitudes(action.uri)
                            _state.update { it.copy(amplitudes = result) }
                        }

                        else -> {
                            mediaDataSource.loadVideoPreviewFrames(action.uri) { bitmap ->
                                _state.update { it.copy(videoFrames = it.videoFrames + bitmap) }
                            }
                        }
                    }

                }
            }

            is CutOperationMediaPlayerAction.OnUpdateProgress -> {
                val position = player.duration.times(action.progress).toLong()
                _state.update { it.copy(position = position) }
                player.seekTo(position)
            }
        }
    }


    private suspend fun observePlaybackEvents() {
        mediaPlaybackRepository.events.collectLatest {
            when (it) {
                is MediaPlaybackRepository.MediaPlaybackEvent.PositionChanged -> updatePlaybackProgress(
                    it.position
                )
            }
        }
    }

    private fun updatePlaybackProgress(position: Long) {
        _state.update { it.copy(position = position) }
    }
}