// Credit: https://github.com/lincollincol/compose-audiowaveform/blob/master/app/src/main/java/com/linc/audiowaveform/sample/android/AudioPlaybackManager.kt
package com.basset.operations.data.media

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.basset.operations.domain.cut_operation.MediaPlaybackManager
import com.basset.operations.domain.cut_operation.MediaPlaybackManager.Event
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking

class LocalMediaPlaybackManager(
    private val player: Player
) : MediaPlaybackManager, Player.Listener {

    companion object {
        private const val PLAYER_POSITION_UPDATE_TIME = 500L
    }

    override val events: MutableSharedFlow<Event> = MutableSharedFlow()

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

    override fun releaseMedia() {
        clearMedia()
        player.removeListener(this)
        handler?.removeCallbacks(playerPositionRunnable)
        handler = null
    }

    override fun loadMedia(uri: Uri) {
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
}