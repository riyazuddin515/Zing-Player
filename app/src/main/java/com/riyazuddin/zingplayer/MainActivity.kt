package com.riyazuddin.zingplayer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.riyazuddin.zingplayer.adapters.SongsAdapter
import com.riyazuddin.zingplayer.databinding.ActivityMainBinding
import com.riyazuddin.zingplayer.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var songsAdapter: SongsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpRecyclerView()
        subscribeToObservers()
        viewModel.getAllSongs()

        songsAdapter.setOnSongClickListener {
            Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
        }
    }

    private fun subscribeToObservers() {
        viewModel.songs.observe(this) {
            songsAdapter.list = it
        }
    }

    private fun setUpRecyclerView() {
        binding.rvSongs.apply {
            adapter = songsAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            itemAnimator = null
        }
    }
}