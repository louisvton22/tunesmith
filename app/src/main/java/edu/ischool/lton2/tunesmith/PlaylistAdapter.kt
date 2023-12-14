package edu.ischool.lton2.tunesmith


import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.net.URL
import java.util.concurrent.Executors


class PlaylistAdapter(private val songs: List<Song>, private val onSongClickListener: OnSongClickListener) : BaseAdapter() {
    val networkThread = Executors.newSingleThreadExecutor()
    interface OnSongClickListener {
        fun onSongClick(song: Song)

        fun onSongSelected(song:Song, view:View)
    }
    override fun getCount(): Int {
        return songs.size
    }
    override fun getItem(position: Int): Any {
        return songs[position]
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val song = getItem(position) as Song
        val inflater = LayoutInflater.from(parent?.context)

        val view = convertView ?: inflater.inflate(R.layout.list_items, parent, false)
        val viewHolder: SongViewHolder

        if (convertView == null) {
            viewHolder = SongViewHolder(view)
            view.tag = viewHolder
        } else {
            viewHolder = convertView.tag as SongViewHolder
        }

        viewHolder.songTitle.text = song.title
        viewHolder.songArtist.text = song.artist
        viewHolder.duration.text = formatTime(song.length)
        val imgURL = URL(song.cover)
        var image: Bitmap
//            BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.music_note)
        networkThread.execute{
            try {
                image = BitmapFactory.decodeStream(imgURL.openConnection().getInputStream())
                viewHolder.image.setImageBitmap(image)
            } catch(e: Exception) {
                Log.e("PlaylistAdapter", e.toString())
            }
        }
        if (song.selected) {
            view.setBackgroundColor(Color.DKGRAY)
            view.findViewById<TextView>(R.id.songArtist).setTextColor(Color.LTGRAY)
            view.findViewById<TextView>(R.id.songTitle).setTextColor(Color.WHITE)
            view.findViewById<TextView>(R.id.songLength).setTextColor(Color.LTGRAY)
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
            view.findViewById<TextView>(R.id.songArtist).setTextColor(Color.LTGRAY)
            view.findViewById<TextView>(R.id.songTitle).setTextColor(Color.GRAY)
            view.findViewById<TextView>(R.id.songLength).setTextColor(Color.LTGRAY)
        }
        view.setOnClickListener {
            onSongClickListener.onSongClick(song)
        }

        view.setOnLongClickListener {
            onSongClickListener.onSongSelected(song, it)
            true
        }

        return view
    }
    class SongViewHolder(view: View) {
        val songTitle = view.findViewById<TextView>(R.id.songTitle)
        val songArtist = view.findViewById<TextView>(R.id.songArtist)
        val image = view.findViewById<ImageView>(R.id.songImg)
        val duration = view.findViewById<TextView>(R.id.songLength)
    }
}

fun formatTime(milliseconds: Int) : String {
    // example durations 222025, 184248, 162569
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val remainder = if (totalSeconds % 60 < 10)  "0${totalSeconds % 60}" else totalSeconds % 60
    return "$minutes:$remainder"
}
