package edu.ischool.lton2.tunesmith

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.android.appremote.api.error.UserNotAuthorizedException

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.error.SpotifyAppRemoteException
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;


class SpotifyConnection: Application() {
    var connection: SpotifyAppRemote? = null
    val clientId = "23b30f0dcd494714b0fe85df516f4d02"
    val redirectUri = "https://louis-ton.netlify.app/"
    private val TAG = "SpotifyConnection"
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG,"Spotify Connection initialized")
    }
    fun getConn() : SpotifyAppRemote? {
        return connection
    }
}
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    //private var spotifyAppRemote: SpotifyAppRemote? = null
    private val mainActivity = this
    lateinit var spotifyConnection: SpotifyConnection

    lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        spotifyConnection = (application as SpotifyConnection)

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


    }

    override fun onStart() {
        super.onStart()
        findViewById<Button>(R.id.btnConnect).setOnClickListener{ authorizeUser() }
        Log.i(TAG, "Showing Auth Screen")




    }
    fun handleException(error: SpotifyAppRemoteException) {
        val alert = AlertDialog.Builder(this)

        alert.setTitle("Linking Error")
        when (error) {
            is NotLoggedInException -> {
                alert.setMessage("Please Login onto the spotify")
                alert.setPositiveButton("Go") { dialog, which ->
                    val spotifyIntent = packageManager.getLaunchIntentForPackage("com.spotify.music")
                    startActivity(spotifyIntent)
                    dialog.dismiss()
                }
                alert.setNegativeButton("Close") {dialog, which ->
                    dialog.dismiss()
                }
            }

            is UserNotAuthorizedException -> {
                alert.setMessage("Please agree to the listed terms.")
                alert.setPositiveButton("Retry") { dialog, which ->
                    authorizeUser()
                    dialog.dismiss()
                }
                alert.setNegativeButton("Cancel") {dialog, which ->
                    dialog.dismiss()
                }
            }

            else -> {
                alert.setMessage("Spotify not installed. Please downlaod from the Play Store")
                alert.setPositiveButton("Go") { dialog, which ->
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")
                    startActivity(intent)
                    dialog.dismiss()
                }
                alert.setNegativeButton("Cancel") {dialog, which ->
                    dialog.dismiss()
                }
            }

        }
        alert.create().show()
    }

    fun authorizeUser() {
        val connectionParams = ConnectionParams.Builder(spotifyConnection.clientId)
            .setRedirectUri(spotifyConnection.redirectUri)
            .showAuthView(true)
            .build()


        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(p0: SpotifyAppRemote?) {
                (application as SpotifyConnection).connection = p0
                Log.i(TAG, "Connection Successful")
//                connected()
                // TODO: send user to home screen activity
//                val homeIntent = Intent(mainActivity, PlaylistViewActivity::class.java)
                val homeIntent = Intent(mainActivity, HomeActivity::class.java)
                startActivity(homeIntent)

            }

            override fun onFailure(p0: Throwable?) {
                Log.e(TAG, "connection failed $p0")
                handleException(p0 as SpotifyAppRemoteException)
            }
        })
    }
    private fun connected() {
        // Then we will write some more code here.
        (application as SpotifyConnection).getConn()?.let {
            val playlistURI = "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL"
            it.playerApi.play(playlistURI)
            // Subscribe to PlayerState
            it.playerApi.subscribeToPlayerState().setEventCallback {
                val track: Track = it.track
                Log.d("MainActivity", track.name + " by " + track.artist.name)
            }
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "disconnected Spotify App Remote")
        (application as SpotifyConnection).getConn()?.let {
            SpotifyAppRemote.disconnect(it)
        }
        super.onDestroy()
    }
}