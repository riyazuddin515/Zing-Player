package com.riyazuddin.zingplayer.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
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
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LogITag"

@Singleton
class MusicNotificationManager @Inject constructor(
    @ApplicationContext val context: Context
) {

    fun createNotification(
        title: String,
        showPlay: Boolean,
        showPrevious: Boolean,
        showNext: Boolean
    ): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel()

        val playOrPausePI: PendingIntent
        if (showPlay) {
            val playIntent = Intent(context, MusicService::class.java)
            playIntent.action = MUSIC_PLAY
            playIntent.putExtra(TITLE, title)
            playIntent.putExtra(SHOW_PREVIOUS, showPrevious)
            playIntent.putExtra(SHOW_NEXT, showNext)
            playOrPausePI =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getService(context, 500, playIntent, PendingIntent.FLAG_MUTABLE)
                } else {
                    PendingIntent.getService(
                        context,
                        500,
                        playIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
        } else {
            val pauseIntent = Intent(context, MusicService::class.java)
            pauseIntent.action = MUSIC_PAUSE
            pauseIntent.putExtra(TITLE, title)
            pauseIntent.putExtra(SHOW_PREVIOUS, showPrevious)
            pauseIntent.putExtra(SHOW_NEXT, showNext)
            playOrPausePI =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getService(context, 500, pauseIntent, PendingIntent.FLAG_MUTABLE)
                } else {
                    PendingIntent.getService(
                        context,
                        500,
                        pauseIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
        }

        val stopIntent = Intent(context, MusicService::class.java)
        stopIntent.action = MUSIC_STOP
        stopIntent.putExtra(TITLE, title)
        stopIntent.putExtra(SHOW_PREVIOUS, showPrevious)
        stopIntent.putExtra(SHOW_NEXT, showNext)
        val stopPI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(context, 500, stopIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getService(context, 500, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notificationLayout = RemoteViews(context.packageName, R.layout.music_layout)
        notificationLayout.setTextViewText(R.id.tvTitle, title)
        if (showPlay) {
            notificationLayout.setImageViewResource(R.id.ivPlayOrPause, R.drawable.ic_play)
        } else {
            notificationLayout.setImageViewResource(R.id.ivPlayOrPause, R.drawable.ic_pause)
        }
        notificationLayout.setOnClickPendingIntent(R.id.ivPlayOrPause, playOrPausePI)
        notificationLayout.setOnClickPendingIntent(R.id.ivStop, stopPI)
        Log.i(TAG, "createNotification: $showPrevious")
        Log.i(TAG, "createNotification: $showNext")
        if (showPrevious) {
            val skipPreviousIntent = Intent(context, MusicService::class.java)
            skipPreviousIntent.action = SKIP_PREVIOUS
            skipPreviousIntent.putExtra(TITLE, title)
            skipPreviousIntent.putExtra(SHOW_PREVIOUS, showPrevious)
            skipPreviousIntent.putExtra(SHOW_NEXT, showNext)
            val skipPreviousPI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getService(
                    context,
                    500,
                    skipPreviousIntent,
                    PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    500,
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
            skipNextIntent.putExtra(TITLE, title)
            skipNextIntent.putExtra(SHOW_PREVIOUS, showPrevious)
            skipNextIntent.putExtra(SHOW_NEXT, showNext)
            val skipNextPI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getService(context, 500, skipNextIntent, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getService(
                    context,
                    500,
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
        val channel = NotificationChannel(
            MUSIC_CHANNEL,
            "This channel is used to show music notification",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(MUSIC_CHANNEL) == null) {
            notificationManager.createNotificationChannel(channel)
        }
    }
}