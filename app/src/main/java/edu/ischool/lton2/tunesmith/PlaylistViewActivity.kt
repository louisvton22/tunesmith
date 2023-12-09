package edu.ischool.lton2.tunesmith

import android.os.Bundle
import android.text.Html.ImageGetter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class PlaylistViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_view)
        val listView = findViewById<ListView>(R.id.list_view)
//        listView.adapter =

    }
}

data class Playlist(
    val name: String,
    val description: String,
    val image: ImageGetter,
    val songs: ArrayList<Song>
    )

data class Song(
    val title: String,
    val artist: String, // or list?
    val cover: ImageGetter, // ??
    val length: String
    )

