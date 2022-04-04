package com.riyazuddin.zingplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.riyazuddin.zingplayer.data.model.Song
import com.riyazuddin.zingplayer.databinding.ItemSongBinding
import javax.inject.Inject

class SongsAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<SongsAdapter.SongsViewHolder>() {

    inner class SongsViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.tvTitle.text = song.title
            glide.load(song.imageUrl).into(binding.ivSong)
            binding.root.setOnClickListener {
                songClickListener?.let {
                    it(song)
                }
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaID == newItem.mediaID
        }
    }
    private val differ = AsyncListDiffer(this, differCallback)
    var list: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        return SongsViewHolder(
            ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size


    private var songClickListener: ((Song) -> Unit)? = null
    fun setOnSongClickListener(listener: (Song) -> Unit){
        songClickListener = listener
    }
}