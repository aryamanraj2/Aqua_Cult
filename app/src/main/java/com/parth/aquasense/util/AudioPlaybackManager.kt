package com.parth.aquasense.util

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class AudioPlaybackManager @Inject constructor(
    private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private val playbackStateChannel = Channel<PlaybackState>(Channel.CONFLATED)

    companion object {
        private const val TAG = "AudioPlaybackManager"
    }

    val playbackState: Flow<PlaybackState> = playbackStateChannel.receiveAsFlow()

    fun initialize() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_IDLE -> {
                                playbackStateChannel.trySend(PlaybackState.Idle)
                            }
                            Player.STATE_BUFFERING -> {
                                playbackStateChannel.trySend(PlaybackState.Buffering)
                            }
                            Player.STATE_READY -> {
                                if (isPlaying) {
                                    playbackStateChannel.trySend(PlaybackState.Playing)
                                } else {
                                    playbackStateChannel.trySend(PlaybackState.Paused)
                                }
                            }
                            Player.STATE_ENDED -> {
                                playbackStateChannel.trySend(PlaybackState.Completed)
                            }
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        Log.e(TAG, "Playback error: ${error.message}")
                        playbackStateChannel.trySend(PlaybackState.Error(error.message ?: "Unknown error"))
                    }
                })
            }
        }
    }

    fun playAudioFromUrl(url: String) {
        Log.d(TAG, "Playing audio from: $url")
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun pause() {
        exoPlayer?.pause()
        playbackStateChannel.trySend(PlaybackState.Paused)
    }

    fun resume() {
        exoPlayer?.play()
        playbackStateChannel.trySend(PlaybackState.Playing)
    }

    fun stop() {
        exoPlayer?.stop()
        playbackStateChannel.trySend(PlaybackState.Idle)
    }

    fun release() {
        Log.d(TAG, "Releasing ExoPlayer")
        exoPlayer?.release()
        exoPlayer = null
    }

    sealed class PlaybackState {
        data object Idle : PlaybackState()
        data object Buffering : PlaybackState()
        data object Playing : PlaybackState()
        data object Paused : PlaybackState()
        data object Completed : PlaybackState()
        data class Error(val message: String) : PlaybackState()
    }
}
