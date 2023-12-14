package edu.ischool.lton2.tunesmith

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.edit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class HomeActivity : AppCompatActivity(), NavBar {
    lateinit var spotifyConnection: SpotifyConnection
    private val TAG = "HomeActivity"
    private val REQUEST_CODE = 1337
    val homeActivity = this
    lateinit var mAccessToken: String
    val networkThread = Executors.newSingleThreadExecutor()
    lateinit var user: JSONObject
    lateinit var sharedPref: SharedPreferences

    lateinit var bottomNav : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        spotifyConnection = (application as SpotifyConnection)
        sharedPref = getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        this.setupNav(this, R.id.nav_home)
        findViewById<TextView>(R.id.txtRec).visibility = View.INVISIBLE
        findViewById<TextView>(R.id.txtHistoryRec).visibility = View.INVISIBLE

        var recyclerView = findViewById<RecyclerView>(R.id.recHistory)
        var layoutManager = GridLayoutManager(this, 1,  LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = SongAdapter(listOf(), this)

        recyclerView = findViewById<RecyclerView>(R.id.recRecommends)
        layoutManager = GridLayoutManager(this, 1,  LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = SongAdapter(listOf(), this)

        var builder: AuthorizationRequest.Builder = AuthorizationRequest.Builder(
            spotifyConnection.clientId,
            AuthorizationResponse.Type.TOKEN,
            spotifyConnection.redirectUri
        )

        builder.setScopes(
            arrayOf(
                "streaming",
                "user-read-private",
                "playlist-read",
                "playlist-read-private",
                "user-read-recently-played",
                "playlist-modify-public",
                "playlist-modify-private",
                "ugc-image-upload"
            )
        )

        var request: AuthorizationRequest = builder.build()

        networkThread.execute {
            try {
                Log.i(TAG, "opening Login Activity")
                AuthorizationClient.openLoginActivity(homeActivity, REQUEST_CODE, request)
            } catch (e: Exception) {
                Log.e("Thread", "Error on network thread ${e.message}")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult fired.")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {

            var response: AuthorizationResponse =
                AuthorizationClient.getResponse(resultCode, data)
            Log.i(TAG, "Checking request type ${response.type}")
            when (response.getType()) {
                AuthorizationResponse.Type.TOKEN -> {
                    sharedPref.edit {
                        this.putString("AccessToken", response.accessToken)
                        apply()
                    }
                    Log.i(TAG, "Added accesstoken to shared preferences")
                    mAccessToken = response.accessToken;
                    networkThread.execute {
                        try {
                            getUserDetails(mAccessToken)
                        } catch (e: Exception) {
                        }
                    }
                }

                AuthorizationResponse.Type.ERROR -> return
                else -> return
            }
        }
    }

    fun getUserDetails(accessToken: String) {
        if (!sharedPref.contains("User")) {
            val apiUrl = URL("https://api.spotify.com/v1/me")
            // val headers = mapOf("Authorization" to "Bearer $accessToken")

            val urlConnection = apiUrl.openConnection() as HttpURLConnection
            Log.i(TAG, "requesting user details ${accessToken}")
            urlConnection.setRequestProperty("Authorization", "Bearer $accessToken")

            val inputStream = urlConnection.inputStream

            val reader = InputStreamReader(inputStream)

            reader.use {
                val details = it.readText()
                Log.i(TAG, "response details: $details")
                user = JSONObject(details)
                with(sharedPref.edit()) {
                    putString("User", user["display_name"].toString())
                    Log.i(TAG, "Added display name to shared preferences")
                    putString("UserID", user["id"].toString())
                    Log.i(TAG, "Added spotify id name to shared preferences")
                    apply()
                }

            }
        }
        this.runOnUiThread {
            findViewById<TextView>(R.id.txtHistoryRec).text = "Here's what you've been listening to"
            findViewById<TextView>(R.id.txtHistoryRec).visibility = View.VISIBLE
            findViewById<TextView>(R.id.txtWelcome).text =
                "Welcome, ${sharedPref.getString("User", "listener")}"
            Log.i(TAG, "setting up HomeUI")
        }
        setupHomeUI()
    }


    data class Song(val name: String, val artists: String, val trackId: String, val cover: String = "")

    // Set up home screen for TuneSmith
    fun setupHomeUI() {

        try {
            var apiUrl = URL("https://api.spotify.com/v1/me/player/recently-played?limit=5")
            // val headers = mapOf("Authorization" to "Bearer $accessToken")

            var urlConnection = apiUrl.openConnection() as HttpURLConnection
            Log.d(TAG, "Bearer ${sharedPref.getString("AccessToken", "")}")
            Log.i(TAG, "requesting listening history details")
            urlConnection.setRequestProperty(
                "Authorization",
                "Bearer ${sharedPref.getString("AccessToken", "")}"
            )

            var inputStream = urlConnection.inputStream

            var reader = InputStreamReader(inputStream)
            var tracks: JSONArray
            reader.use {
                val json = JSONObject(it.readText())
                tracks = json.getJSONArray("items")

            }
            var recentSongs: MutableList<Song> = mutableListOf()
            for (i in 0 until tracks.length()) {
                val track = tracks.getJSONObject(i).getJSONObject("track")

                var smallImageObj = track.getJSONObject("album")
                    .getJSONArray("images")
                    .getJSONObject(1)
                val song = Song(
                    track.getString("name"),
                    track.getJSONArray("artists").getJSONObject(0).getString("name"),
                    track.getString("id"),
                    smallImageObj.getString("url")
                )
                recentSongs.add(song)

            }

            // inflate listen history carousel
            this.runOnUiThread {
                Log.i(TAG, "inflating history carousel")
                findViewById<RecyclerView>(R.id.recHistory).adapter = SongAdapter(recentSongs, this)
            }

            // inflate recommended songs carousel

            // get list of seed tracks
            val seedArtists = recentSongs.joinToString(separator = ",") { element ->
                element.trackId
            }
            Log.i(TAG, "$seedArtists")
            val recUrl = URL(
                "https://api.spotify.com/v1/recommendations?limit=5&" +
                        "seed_tracks=$seedArtists"
            )
            urlConnection.disconnect()
            urlConnection = recUrl.openConnection() as HttpURLConnection
            Log.i(TAG, "requesting recommended details")
            urlConnection.setRequestProperty(
                "Authorization",
                "Bearer ${sharedPref.getString("AccessToken", "")}"
            )

            Log.i(TAG, "${urlConnection.responseCode} ${urlConnection.getHeaderField("Retry-After")}")
            inputStream = urlConnection.inputStream

            reader = InputStreamReader(inputStream)
            reader.use {
                val json = JSONObject(it.readText())
                tracks = json.getJSONArray("tracks")

            }
            recentSongs = mutableListOf()
            for (i in 0 until tracks.length()) {
                val track = tracks.getJSONObject(i)
                var smallImageObj = track.getJSONObject("album")
                    .getJSONArray("images")
                    .getJSONObject(1)
                val song = Song(
                    track.getString("name"),
                    track.getJSONArray("artists").getJSONObject(0).getString("name"),
                    track.getString("id"),
                    smallImageObj.getString("url")
                )
                recentSongs.add(song)

            }
            this.runOnUiThread {
                findViewById<TextView>(R.id.txtRec).text =
                    "Recommended songs based on your listening history"
                findViewById<TextView>(R.id.txtRec).visibility = View.VISIBLE
                Log.i(TAG, "inflating recommended carousel")
                findViewById<RecyclerView>(R.id.recRecommends).adapter =
                    SongAdapter(recentSongs, this)
            }
        } catch(e: Exception) {
            Log.e(TAG, "Error on network thread: $e ${e.message}")
        }
    }

    class SongAdapter(private val songs: List<Song>, private val context: Activity) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textTitle: TextView = itemView.findViewById(R.id.textTitle)
            val textArtist: TextView = itemView.findViewById(R.id.textArtist)
            val image = itemView.findViewById<ImageView>(R.id.trackCover)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val song = songs[position]
            holder.textArtist.text = song.artists
            holder.textTitle.text = song.name
            val imgURL = URL(song.cover)
            var image: Bitmap
            val networkThread = Executors.newSingleThreadExecutor() // is it ok to make another one?
            networkThread.execute{
                try {
                    image = BitmapFactory.decodeStream(imgURL.openConnection().getInputStream())
                    this.context.runOnUiThread {
                        holder.image.setImageBitmap(image)
                    }

                } catch(e: Exception) {
                    Log.e("PlaylistAdapter", e.toString())
                }
            }
        }
        override fun getItemCount(): Int {
            return songs.size
        }
    }
}
