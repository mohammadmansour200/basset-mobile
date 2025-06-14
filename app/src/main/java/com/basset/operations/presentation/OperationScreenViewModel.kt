package com.basset.operations.presentation

import android.content.ContentValues
import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.Log
import com.arthenica.ffmpegkit.LogCallback
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.SessionState
import com.arthenica.ffmpegkit.Statistics
import com.arthenica.ffmpegkit.StatisticsCallback
import com.basset.core.domain.model.MimeType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.data.android.createMediaStoreUri
import com.basset.operations.data.android.getUriExtension
import com.basset.operations.domain.OutputFile
import com.basset.operations.presentation.utils.progress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class OperationScreenViewModel(context: Context) : ViewModel() {
    private val appContext: Context = context

    private val _state = MutableStateFlow(OperationScreenState())
    val state: StateFlow<OperationScreenState> = _state

    fun onAction(action: OperationScreenAction) {
        when (action) {
            is OperationScreenAction.OnCut -> handleCut(action.pickedFile, action.start, action.end)
            is OperationScreenAction.OnCompress -> handleCompress(
                action.pickedFile,
                action.compressionRate
            )

            is OperationScreenAction.OnConvert -> handleConvert(
                action.pickedFile,
                action.outputFormat
            )

            is OperationScreenAction.OnRemoveBackground -> handleBgRemove(action.pickedFile)
        }
    }

    private fun handleCut(pickedFile: OperationRoute, start: Double, end: Double) {
        val inputPath =
            FFmpegKitConfig.getSafParameterForRead(appContext, pickedFile.uri.toUri())
        viewModelScope.launch {
            runFFmpeg(
                command = "-ss $start -to $end -i $inputPath -c copy",
                pickedFile = pickedFile,
                outputFile = OutputFile(
                    appContext.contentResolver.getUriExtension(pickedFile.uri.toUri()).toString(),
                    null
                )
            )
        }
    }

    private fun handleCompress(pickedFile: OperationRoute, compressionRate: Int) {
        when (pickedFile.mimeType) {
            MimeType.VIDEO, MimeType.AUDIO -> {

            }

            MimeType.IMAGE -> {

            }
        }
    }

    private fun handleConvert(pickedFile: OperationRoute, outputFormat: String) {
        when (pickedFile.mimeType) {
            MimeType.VIDEO, MimeType.AUDIO -> {

            }

            MimeType.IMAGE -> {

            }
        }
    }

    private suspend fun runFFmpeg(
        command: String, pickedFile: OperationRoute,
        outputFile: OutputFile
    ) = withContext(Dispatchers.IO) {
        val outputUri = createMediaStoreUri(appContext, pickedFile, outputFile)
        _state.update { it.copy(isOperating = true) }

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(appContext, pickedFile.uri.toUri())
        val durationMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
        retriever.release()

        outputUri?.let {
            val outputPath = FFmpegKitConfig.getSafParameterForWrite(appContext, outputUri)
            FFmpegKit.executeAsync(
                "-y -protocol_whitelist saf,file,crypto $command $outputPath",
                object : FFmpegSessionCompleteCallback {
                    override fun apply(session: FFmpegSession) {
                        val state = session.getState()

                        when (session.returnCode.value) {
                            ReturnCode.CANCEL -> {
                                appContext.contentResolver.delete(outputUri, null, null)
                                _state.update { it.copy(isOperating = false) }
                            }

                            ReturnCode.SUCCESS -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    val values = ContentValues().apply {
                                        put(MediaStore.Video.Media.IS_PENDING, 0)
                                    }
                                    appContext.contentResolver.update(outputUri, values, null, null)
                                }
                                _state.update { it.copy(isOperating = false) }
                            }
                        }

                        when (state) {
                            SessionState.FAILED -> {
                                appContext.contentResolver.delete(outputUri, null, null)
                                _state.update { it.copy(isOperating = false) }
                            }

                            SessionState.CREATED -> {
                                _state.update { it.copy(isOperating = true) }
                            }

                            else -> {}
                        }

                    }
                },
                object : LogCallback {
                    override fun apply(log: Log?) {
                        android.util.Log.d("ffmpeg-kit", log?.message.toString())
                        val progress = log?.progress(durationMs?.div(1000) ?: 0)
                        if (progress != null) _state.update { it.copy(progress = progress) }
                    }
                },
                object : StatisticsCallback {
                    override fun apply(statistics: Statistics?) {
                    }
                })
        }
    }

    private fun handleBgRemove(pickedFile: OperationRoute) {
        if (pickedFile.mimeType == MimeType.IMAGE) {

        }
    }
}