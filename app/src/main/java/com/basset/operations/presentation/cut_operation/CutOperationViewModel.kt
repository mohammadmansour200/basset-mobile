package com.basset.operations.presentation.cut_operation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.basset.core.domain.model.MimeType
import com.basset.operations.domain.MediaDataSource
import com.basset.operations.domain.MediaPlaybackRepository
import com.basset.operations.domain.cutOperation.CutOperationError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class CutOperationViewModel(
    val player: Player,
    private val mediaPlaybackRepository: MediaPlaybackRepository,
    private val mediaDataSource: MediaDataSource
) : ViewModel() {
    private val _state = MutableStateFlow(CutOperationState())
    val state: StateFlow<CutOperationState> = _state

    private val _events = Channel<CutOperationEvent>()
    val events = _events.receiveAsFlow()

    override fun onCleared() {
        super.onCleared()
        _state.update { CutOperationState() }
        mediaPlaybackRepository.releaseMedia()
    }

    fun onAction(action: CutOperationAction) {
        when (action) {
            is CutOperationAction.OnLoadMedia -> {
                viewModelScope.launch {
                    mediaPlaybackRepository.loadMedia(uri = action.uri)
                    Log.d("MediaPlayer", "Media item set")

                    launch { observePlaybackEvents() }
                    Log.d("MediaPlayer", "Preview Loaded")

                    when (action.mimeType) {
                        MimeType.AUDIO -> {
                            try {
                                val result = mediaDataSource.loadAmplitudes(action.uri)
                                _state.update { it.copy(amplitudes = result) }
                            } catch (_: Exception) {
                                _events.send(CutOperationEvent.Error(CutOperationError.MEDIA_PREVIEW_LOADING))
                            }
                        }

                        else -> {
                            try {
                                mediaDataSource.loadVideoPreviewFrames(action.uri) { bitmap ->
                                    _state.update { it.copy(videoFrames = it.videoFrames + bitmap) }
                                }
                            } catch (_: Exception) {
                                _events.send(CutOperationEvent.Error(CutOperationError.MEDIA_PREVIEW_LOADING))
                            }
                        }
                    }

                }
            }

            is CutOperationAction.OnUpdateProgress -> {
                val position = player.duration.times(action.progress).toLong()

                val startRangePosition = player.duration * _state.value.startRangeProgress
                val endRangePosition = player.duration * _state.value.endRangeProgress
                if (position < player.duration * _state.value.startRangeProgress) {
                    _state.update { it.copy(position = startRangePosition.toLong()) }
                    player.seekTo(
                        startRangePosition.toLong()
                    )

                    return
                }
                if (position > player.duration * _state.value.endRangeProgress) {
                    _state.update { it.copy(position = endRangePosition.toLong()) }
                    player.pause()

                    return
                }

                _state.update { it.copy(position = position) }
                player.seekTo(
                    position
                )
            }

            is CutOperationAction.OnEndRangeChange -> {
                val startRangePosition = player.duration * _state.value.startRangeProgress
                val endRangePosition = player.duration * action.position
                if (startRangePosition > player.duration) {
                    viewModelScope.launch {
                        _events.send(CutOperationEvent.Error(CutOperationError.CUT_RANGE_EXCEEDS_DURATION))
                    }
                    return
                }
                if (startRangePosition > endRangePosition) {
                    viewModelScope.launch {
                        _events.send(CutOperationEvent.Error(CutOperationError.WRONG_END_CUT_RANGE_POSITION))
                    }
                    return
                }

                _state.update { it.copy(endRangeProgress = action.position) }
                if (_state.value.position > player.duration * _state.value.endRangeProgress) {
                    _state.update { it.copy(position = startRangePosition.toLong()) }
                    player.seekTo(
                        startRangePosition.toLong()
                    )
                }
            }

            is CutOperationAction.OnStartRangeChange -> {
                val startRangePosition = player.duration * action.position
                val endRangePosition = player.duration * _state.value.endRangeProgress
                if (startRangePosition > player.duration) {
                    viewModelScope.launch {
                        _events.send(CutOperationEvent.Error(CutOperationError.CUT_RANGE_EXCEEDS_DURATION))
                    }
                    return
                }
                if (startRangePosition > endRangePosition) {
                    viewModelScope.launch {
                        _events.send(CutOperationEvent.Error(CutOperationError.WRONG_START_CUT_RANGE_POSITION))
                    }
                    return
                }

                _state.update { it.copy(startRangeProgress = action.position) }
                if (_state.value.position < player.duration * action.position) {
                    _state.update { it.copy(position = startRangePosition.toLong()) }
                    player.seekTo(
                        startRangePosition.toLong()
                    )
                }
            }
        }
    }


    private suspend fun observePlaybackEvents() {
        mediaPlaybackRepository.events.collectLatest {
            when (it) {
                is MediaPlaybackRepository.MediaPlaybackEvent.PositionChanged -> {
                    updatePlaybackPosition(it.position)

                    val startRangePosition = player.duration * _state.value.startRangeProgress
                    if (it.position < player.duration * _state.value.startRangeProgress) player.seekTo(
                        startRangePosition.toLong()
                    )
                    if (it.position > player.duration * _state.value.endRangeProgress) player.seekTo(
                        startRangePosition.toLong()
                    )

                }
            }
        }
    }

    private fun updatePlaybackPosition(position: Long) {
        _state.update { it.copy(position = position) }
    }
}