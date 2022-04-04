package com.riyazuddin.zingplayer.repository.remote

import com.riyazuddin.zingplayer.data.model.Song

interface IFirebaseMusicDatabase {

    suspend fun getSongsList(): List<Song>
}