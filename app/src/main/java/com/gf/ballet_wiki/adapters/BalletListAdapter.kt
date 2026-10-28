package com.gf.ballet_wiki.adapters

import android.view.LayoutInflater
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
            binding.tvPremiereYear.text = context.getString(R.string.premiere_year_format, ballet.premiereYear)

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
            binding.btnFavorite.setImageResource(
                if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
            )
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
