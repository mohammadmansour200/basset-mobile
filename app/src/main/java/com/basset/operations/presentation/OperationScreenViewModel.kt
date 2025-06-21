package com.basset.operations.presentation

import android.content.Context
import android.graphics.Canvas
import android.net.Uri
import androidx.core.graphics.createBitmap
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
import com.basset.operations.data.android.getUriExtension
import com.basset.operations.data.android.toBitmap
import com.basset.operations.domain.BackgroundRemover
import com.basset.operations.domain.MediaMetadataDataSource
import com.basset.operations.domain.MediaStoreManager
import com.basset.operations.domain.model.CompressionRate
import com.basset.operations.domain.model.Format
import com.basset.operations.domain.model.OutputFileInfo
import com.basset.operations.presentation.utils.progress
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class OperationScreenViewModel(
    context: Context,
    val mediaStoreManager: MediaStoreManager,
    val metadataDataSource: MediaMetadataDataSource,
    val backgroundRemover: BackgroundRemover,
    val pickedFile: OperationRoute
) :
    ViewModel() {
    private val appContext: Context = context

    private val _state = MutableStateFlow(OperationScreenState())
    val state: StateFlow<OperationScreenState> = _state

    init {
        viewModelScope.launch {
            val metadata =
                metadataDataSource.loadMetadata(pickedFile.uri.toUri(), pickedFile.mimeType)
            _state.update { it.copy(metadata = metadata) }
        }
    }

    fun onAction(action: OperationScreenAction) {
        when (action) {
            is OperationScreenAction.OnCut -> handleCut(action.start, action.end)
            is OperationScreenAction.OnCompress -> handleCompress(
                action.compressionRate
            )

            is OperationScreenAction.OnConvert -> handleConvert(
                action.outputFormat
            )

            is OperationScreenAction.OnRemoveBackground -> {
                viewModelScope.launch {
                    handleBgRemove(
                        outputFileInfo = OutputFileInfo(
                            appContext.contentResolver.getUriExtension(pickedFile.uri.toUri())
                                .toString(),
                            null
                        ),
                        background = action.background
                    )
                }
            }

            is OperationScreenAction.OnAudioToVideoConvert -> handleAudioToVideoConvert(
                action.outputFormat,
                action.image
            )
        }
    }

    private fun handleCut(start: Double, end: Double) {
        val inputPath =
            FFmpegKitConfig.getSafParameterForRead(appContext, pickedFile.uri.toUri())
        viewModelScope.launch {
            runFFmpeg(
                command = "-ss $start -to $end -i $inputPath -c copy",
                outputFileInfo = OutputFileInfo(
                    appContext.contentResolver.getUriExtension(pickedFile.uri.toUri()).toString(),
                    null
                )
            )
        }
    }

    private fun handleCompress(compressionRate: CompressionRate) {
        when (pickedFile.mimeType) {
            MimeType.VIDEO, MimeType.AUDIO -> {
                val videoCompressBitrate = when (compressionRate) {
                    CompressionRate.LOW -> "2000k"
                    CompressionRate.MEDIUM -> "1000k"
                    CompressionRate.HIGH -> "500k"
                }

                val audioCompressBitrate = when (compressionRate) {
                    CompressionRate.LOW -> "192k"
                    CompressionRate.MEDIUM -> "128k"
                    CompressionRate.HIGH -> "64k"
                }

                val inputPath =
                    FFmpegKitConfig.getSafParameterForRead(appContext, pickedFile.uri.toUri())
                val outputExt =
                    appContext.contentResolver.getUriExtension(pickedFile.uri.toUri()).toString()


                val command =
                    if (pickedFile.mimeType == MimeType.VIDEO) "-i $inputPath -b:v $videoCompressBitrate -b:a $audioCompressBitrate" else "-i $inputPath -b:a $audioCompressBitrate"

                viewModelScope.launch {
                    runFFmpeg(
                        command = command,
                        outputFileInfo = OutputFileInfo(
                            outputExt,
                            null
                        )
                    )
                }
            }

            MimeType.IMAGE -> {
                viewModelScope.launch {
                    _state.update { it.copy(isOperating = true) }
                    val ext = appContext.contentResolver.getUriExtension(pickedFile.uri.toUri())
                    if (ext == null) {
                        _state.update { it.copy(isOperating = false) }
                        return@launch
                    }

                    val outputFileInfo = OutputFileInfo(ext, null)
                    val outputUri = mediaStoreManager.createMediaUri(
                        pickedFile,
                        outputFileInfo
                    )
                    if (outputUri == null) {
                        _state.update { it.copy(isOperating = false) }
                        return@launch
                    }

                    val bitmap =
                        pickedFile.uri.toUri().toBitmap(null, null, appContext, scaled = false)
                    val imageQuality = when (compressionRate) {
                        CompressionRate.LOW -> 90
                        CompressionRate.MEDIUM -> 75
                        CompressionRate.HIGH -> 50
                    }
                    mediaStoreManager.writeBitmap(
                        outputUri,
                        outputFileInfo,
                        bitmap, imageQuality
                    )
                    mediaStoreManager.saveMedia(outputUri, pickedFile)
                    _state.update { it.copy(isOperating = false) }
                }
            }
        }
    }

    private fun handleConvert(outputFormat: Format) {
        when (pickedFile.mimeType) {
            MimeType.VIDEO, MimeType.AUDIO -> {
                val inputPath =
                    FFmpegKitConfig.getSafParameterForRead(appContext, pickedFile.uri.toUri())
                viewModelScope.launch {
                    runFFmpeg(
                        command = "-i $inputPath",
                        outputFileInfo = OutputFileInfo(
                            outputFormat.name.lowercase(),
                            null
                        )
                    )
                }
            }

            MimeType.IMAGE -> {
                viewModelScope.launch {
                    _state.update { it.copy(isOperating = true) }
                    val outputFileInfo = OutputFileInfo(outputFormat.name.lowercase(), null)
                    val outputUri = mediaStoreManager.createMediaUri(
                        pickedFile,
                        outputFileInfo
                    )
                    if (outputUri == null) {
                        _state.update { it.copy(isOperating = false) }
                        return@launch
                    }

                    val bitmap =
                        pickedFile.uri.toUri().toBitmap(null, null, appContext, scaled = false)

                    mediaStoreManager.writeBitmap(
                        outputUri,
                        outputFileInfo,
                        bitmap
                    )
                    mediaStoreManager.saveMedia(outputUri, pickedFile)
                    _state.update { it.copy(isOperating = false) }
                }
            }
        }
    }

    private fun handleAudioToVideoConvert(outputFormat: Format, image: Uri) {
        val inputPath =
            FFmpegKitConfig.getSafParameterForRead(appContext, pickedFile.uri.toUri())

        val imageExtension = appContext.contentResolver.getUriExtension(image)
        val imageInputPath = if (imageExtension != "jpg" && imageExtension != "jpeg") {
            val imageBitmap = image.toBitmap(null, null, appContext, false)

            val tempFile = File.createTempFile("temp", ".jpg", appContext.cacheDir)
            val outputStream = java.io.FileOutputStream(tempFile)
            imageBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()

            val tempFileUri = tempFile.toUri()
            FFmpegKitConfig.getSafParameterForRead(appContext, tempFileUri)
        } else FFmpegKitConfig.getSafParameterForRead(appContext, image)

        viewModelScope.launch {
            runFFmpeg(
                command = "-r 1 -loop 1 -i $imageInputPath -i $inputPath -acodec copy -r 1 -pix_fmt yuv420p -tune stillimage -shortest",
                outputFileInfo = OutputFileInfo(
                    outputFormat.name.lowercase(),
                    null
                )
            )
            if (imageExtension != "jpg" && imageExtension != "jpeg") File(imageInputPath).delete()
        }
    }

    private suspend fun runFFmpeg(
        command: String,
        outputFileInfo: OutputFileInfo
    ) = withContext(Dispatchers.IO) {
        val outputUri = mediaStoreManager.createMediaUri(pickedFile, outputFileInfo)
        _state.update { it.copy(isOperating = true) }
        android.util.Log.d("ffmpeg-kit", "format: ${outputFileInfo.extension}, uri: $outputUri")
        outputUri?.let {
            val outputPath = FFmpegKitConfig.getSafParameterForWrite(appContext, outputUri)
            FFmpegKit.executeAsync(
                "-y -protocol_whitelist saf,file,crypto $command $outputPath",
                object : FFmpegSessionCompleteCallback {
                    override fun apply(session: FFmpegSession) {
                        val state = session.getState()

                        when (session.returnCode.value) {
                            ReturnCode.CANCEL -> {
                                viewModelScope.launch {
                                    mediaStoreManager.deleteMedia(it)
                                    _state.update { it.copy(isOperating = false) }
                                }
                            }

                            ReturnCode.SUCCESS -> {
                                viewModelScope.launch {
                                    mediaStoreManager.saveMedia(it, pickedFile)
                                    _state.update { it.copy(isOperating = false) }
                                }
                            }
                        }

                        when (state) {
                            SessionState.FAILED -> {
                                viewModelScope.launch {
                                    mediaStoreManager.deleteMedia(it)
                                    _state.update { it.copy(isOperating = false) }
                                }
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
                        val durationMs = _state.value.metadata?.durationMs ?: 0L
                        val progress = log?.progress(durationMs.div(1000))
                        if (progress != null) _state.update { it.copy(progress = progress) }
                    }
                },
                object : StatisticsCallback {
                    override fun apply(statistics: Statistics?) {
                    }
                })
        }
    }

    private suspend fun handleBgRemove(outputFileInfo: OutputFileInfo, background: Any?) =
        withContext(Dispatchers.IO) {
            _state.update { it.copy(isOperating = true) }
            try {
                val outputUri = mediaStoreManager.createMediaUri(pickedFile, outputFileInfo)
                if (outputUri == null) {
                    _state.update { it.copy(isOperating = false) }
                    return@withContext
                }

                val originalUri = pickedFile.uri.toUri()
                val foregroundBitmap = backgroundRemover.processImage(originalUri)

                val finalResult = createBitmap(foregroundBitmap.width, foregroundBitmap.height)
                val canvas = Canvas(finalResult)
                // Background
                when (background) {
                    is HsvColor -> canvas.drawColor(background.toColorInt())
                    is Uri -> {
                        val backgroundBitmap = background.toBitmap(
                            targetWidth = foregroundBitmap.width,
                            targetHeight = foregroundBitmap.height,
                            context = appContext
                        )
                        canvas.drawBitmap(
                            backgroundBitmap,
                            0f,
                            0f,
                            null
                        )
                    }

                    else -> {
                        if (background != null) Exception("Background can only be HsvColor, Uri and null")
                    }
                }
                canvas.drawBitmap(foregroundBitmap, 0f, 0f, null) // Foreground

                mediaStoreManager.writeBitmap(outputUri, outputFileInfo, finalResult)
                mediaStoreManager.saveMedia(outputUri, pickedFile)

                _state.update {
                    it.copy(outputedFile = outputUri, isOperating = false)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isOperating = false) }
            }
        }
}