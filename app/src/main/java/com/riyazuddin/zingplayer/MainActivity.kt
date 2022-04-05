package com.riyazuddin.zingplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.riyazuddin.zingplayer.adapters.SongsAdapter
import com.riyazuddin.zingplayer.data.model.Song
import com.riyazuddin.zingplayer.databinding.ActivityMainBinding
import com.riyazuddin.zingplayer.other.Constants.START_SERVICE_INTENT_ACTION
import com.riyazuddin.zingplayer.services.MusicService
import com.riyazuddin.zingplayer.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.log

private const val TAG = "LogITag"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var songsAdapter: SongsAdapter

    private lateinit var musicService: MusicService
    private var isBounded = false
    private val serviceConnection = object : ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
            musicService = (binder as MusicService.MusicServiceBinder).getMusicService()
            isBounded = true
            Log.i(TAG, "onServiceConnected: ")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBounded = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRecyclerView()
        subscribeToObservers()
        viewModel.getAllSongs()

        songsAdapter.setOnSongClickListener { _ , position: Int ->
            musicService.seek(position)
        }
    }

    private fun subscribeToObservers() {
        viewModel.songs.observe(this){
            songsAdapter.list = it
        }
        viewModel.mediaItems.observe(this){
            musicService.setMediaItem(it)
        }
    }

    private fun setUpRecyclerView() {
        binding.rvSongs.apply {
            adapter = songsAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            itemAnimator = null
        }
    }


    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicService::class.java)
        intent.action = START_SERVICE_INTENT_ACTION
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        startService(intent)
        Log.i(TAG, "onStart: ")
    }

    override fun onStop() {
        if (isBounded) {
            unbindService(serviceConnection)
        }
        Log.i(TAG, "onStop: ")
        super.onStop()
    }
}