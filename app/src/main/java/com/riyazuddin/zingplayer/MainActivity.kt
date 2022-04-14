package com.riyazuddin.zingplayer

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.riyazuddin.zingplayer.adapters.SongsAdapter
import com.riyazuddin.zingplayer.databinding.ActivityMainBinding
import com.riyazuddin.zingplayer.other.Constants.BROADCAST_ACTION
import com.riyazuddin.zingplayer.other.Constants.MEDIA_ITEM_TRANSITION
import com.riyazuddin.zingplayer.other.Constants.MUSIC_BROADCAST
import com.riyazuddin.zingplayer.other.Constants.MUSIC_PAUSE
import com.riyazuddin.zingplayer.other.Constants.MUSIC_PLAY
import com.riyazuddin.zingplayer.other.Constants.MUSIC_STOP
import com.riyazuddin.zingplayer.services.MusicService
import com.riyazuddin.zingplayer.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "LogITag"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var songsAdapter: SongsAdapter

    private lateinit var musicService: MusicService
    private var isBounded = false
    private var rebound = false
    private var indexToPlayAfterRebound = 0;
    private lateinit var serviceConnection: ServiceConnection

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
            if (intent.action == MUSIC_BROADCAST) {
                when (intent.getStringExtra(BROADCAST_ACTION)) {
                    MUSIC_PLAY, MUSIC_PAUSE -> showMusicPlayerLayout()
                    MEDIA_ITEM_TRANSITION -> showMusicPlayerLayout()
                    MUSIC_STOP -> {
                        binding.musicPlayerLayout.visibility = View.GONE
                        unBind()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRecyclerView()
        setUpClickListener()
        subscribeToObservers()
        viewModel.getAllSongs()

        songsAdapter.setOnSongClickListener { _, position: Int ->
            if (isBounded) {
                musicService.seek(position)
            } else {
                rebound = true
                indexToPlayAfterRebound = position
                bind()
            }
        }

    }

    private fun showMusicPlayerLayout() {
        binding.ivPrevious.isVisible = musicService.hasPrevious()
        binding.ivNext.isVisible = musicService.hasNext()
        if (musicService.isPlaying())
            binding.ivPlayOrPause.setImageResource(R.drawable.ic_pause)
        else
            binding.ivPlayOrPause.setImageResource(R.drawable.ic_play)
        binding.tvTitle.text = musicService.currentMusicTitle()
        binding.musicPlayerLayout.isVisible = true
    }

    private fun setUpClickListener() {
        binding.ivPlayOrPause.setOnClickListener {
            if (musicService.isPlaying())
                musicService.pause()
            else
                musicService.play()
        }
        binding.ivPrevious.setOnClickListener {
            musicService.playPrevious()
        }
        binding.ivNext.setOnClickListener {
            musicService.playNext()
        }
    }

    private fun startServiceOnAPIBase(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent)
        else
            startService(intent)
    }

    private fun subscribeToObservers() {
        viewModel.songs.observe(this) {
            songsAdapter.list = it
        }
        viewModel.mediaItems.observe(this) {
            musicService.setMediaItem(it)
        }
    }

    private fun setUpRecyclerView() {
        binding.rvSongs.apply {
            adapter = songsAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            itemAnimator = null
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }


    override fun onStart() {
        super.onStart()
        bind()
    }

    private fun bind() {
        Log.i(TAG, "bind: ")
        try {
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
                    musicService = (binder as MusicService.MusicServiceBinder).getMusicService()
                    isBounded = true
                    Log.i(TAG, "onServiceConnected: ")
                    if (rebound) {
                        rebound = false
                        musicService.setMediaItem(viewModel.getMediaItems())
                        musicService.seek(indexToPlayAfterRebound)
                    }
                }

                override fun onServiceDisconnected(p0: ComponentName?) {
                    isBounded = false
                }
            }
            val intent = Intent(this, MusicService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            startServiceOnAPIBase(intent)
            LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                broadcastReceiver, IntentFilter(
                    MUSIC_BROADCAST
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "bind: ", e)
        }
    }

    override fun onStop() {
        unBind()
        super.onStop()
    }

    private fun unBind() {
        Log.i(TAG, "unBind: ")
        unbindService(serviceConnection)
        isBounded = false
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy: activity")
        super.onDestroy()
    }
}