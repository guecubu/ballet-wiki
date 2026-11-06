package com.gf.ballet_wiki.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.gf.ballet_wiki.utils.TranslationManager
import com.gf.ballet_wiki.utils.loadUrl
import com.gf.ballet_wiki.utils.toLocalizedDate
import com.google.android.material.chip.Chip
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
            //            binding.tvPhotoCredit.alpha = badgeAlpha
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
        TranslationManager.translate(
                context = this,
                originalText = ballet.title,
                sourceLanguage = "en",
                onTranslating = { binding.tvTitle.text = getString(R.string.translating) },
                onTranslated = { translatedTitle ->
                    binding.tvTitle.text = translatedTitle

                    // Original title as separate data field compared against actual translated title
                    val showOriginal = !ballet.originalTitle.isNullOrBlank() &&
                                       !ballet.originalTitle.equals(
                                               translatedTitle, ignoreCase = true
                                                                   )
                    binding.tvOriginalTitle.isVisible = showOriginal
                    binding.tvOriginalTitle.text = ballet.originalTitle
                }
                                    )

        // Creators
        binding.tvCreators.text = "${ballet.composer} • ${ballet.originalChoreographer.orEmpty()}"

        // Floating Badges on photo (Optimized batch translation & repository cache)
        val styleLabel = if (!ballet.style.isNullOrBlank()) repository.getStyleTranslation(
                ballet.style
                                                                                          )
        else "Classical"
        binding.tvBadgeStyle.text = styleLabel
        binding.tvBadgeActs.text = getString(R.string.acts_format, ballet.acts)

        val epochRaw = ballet.epochPeriod.orEmpty()
        val diffRaw = ballet.difficultyLevel.orEmpty()

        binding.tvBadgeEpoch.isVisible = epochRaw.isNotBlank()
        binding.tvBadgeDifficulty.isVisible = diffRaw.isNotBlank()

        if (epochRaw.isNotBlank() || diffRaw.isNotBlank()) {
            val termsToTranslate = listOf(epochRaw, diffRaw).filter { it.isNotBlank() }
            TranslationManager.translateList(this, termsToTranslate) { translatedTerms ->
                var idx = 0
                if (epochRaw.isNotBlank() && idx < translatedTerms.size) {
                    binding.tvBadgeEpoch.text = translatedTerms[idx++]
                }
                if (diffRaw.isNotBlank() && idx < translatedTerms.size) {
                    binding.tvBadgeDifficulty.text = translatedTerms[idx++]
                }
            }
        }

        // Photo credit
        //        val credit = ballet.multimedia?.attributionCover
        //        binding.tvPhotoCredit.text = credit ?: ""
        //        binding.tvPhotoCredit.isVisible = !credit.isNullOrBlank()

        // Modular Box 1: Genre (translated item by item)
        val rawGenres = ballet.genre.orEmpty().filter { it.isNotBlank() }
        if (rawGenres.isNotEmpty()) {
            TranslationManager.translateList(this, rawGenres) { translatedGenres ->
                binding.tvValueGenre.text = translatedGenres.joinToString(" • ")
            }
        }
        else {
            binding.tvValueGenre.text = ballet.style ?: "Classical • Full-length"
        }

        // Modular Box 2: Tradition & Schools (Dynamic columns with inner pills, translated item by item)
        val traditionsList = ballet.traditions.orEmpty().filter { it.isNotBlank() }
        val schoolsList = ballet.schools.orEmpty().filter { it.isNotBlank() }

        val density = resources.displayMetrics.density
        val padH = (8 * density).toInt()
        val padV = (6 * density).toInt()
        val marginTopItem = (6 * density).toInt()

        // Populate Traditions
        TranslationManager.translateList(this, traditionsList) { translatedTraditions ->
            val finalTraditions = if (translatedTraditions.isNotEmpty()) translatedTraditions
            else listOf(getString(R.string.unavailable))

            binding.layoutTraditionItems.removeAllViews()
            for ((index, item) in finalTraditions.withIndex()) {
                val tv = TextView(this).apply {
                    text = item
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTextColor(getColor(R.color.ballet_text_high_emphasis))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setBackgroundResource(R.drawable.bg_pill_item_inner)
                    setPadding(padH, padV, padH, padV)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                            ).apply {
                        if (index > 0) topMargin = marginTopItem
                    }
                }
                binding.layoutTraditionItems.addView(tv)
            }
        }

        // Populate Schools
        TranslationManager.translateList(this, schoolsList) { translatedSchools ->
            val finalSchools = if (translatedSchools.isNotEmpty()) translatedSchools
            else listOf(getString(R.string.unavailable))

            binding.layoutSchoolsItems.removeAllViews()
            for ((index, item) in finalSchools.withIndex()) {
                val tv = TextView(this).apply {
                    text = item
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTextColor(getColor(R.color.ballet_text_high_emphasis))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setBackgroundResource(R.drawable.bg_pill_item_inner)
                    setPadding(padH, padV, padH, padV)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                            ).apply {
                        if (index > 0) topMargin = marginTopItem
                    }
                }
                binding.layoutSchoolsItems.addView(tv)
            }
        }

        // Modular Box 3: Premiere (location translated item by item splitting by comma)
        binding.tvLabelPremiere.text = "Estreno • ${ballet.premiereYear}"
        val rawLoc = ballet.premiereLocation.orEmpty()
        if (rawLoc.isNotBlank()) {
            val locParts = rawLoc.split(",").map { it.trim() }.filter { it.isNotBlank() }
            TranslationManager.translateList(this, locParts) { translatedLocs ->
                binding.tvValuePremiere.text = translatedLocs.joinToString(", ")
            }
        }
        else {
            binding.tvValuePremiere.text = ""
        }

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
        TranslationManager.translate(
                context = this,
                originalText = ballet.synopsis.orEmpty(),
                sourceLanguage = "en",
                onTranslating = { binding.tvDetailSynopsis.text = getString(R.string.translating) },
                onTranslated = { translatedSynopsis ->
                    binding.tvDetailSynopsis.text = translatedSynopsis
                }
                                    )

        // Translated History
        val historyContent = ballet.historyAndEvolution.orEmpty()
        TranslationManager.translate(
                context = this,
                originalText = historyContent,
                sourceLanguage = "en",
                onTranslating = { binding.tvDetailHistory.text = getString(R.string.translating) },
                onTranslated = { translatedHistory ->
                    binding.tvDetailHistory.text = translatedHistory
                }
                                    )

        // Historical Performances Card (Vertical Timeline with Alternating Item Backgrounds)
        val performances = ballet.historicalPerformances.orEmpty()
        if (performances.isNotEmpty()) {
            binding.cardPerformances.isVisible = true

            val relevances = performances.map { it.relevance }
            TranslationManager.translateList(this, relevances) { translatedRelevances ->
                binding.layoutPerformancesContainer.removeAllViews()
                val density = resources.displayMetrics.density
                val padItem = (12 * density).toInt()
                val marginBetween = (10 * density).toInt()

                val alternatingBgs = listOf(
                        R.color.ballet_surface_variant,
                        R.color.ballet_floating_chip_purple_light
                                           )

                performances.forEachIndexed { index, perf ->
                    val itemCard = com.google.android.material.card.MaterialCardView(this).apply {
                        setCardBackgroundColor(
                                getColor(alternatingBgs[index % alternatingBgs.size])
                                              )
                        radius = 12 * density
                        cardElevation = 0f
                        strokeWidth = 0
                        layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                                                                ).apply {
                            if (index > 0) topMargin = marginBetween
                        }
                    }

                    val itemLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(padItem, padItem, padItem, padItem)
                    }

                    // Row 1: Year badge + Company
                    val topRow = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }

                    val tvYear = TextView(this).apply {
                        text = perf.year.toString()
                        setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
                        setTextColor(getColor(R.color.ballet_badge_era_text))
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setBackgroundResource(R.drawable.bg_badge_era)
                        setPadding(
                                (8 * density).toInt(), (4 * density).toInt(), (8 * density).toInt(),
                                (4 * density).toInt()
                                  )
                    }

                    val tvCompany = TextView(this).apply {
                        text = "${perf.company} • ${perf.theater}"
                        setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13f)
                        setTextColor(getColor(R.color.ballet_text_high_emphasis))
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        layoutParams = LinearLayout
                                .LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            marginStart = (10 * density).toInt()
                        }
                    }

                    topRow.addView(tvYear)
                    topRow.addView(tvCompany)
                    itemLayout.addView(topRow)

                    // Choreographer
                    if (perf.choreographer.isNotBlank()) {
                        val tvChoreo = TextView(this).apply {
                            text = "Coreografía: ${perf.choreographer}"
                            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
                            setTextColor(getColor(R.color.ballet_gold_primary))
                            layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                                                    ).apply {
                                topMargin = (6 * density).toInt()
                            }
                        }
                        itemLayout.addView(tvChoreo)
                    }

                    // Relevance (translated)
                    val relText = translatedRelevances.getOrNull(index) ?: perf.relevance
                    if (relText.isNotBlank()) {
                        val tvRelevance = TextView(this).apply {
                            text = relText
                            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
                            setTextColor(getColor(R.color.ballet_text_medium_emphasis))
                            setTypeface(null, android.graphics.Typeface.ITALIC)
                            layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                                                    ).apply {
                                topMargin = (6 * density).toInt()
                            }
                        }
                        itemLayout.addView(tvRelevance)
                    }

                    itemCard.addView(itemLayout)
                    binding.layoutPerformancesContainer.addView(itemCard)
                }
            }
        }
        else {
            binding.cardPerformances.isVisible = false
        }

        // Prominent Companies Card with Browser URL Launcher
        val companies = ballet.prominentCompanies.orEmpty()
        if (companies.isNotEmpty()) {
            binding.cardCompanies.isVisible = true
            binding.layoutCompaniesContainer.removeAllViews()
            val density = resources.displayMetrics.density
            val padV = (8 * density).toInt()

            companies.forEachIndexed { index, company ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setPadding(0, padV, 0, padV)
                }

                val infoCol = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val tvName = TextView(this).apply {
                    text = company.name
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(getColor(R.color.ballet_text_high_emphasis))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }

                val tvCountry = TextView(this).apply {
                    text = company.country
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(getColor(R.color.ballet_text_medium_emphasis))
                }

                infoCol.addView(tvName)
                infoCol.addView(tvCountry)

                val btnUrl = android.widget.ImageButton(this).apply {
                    setImageResource(R.drawable.ic_open_in_new)
                    setBackgroundResource(R.drawable.bg_badge_light)
                    setPadding(
                            (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(),
                            (8 * density).toInt()
                              )
                    contentDescription = "Abrir sitio web"
                    setOnClickListener {
                        val rawUrl = company.officialUrl.trim()
                        if (rawUrl.isNotBlank()) {
                            try {
                                val validUrl =
                                        if (!rawUrl.startsWith("http://") && !rawUrl.startsWith(
                                                        "https://"
                                                                                               )
                                        ) {
                                            "https://$rawUrl"
                                        }
                                        else rawUrl
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(validUrl)))
                            }
                            catch (e: Exception) {
                                Toast.makeText(
                                        this@DetailActivity, "No se pudo abrir el sitio web",
                                        Toast.LENGTH_SHORT
                                              ).show()
                            }
                        }
                    }
                }

                row.addView(infoCol)
                row.addView(btnUrl)

                binding.layoutCompaniesContainer.addView(row)

                if (index < companies.size - 1) {
                    val divider = com.google.android.material.divider.MaterialDivider(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1
                                                                ).apply {
                            topMargin = (4 * density).toInt()
                            bottomMargin = (4 * density).toInt()
                        }
                    }
                    binding.layoutCompaniesContainer.addView(divider)
                }
            }
        }
        else {
            binding.cardCompanies.isVisible = false
        }

        // Characters & Key Variations (translated item by item, separated into 2 distinct cards)
        val chars = ballet.characters.orEmpty().filter { it.isNotBlank() }
        val variations = ballet.keyVariations.orEmpty().filter { it.isNotBlank() }

        TranslationManager.translateList(this, chars) { translatedChars ->
            binding.chipGroupCharacters.removeAllViews()
            val chipBackgroundsColors = listOf(
                    R.color.ballet_floating_chip_rose,
                    R.color.ballet_floating_chip_purple_light,
                    R.color.ballet_floating_chip_rose_light,
                    R.color.ballet_floating_chip_gold
                                        )
            val chipTextColors = listOf(
                    getColor(R.color.ballet_floating_chip_rose_text),
                    getColor(R.color.ballet_floating_chip_purple_light_text),
                    getColor(R.color.ballet_floating_chip_rose_light_text),
                    getColor(R.color.ballet_floating_chip_gold_text)
                                       )

            if (translatedChars.isNotEmpty()) {
                translatedChars.forEachIndexed { index, characterName ->
                    val chip = Chip(this).apply {
                        text = characterName
                        isClickable = false
                        isCheckable = false

                        setChipBackgroundColorResource(
                                chipBackgroundsColors[index % chipBackgroundsColors.size]
                                                      )
                        setTextColor(chipTextColors[index % chipTextColors.size])
                        textSize = 12f
                    }
                    binding.chipGroupCharacters.addView(chip)
                }
            }
            else {
                val chip = Chip(this).apply {
                    text = getString(R.string.unavailable)
                    isClickable = false
                    setBackgroundResource(R.drawable.bg_badge_light)
                    setTextColor(getColor(R.color.ballet_floating_chip_light_text))
                    textSize = 12f
                }
                binding.chipGroupCharacters.addView(chip)
            }
        }

        TranslationManager.translateList(this, variations) { translatedVariations ->
            binding.tvDetailVariations.text = buildString {
                if (translatedVariations.isNotEmpty()) {
                    append(translatedVariations.joinToString("\n") { "• $it" })
                }
                else {
                    append(getString(R.string.unavailable))
                }
            }
        }
    }

    private fun loadOpenOpusInfo(composerName: String) {
        lifecycleScope.launch {
            repository.searchComposer(composerName)
            val cachedComposers = repository.getCachedComposers(composerName)
            if (cachedComposers.isNotEmpty()) {
                binding.groupComposer.visibility = View.VISIBLE
                val primaryComposer = cachedComposers.firstOrNull { !it.portrait.isNullOrBlank() }
                                      ?: cachedComposers.first()
                if (cachedComposers.size == 1) {
                    val comp = cachedComposers.first()
                    binding.tvOpenOpusHeader.text = comp.completeName
                    val b = comp.birth.toLocalizedDate(DateStyle.YEAR_ONLY)
                    val d = comp.death.toLocalizedDate(DateStyle.YEAR_ONLY)
                    binding.tvOpenOpusInfo.text =
                            if (b.isNotBlank() && d.isNotBlank()) "$b - $d" else if (b.isNotBlank()) b else ""
                    binding.ivComposer.loadUrl(comp.portrait)
                }
                else {
                    val names = cachedComposers.joinToString(", ") { it.name }
                    binding.tvOpenOpusHeader.text = names
                    binding.tvOpenOpusInfo.text =
                            "Música compilada • ${cachedComposers.size} compositores"
                    binding.ivComposer.loadUrl(primaryComposer.portrait)
                }
            }
            else {
                binding.groupComposer.visibility = View.GONE
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

                //                if (sounds.isNotEmpty()) {
                //                    binding.layoutAudioSection.visibility = View.VISIBLE
                //                    val playableSounds = sounds.filter { sound ->
                //                        val url = sound.highResUrl?.lowercase()?.trim().orEmpty()
                //                        url.endsWith(".mp3") || url.endsWith(".m4a") || url.endsWith(".wav") ||
                //                        url.endsWith(".aac") || url.contains(".mp3?")
                //                    }
                //                    if (playableSounds.isNotEmpty()) {
                //                        binding.cardAudioPlayer.visibility = View.VISIBLE
                //                        setupAudioPlayer(playableSounds)
                //                    }
                //                    else {
                //                        binding.cardAudioPlayer.visibility = View.GONE
                //                    }
                //                }
                //                else {
                //                    binding.layoutAudioSection.visibility = View.GONE
                //                }
                //
                //                if (videos.isNotEmpty()) {
                //                    binding.layoutVideoSection.visibility = View.VISIBLE
                //                }
                //                else {
                //                    binding.layoutVideoSection.visibility = View.GONE
                //                }
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
