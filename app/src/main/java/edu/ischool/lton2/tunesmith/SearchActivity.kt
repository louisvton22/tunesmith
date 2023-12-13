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
        val fillerQuery = "blackpink"
        var searchUrl = URL("https://api.spotify.com/v1/search?q=$fillerQuery&type=track")
        var urlConnection = searchUrl.openConnection() as HttpURLConnection
        Log.i(TAG, "searching for tracks matching query")
        urlConnection.setRequestProperty("Authorization", "Bearer ${sharedPref.getString("AccessToken", "")}")
        val checkAuth = urlConnection.getRequestProperty("Authorization")
        Log.i(TAG, "check url auth token: ${checkAuth}")

        val inputStream = urlConnection.inputStream

        val reader  = InputStreamReader(inputStream)
        val tracks: JSONArray
        reader.use {
            val json = JSONObject(it.readText())
            Log.i(TAG, "search results: $json")
            tracks = json.getJSONArray("items")
            Log.i(TAG, "tracks : $tracks")
        }
    }
}