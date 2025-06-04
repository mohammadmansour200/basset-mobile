// Credit: https://github.com/lincollincol/compose-audiowaveform/blob/master/app/src/main/java/com/linc/audiowaveform/sample/android/AudioPlaybackManager.kt
package com.basset.operations.data.android

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking

class MediaPlaybackManager(
    private val player: Player
) : Player.Listener {

    companion object {
        private const val PLAYER_POSITION_UPDATE_TIME = 500L
    }

    val events: MutableSharedFlow<Event> = MutableSharedFlow()

    private var lastEmittedPosition: Long = 0
    private var handler: Handler? = null

    private val playerPositionRunnable = object : Runnable {
        override fun run() {
            val playbackPosition = player.currentPosition
            // Emit only new player position
            if (playbackPosition != lastEmittedPosition) {
                sendEvent(Event.PositionChanged(playbackPosition))
                lastEmittedPosition = playbackPosition
            }
            handler?.postDelayed(this, PLAYER_POSITION_UPDATE_TIME)
        }
    }

    fun releaseMedia() {
        clearMedia()
        player.removeListener(this)
        handler?.removeCallbacks(playerPositionRunnable)
        handler = null
    }

    fun loadMedia(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        startTrackingPlaybackPosition()
    }

    private fun clearMedia() {
        player.stop()
        player.clearMediaItems()
    }

    private fun startTrackingPlaybackPosition() {
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed(playerPositionRunnable, PLAYER_POSITION_UPDATE_TIME)
        player.addListener(this)
    }

    private fun sendEvent(event: Event) {
        runBlocking { events.emit(event) }
    }

    sealed interface Event {
        data class PositionChanged(val position: Long) : Event
    }
}