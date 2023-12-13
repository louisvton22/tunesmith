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

//TODO: Delete when done
val layoutTester  = listOf<Song>(
    Song (
        "song1",
        "artist1",
        "image1",
        "length1",
        "id1"
    ),
    Song (
        "song2",
        "artist2",
        "image2",
        "length2",
        "id2"
    )
)
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
        Log.i(TAG, "oncreateoptionsmenu")
        menuInflater.inflate(R.menu.top_menu, menu)

        val searchViewItem = menu.findItem(R.id.search_bar)
        val searchView = searchViewItem.actionView as SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.i(TAG, "submitted query")
                var tracklist : List<Song>
                networkThread.execute{
                    try {
                        tracklist = searchSong("filler")
                        val listView = findViewById<ListView>(R.id.listView)
//                        listView.adapter = PlaylistAdapter(tracklist, context: Context )
                    } catch (e: Exception) {
                        Log.d(TAG, e.toString())
                    }
                }
                Log.i(TAG, "searched")
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.i(TAG, "textchaange")
//                adapter.filter.filter(newText)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun searchSong(query: String) : List<Song> {
        Log.i(TAG, "User token: ${sharedPref.getString("AccessToken", "")}")
        var fillerQuery = "White Lie"
        fillerQuery = fillerQuery.replace(" ", "+")
        var searchUrl = URL("https://api.spotify.com/v1/search?q=$fillerQuery&type=track&limit=2")

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
//        displayResults(songResults.toList())
        return songResults.toList()

    }

    override fun onSongClick(song: Song) {
        Log.i(TAG, "song clicked")
        TODO("Not yet implemented")
    }

//    private fun displayResults(trackList: List<Song>) {
//        this.runOnUiThread{
//
//            val listView = findViewById<ListView>(R.id.listView)
//
//            listView.adapter = PlaylistAdapter(trackList, this )
//        }
//    }
}

class SearchResultsAdapter(private val trackList: List<Song>): BaseAdapter() {
    override fun getCount(): Int {
        return trackList.size
    }

    override fun getItem(index: Int): Any {
        return trackList.get(index)
    }

    override fun getItemId(index: Int): Long {
        //TODO: edit Song data class to have id
        return index.toLong()
    }

    override fun getView(index: Int, convertView: View?, parent: ViewGroup?): View {
        val track = getItem(index) as Song
        val inflater = LayoutInflater.from(parent?.context)
        val view = convertView ?: inflater.inflate(R.layout.list_items, parent, false)
        val viewHolder : SongViewHolder

        if (convertView == null) {
            viewHolder = SongViewHolder(view)
            view.tag = viewHolder
        } else {
            viewHolder = convertView.tag as SongViewHolder
        }

        viewHolder.songTitle.text = track.title
        viewHolder.songArtist.text = track.artist

        return view
    }
    class SongViewHolder(view: View) {
        val songTitle = view.findViewById<TextView>(R.id.songTitle)
        val songArtist = view.findViewById<TextView>(R.id.songArtist)
    }


}
