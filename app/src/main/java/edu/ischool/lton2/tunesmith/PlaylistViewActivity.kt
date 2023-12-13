package edu.ischool.lton2.tunesmith

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PlaylistViewActivity : AppCompatActivity(), NavBar {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_view)

        this.setupNav(this)
        val listView = findViewById<ListView>(R.id.list_view)

        val playlistAdapter = PlaylistAdapter(playlistExample)

        listView.adapter = playlistAdapter

    }
}

data class Playlist(
    val name: String,
    val description: String,
    val image: String,
    val songs: List<Song>
    )

data class Song(
    val title: String,
    val artist: String,
    val cover: String,
    val length: String
    )

val example  = listOf<Song>(
    Song (
        "song1",
        "artist1",
        "image1",
        "length1"
    ),
    Song (
        "song2",
        "artist2",
        "image2",
        "length2"
    )
)

val playlistExample = Playlist (
    "playlist name",
    "playlist description",
    "playlist image",
    example
)
