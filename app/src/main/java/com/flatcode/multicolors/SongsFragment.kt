package com.flatcode.multicolors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.flatcode.multicolors.databinding.FragmentSongsBinding
import com.flatcode.multicolors.databinding.ItemMusicBinding

class SongsFragment : Fragment() {

    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        val songs = List(50) { index -> "Song Title ${index + 1}" }
        binding.recyclerView.adapter = SongsAdapter(songs)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class SongsAdapter(private val songs: List<String>) :
        RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {

        class SongViewHolder(val binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
            val binding =
                ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SongViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
            holder.binding.songName.text = songs[position]
            holder.binding.songDetails.text = "Artist Name | Album ${position + 1}"
            holder.binding.image.load("https://images.unsplash.com/photo-1507838596018-7270c11f78f6?w=200&q=80")
        }

        override fun getItemCount() = songs.size
    }
}