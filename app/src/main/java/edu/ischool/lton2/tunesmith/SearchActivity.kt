package edu.ischool.lton2.tunesmith

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.widget.SearchView
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
import java.util.concurrent.Executors
import kotlin.reflect.typeOf

class SearchActivity : AppCompatActivity() {
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

//        val testSearch = findViewById<Button>(R.id.search_btn)
//        testSearch.setOnClickListener{
//            networkThread.execute{
//                try {
//                    searchSong("blah")
//                } catch(err: Exception){
//                    Log.d(TAG, err.toString())
//                }
//            }
//
//        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.i(TAG, "oncreateoptionsmenu")
        menuInflater.inflate(R.menu.top_menu, menu)

        val searchViewItem = menu.findItem(R.id.search_bar)
        val searchView = MenuItemCompat.getActionView(searchViewItem) as SearchView
        Log.i(TAG, "searchView: " + searchView.toString())

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.i(TAG, "submitted query")
                searchSong("bkah")
                Log.i(TAG, "searched")
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.i(TAG, "textchaange")
//                adapter.filter.filter(newText)
                return false
            }
        })
        Log.i(TAG, "end of oncreateoptionsmenu")
        return super.onCreateOptionsMenu(menu)
    }

    private fun searchSong(query: String) {
        Log.i(TAG, "User token: ${sharedPref.getString("AccessToken", "")}")
        val fillerQuery = "White Lie"
        var searchUrl = URL("https://api.spotify.com/v1/search?q=$fillerQuery&type=track&limit=2")

        networkThread.execute{
            try {
                var urlConnection = searchUrl.openConnection() as HttpURLConnection
                Log.i(TAG, "searching for tracks matching query")
                urlConnection.setRequestProperty("Authorization", "Bearer ${sharedPref.getString("AccessToken", "")}")
                val checkAuth = urlConnection.getRequestProperty("Authorization")
                Log.i(TAG, "check url auth token: ${checkAuth}")

                val inputStream = urlConnection.inputStream

                val reader  = InputStreamReader(inputStream)
                var tracks: JSONArray
                reader.use {
                    val json = JSONObject(it.readText())
                    tracks = json.getJSONObject("tracks").getJSONArray("items")
                    Log.i(TAG, "search results: $tracks")
                }
                var songResults: MutableList<Song> = mutableListOf()
                // grab "name", duration "duration_ms", "artists" "name" (nested)
                for (i in 0 until tracks.length()) {
                    val track = tracks.getJSONObject(i)
                    Log.i(TAG, "test iterate: $track")
                    Log.i(TAG, "track name: ${track.getString("name")}")
                    Log.i(TAG, "duration: ${track.getInt("duration_ms")}")
                    Log.i(TAG, "song id: ${track.getString("id")}")
                    val artistObj = track.getJSONArray("artists")

                    var artistName = artistObj.getJSONObject(0).getString("name")
                    for (j in 1 until artistObj.length()) {
                        artistName += ", " + artistObj.getJSONObject(j).getString("name")
                    }
                    Log.i(TAG, "artist: $artistName")

                    // missing id rn
                    val trackData = Song(
                        track.getString("name"),
                        artistName,
                        "",
                        track.getInt("duration_ms").toString()
                    )
                    songResults.add(trackData)
                }
                Log.i(TAG, songResults.toString())
            } catch(error: Exception) {
                Log.d(TAG, error.toString())
            }
        }

    }

    private fun displayResults(trackList: MutableList<Song>) {
        this.runOnUiThread{
            val listView = findViewById<ListView>(R.id.listView)
//            listView.adapter = PlaylistAdapter(trackList)
        }
    }
}
