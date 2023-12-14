package edu.ischool.lton2.tunesmith

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.net.URL
import java.util.concurrent.Executors

class SongAdapter(private val songs: List<HomeActivity.Song>, private val context: Activity) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {
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