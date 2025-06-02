package com.basset.operations.presentation

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.basset.core.domain.model.MimeType
import com.basset.core.utils.getFileName
import com.basset.core.utils.uriToFile
import com.linc.amplituda.Amplituda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val MAX_VIDEO_PREVIEW_IMAGES = 8

class CutOperationMediaPlayerViewModel(context: Context, val player: Player) : ViewModel() {
    private val appContext: Context = context

    private val playbackManager = MediaPlaybackManager(player)

    private val _state = MutableStateFlow(CutOperationMediaPlayerState())
    val state: StateFlow<CutOperationMediaPlayerState> = _state

    override fun onCleared() {
        super.onCleared()
        _state.update { CutOperationMediaPlayerState() }
        playbackManager.releaseController()
    }

    fun onAction(action: CutOperationMediaPlayerAction) {
        when (action) {
            is CutOperationMediaPlayerAction.OnLoadMedia -> {
                viewModelScope.launch {
                    val mediaItem = MediaItem.fromUri(action.uri as Uri)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    Log.d("MediaPlayer", "Media item set")


                    when (action.mimeType) {
                        MimeType.AUDIO -> {
                            val result = loadAmplitudes(action.uri)
                            _state.update { it.copy(amplitudes = result) }
                        }

                        else -> {
                            loadVideoPreviewFrames(action.uri, onThumbnailReady = { bitmap ->
                                _state.update { it.copy(videoFrames = it.videoFrames + bitmap) }
                            })
                        }
                    }
                    launch { observePlaybackEvents() }

                    Log.d("MediaPlayer", "Preview Loaded")
                }
            }

            is CutOperationMediaPlayerAction.OnUpdateProgress -> {
                val position = player.duration.times(action.progress).toLong()
                player.seekTo(position)
                _state.update { it.copy(position = position) }
            }
        }
    }


    private suspend fun observePlaybackEvents() {
        playbackManager.startTrackingPlaybackPosition()
        playbackManager.events.collectLatest {
            when (it) {
                is MediaPlaybackManager.Event.PositionChanged -> updatePlaybackProgress(it.position)
            }
        }
    }

    private fun updatePlaybackProgress(position: Long) {
        _state.update { it.copy(position = position) }
    }

    private suspend fun loadAmplitudes(uri: Uri): List<Int> = withContext(Dispatchers.IO) {
        try {
            val amplituda = Amplituda(appContext)
            val inputFile = appContext.uriToFile(uri = uri)
            val result = amplituda.processAudio(inputFile).get()
            inputFile.delete()
            Log.d("MediaPlayer", "temp file deleted")
            return@withContext result.amplitudesAsList() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun loadVideoPreviewFrames(
        uri: Uri,
        onThumbnailReady: (bitmap: Bitmap) -> Unit
    ) = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(appContext, uri)

            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()

            Log.d(
                "ThumbnailExtract",
                uri.getFileName(appContext)
            )

            if (durationMs == null) {
                Log.e("ThumbnailExtract", "Failed to read video duration.")
                return@withContext
            }

            val interval = (12.5f * durationMs) / 100

            for (i in 0 until MAX_VIDEO_PREVIEW_IMAGES) {
                val timeUs = i * interval * 1000
                val bitmap =
                    retriever.getFrameAtTime(timeUs.toLong(), MediaMetadataRetriever.OPTION_CLOSEST)
                if (bitmap != null) onThumbnailReady(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
    }
}