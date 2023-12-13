package edu.ischool.lton2.tunesmith

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
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
    //TODO: make UI thread
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

        val testSearch = findViewById<Button>(R.id.search_btn)
        testSearch.setOnClickListener{
            networkThread.execute{
                try {
                    searchSong("blah")
                } catch(err: Exception){
                    Log.d(TAG, err.toString())
                }
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate menu with items using MenuInflator
        val inflater = menuInflater
        inflater.inflate(R.menu.top_menu, menu)

        // Initialise menu item search bar
        // with id and take its object
//        val searchViewItem = menu.findItem(R.id.search_bar)
//        val searchView = MenuItemCompat.getActionView(searchViewItem) as SearchView
        val searchView = menu.findItem(R.id.search_bar)?.actionView as? SearchView

        // attach setOnQueryTextListener
        // to search view defined above

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Override onQueryTextSubmit method which is call when submit query is searched
            override fun onQueryTextSubmit(query: String): Boolean {
                // If the list contains the search query than filter the adapter
                // using the filter method with the query as its argument
//                if (mylist.contains(query)) {
//                    adapter.filter.filter(query)
//                } else {
//                    // Search query not found in List View
//                    Toast.makeText(this@MainActivity, "Not found", Toast.LENGTH_LONG).show()
//                }
                return false
            }

            // This method is overridden to filter the adapter according
            // to a search query when the user is typing search
            override fun onQueryTextChange(newText: String): Boolean {
//                adapter.filter.filter(newText)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun searchSong(query: String) {
        Log.i(TAG, "User token: ${sharedPref.getString("AccessToken", "")}")
        val fillerQuery = "White Lie"
        var searchUrl = URL("https://api.spotify.com/v1/search?q=$fillerQuery&type=track&limit=2")
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

        var songResults: MutableList<HomeActivity.Song> = mutableListOf()
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
//            Log.i(TAG, "track name: ${track.getString("name")}")
//            val song = HomeActivity.Song(
//                track.getString("name"),
//                track.getJSONArray("artists").getJSONObject(0).getString("name"),
//                track.getString("id")
//            )
//            songResults.add(song)

        }
    }
}
//TODO: extract to be used across all activities
data class Track(val name: String, val artists: String, val trackId: String, val durationMs: Int)