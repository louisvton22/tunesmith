package edu.ischool.lton2.tunesmith

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

class SearchActivity : AppCompatActivity() , PlaylistAdapter.OnSongClickListener{
    private val TAG = "SearchActivity"
    lateinit var bottomNav : BottomNavigationView

    lateinit var spotifyConnection: SpotifyConnection
    lateinit var sharedPref: SharedPreferences

    val networkThread = Executors.newSingleThreadExecutor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        spotifyConnection = (application as SpotifyConnection)
        sharedPref = getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE)

        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    val homeIntent = Intent(this, HomeActivity::class.java)
                    startActivity(homeIntent)
                    true
                }
                R.id.nav_search -> {
                    val searchIntent = Intent(this, SearchActivity::class.java)
                    startActivity(searchIntent)
                    true
                }
                else -> {true}
            }

        }
        Log.i(TAG, "search activity created")

//        val songListView = findViewById<ListView>(R.id.listView)
//        val searchAdapter = SearchResultsAdapter(layoutTester)
//        songListView.adapter = searchAdapter

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        var item = this

        Log.i(TAG, "oncreateoptionsmenu")
        menuInflater.inflate(R.menu.top_menu, menu)

        val searchViewItem = menu.findItem(R.id.search_bar)
        val searchView = searchViewItem.actionView as SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.i(TAG, "submitted query")
                var tracklist : List<Song> = listOf()
                networkThread.execute{
                    try {
                        tracklist = searchSong("filler", item)
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
        var fillerQuery = "White Lie"
        fillerQuery = fillerQuery.replace(" ", "+")
        var searchUrl = URL("https://api.spotify.com/v1/search?q=$fillerQuery&type=track&limit=15")

        var urlConnection = searchUrl.openConnection() as HttpURLConnection
        Log.i(TAG, "searching for tracks matching query")
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
            Log.i(TAG, "artist: $artistName")

            val trackData = Song(
                track.getString("name"),
                artistName,
                "", //TODO
                track.getInt("duration_ms").toString(),
                track.getString("id")
            )
            songResults.add(trackData)
        }
        Log.i(TAG, songResults.toString())
        val tracklist = songResults.toList()
        this.runOnUiThread{
            val listView = findViewById<ListView>(R.id.listView)
            Log.i(TAG, "tracklist before adding to adapter: $tracklist")
            listView.adapter = PlaylistAdapter(tracklist, item)
        }
        return songResults.toList()

    }

    override fun onSongClick(song: Song) {
        Log.i(TAG, "song clicked")
        TODO("Not yet implemented")
    }

}







