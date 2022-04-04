package com.riyazuddin.zingplayer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
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

    fun getAllSongs() {
        viewModelScope.launch {
            _songs.postValue(firebaseMusicDatabase.getSongsList())

//            MediaItem.Builder()
//                .setUri(song.songUrl)
//                .setMediaId(song.mediaID)
//                .build()
        }
    }

}