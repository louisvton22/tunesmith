package edu.ischool.lton2.tunesmith

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track

class PlaylistViewActivity : AppCompatActivity(), PlaylistAdapter.OnSongClickListener {

    private val TAG = "PlaylistActivity"
    private var currentlyPlaying = ""
    private var subscription:Subscription<PlayerState>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_view)
        val listView = findViewById<ListView>(R.id.list_view)

        val playlistAdapter = PlaylistAdapter(playlistExample.songs, this)

        listView.adapter = playlistAdapter

    }

    override fun onSongClick(song: Song) {
        Log.i(TAG, "${song.title} clicked")
        Log.i(TAG, "$song click")

        if(currentlyPlaying == song.title) {
            // song currently playing, pause the song
            pausePlayer()
            currentlyPlaying = ""
        } else {
            currentlyPlaying = song.title
        (application as SpotifyConnection).getConn()?.let { appRemote ->
            val trackURI = song.id
            Log.d("song id", trackURI)
            // Set shuffle mode to OFF (optional)
            appRemote.playerApi.setShuffle(false).setResultCallback { _ ->
                Log.e(TAG, "Set shuffle mode to OFF")
            }
            subscription?.cancel()
            appRemote.playerApi.play(trackURI).setResultCallback { _ ->
                Log.i(TAG, "Start new song")
                Handler().postDelayed({
                    subscription = appRemote.playerApi.subscribeToPlayerState().setEventCallback {

                        val track: Track = it.track
                        Log.d(
                            "PlaylistActivity",
                            track.name + " by " + track.artist.name + " track id: ${track.uri} song selected: $trackURI"
                        )
                        if (track.uri != trackURI) {
                            // Song has changed, pause the player
                            pausePlayer()
                        }
                    }
                }, 500)
            }

        }
        }
    }
    private fun pausePlayer() {
        (application as SpotifyConnection).getConn()?.let { appRemote ->
            appRemote.playerApi.pause().setResultCallback { _ ->
                Log.e(TAG, "Paused playback after one song")
            }
        }
        subscription?.cancel()
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
