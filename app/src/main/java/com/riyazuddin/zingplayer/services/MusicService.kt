package com.riyazuddin.zingplayer.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "LogITag"

@AndroidEntryPoint
class MusicService : Service() {

    private val binder = MusicServiceBinder()

    @Inject
    lateinit var exoPlayer: ExoPlayer

    inner class MusicServiceBinder: Binder(){
        fun getMusicService() = this@MusicService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder = binder

    fun setMediaItem(mediaItems: List<MediaItem>){
        exoPlayer.addMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    fun seek(mediaItemIndex: Int) {
        Log.i(TAG, "seek: $mediaItemIndex")
        if (exoPlayer.isPlaying)
            exoPlayer.pause()
        exoPlayer.seekTo(mediaItemIndex, 0L)
        exoPlayer.playWhenReady = true
    }
}