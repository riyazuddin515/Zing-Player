package com.riyazuddin.zingplayer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.riyazuddin.zingplayer.MainActivity
import com.riyazuddin.zingplayer.other.Constants.BROADCAST_ACTION
import com.riyazuddin.zingplayer.other.Constants.MEDIA_ITEM_TRANSITION
import com.riyazuddin.zingplayer.other.Constants.MUSIC_BROADCAST
import com.riyazuddin.zingplayer.other.Constants.MUSIC_PAUSE
import com.riyazuddin.zingplayer.other.Constants.MUSIC_PLAY
import com.riyazuddin.zingplayer.other.Constants.MUSIC_STOP
import com.riyazuddin.zingplayer.other.Constants.SEEK_POSITION
import com.riyazuddin.zingplayer.other.Constants.SKIP_NEXT
import com.riyazuddin.zingplayer.other.Constants.SKIP_PREVIOUS
import com.riyazuddin.zingplayer.other.Constants.START_SERVICE_INTENT_ACTION
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

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: service")
        val a = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    sendBroadcastUpdates(MUSIC_PLAY)
                    showNotification(getImageUrl(), currentMusicTitle(), !isPlaying, hasPrevious(), hasNext())
                } else {
                    sendBroadcastUpdates(MUSIC_PAUSE)
                    showNotification(getImageUrl(), currentMusicTitle(), true, hasPrevious(), hasNext())
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                sendBroadcastUpdates(MEDIA_ITEM_TRANSITION)
            }
        }
        exoPlayer.addListener(a)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: ")
        when (intent.action) {
            START_SERVICE_INTENT_ACTION ->{
                val position = intent.getIntExtra(SEEK_POSITION, 0)
                seek(position)
            }
            MUSIC_PLAY -> {
                play()
            }
            MUSIC_PAUSE -> {
                pause()
            }
            MUSIC_STOP -> {
                stop()
                sendBroadcastUpdates(MUSIC_STOP)
                stopForeground(true)
                stopSelf()
            }
            SKIP_PREVIOUS -> {
                playPrevious()
            }
            SKIP_NEXT -> {
                playNext()
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

    fun playPrevious() {
        seek( currentMediaIndex()- 1)
    }

    fun playNext() {
        seek(currentMediaIndex() + 1)
    }

    private fun stop() {
        exoPlayer.stop()
    }

    fun hasPrevious() = exoPlayer.hasPreviousMediaItem()
    fun hasNext() = exoPlayer.hasNextMediaItem()
    fun currentMusicTitle() = exoPlayer.currentMediaItem?.mediaMetadata?.title.toString()
    fun getImageUrl() = exoPlayer.currentMediaItem?.mediaMetadata?.mediaUri.toString()
    fun currentMediaIndex() = exoPlayer.currentMediaItemIndex

    fun seek(mediaItemIndex: Int) {
        try {
            Log.i(TAG, "seek: ${exoPlayer.mediaItemCount}")
            if (isPlaying()) {
                pause()
            }
            exoPlayer.seekTo(mediaItemIndex, 0L)
            play()
            showNotification(
                getImageUrl(),
                currentMusicTitle(),
                false,
                hasPrevious(),
                hasNext()
            )
        } catch (e: Exception) {
            Log.e(TAG, "seek: ", e)
        }
    }

    private fun showNotification(
        imageUrl: String,
        title: String,
        showPlay: Boolean,
        showPrevious: Boolean,
        showNext: Boolean
    ) {
        val notification =
            musicNotificationManager.createNotification(imageUrl, title, showPlay, showPrevious, showNext)
        startForeground(5, notification)
    }

    private fun sendBroadcastUpdates(action: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = MUSIC_BROADCAST
        intent.putExtra(BROADCAST_ACTION, action)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy: Service")
        exoPlayer.release()
        super.onDestroy()
    }
}