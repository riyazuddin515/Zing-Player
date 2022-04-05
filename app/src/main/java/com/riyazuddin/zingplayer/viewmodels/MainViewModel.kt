package com.riyazuddin.zingplayer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.riyazuddin.zingplayer.data.model.Song
import com.riyazuddin.zingplayer.repository.remote.IFirebaseMusicDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseMusicDatabase: IFirebaseMusicDatabase
) : ViewModel() {

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _mediaItems = MutableLiveData<List<MediaItem>>()
    val mediaItems: LiveData<List<MediaItem>> = _mediaItems

    fun getAllSongs() {
        viewModelScope.launch {
            val list = firebaseMusicDatabase.getSongsList()
            val mediaItems = list.map { song ->
                MediaItem.Builder()
                    .setUri(song.songUrl)
                    .setMediaId(song.mediaID)
                    .setMediaMetadata(MediaMetadata.Builder().setTitle(song.title).build())
                    .build()
            }
            _songs.postValue(list)
            _mediaItems.postValue(mediaItems)
        }
    }

}