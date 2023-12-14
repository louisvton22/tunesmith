package edu.ischool.lton2.tunesmith

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.SearchView
import androidx.core.content.edit
import androidx.core.view.MenuItemCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import java.util.concurrent.Executors
import kotlin.reflect.typeOf

class SearchActivity : AppCompatActivity() , PlaylistAdapter.OnSongClickListener, NavBar{
    private val TAG = "SearchActivity"
    var subscription: Subscription<PlayerState>? = null
    var currentlyPlaying: String = ""
    lateinit var spotifyConnection: SpotifyConnection
    lateinit var sharedPref: SharedPreferences
    var selectedSongs: MutableList<Song> = mutableListOf()

    val networkThread = Executors.newSingleThreadExecutor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        invalidateOptionsMenu()
        setContentView(R.layout.activity_search)
        val btnGetRec  = findViewById<Button>(R.id.btnGetRec)
        btnGetRec.text = "Get Recommended Songs"
        btnGetRec.setOnLongClickListener {
            Log.i(TAG, "get recs clicked")
            if (selectedSongs.isNotEmpty()) {
                Log.i(TAG, "Selected songs: $selectedSongs")
                val playlistIntent = Intent(this, PlaylistCreatorActivity::class.java)
                val bundle = Bundle()
                val trackSeeds = selectedSongs.map { song ->
                    song.id.replace("spotify:track:", "")
                }
                bundle.putStringArrayList("Songs", ArrayList(trackSeeds))
                playlistIntent.putExtras(bundle)
                startActivity(playlistIntent)
            }
            true
        }

        spotifyConnection = (application as SpotifyConnection)
        sharedPref = getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE)
        Log.i(TAG, "search activity created")

        this.setupNav(this, R.id.nav_search)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        var item = this
        menuInflater.inflate(R.menu.top_menu, menu)

        val searchViewItem = menu.findItem(R.id.search_bar)
        val searchView = searchViewItem.actionView as SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                var tracklist : List<Song> = listOf()
                networkThread.execute{
                    try {
                        tracklist = searchSong(query, item)
                    } catch (e: Exception) {
                        Log.d(TAG, e.toString())
                    }
                }
                Log.i(TAG, "searched")
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun searchSong(query: String, item: PlaylistAdapter.OnSongClickListener) : List<Song> {
        Log.i(TAG, "User token: ${sharedPref.getString("AccessToken", "")}")
        val processedQuery = query.replace(" ", "+")
        var searchUrl = URL("https://api.spotify.com/v1/search?q=$processedQuery&type=track&limit=20")

        var urlConnection = searchUrl.openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("Authorization", "Bearer ${sharedPref.getString("AccessToken", "")}")
        val inputStream = urlConnection.inputStream
        val reader  = InputStreamReader(inputStream)

        var tracks: JSONArray
        reader.use {
            val json = JSONObject(it.readText())
            tracks = json.getJSONObject("tracks").getJSONArray("items")
            Log.i(TAG, "search results: $tracks")
        }
        var songResults: MutableList<Song> = mutableListOf()
        for (i in 0 until tracks.length()) {
            val track = tracks.getJSONObject(i)
            val artistObj = track.getJSONArray("artists")

            var artistName = artistObj.getJSONObject(0).getString("name")
            for (j in 1 until artistObj.length()) {
                artistName += ", " + artistObj.getJSONObject(j).getString("name")
            }

            var smallImageObj= track.getJSONObject("album")
                .getJSONArray("images")
                .getJSONObject(1)
            Log.i(TAG, smallImageObj.getString("url"))
            val trackData = Song(
                track.getString("name"),
                artistName,
                smallImageObj.getString("url"),
                track.getInt("duration_ms"),
                "spotify:track:${track.getString("id")}",
                false
            )
            songResults.add(trackData)
        }
        Log.i(TAG, songResults.toString())
        val tracklist = songResults.toList()

        this.runOnUiThread{
            val listView = findViewById<ListView>(R.id.listView)
            listView.adapter = PlaylistAdapter(selectedSongs.toList() + tracklist, item)
        }
        return songResults.toList()

    }

    override fun onSongClick(song: Song) {
        Log.i(TAG, "song clicked")
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

    override fun onStop() {
        super.onStop()
        subscription?.cancel()
    }

    //highlight songs selected to generate new playlist
    override fun onSongSelected(song: Song, view: View) {
        // select songs if they are not already in selected category
        val typedValue = TypedValue()
        val ogBgColor = (view.rootView.background as ColorDrawable).color
        val ogTxtColor = this.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        if (!selectedSongs.contains(song)) {
//            view.setBackgroundColor(Color.parseColor("#1DB954"))
            view.setBackgroundColor(Color.DKGRAY)
//            view.findViewById<TextView>(R.id.songArtist).setTextColor(Color.parseColor("#191414"))
//            view.findViewById<TextView>(R.id.songTitle).setTextColor(Color.parseColor("#191414"))
            view.findViewById<TextView>(R.id.songArtist).setTextColor(Color.LTGRAY)
            view.findViewById<TextView>(R.id.songTitle).setTextColor(Color.WHITE)
            view.findViewById<TextView>(R.id.songLength).setTextColor(Color.LTGRAY)
            song.selected = true
            selectedSongs.add(song)
            Log.i(TAG, "Song selected: ${song.title}")
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
//            view.findViewById<TextView>(R.id.songArtist).setTextColor(Color.parseColor("#80FFFFFF"))
//            view.findViewById<TextView>(R.id.songTitle).setTextColor(Color.parseColor("#B3FFFFFF"))
            view.findViewById<TextView>(R.id.songTitle).setTextColor(Color.GRAY)
            view.findViewById<TextView>(R.id.songArtist).setTextColor(Color.LTGRAY)
            view.findViewById<TextView>(R.id.songLength).setTextColor(Color.LTGRAY)
            song.selected = false
            selectedSongs.remove(song)
            Log.i(TAG, "Song deselected: ${song.title}")
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







