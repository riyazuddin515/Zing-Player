package com.riyazuddin.zingplayer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.riyazuddin.zingplayer.other.Constants.MUSIC_PAUSE
import com.riyazuddin.zingplayer.other.Constants.MUSIC_PLAY
import com.riyazuddin.zingplayer.other.Constants.MUSIC_STOP
import com.riyazuddin.zingplayer.other.Constants.SHOW_NEXT
import com.riyazuddin.zingplayer.other.Constants.SHOW_PREVIOUS
import com.riyazuddin.zingplayer.other.Constants.SKIP_NEXT
import com.riyazuddin.zingplayer.other.Constants.SKIP_PREVIOUS
import com.riyazuddin.zingplayer.other.Constants.TITLE
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "LogITag"

@AndroidEntryPoint
class MusicService : Service() {

    private val binder = MusicServiceBinder()

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var musicNotificationManager: MusicNotificationManager

    inner class MusicServiceBinder : Binder() {
        fun getMusicService() = this@MusicService
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val showPrevious = intent.getBooleanExtra(SHOW_PREVIOUS, false)
        val showNext = intent.getBooleanExtra(SHOW_NEXT, false)
        Log.i(TAG, "onStartCommand: ${intent.action}")
        when (intent.action) {
            MUSIC_PLAY -> {
                play()
//                sendCompleteBroadcast(MUSIC_PLAY)
                showNotification(intent.getStringExtra(TITLE)!!, false, showPrevious, showNext)
            }
            MUSIC_PAUSE -> {
                pause()
//                sendCompleteBroadcast(MUSIC_PAUSE)
                showNotification(intent.getStringExtra(TITLE)!!, true, showPrevious, showNext)
            }
            MUSIC_STOP -> {
                stop()
//                sendCompleteBroadcast(MUSIC_STOP)
                stopForeground(true)
                stopSelf()
            }
            SKIP_PREVIOUS -> {
                Log.i(TAG, "onStartCommand: ${exoPlayer.currentMediaItemIndex}")
                seek(exoPlayer.currentMediaItemIndex - 1)
            }
            SKIP_NEXT -> {
                seek(exoPlayer.currentMediaItemIndex + 1)
            }
        }

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder = binder

    fun setMediaItem(mediaItems: List<MediaItem>) {
        exoPlayer.addMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    fun isPlaying() = exoPlayer.isPlaying
    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.release()
    }

    fun seek(mediaItemIndex: Int) {
        if (isPlaying())
            pause()
        exoPlayer.seekTo(mediaItemIndex, 0L)
        play()
        showNotification(
            exoPlayer.currentMediaItem!!.mediaMetadata.title.toString(),
            false,
            exoPlayer.hasPreviousMediaItem(),
            exoPlayer.hasNextMediaItem()
        )
    }

    private fun showNotification(
        title: String,
        showPlay: Boolean,
        showPrevious: Boolean,
        showNext: Boolean
    ) {
        val notification =
            musicNotificationManager.createNotification(title, showPlay, showPrevious, showNext)
        startForeground(5, notification)
    }
}