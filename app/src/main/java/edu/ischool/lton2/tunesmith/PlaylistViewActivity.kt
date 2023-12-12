package edu.ischool.lton2.tunesmith

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spotify.protocol.types.Track

class PlaylistViewActivity : AppCompatActivity(), PlaylistAdapter.OnSongClickListener {

    private val TAG = "PlaylistActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_view)
        val listView = findViewById<ListView>(R.id.list_view)

        val playlistAdapter = PlaylistAdapter(playlistExample, this)

        listView.adapter = playlistAdapter

    }

    override fun onSongClick(song: Song) {
        Log.i(TAG, "${song.title} clicked")
        Log.i(TAG, "${(application as SpotifyConnection).getConn()}")
        (application as SpotifyConnection).getConn()?.let {
            val trackURI = song.id
            it.playerApi.play(trackURI).setResultCallback { result ->
                Log.e(TAG, result.toString())
            }
            // Subscribe to PlayerState
            it.playerApi.subscribeToPlayerState().setEventCallback {
                val track: Track = it.track
                Log.d("PlaylistActivity", track.name + " by " + track.artist.name)
            }
        }
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
    val length: String,
    val id: String
    )

val example  = listOf<Song>(
    Song (
        "Disco Man",
        "Remi Wolf",
        "image1",
        "length1",
        "spotify:track:0T7aTl1t15HKHfwep4nANV"
    ),
    Song (
        "Rush",
        "Troye Sivan",
        "image2",
        "length2",
        "spotify:track:3xIMkM5LgbVDkpO74O3Np3"
    ),
    Song (
        "Bounce",
        "Emotional Oranges",
        "image3",
        "length3",
        "spotify:track:3qptm6j356NV9FOJri6OgZ"
    )
)

val playlistExample = Playlist (
    "playlist name",
    "playlist description",
    "playlist image",
    example
)
