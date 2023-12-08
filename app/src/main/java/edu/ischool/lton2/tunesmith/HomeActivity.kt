package edu.ischool.lton2.tunesmith

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HomeActivity : AppCompatActivity() {
    lateinit var spotifyConnection: SpotifyConnection
    private val TAG = "HomeActivity"
    private val REQUEST_CODE = 1337
    override fun onCreate(savedInstanceState: Bundle?) {
        spotifyConnection = (application as SpotifyConnection)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<TextView>(R.id.txtHistoryRec).text = "Here are some songs based on your listening history"
        findViewById<TextView>(R.id.txtWelcome).text = "Welcome ${this as SpotifyConnection}"


        var builder: AuthorizationRequest.Builder = AuthorizationRequest.Builder(spotifyConnection.clientId, AuthorizationResponse.Type.TOKEN, spotifyConnection.redirectUri)

        builder.setScopes(arrayOf("streaming", "user-read-private", "playlist-read", "playlist-read-private"))

        var request: AuthorizationRequest = builder.build()

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {

            var response: AuthorizationResponse = AuthorizationClient.getResponse(resultCode, data)

            when(response.getType()) {
                AuthorizationResponse.Type.TOKEN -> getUserDetails(response.accessToken)
                AuthorizationResponse.Type.ERROR -> return
                else -> return
            }
        }
    }

    fun getUserDetails(accessToken: String) {
        val apiUrl = URL("https://api.spotify.com/v1/me")
        // val headers = mapOf("Authorization" to "Bearer $accessToken")

        val urlConnection = apiUrl.openConnection() as HttpURLConnection
        Log.i(TAG, "requesting user details")
        urlConnection.setRequestProperty("Authorization", "Bearer $accessToken")

        val inputStream = urlConnection.inputStream

        val reader  = InputStreamReader(inputStream)

        reader.use {
            val details = it.readText()
            Log.i(TAG, "resposne details: $details")
        }

    }

}