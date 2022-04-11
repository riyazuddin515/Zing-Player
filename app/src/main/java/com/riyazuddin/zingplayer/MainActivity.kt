package com.riyazuddin.zingplayer

import android.os.Bundle
import android.util.Log
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

        CoroutineScope(Dispatchers.IO).launch {
            listAll()
        }
    }

    private suspend fun listAll() {
        val songsDB = FirebaseFirestore.getInstance().collection("songs")
        val songStorageRef = Firebase.storage.getReference("songs")
        val listResult = songStorageRef.listAll().await()
        var c = 0
        listResult.items.forEach { storageRef ->
            val url = storageRef.downloadUrl.await()
            val name =
                storageRef.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val song = Song((++c).toString(), name, url.toString(), "")
            val dId = if (c < 10) "media0$c" else "media$c"
            songsDB.document(dId).set(song).await()
            Log.d(TAG, "Success $c")
        }
    }

    data class Song(
        val mediaId: String,
        val title: String,
        val songUrl: String,
        val imageUrl: String
    )


}