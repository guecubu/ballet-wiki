package com.gf.ballet_wiki.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gf.ballet_wiki.data.local.EuropeanaEntity
import com.gf.ballet_wiki.databinding.ItemMediaGalleryBinding
import com.gf.ballet_wiki.utils.loadUrl

class MediaGalleryAdapter(
        private val onItemClick: (EuropeanaEntity) -> Unit
                         ) :
        ListAdapter<EuropeanaEntity, MediaGalleryAdapter.MediaViewHolder>(MediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaGalleryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
                                                     )
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MediaViewHolder(private val binding: ItemMediaGalleryBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EuropeanaEntity) {
            binding.tvMediaTitle.text = item.title
            binding.tvMediaProvider.text = item.dataProvider ?: "Europeana Archive"

            val imageUrl = item.highResUrl ?: item.previewUrl
            binding.ivMediaCover.loadUrl(imageUrl)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class MediaDiffCallback : DiffUtil.ItemCallback<EuropeanaEntity>() {
        override fun areItemsTheSame(oldItem: EuropeanaEntity, newItem: EuropeanaEntity): Boolean {
            return oldItem.europeanaId == newItem.europeanaId
        }

        override fun areContentsTheSame(
                oldItem: EuropeanaEntity, newItem: EuropeanaEntity
                                       ): Boolean {
            return oldItem == newItem
        }
    }
}
