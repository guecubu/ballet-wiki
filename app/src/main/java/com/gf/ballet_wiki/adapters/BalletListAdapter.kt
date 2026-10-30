package com.gf.ballet_wiki.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gf.ballet_wiki.R
import com.gf.ballet_wiki.data.remote.dto.BalletDto
import com.gf.ballet_wiki.databinding.ItemBalletBinding
import com.gf.ballet_wiki.utils.loadUrl
import com.gf.ballet_wiki.utils.setTranslatedText

class BalletListAdapter(
    private val onBalletClick: (BalletDto) -> Unit,
    private val onFavoriteClick: (BalletDto) -> Unit,
    private val isFavoriteChecker: (Int) -> Boolean
) : ListAdapter<BalletDto, BalletListAdapter.BalletViewHolder>(BalletDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalletViewHolder {
        val binding = ItemBalletBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BalletViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BalletViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BalletViewHolder(private val binding: ItemBalletBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ballet: BalletDto) {
            val context = binding.root.context

            // Title with translation and original in parentheses
            binding.tvTitle.setTranslatedText(
                originalText = ballet.title,
                sourceLanguage = "en",
                showOriginalInParentheses = true
            )

            binding.tvComposer.text = ballet.composer
            binding.tvActs.text = context.getString(R.string.acts_format, ballet
                    .acts)

            // Era badge (e.g. 1877 • Petipa)
            val choreographer = ballet.originalChoreographer?.takeIf { it.isNotBlank() }?.split(" ")?.last() ?: "Ballet"
            binding.tvEraBadge.text = "${ballet.premiereYear} • $choreographer"

            // Cover Image with Coil
            binding.ivCover.loadUrl(ballet.multimedia?.coverImageUrl)

            // Favorite status & click
            updateFavoriteIcon(ballet.id)

            binding.btnFavorite.setOnClickListener {
                onFavoriteClick(ballet)
                updateFavoriteIcon(ballet.id)
            }

            // Cell click
            binding.root.setOnClickListener {
                onBalletClick(ballet)
            }
        }

        private fun updateFavoriteIcon(balletId: Int) {
            val isFav = isFavoriteChecker(balletId)
            binding.btnFavorite.visibility = if (isFav) {
                View.VISIBLE } else { View.GONE }
        }
    }

    class BalletDiffCallback : DiffUtil.ItemCallback<BalletDto>() {
        override fun areItemsTheSame(oldItem: BalletDto, newItem: BalletDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BalletDto, newItem: BalletDto): Boolean {
            return oldItem == newItem
        }
    }
}
