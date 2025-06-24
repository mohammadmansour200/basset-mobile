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
import com.basset.core.domain.model.MediaType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.data.android.getUriExtension
import com.basset.operations.data.android.toBitmap
import com.basset.operations.domain.BackgroundRemover
import com.basset.operations.domain.MediaMetadataDataSource
import com.basset.operations.domain.MediaStoreManager
import com.basset.operations.domain.model.CompressionRate
import com.basset.operations.domain.model.OperationError
import com.basset.operations.presentation.utils.parseFfmpegError
import com.basset.operations.presentation.utils.progress
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _events = Channel<OperationScreenEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val metadata =
                metadataDataSource.loadMetadata(pickedFile.uri.toUri(), pickedFile.mediaType)
            _state.update { it.copy(metadata = metadata) }
        }
    }

    fun onAction(action: OperationScreenAction) {
        when (action) {
            is OperationScreenAction.OnCut -> safeExecute { handleCut(action.start, action.end) }
            is OperationScreenAction.OnCompress -> safeExecute {
                handleCompress(
                    compressionRate = action.compressionRate
                )
            }

            is OperationScreenAction.OnConvert -> safeExecute {
                handleConvert(
                    outputExtension = action.outputExtension
                )
            }

            is OperationScreenAction.OnRemoveBackground -> {
                safeExecute {
                    handleBgRemove(
                        outputName = null,
                        outputExtension = appContext.getUriExtension(pickedFile.uri.toUri())
                            .toString(),
                        background = action.background
                    )
                }
            }

            is OperationScreenAction.OnAudioToVideoConvert -> safeExecute {
                handleAudioToVideoConvert(
                    outputExtension = action.outputExtension,
                    image = action.image
                )
            }
        }
    }

    private suspend fun handleError(operationError: OperationError, e: Throwable?, uri: Uri?) {
        uri?.let { mediaStoreManager.deleteMedia(uri) }
        _state.update {
            it.copy(
                isOperating = false,
                operationError = operationError,
                detailedErrorMessage = e?.localizedMessage
            )
        }
        _events.send(OperationScreenEvent.Error)
    }

    private suspend fun handleSuccess(uri: Uri) {
        mediaStoreManager.saveMedia(uri)
        _state.update {
            it.copy(
                isOperating = false,
                outputedFile = uri
            )
        }
        _events.send(OperationScreenEvent.Success)
    }

    private fun safeExecute(operationBlock: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isOperating = true,
                    operationError = null,
                )
            }
            try {
                operationBlock()
            } catch (e: Throwable) {
                handleError(operationError = OperationError.ERROR_UNKNOWN, e = e, uri = null)
            }
        }
    }

    private suspend fun handleCut(start: Double, end: Double) {
        val inputPath =
            FFmpegKitConfig.getSafParameterForRead(appContext, pickedFile.uri.toUri())
        runFFmpeg(
            command = "-ss $start -to $end -i $inputPath -c copy",
            outputName = null,
            outputExtension = appContext.getUriExtension(pickedFile.uri.toUri()).toString(),
        )
    }

    private suspend fun handleCompress(compressionRate: CompressionRate) {
        val outputExt =
            appContext.getUriExtension(pickedFile.uri.toUri()).toString()

        when (pickedFile.mediaType) {
            MediaType.VIDEO, MediaType.AUDIO -> {
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

                val command =
                    if (pickedFile.mediaType == MediaType.VIDEO) "-i $inputPath -b:v $videoCompressBitrate -b:a $audioCompressBitrate -preset ultrafast" else "-i $inputPath -b:a $audioCompressBitrate -preset ultrafast"

                runFFmpeg(
                    command = command,
                    outputName = null,
                    outputExtension = outputExt,
                )
            }

            MediaType.IMAGE -> {
                mediaStoreManager.createMediaUri(
                    pickedFile.uri.toUri(),
                    name = null,
                    extension = outputExt
                ).fold(onSuccess = { uri ->
                    if (uri == null) {
                        handleError(OperationError.ERROR_INVALID_FORMAT, null, null)
                        return@fold
                    }

                    val bitmap =
                        pickedFile.uri.toUri()
                            .toBitmap(
                                targetWidth = null,
                                targetHeight = null,
                                context = appContext,
                                scaled = false
                            )
                    val imageQuality = when (compressionRate) {
                        CompressionRate.LOW -> 90
                        CompressionRate.MEDIUM -> 75
                        CompressionRate.HIGH -> 50
                    }
                    mediaStoreManager.writeBitmap(
                        uri = uri,
                        extension = outputExt,
                        bitmap = bitmap,
                        quality = imageQuality
                    ).onFailure {
                        handleError(OperationError.ERROR_WRITING_OUTPUT, null, uri)
                        return@fold
                    }
                    handleSuccess(uri)
                }, onFailure = {
                    handleError(OperationError.ERROR_WRITING_OUTPUT, null, null)
                    return@fold
                })
            }
        }
    }

    private suspend fun handleConvert(outputExtension: String) {
        val outputName = null
        when (pickedFile.mediaType) {
            MediaType.VIDEO, MediaType.AUDIO -> {
                val inputPath =
                    FFmpegKitConfig.getSafParameterForRead(appContext, pickedFile.uri.toUri())
                runFFmpeg(
                    command = "-i $inputPath",
                    outputName = outputName,
                    outputExtension = outputExtension
                )
            }

            MediaType.IMAGE -> {
                mediaStoreManager.createMediaUri(
                    pickedFile.uri.toUri(),
                    name = outputName,
                    extension = outputExtension
                ).fold(onSuccess = { uri ->
                    if (uri == null) {
                        handleError(OperationError.ERROR_INVALID_FORMAT, null, null)
                        return@fold
                    }

                    val bitmap =
                        pickedFile.uri.toUri()
                            .toBitmap(
                                targetWidth = null,
                                targetHeight = null,
                                context = appContext,
                                scaled = false
                            )

                    mediaStoreManager.writeBitmap(
                        uri,
                        outputExtension,
                        bitmap
                    ).onFailure {
                        handleError(OperationError.ERROR_WRITING_OUTPUT, null, uri)
                        return@fold
                    }
                    handleSuccess(uri)
                }, onFailure = {
                    handleError(OperationError.ERROR_WRITING_OUTPUT, null, null)
                    return@fold
                })
            }
        }
    }

    private suspend fun handleAudioToVideoConvert(outputExtension: String, image: Uri) {
        val inputPath =
            FFmpegKitConfig.getSafParameterForRead(appContext, pickedFile.uri.toUri())

        // For a certain reason FFmpeg only supports jpeg format
        val cacheDir = appContext.cacheDir
        val cachedImageFile = File(cacheDir, "temp.jpeg")

        val bitmap = image.toBitmap(
            targetWidth = null,
            targetHeight = null,
            context = appContext,
            scaled = false
        )
        val writtenBitmap = mediaStoreManager.writeBitmap(
            cachedImageFile.toUri(),
            "jpeg",
            bitmap
        )

        if (writtenBitmap.isFailure) {
            handleError(OperationError.ERROR_IMAGE_PROCESSING, null, null)
            return
        }

        runFFmpeg(
            command = "-r 1 -loop 1 -i $cachedImageFile -i $inputPath -acodec copy -r 1 -pix_fmt yuv420p -tune stillimage -shortest",
            null,
            outputExtension
        )
        cachedImageFile.delete()
    }

    private suspend fun runFFmpeg(
        command: String,
        outputName: String?,
        outputExtension: String,
    ) = withContext(Dispatchers.IO) {
        mediaStoreManager.createMediaUri(pickedFile.uri.toUri(), outputName, outputExtension)
            .fold(onSuccess = { uri ->
                if (uri == null) {
                    handleError(OperationError.ERROR_INVALID_FORMAT, null, null)
                    return@withContext
                }

                android.util.Log.d(
                    "ffmpeg-kit",
                    "format: ${outputExtension}, uri: $uri"
                )
                val outputPath = FFmpegKitConfig.getSafParameterForWrite(appContext, uri)

                FFmpegKit.executeAsync(
                    "-y -protocol_whitelist saf,file,crypto $command $outputPath",
                    object : FFmpegSessionCompleteCallback {
                        override fun apply(session: FFmpegSession) {
                            val returnCode = session.returnCode
                            val logs = session.allLogsAsString

                            viewModelScope.launch {
                                when {
                                    returnCode.isValueSuccess -> {
                                        handleSuccess(uri)
                                    }

                                    returnCode.isValueCancel -> {
                                        mediaStoreManager.deleteMedia(uri)
                                        _state.update {
                                            it.copy(
                                                isOperating = false,
                                            )
                                        }
                                    }

                                    returnCode.isValueError -> {
                                        val parsedError = parseFfmpegError(logs)
                                        handleError(parsedError, null, uri)
                                    }
                                }
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
                    null
                )
            }, onFailure = {
                handleError(OperationError.ERROR_WRITING_OUTPUT, null, null)
                return@withContext
            })
    }

    private suspend fun handleBgRemove(
        outputName: String?,
        outputExtension: String,
        background: Any?
    ) =
        withContext(Dispatchers.IO) {
            _state.update { it.copy(isOperating = true) }
            mediaStoreManager.createMediaUri(
                pickedFile.uri.toUri(),
                outputName,
                outputExtension
            ).fold(onSuccess = { uri ->
                if (uri == null) {
                    handleError(OperationError.ERROR_INVALID_FORMAT, null, null)
                    return@withContext
                }

                val originalUri = pickedFile.uri.toUri()
                val foregroundBitmap =
                    try {
                        backgroundRemover.processImage(originalUri)
                    } catch (_: Throwable) {
                        handleError(OperationError.ERROR_BACKGROUND_REMOVAL, null, uri)
                        return@withContext
                    }

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
                        if (background != null) RuntimeException("Background can only be HsvColor, Uri and null")
                    }
                }

                canvas.drawBitmap(foregroundBitmap, 0f, 0f, null) // Foreground

                mediaStoreManager.writeBitmap(uri, outputExtension, finalResult)
                    .onFailure {
                        handleError(OperationError.ERROR_WRITING_OUTPUT, null, uri)
                        return@withContext
                    }
                handleSuccess(uri)

            }, onFailure = {
                handleError(OperationError.ERROR_WRITING_OUTPUT, null, null)
                return@withContext
            })
        }
}