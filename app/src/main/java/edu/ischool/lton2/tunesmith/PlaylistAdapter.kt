package edu.ischool.lton2.tunesmith

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView

class PlaylistAdapter(private val playlist: Playlist, private val onSongClickListener: OnSongClickListener) : BaseAdapter() {

    interface OnSongClickListener {
        fun onSongClick(song: Song)
    }
    override fun getCount(): Int {
        return playlist.songs.size
    }
    override fun getItem(position: Int): Any {
        return playlist.songs[position]
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

        view.setOnClickListener {
            onSongClickListener.onSongClick(song)
        }

        return view
    }
    class SongViewHolder(view: View) {
        val songTitle = view.findViewById<TextView>(R.id.songTitle)
        val songArtist = view.findViewById<TextView>(R.id.songArtist)
    }
}
