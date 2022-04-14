package com.riyazuddin.zingplayer

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

private const val TAG = "UploadedLog"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.btnUpload)
        button.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                listAll()
            }
        }

        val setThumbnailBtn = findViewById<Button>(R.id.btnSetThumbnails)
        setThumbnailBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                setThumbnails()
            }
        }
    }

    private suspend fun listAll() {
        val songsDB = FirebaseFirestore.getInstance().collection("songs")
        val songStorageRef = Firebase.storage.getReference("songs")
        val listResult = songStorageRef.listAll().await()
        var c = 1
        listResult.items.forEach { storageRef ->
            val url = storageRef.downloadUrl.await()
            val name = storageRef.name.lowercase().removeSuffix(".mp3").capitalize()
            val dId = if (c < 10) "media0$c" else "media$c"
            val song = Song(dId, name, url.toString(), "")
            songsDB.document(dId).set(song).await()
            Log.d(TAG, "Success $c")
            c++
        }
    }

    private suspend fun setThumbnails() {
        val songsDB = FirebaseFirestore.getInstance().collection("songs")
        val songList = songsDB.get().await().toObjects(Song::class.java)
        val thumbnailStorageRef = Firebase.storage.getReference("thumbnails")
        val thumbnailList = thumbnailStorageRef.listAll().await()
        var c = 0
        thumbnailList.items.forEach {
            val name = it.name.lowercase().removeSuffix(".jpeg").trim()
            val song = songList.filter { song ->
                song.title.lowercase().trim() == name
            }
            if (song.isNotEmpty()) {
                val url = it.downloadUrl.await().toString()
                song[0].imageUrl = url
                Log.i(TAG, "setThumbnails: ${song[0]}")
                Log.i(TAG, "setThumbnails: <---------------------${++c}---------------------------->")
                songsDB.document(song[0].mediaId).update("imageUrl", url)
            }else{
                Log.e(TAG, "setThumbnails: NOT FOUND -> $name", )
            }

        }
    }

    data class Song(
        val mediaId: String = "",
        val title: String = "",
        val songUrl: String = "",
        var imageUrl: String = ""
    )


}