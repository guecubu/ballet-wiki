package com.gf.ballet_wiki.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.gf.ballet_wiki.R
import com.gf.ballet_wiki.adapters.MediaGalleryAdapter
import com.gf.ballet_wiki.data.local.EuropeanaEntity
import com.gf.ballet_wiki.data.remote.dto.BalletDto
import com.gf.ballet_wiki.data.repository.BalletRepository
import com.gf.ballet_wiki.databinding.ActivityDetailBinding
import com.gf.ballet_wiki.databinding.DialogImageZoomBinding
import com.gf.ballet_wiki.utils.DateStyle
import com.gf.ballet_wiki.utils.loadUrl
import com.gf.ballet_wiki.utils.setTranslatedText
import com.gf.ballet_wiki.utils.toLocalizedDate
import kotlinx.coroutines.launch
import kotlin.math.abs

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WORK_ID = "extra_work_id"
    }

    private lateinit var binding: ActivityDetailBinding
    private lateinit var repository: BalletRepository
    private var workId: Int = -1
    private var currentBallet: BalletDto? = null
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.coordinatorDetail) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = BalletRepository.getInstance(this)
        workId = intent.getIntExtra(EXTRA_WORK_ID, -1)

        if (workId == -1) {
            finish()
            return
        }

        // Save last viewed work in SharedPreferences
        repository.setLastViewedWorkId(workId)

        setupToolbar()
        setupListeners()
        setupParallaxListener()
        loadBalletDetail()
    }

    private fun setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnDetailFavorite.setOnClickListener {
            val isNowFav = repository.toggleFavorite(workId)
            updateFavoriteIcon(isNowFav)
        }

        binding.btnOpenNotes.setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java).apply {
                putExtra(NotesActivity.EXTRA_WORK_ID, workId)
                putExtra(NotesActivity.EXTRA_WORK_TITLE, currentBallet?.title ?: "")
            }
            startActivity(intent)
        }

        binding.btnDetailRetry.setOnClickListener {
            loadBalletDetail()
        }
    }

    private fun setupParallaxListener() {
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange.toFloat()
            if (totalScrollRange == 0f) return@addOnOffsetChangedListener

            // Progreso del colapso: 0.0 (arriba) a 1.0 (totalmente colapsado)
            val collapseProgress = abs(verticalOffset) / totalScrollRange

            // 1. Desvanecer los badges sobre la foto cuando la card empieza a cubrirla
            val badgeAlpha = (1f - (collapseProgress * 2f)).coerceIn(0f, 1f)
            binding.layoutHeroBadges.alpha = badgeAlpha
            binding.tvPhotoCredit.alpha = badgeAlpha
        }
    }

    private fun loadBalletDetail() {
        showLoading()
        lifecycleScope.launch {
            val result = repository.getBalletById(workId)
            result.onSuccess { ballet ->
                currentBallet = ballet
                bindBalletData(ballet)
                showContent()

                // Load extra composer info from Open Opus
                loadOpenOpusInfo(ballet.composer)

                // Load extra images from Europeana
                val europeanaTerm = ballet.multimedia?.europeanaSearchTerm
                if (!europeanaTerm.isNullOrBlank()) {
                    loadEuropeanaInfo(europeanaTerm)
                }
            }.onFailure {
                showError()
            }
        }
    }

    private fun bindBalletData(ballet: BalletDto) {
        // Translated title in editorial sheet
        binding.tvTitle.setTranslatedText(
                originalText = ballet.title,
                sourceLanguage = "en",
                showOriginalInParentheses = false
                                         )

        // Original title as separate data field
        val showOriginal = !ballet.originalTitle.isNullOrBlank() &&
                           !ballet.originalTitle.equals(ballet.title, ignoreCase = true)
        binding.tvOriginalTitle.isVisible = showOriginal
        binding.tvOriginalTitle.text = ballet.originalTitle

        // Creators
        binding.tvCreators.text = "${ballet.composer} • ${ballet.originalChoreographer.orEmpty()}"

        // Badges on photo
        binding.tvBadgeStyle.text = ballet.style ?: "Classical"
        binding.tvBadgeActs.text = getString(R.string.acts_format, ballet.acts)

        // Photo credit
        val credit = ballet.multimedia?.attributionCover
        binding.tvPhotoCredit.text = credit ?: ""
        binding.tvPhotoCredit.isVisible = !credit.isNullOrBlank()

        // Modular Box 1: Genre
        val genreFormatted = if (!ballet.genre.isNullOrEmpty()) {
            ballet.genre.joinToString(" • ")
        }
        else {
            ballet.style ?: "Classical • Full-length"
        }
        binding.tvValueGenre.text = genreFormatted

        // Modular Box 2: Tradition / School
        val traditionText = buildString {
            val trads = ballet.traditions.orEmpty()
            val schs = ballet.schools.orEmpty()
            val combined = (trads + schs).distinct()
            if (combined.isNotEmpty()) {
                append(combined.joinToString(" • "))
            }
        }
        if (traditionText.isNotBlank()) {
            binding.boxTradition.visibility = View.VISIBLE
            binding.tvValueTradition.text = traditionText
        }
        else {
            binding.boxTradition.visibility = View.GONE
        }

        // Modular Box 3: Premiere
        binding.tvLabelPremiere.text = "Estreno • ${ballet.premiereYear}"
        binding.tvValuePremiere.text = ballet.premiereLocation.orEmpty()

        // Cover image & zoom click with Room fallback
        val primaryCover = ballet.multimedia?.coverImageUrl
        if (!primaryCover.isNullOrBlank()) {
            binding.ivHeroCover.loadUrl(primaryCover)
            binding.ivHeroCover.tag = primaryCover
            binding.ivHeroCover.setOnClickListener {
                showImageZoomDialog(primaryCover)
            }
        }
        else {
            lifecycleScope.launch {
                val cachedRecords = repository.getCachedEuropeanaRecords(workId)
                val fallbackImg =
                        cachedRecords.firstOrNull { !it.highResUrl.isNullOrBlank() }?.highResUrl
                        ?: cachedRecords.firstOrNull { !it.previewUrl.isNullOrBlank() }?.previewUrl
                binding.ivHeroCover.loadUrl(fallbackImg)
                binding.ivHeroCover.tag = fallbackImg
                binding.ivHeroCover.setOnClickListener {
                    showImageZoomDialog(fallbackImg)
                }
            }
        }

        // Favorite icon in toolbar
        updateFavoriteIcon(repository.isFavorite(workId))

        // Translated Synopsis
        binding.tvDetailSynopsis.setTranslatedText(
                originalText = ballet.synopsis.orEmpty(),
                sourceLanguage = "en",
                showOriginalInParentheses = false
                                                  )

        // Translated History, Performances & Companies
        val historyContent = buildString {
            append(ballet.historyAndEvolution.orEmpty())
            val performances = ballet.historicalPerformances.orEmpty()
            if (performances.isNotEmpty()) {
                append("\n\nRepresentaciones Históricas:\n")
                performances.forEach { p ->
                    append("• ${p.year} - ${p.company} (${p.theater}): ${p.relevance}\n")
                }
            }
            val companies = ballet.prominentCompanies.orEmpty()
            if (companies.isNotEmpty()) {
                append("\nCompañías Prominentes:\n")
                companies.forEach { c ->
                    append("• ${c.name} (${c.country}) - ${c.officialUrl}\n")
                }
            }
        }
        binding.tvDetailHistory.setTranslatedText(
                originalText = historyContent,
                sourceLanguage = "en",
                showOriginalInParentheses = false
                                                 )

        // Characters & Key Variations
        val chars = ballet.characters.orEmpty()
        val variations = ballet.keyVariations.orEmpty()
        binding.tvDetailCharacters.text = buildString {
            if (chars.isNotEmpty()) {
                append(chars.joinToString("\n") { "• $it" })
            }
            if (variations.isNotEmpty()) {
                if (chars.isNotEmpty()) append("\n\n")
                append("Variaciones Clave:\n")
                append(variations.joinToString("\n") { "• $it" })
            }
            if (chars.isEmpty() && variations.isEmpty()) {
                append(getString(R.string.unavailable))
            }
        }
    }

    private fun loadOpenOpusInfo(composerName: String) {
        lifecycleScope.launch {
            repository.searchComposer(composerName)
            val cachedComposer = repository.getCachedComposer(composerName)
            if (cachedComposer != null) {
                binding.groupComposer.visibility = View.VISIBLE
                val dates = cachedComposer.birth.toLocalizedDate(DateStyle.YEAR_ONLY) +
                            " - " + cachedComposer.death.toLocalizedDate(DateStyle.YEAR_ONLY)
                binding.tvOpenOpusHeader.text = cachedComposer.completeName
                binding.tvOpenOpusInfo.text = dates
                binding.ivComposer.loadUrl(cachedComposer.portrait)
            }
        }
    }

    private fun loadEuropeanaInfo(searchTerm: String) {
        lifecycleScope.launch {
            binding.tvEuropeanaHeader.visibility = View.VISIBLE
            repository.getEuropeanaImages(searchTerm, workId)
            val cachedRecords = repository.getCachedEuropeanaRecords(workId)
            if (cachedRecords.isNotEmpty()) {
                val images =
                        cachedRecords.filter { it.mediaType.equals("IMAGE", ignoreCase = true) }
                val sounds = cachedRecords.filter {
                    it.mediaType.equals("SOUND", ignoreCase = true) || it.mediaType.equals(
                            "AUDIO", ignoreCase = true
                                                                                          )
                }
                val videos =
                        cachedRecords.filter { it.mediaType.equals("VIDEO", ignoreCase = true) }

                if (images.isNotEmpty()) {
                    binding.tvImageGalleryHeader.visibility = View.VISIBLE
                    binding.rvMediaGallery.visibility = View.VISIBLE
                    val adapter = MediaGalleryAdapter { item ->
                        val zoomUrl = item.highResUrl ?: item.previewUrl
                        showImageZoomDialog(zoomUrl)
                    }
                    binding.rvMediaGallery.adapter = adapter
                    adapter.submitList(images)
                }
                else {
                    binding.tvImageGalleryHeader.visibility = View.GONE
                    binding.rvMediaGallery.visibility = View.GONE
                }

                if (sounds.isNotEmpty()) {
                    binding.layoutAudioSection.visibility = View.VISIBLE
                    val playableSounds = sounds.filter { sound ->
                        val url = sound.highResUrl?.lowercase()?.trim().orEmpty()
                        url.endsWith(".mp3") || url.endsWith(".m4a") || url.endsWith(".wav") ||
                        url.endsWith(".aac") || url.contains(".mp3?")
                    }
                    if (playableSounds.isNotEmpty()) {
                        binding.cardAudioPlayer.visibility = View.VISIBLE
                        setupAudioPlayer(playableSounds)
                    }
                    else {
                        binding.cardAudioPlayer.visibility = View.GONE
                    }
                }
                else {
                    binding.layoutAudioSection.visibility = View.GONE
                }

                if (videos.isNotEmpty()) {
                    binding.layoutVideoSection.visibility = View.VISIBLE
                }
                else {
                    binding.layoutVideoSection.visibility = View.GONE
                }
            }
            else {
                binding.tvImageGalleryHeader.visibility = View.GONE
                binding.rvMediaGallery.visibility = View.GONE
                binding.layoutAudioSection.visibility = View.GONE
                binding.layoutVideoSection.visibility = View.GONE
            }
        }
    }

    private fun setupAudioPlayer(playableSounds: List<EuropeanaEntity>) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build().apply {
                val mediaItems = playableSounds.mapNotNull {
                    val url = it.highResUrl ?: it.previewUrl
                    url?.let { u -> MediaItem.fromUri(u) }
                }
                if (mediaItems.isNotEmpty()) {
                    setMediaItems(mediaItems)
                    prepare()
                }

                addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val currentIdx = currentMediaItemIndex
                        if (currentIdx in playableSounds.indices) {
                            val track = playableSounds[currentIdx]
                            binding.tvAudioCurrentTrack.text =
                                    "Pista ${currentIdx + 1} de ${playableSounds.size} — ${track.title}"
                            binding.tvAudioProvider.text = track.dataProvider ?: "Europeana Sounds"
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        binding.btnAudioPlayPause.setImageResource(
                                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                                                                  )
                    }
                })
            }

            if (playableSounds.isNotEmpty()) {
                val firstTrack = playableSounds[0]
                binding.tvAudioCurrentTrack.text =
                        "Pista 1 de ${playableSounds.size} — ${firstTrack.title}"
                binding.tvAudioProvider.text = firstTrack.dataProvider ?: "Europeana Sounds"
            }
        }

        binding.btnAudioPlayPause.setOnClickListener {
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    binding.btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play)
                }
                else {
                    player.play()
                    binding.btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                }
            }
        }

        binding.btnAudioPrev.setOnClickListener {
            exoPlayer?.seekToPrevious()
        }

        binding.btnAudioNext.setOnClickListener {
            exoPlayer?.seekToNext()
        }
    }

    private fun showImageZoomDialog(imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) return
        val dialogBinding = DialogImageZoomBinding.inflate(LayoutInflater.from(this))
        val dialog = android.app.Dialog(this, R.style.Theme_Ballet_ZoomDialog)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )

        dialogBinding.photoViewZoom.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
        try {
            dialogBinding.photoViewZoom.setScaleLevels(1.0f, 3.0f, 8.0f)
        }
        catch (e: Exception) {
            Log.e("BALLET_API_RAW", "PhotoView scale error: ${e.message}", e)
        }
        dialogBinding.photoViewZoom.loadUrl(imageUrl)

        dialogBinding.btnCloseZoom.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        binding.btnDetailFavorite.setImageResource(
                if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                                                  )
    }

    private fun showLoading() {
        binding.detailProgressBar.visibility = View.VISIBLE
        binding.nestedScrollContent.visibility = View.GONE
        binding.detailLayoutError.visibility = View.GONE
    }

    private fun showContent() {
        binding.detailProgressBar.visibility = View.GONE
        binding.detailLayoutError.visibility = View.GONE
        binding.nestedScrollContent.visibility = View.VISIBLE
    }

    private fun showError() {
        binding.detailProgressBar.visibility = View.GONE
        binding.nestedScrollContent.visibility = View.GONE
        binding.detailLayoutError.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}
