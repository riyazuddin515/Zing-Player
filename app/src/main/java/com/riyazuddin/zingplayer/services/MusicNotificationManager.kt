package com.riyazuddin.zingplayer.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.riyazuddin.zingplayer.R
import com.riyazuddin.zingplayer.other.Constants.MUSIC_CHANNEL
import com.riyazuddin.zingplayer.other.Constants.MUSIC_PAUSE
import com.riyazuddin.zingplayer.other.Constants.MUSIC_PLAY
import com.riyazuddin.zingplayer.other.Constants.MUSIC_STOP
import com.riyazuddin.zingplayer.other.Constants.SHOW_NEXT
import com.riyazuddin.zingplayer.other.Constants.SHOW_PREVIOUS
import com.riyazuddin.zingplayer.other.Constants.SKIP_NEXT
import com.riyazuddin.zingplayer.other.Constants.SKIP_PREVIOUS
import com.riyazuddin.zingplayer.other.Constants.TITLE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private const val TAG = "LogITag"

@Singleton
class MusicNotificationManager @Inject constructor(
    @ApplicationContext val context: Context,
    private val glide: RequestManager
) {

    private val randomNumber
        get() = Random.nextInt(0, 999999999)

    fun createNotification(
        imageUrl: String,
        title: String,
        showPlay: Boolean,
        showPrevious: Boolean,
        showNext: Boolean
    ): Notification {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel()

        val playOrPauseIntent = Intent(context, MusicService::class.java)
        if (showPlay) {
            playOrPauseIntent.action = MUSIC_PLAY
        } else {
            playOrPauseIntent.action = MUSIC_PAUSE
        }
        val playOrPausePI: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(
                context,
                randomNumber,
                playOrPauseIntent,
                PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getService(
                context,
                randomNumber,
                playOrPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val stopIntent = Intent(context, MusicService::class.java)
        stopIntent.action = MUSIC_STOP
        val stopPI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(context, randomNumber, stopIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getService(
                context,
                randomNumber,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val isNightTheme =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val notificationLayout = RemoteViews(context.packageName, R.layout.music_layout)
        if (isNightTheme == Configuration.UI_MODE_NIGHT_YES) {
            notificationLayout.setImageViewResource(
                R.id.ivPrevious,
                R.drawable.ic_skip_previous_white
            )
            notificationLayout.setImageViewResource(R.id.ivNext, R.drawable.ic_skip_next_white)
            notificationLayout.setImageViewResource(R.id.ivStop, R.drawable.ic_close_white)
        } else {
            notificationLayout.setImageViewResource(
                R.id.ivPrevious,
                R.drawable.ic_skip_previous_black
            )
            notificationLayout.setImageViewResource(R.id.ivNext, R.drawable.ic_skip_next_black)
            notificationLayout.setImageViewResource(R.id.ivStop, R.drawable.ic_close_black)
        }
        notificationLayout.setTextViewText(R.id.tvTitle, title)
        if (showPlay) {
            notificationLayout.setImageViewResource(R.id.ivPlayOrPause, R.drawable.ic_play)
        } else {
            notificationLayout.setImageViewResource(R.id.ivPlayOrPause, R.drawable.ic_pause_red)
        }
        notificationLayout.setOnClickPendingIntent(R.id.ivPlayOrPause, playOrPausePI)
        notificationLayout.setOnClickPendingIntent(R.id.ivStop, stopPI)
        if (showPrevious) {
            val skipPreviousIntent = Intent(context, MusicService::class.java)
            skipPreviousIntent.action = SKIP_PREVIOUS
            val skipPreviousPI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getService(
                    context,
                    randomNumber,
                    skipPreviousIntent,
                    PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    randomNumber,
                    skipPreviousIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            notificationLayout.setViewVisibility(R.id.ivPrevious, View.VISIBLE)
            notificationLayout.setOnClickPendingIntent(R.id.ivPrevious, skipPreviousPI)
        }
        if (showNext) {
            val skipNextIntent = Intent(context, MusicService::class.java)
            skipNextIntent.action = SKIP_NEXT
            val skipNextPI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getService(
                    context,
                    randomNumber,
                    skipNextIntent,
                    PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    randomNumber,
                    skipNextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            notificationLayout.setViewVisibility(R.id.ivNext, View.VISIBLE)
            notificationLayout.setOnClickPendingIntent(R.id.ivNext, skipNextPI)
        }

        return NotificationCompat.Builder(context, MUSIC_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setCategory(NotificationCompat.EXTRA_MEDIA_SESSION)
            .setSilent(true)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(MUSIC_CHANNEL) == null) {
            val channel = NotificationChannel(
                MUSIC_CHANNEL,
                "This channel is used to show music notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }
    }
}