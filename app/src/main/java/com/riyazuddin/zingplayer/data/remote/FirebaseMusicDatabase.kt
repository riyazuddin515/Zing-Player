package com.riyazuddin.zingplayer.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.riyazuddin.zingplayer.data.model.Song
import com.riyazuddin.zingplayer.other.Constants.SONGS_COLLECTION
import kotlinx.coroutines.tasks.await

class FirebaseMusicDatabase {

    companion object {
        const val TAG = "FirebaseMusicDatabase"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val songsCollection = firestore.collection(SONGS_COLLECTION)

    suspend fun getSongsList(): List<Song> {
        return try {
            songsCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "getSongsList: ", e)
            emptyList()
        }
    }
}