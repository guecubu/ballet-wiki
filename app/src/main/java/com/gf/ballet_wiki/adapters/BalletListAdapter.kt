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
import com.gf.ballet_wiki.databinding.ItemBalletHeroBinding
import com.gf.ballet_wiki.databinding.ItemBalletLandscapeBinding
import com.gf.ballet_wiki.utils.TranslationManager
import com.gf.ballet_wiki.utils.buildTitleString
import com.gf.ballet_wiki.utils.loadUrl

class BalletListAdapter(
    private val onBalletClick: (BalletDto) -> Unit,
    private val onFavoriteClick: (BalletDto) -> Unit,
    private val isFavoriteChecker: (Int) -> Boolean
                       ) : ListAdapter<BalletDto, RecyclerView.ViewHolder>(BalletDiffCallback()) {

    companion object {
        const val VIEW_TYPE_STANDARD = 0
        const val VIEW_TYPE_HERO = 1
        const val VIEW_TYPE_LANDSCAPE = 2
    }

    override fun getItemViewType(position: Int): Int {
        val ballet = getItem(position)
        val ratio = ballet.effectiveCoverAspectRatio ?: 0.75

        // 1. Explicit Hero (ratio > 1.2) -> Span 2
        if (ratio > 1.2) {
            return VIEW_TYPE_HERO
        }

        // 2. Explicit Landscape (0.9 <= ratio <= 1.2) -> Span 2
        if (ratio >= 0.9) {
            return VIEW_TYPE_LANDSCAPE
        }

        // 3. Anti-Boring Pacing Rule: Never more than 2 consecutive Standard cards (1 row) in a row.
        // If a run reaches 3 consecutive Standard cards, break the monotony by promoting the 3rd to LANDSCAPE (Span 2).
        var consecutiveStandards = 0
        var i = position
        while (i >= 0) {
            val r = getItem(i).effectiveCoverAspectRatio ?: 0.75
            if (r >= 0.9) break
            consecutiveStandards++
            i--
        }

        if (consecutiveStandards > 0 && consecutiveStandards % 3 == 0) {
            return VIEW_TYPE_LANDSCAPE
        }

        // 4. Anti-Lonely-Standard Rule: Prevent odd-sized standard groups from leaving a lonely standard card.
        var groupStart = position
        while (groupStart > 0) {
            val prevRatio = getItem(groupStart - 1).effectiveCoverAspectRatio ?: 0.75
            if (prevRatio >= 0.9) break
            groupStart--
        }

        var groupSize = 0
        var idx = groupStart
        while (idx < itemCount) {
            val r = getItem(idx).effectiveCoverAspectRatio ?: 0.75
            if (r >= 0.9) break
            groupSize++
            idx++
        }

        val indexInGroup = position - groupStart
        if (groupSize % 2 != 0 && indexInGroup == groupSize - 1) {
            return VIEW_TYPE_LANDSCAPE
        }

        return VIEW_TYPE_STANDARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HERO -> HeroViewHolder(ItemBalletHeroBinding.inflate(inflater, parent, false))
            VIEW_TYPE_LANDSCAPE -> LandscapeViewHolder(
                    ItemBalletLandscapeBinding.inflate(inflater, parent, false)
                                                      )

            else -> StandardViewHolder(ItemBalletBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ballet = getItem(position)
        when (holder) {
            is StandardViewHolder -> holder.bind(ballet)
            is HeroViewHolder -> holder.bind(ballet)
            is LandscapeViewHolder -> holder.bind(ballet)
        }
    }

    private fun bindCommon(
            root: View,
            tvTitle: android.widget.TextView,
            tvComposer: android.widget.TextView,
            tvActs: android.widget.TextView,
            tvEraBadge: android.widget.TextView,
            ivCover: android.widget.ImageView,
            btnFavorite: View,
            ballet: BalletDto
                          ) {
        val context = root.context

        // Title with translation and original title in parentheses
        TranslationManager.translate(
                context = context,
                originalText = ballet.title,
                sourceLanguage = "en",
                onTranslating = {
                    tvTitle.text = context.getString(R.string.translating)
                },
                onTranslated = { translatedTitle ->
                    tvTitle.text = buildTitleString(translatedTitle, ballet.originalTitle)
                }
                                    )

        tvComposer.text = ballet.composer
        tvActs.text = context.resources.getQuantityString(
                R.plurals.acts_plural,
                ballet.acts,
                ballet.acts
                                                         )

        // Era badge (e.g. 1877 • Petipa)
        val choreographer =
                ballet.originalChoreographer?.takeIf { it.isNotBlank() }?.split(" ")?.last()
                ?: "Ballet"
        tvEraBadge.text = "${ballet.premiereYear} • $choreographer"

        // Cover Image with Coil
        ivCover.loadUrl(ballet.multimedia?.coverImageUrl)

        // Favorite status & click
        val updateFavIcon = {
            val isFav = isFavoriteChecker(ballet.id)
            btnFavorite.visibility = if (isFav) View.VISIBLE else View.GONE
        }
        updateFavIcon()

        btnFavorite.setOnClickListener {
            onFavoriteClick(ballet)
            updateFavIcon()
        }

        // Cell click
        root.setOnClickListener {
            onBalletClick(ballet)
        }
    }

    inner class StandardViewHolder(private val binding: ItemBalletBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(ballet: BalletDto) {
            bindCommon(
                    binding.root,
                    binding.tvTitle,
                    binding.tvComposer,
                    binding.tvActs,
                    binding.tvEraBadge,
                    binding.ivCover,
                    binding.btnFavorite,
                    ballet
                      )
        }
    }

    inner class HeroViewHolder(private val binding: ItemBalletHeroBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(ballet: BalletDto) {
            bindCommon(
                    binding.root,
                    binding.tvTitle,
                    binding.tvComposer,
                    binding.tvActs,
                    binding.tvEraBadge,
                    binding.ivCover,
                    binding.btnFavorite,
                    ballet
                      )
        }
    }

    inner class LandscapeViewHolder(private val binding: ItemBalletLandscapeBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(ballet: BalletDto) {
            bindCommon(
                    binding.root,
                    binding.tvTitle,
                    binding.tvComposer,
                    binding.tvActs,
                    binding.tvEraBadge,
                    binding.ivCover,
                    binding.btnFavorite,
                    ballet
                      )
            binding.tvOriginalTitle.text = ballet.originalTitle ?: ""
            binding.tvOriginalTitle.visibility =
                    if (ballet.originalTitle.isNullOrBlank()) View.GONE else View.VISIBLE

            val ratio = ballet.effectiveCoverAspectRatio ?: 0.75
            val context = binding.root.context

            val widthDp = if (ratio >= 1.0) 160f else 130f
            val boxRatio = if (ratio >= 1.0) "1:1" else "3:4"

            val widthPx = android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP,
                    widthDp,
                    context.resources.displayMetrics
                                                                ).toInt()

            val lp =
                    binding.ivCover.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            lp.width = widthPx
            lp.dimensionRatio = "H,$boxRatio"
            binding.ivCover.layoutParams = lp
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
