package edu.ischool.lton2.tunesmith

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class PlaylistViewActivity : AppCompatActivity(), NavBar,  PlaylistAdapter.OnSongClickListener { //?
    private val TAG = "PlaylistActivity"
    private var currentlyPlaying = ""
    private var subscription:Subscription<PlayerState>? = null
    lateinit var sharedPref : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_view)
        sharedPref = getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE)
        //this.setupNav(this, R.id.nav_search)
        var recSongs: MutableList<Song> = mutableListOf()
        //get recommended songs
        var seedSongs = intent.extras?.getStringArrayList("Songs")?.toMutableList()
        Log.i(TAG, seedSongs.toString())
        Executors.newSingleThreadExecutor().execute {
            val seedArtists = seedSongs?.joinToString(separator = ",")
            Log.i(TAG, "$seedArtists")
            val recUrl = URL("https://api.spotify.com/v1/recommendations?limit=5&" +
                    "seed_tracks=$seedArtists")
            val urlConnection = recUrl.openConnection() as HttpURLConnection
            Log.i(TAG, "requesting recommended details")
            urlConnection.setRequestProperty("Authorization", "Bearer ${sharedPref.getString("AccessToken", "")}")

            val inputStream = urlConnection.inputStream
            val reader  = InputStreamReader(inputStream)
            var tracks:JSONArray
            reader.use {
                val json = JSONObject(it.readText())
                Log.i(TAG, "recommended json: $json")
                tracks = json.getJSONArray("tracks")
                Log.i(TAG, "tracks : $tracks")

            }
            recSongs = mutableListOf()
            for (i in 0 until tracks.length()) {
                val track = tracks.getJSONObject(i)
                Log.i(TAG, "track name: ${track.getString("name")}")
                val artistObj = track.getJSONArray("artists")

                var artistName = artistObj.getJSONObject(0).getString("name")
                for (j in 1 until artistObj.length()) {
                    artistName += ", " + artistObj.getJSONObject(j).getString("name")
                }
                Log.i(TAG, "artist: $artistName")

                var smallImageObj= track.getJSONObject("album")
                    .getJSONArray("images")
                    .getJSONObject(2)
                Log.i(TAG, smallImageObj.getString("url"))
                val trackData = Song(
                    track.getString("name"),
                    artistName,
                    smallImageObj.getString("url"),
                    track.getInt("duration_ms").toString(),
                    "spotify:track:${track.getString("id")}",
                    false
                )
                recSongs.add(trackData)

            }
        }

        val listView = findViewById<ListView>(R.id.list_view)

        val playlistAdapter = PlaylistAdapter(recSongs, this)

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

    override fun onSongSelected(song: Song, view: View) {}
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
    val id: String,
    var selected: Boolean //determines whether the song should be highlighted or not
    )

val example  = listOf<Song>(
    Song (
        "song1",
        "artist1",
        "image1",
        "length1",
        "spotify:track:0T7aTl1t15HKHfwep4nANV",
        false
    ),
    Song (
        "song2",
        "artist2",
        "image2",
        "length2",
        "spotify:track:3xIMkM5LgbVDkpO74O3Np3",
        false
    ),
    Song (
        "Bounce",
        "Emotional Oranges",
        "image3",
        "length3",
        "spotify:track:3qptm6j356NV9FOJri6OgZ",
        false
    )
)

val playlistExample = Playlist (
    "playlist name",
    "playlist description",
    "playlist image",
    example
)
