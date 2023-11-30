package edu.ischool.lton2.tunesmith

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val clientId = "23b30f0dcd494714b0fe85df516f4d02"
    private val redirectUri = "https://louis-ton.netlify.app/"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        this.runOnUiThread {
            val connectionParams = ConnectionParams.Builder(clientId)
                .setRedirectUri(redirectUri)
                .showAuthView(true)
                .build()

            SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
                override fun onConnected(p0: SpotifyAppRemote?) {
                    spotifyAppRemote = p0
                    Log.i(TAG, "Connection Successful")
                }

                override fun onFailure(p0: Throwable?) {
                    Log.e(TAG, "connection failed $p0")
                }
            })
        }



    }

    private fun connected() {
        // Then we will write some more code here.
        spotifyAppRemote?.let {
            val playlistURI = "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL"
            it.playerApi.play(playlistURI)
            // Subscribe to PlayerState
            it.playerApi.subscribeToPlayerState().setEventCallback {
                val track: Track = it.track
                Log.d("MainActivity", track.name + " by " + track.artist.name)
            }
        }
    }

    override fun onStop() {
        super.onStop()

        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
}