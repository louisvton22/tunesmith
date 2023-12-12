package edu.ischool.lton2.tunesmith

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.content.edit
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class HomeActivity : AppCompatActivity() {
    lateinit var spotifyConnection: SpotifyConnection
    private val TAG = "HomeActivity"
    private val REQUEST_CODE = 1337
    val homeActivity = this
    lateinit var mAccessToken: String
    val networkThread = Executors.newSingleThreadExecutor()
    lateinit var user: JSONObject
    lateinit var sharedPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        spotifyConnection = (application as SpotifyConnection)
        sharedPref = getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)




        var builder: AuthorizationRequest.Builder = AuthorizationRequest.Builder(spotifyConnection.clientId, AuthorizationResponse.Type.TOKEN, spotifyConnection.redirectUri)

        builder.setScopes(arrayOf("streaming", "user-read-private", "playlist-read", "playlist-read-private"))

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
        if (sharedPref.contains("User")) {
            networkThread.execute {
                getUserDetails(sharedPref.getString("AccessToken", "")!!)
            }
        } else {
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
            Log.i(TAG, "response details: $details")
            user = JSONObject(details)
            with (sharedPref.edit()) {
                putString("User", user["display_name"].toString())
                Log.i(TAG, "Added display name to shared preferences")
                apply()
            }
            this.runOnUiThread {
                findViewById<TextView>(R.id.txtHistoryRec).text = "Here are some songs based on your listening history"
                findViewById<TextView>(R.id.txtWelcome).text = "Welcome, ${sharedPref.getString("User", "listener")}"
            }
        }

    }

}