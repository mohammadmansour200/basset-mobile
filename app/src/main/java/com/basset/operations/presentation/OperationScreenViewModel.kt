package com.basset.operations.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.basset.core.domain.model.MediaType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.domain.BackgroundRemover
import com.basset.operations.domain.MediaMetadataDataSource
import com.basset.operations.domain.MediaStoreManager
import com.basset.operations.domain.model.OperationError
import com.basset.operations.domain.model.Rate
import com.basset.operations.presentation.utils.parseFfmpegError
import com.basset.operations.presentation.utils.progress
import com.basset.operations.utils.FastBlur
import com.basset.operations.utils.compressPdf
import com.basset.operations.utils.toBitmap
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toColorInt
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

    private val deferred = CompletableDeferred<Boolean>()

    private val pickedUri = pickedFile.uri.toUri()

    private val isAndroidQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q


    init {
        viewModelScope.launch {
            val metadata =
                metadataDataSource.loadMetadata(pickedUri, pickedFile.mediaType)
            _state.update { it.copy(metadata = metadata) }
        }
    }

    fun onAction(action: OperationScreenAction) {
        when (action) {
            is OperationScreenAction.OnCut -> safeExecute { handleCut(action.start, action.end) }
            is OperationScreenAction.OnCompress -> safeExecute {
                handleCompress(
                    rate = action.rate
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
                        outputExtension = _state.value.metadata.ext.toString(),
                        newBackground = action.background
                    )
                }
            }

            is OperationScreenAction.OnAudioToVideoConvert -> safeExecute {
                handleAudioToVideoConvert(
                    outputExtension = action.outputExtension,
                    image = action.image
                )
            }

            is OperationScreenAction.OnSetOutputAlbumArt -> {
                _state.update {
                    it.copy(
                        outputAlbumArt = action.album
                    )
                }
            }

            is OperationScreenAction.OnSetOutputAuthor -> {
                _state.update {
                    it.copy(
                        outputAuthor = action.author
                    )
                }
            }

            is OperationScreenAction.OnSetOutputFilename -> {
                _state.update {
                    it.copy(
                        outputFilename = action.filename
                    )
                }
            }
        }
    }

    private suspend fun handleError(operationError: OperationError, e: Throwable?, uri: Uri?) {
        uri?.let { mediaStoreManager.deleteMedia(uri) }
        android.util.Log.e("OperationScreenViewModel", "Error: ${operationError.name}", e)
        _state.update {
            it.copy(
                isOperating = false,
                operationError = operationError,
                detailedErrorMessage = e?.message
            )
        }
        _events.send(OperationScreenEvent.Error)
    }

    private suspend fun handleSuccess(uri: Uri) {
        mediaStoreManager.saveMedia(uri)
        val metadata = metadataDataSource.loadCompactMetadata(uri)
        _state.update {
            it.copy(
                isOperating = false,
                outputedFile = uri,
                outputedFileMetadata = metadata
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

            val isWriteGranted =
                ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) ==
                        PackageManager.PERMISSION_GRANTED

            if (!isWriteGranted && !isAndroidQOrLater) {
                _events.send(OperationScreenEvent.PermissionRequired(deferred))
                val granted = deferred.await()

                if (!granted) {
                    handleError(operationError = OperationError.ERROR_WRITING_OUTPUT, null, null)
                    return@launch
                }
            }

            try {
                operationBlock()
            } catch (e: Throwable) {
                handleError(operationError = OperationError.ERROR_UNKNOWN, e = e, uri = null)
            }
        }
    }

    private suspend fun handleCut(start: Double, end: Double) {
        val outputExt =
            _state.value.metadata.ext.toString()

        val inputPath =
            FFmpegKitConfig.getSafParameterForRead(appContext, pickedUri)
        val albumPath =
            _state.value.outputAlbumArt?.let {
                FFmpegKitConfig.getSafParameterForRead(
                    appContext,
                    it
                )
            }

        val album =
            if (_state.value.outputAlbumArt != null && outputExt == "mp3") "-i $albumPath -map 0:a -map 1:v -id3v2_version 3 -metadata:s:v title=\"Album cover\" -metadata:s:v comment=\"Cover (front)\"" else ""
        runFFmpeg(
            command = "-ss $start -to $end -i $inputPath $album -c copy",
            outputExtension = outputExt,
        )
    }

    private suspend fun handleCompress(rate: Rate) {
        val outputExt =
            _state.value.metadata.ext.toString()

        val imageQuality = when (rate) {
            Rate.LOW -> 90
            Rate.MEDIUM -> 75
            Rate.HIGH -> 50
        }

        when (pickedFile.mediaType) {
            MediaType.VIDEO, MediaType.AUDIO -> {
                val metadataBitrate = _state.value.metadata.bitrate

                val lowCompressRate = metadataBitrate?.times(0.9)?.toLong()
                val mediumCompressRate = metadataBitrate?.times(0.6)?.toLong()
                val highCompressRate = metadataBitrate?.times(0.3)?.toLong()

                val videoCompressBitrate = when (rate) {
                    Rate.LOW -> "${lowCompressRate ?: 2000}k"
                    Rate.MEDIUM -> "${mediumCompressRate ?: 1000}k"
                    Rate.HIGH -> "${highCompressRate ?: 500}k"
                }

                val audioCompressBitrate = when (rate) {
                    Rate.LOW -> "${lowCompressRate ?: 192}k"
                    Rate.MEDIUM -> "${mediumCompressRate ?: 128}k"
                    Rate.HIGH -> "${highCompressRate ?: 64}k"
                }

                val inputPath =
                    FFmpegKitConfig.getSafParameterForRead(appContext, pickedUri)
                val albumPath =
                    _state.value.outputAlbumArt?.let {
                        FFmpegKitConfig.getSafParameterForRead(
                            appContext,
                            it
                        )
                    }

                val albumArt =
                    if (_state.value.outputAlbumArt != null && outputExt == "mp3") "-i $albumPath -map 0:a -map 1:v -id3v2_version 3 -metadata:s:v title=\"Album cover\" -metadata:s:v comment=\"Cover (front)\"" else ""
                val command =
                    if (pickedFile.mediaType == MediaType.VIDEO) "-i $inputPath -b:v $videoCompressBitrate -b:a $audioCompressBitrate -preset ultrafast" else "-i $inputPath $albumArt -b:a $audioCompressBitrate -preset ultrafast"
                runFFmpeg(
                    command = command,
                    outputExtension = outputExt,
                )
            }

            MediaType.IMAGE -> {
                mediaStoreManager.createMediaUri(
                    pickedUri,
                    name = _state.value.outputFilename,
                    extension = outputExt
                ).fold(onSuccess = { uri ->
                    if (uri == null) {
                        handleError(OperationError.ERROR_INVALID_FORMAT, null, null)
                        return@fold
                    }

                    val bitmap =
                        pickedUri
                            .toBitmap(
                                targetWidth = null,
                                targetHeight = null,
                                context = appContext,
                                scaled = false
                            )

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

            MediaType.PDF -> {
                mediaStoreManager.createMediaUri(
                    pickedUri,
                    name = _state.value.outputFilename,
                    extension = outputExt
                ).fold(onSuccess = { uri ->
                    if (uri == null) {
                        handleError(OperationError.ERROR_INVALID_FORMAT, null, null)
                        return@fold
                    }

                    compressPdf(
                        context = appContext,
                        inputUri = pickedUri,
                        outputUri = uri,
                        quality = imageQuality
                    )

                    handleSuccess(uri)
                }, onFailure = {
                    handleError(OperationError.ERROR_WRITING_OUTPUT, null, null)
                    return@fold
                })
            }
        }
    }

    private suspend fun handleConvert(outputExtension: String) {
        when (pickedFile.mediaType) {
            MediaType.VIDEO, MediaType.AUDIO -> {
                val inputPath =
                    FFmpegKitConfig.getSafParameterForRead(appContext, pickedUri)
                val albumPath =
                    _state.value.outputAlbumArt?.let {
                        FFmpegKitConfig.getSafParameterForRead(
                            appContext,
                            it
                        )
                    }

                val album =
                    if (_state.value.outputAlbumArt != null && outputExtension == "mp3") "-i $albumPath -map 0:a -map 1:v -id3v2_version 3 -metadata:s:v title=\"Album cover\" -metadata:s:v comment=\"Cover (front)\"" else ""
                runFFmpeg(
                    command = "-i $inputPath $album",
                    outputExtension = outputExtension
                )
            }

            else -> {
                mediaStoreManager.createMediaUri(
                    pickedUri,
                    name = _state.value.outputFilename,
                    extension = outputExtension
                ).fold(onSuccess = { uri ->
                    if (uri == null) {
                        handleError(OperationError.ERROR_INVALID_FORMAT, null, null)
                        return@fold
                    }

                    val bitmap =
                        pickedUri
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
        val inputImagePath = FFmpegKitConfig.getSafParameterForRead(appContext, image)
        val inputAudioPath =
            FFmpegKitConfig.getSafParameterForRead(appContext, pickedUri)

        runFFmpeg(
            command = "-i $inputImagePath -i $inputAudioPath -c:v libx264 -c:a aac -pix_fmt yuv420p -tune stillimage -vf \"scale=1280:-1,pad=ceil(iw/2)*2:ceil(ih/2)*2\"",
            outputExtension
        )
    }

    private suspend fun runFFmpeg(
        command: String,
        outputExtension: String,
    ) = withContext(Dispatchers.IO) {
        mediaStoreManager.createMediaUri(pickedUri, _state.value.outputFilename, outputExtension)
            .fold(onSuccess = { uri ->
                if (uri == null) {
                    handleError(OperationError.ERROR_INVALID_FORMAT, null, null)
                    return@withContext
                }

                Log.d(
                    "ffmpeg-kit",
                    "format: ${outputExtension}, uri: $uri"
                )
                val outputPath = if (isAndroidQOrLater) FFmpegKitConfig.getSafParameterForWrite(
                    appContext,
                    uri
                ) else uri.path
                val titleMetadata =
                    if (_state.value.outputFilename != null) "-metadata title=\"${_state.value.outputFilename}\"" else ""
                val authorMetadata =
                    if (_state.value.outputAuthor != null) "-metadata artist=\"${_state.value.outputAuthor}\"" else ""

                FFmpegKit.executeAsync(
                    "-y -protocol_whitelist saf,file,crypto $command $titleMetadata $authorMetadata $outputPath",
                    { session ->
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
                    },
                    { log ->
                        android.util.Log.d("ffmpeg-kit", log?.message.toString())
                        val durationMs = _state.value.metadata.durationMs ?: 0L
                        val progress = log?.progress(durationMs.div(1000))
                        if (progress != null) _state.update { it.copy(progress = progress) }
                    },
                    null
                )
            }, onFailure = {
                handleError(OperationError.ERROR_WRITING_OUTPUT, null, null)
                return@withContext
            })
    }

    private suspend fun handleBgRemove(
        outputExtension: String,
        newBackground: Any?
    ) =
        withContext(Dispatchers.IO) {
            _state.update { it.copy(isOperating = true) }
            mediaStoreManager.createMediaUri(
                pickedUri,
                _state.value.outputFilename,
                outputExtension
            ).fold(onSuccess = { uri ->
                if (uri == null) {
                    handleError(OperationError.ERROR_INVALID_FORMAT, null, null)
                    return@withContext
                }

                val pickedFileBitmap = pickedUri.toBitmap(
                    targetWidth = null,
                    targetHeight = null,
                    context = appContext,
                )

                val (backgroundBitmap, foregroundBitmap) = backgroundRemover.processImage(
                    pickedFileBitmap
                )

                val finalResult = createBitmap(pickedFileBitmap.width, pickedFileBitmap.height)
                val canvas = Canvas(finalResult)
                // Background
                when (newBackground) {
                    is HsvColor -> canvas.drawColor(newBackground.toColorInt())
                    is Uri -> {
                        val newBackgroundBitmap = newBackground.toBitmap(
                            targetWidth = foregroundBitmap.width,
                            targetHeight = foregroundBitmap.height,
                            context = appContext
                        )
                        canvas.drawBitmap(
                            newBackgroundBitmap,
                            0f,
                            0f,
                            null
                        )
                    }

                    is Rate -> {
                        val blurRadius = when (newBackground) {
                            Rate.LOW -> 25
                            Rate.MEDIUM -> 50
                            Rate.HIGH -> 75
                        }
                        // Credit: https://github.com/wasabeef/glide-transformations
                        val bitmap = FastBlur.blur(backgroundBitmap, blurRadius, true)

                        canvas.drawBitmap(bitmap as Bitmap, 0f, 0f, null)
                    }

                    else -> {
                        if (newBackground != null) throw RuntimeException("Background can only be HsvColor, Uri and null")
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