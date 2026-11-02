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
import androidx.lifecycle.lifecycleScope
import com.gf.ballet_wiki.R
import com.gf.ballet_wiki.adapters.MediaGalleryAdapter
import com.gf.ballet_wiki.data.remote.dto.BalletDto
import com.gf.ballet_wiki.data.repository.BalletRepository
import com.gf.ballet_wiki.databinding.ActivityDetailBinding
import com.gf.ballet_wiki.databinding.DialogImageZoomBinding
import com.gf.ballet_wiki.utils.DateStyle
import com.gf.ballet_wiki.utils.loadUrl
import com.gf.ballet_wiki.utils.setTranslatedText
import com.gf.ballet_wiki.utils.toLocalizedDate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WORK_ID = "extra_work_id"
    }

    //    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateFormat =
            SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, Locale.getDefault())

    private lateinit var binding: ActivityDetailBinding
    private lateinit var repository: BalletRepository
    private var workId: Int = -1
    private var currentBallet: BalletDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.detailMain) { view, insets ->
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
        loadBalletDetail()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
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
        // Translated title
        binding.tvDetailTitle.setTranslatedText(
                originalText = ballet.title,
                sourceLanguage = "en",
                showOriginalInParentheses = true
                                               )

        binding.tvDetailComposer.text = getString(R.string.composer_format, ballet.composer)
        binding.tvDetailChoreographer.text =
                getString(R.string.choreographer_format, ballet.originalChoreographer)
        binding.tvDetailPremiere.text = getString(
                R.string.premiere_detail_format,
                ballet.premiereYear,
                ballet.premiereLocation,
                ballet.acts
                                                 )

        // Cover image & zoom click with Room fallback
        val primaryCover = ballet.multimedia?.coverImageUrl
        if (!primaryCover.isNullOrBlank()) {
            binding.ivDetailCover.loadUrl(primaryCover)
            binding.ivDetailCover.tag = primaryCover
            binding.ivDetailCover.setOnClickListener {
                showImageZoomDialog(primaryCover)
            }
        }
        else {
            lifecycleScope.launch {
                val cachedRecords = repository.getCachedEuropeanaRecords(workId)
                val fallbackImg =
                        cachedRecords.firstOrNull { !it.highResUrl.isNullOrBlank() }?.highResUrl
                        ?: cachedRecords.firstOrNull { !it.previewUrl.isNullOrBlank() }?.previewUrl
                binding.ivDetailCover.loadUrl(fallbackImg)
                binding.ivDetailCover.tag = fallbackImg
                binding.ivDetailCover.setOnClickListener {
                    showImageZoomDialog(fallbackImg)
                }
            }
        }

        // Favorite icon
        updateFavoriteIcon(repository.isFavorite(workId))

        // Translated Synopsis
        binding.tvDetailSynopsis.setTranslatedText(
                originalText = ballet.synopsis.orEmpty(),
                sourceLanguage = "en",
                showOriginalInParentheses = false
                                                  )

        // Translated History
        binding.tvDetailHistory.setTranslatedText(
                originalText = ballet.historyAndEvolution.orEmpty(),
                sourceLanguage = "en",
                showOriginalInParentheses = false
                                                 )

        // Characters
        val chars = ballet.characters.orEmpty()
        if (chars.isNotEmpty()) {
            binding.tvDetailCharacters.text = chars.joinToString("\n") { "• $it" }
        }
        else {
            binding.tvDetailCharacters.text = getString(R.string.unavailable)
        }
    }

    private fun loadOpenOpusInfo(composerName: String) {
        lifecycleScope.launch {

            //            binding.tvOpenOpusHeader.visibility = View.VISIBLE
            //            binding.tvOpenOpusInfo.visibility = View.VISIBLE
            //            binding.ivComposer.visibility = View.VISIBLE

            // Fetch from API and save to Room
            repository.searchComposer(composerName)

            // Read and display exclusively the LOCAL ROOM ENTITY
            val cachedComposer = repository.getCachedComposer(composerName)
            if (cachedComposer != null) {
                //                Log.i("BALLET_API_RAW", "DISPLAYING LOCAL ROOM COMPOSER ENTITY: $cachedComposer")
                binding.groupComposer.visibility = View.VISIBLE
                val dates = cachedComposer.birth.toLocalizedDate(
                        DateStyle.YEAR_ONLY
                                                                ) + " - " + cachedComposer.death.toLocalizedDate(
                        DateStyle.YEAR_ONLY
                                                                                                                )
                Log.i("BALLET_API_RAW", dates)
                binding.tvOpenOpusHeader.text = cachedComposer.completeName
                binding.tvOpenOpusInfo.text = dates
                binding.ivComposer.loadUrl(cachedComposer.portrait)

                //                        "ENTIDAD LOCAL ROOM (ComposerEntity):\n\n$cachedComposer"

            }
            else {
                binding.tvOpenOpusInfo.text = "No se encontraron datos de compositor en Room."
            }
        }
    }

    private fun loadEuropeanaInfo(searchTerm: String) {
        lifecycleScope.launch {
            binding.tvEuropeanaHeader.visibility = View.VISIBLE

            // Fetch from API and save to Room
            repository.getEuropeanaImages(searchTerm, workId)

            // Read from Room cache and populate 3 modules
            val cachedRecords = repository.getCachedEuropeanaRecords(workId)
            if (cachedRecords.isNotEmpty()) {
                Log.i(
                        "BALLET_API_RAW",
                        "EUROPEANA MODULES LOADED FROM ROOM (${cachedRecords.size} records)"
                     )

                val images =
                        cachedRecords.filter { it.mediaType.equals("IMAGE", ignoreCase = true) }
                val sounds = cachedRecords.filter {
                    it.mediaType.equals("SOUND", ignoreCase = true) || it.mediaType.equals(
                            "AUDIO", ignoreCase = true
                                                                                          )
                }
                val videos =
                        cachedRecords.filter { it.mediaType.equals("VIDEO", ignoreCase = true) }

                // Module 1: Image Gallery RecyclerView
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

                // Module 2: Audio Records
                if (sounds.isNotEmpty()) {
                    binding.layoutAudioSection.visibility = View.VISIBLE
                    binding.tvAudioList.text = buildString {
                        sounds.forEachIndexed { idx, s ->
                            append("${idx + 1}. ${s.title}")
                            if (!s.dataProvider.isNullOrBlank()) append(" — [${s.dataProvider}]")
                            append("\n")
                        }
                    }
                }
                else {
                    binding.layoutAudioSection.visibility = View.GONE
                }

                // Module 3: Video Records
                if (videos.isNotEmpty()) {
                    binding.layoutVideoSection.visibility = View.VISIBLE
                    binding.tvVideoList.text = buildString {
                        videos.forEachIndexed { idx, v ->
                            append("${idx + 1}. ${v.title}")
                            if (!v.dataProvider.isNullOrBlank()) append(" — [${v.dataProvider}]")
                            append("\n")
                        }
                    }
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
        binding.scrollViewContent.visibility = View.GONE
        binding.detailLayoutError.visibility = View.GONE
    }

    private fun showContent() {
        binding.detailProgressBar.visibility = View.GONE
        binding.scrollViewContent.visibility = View.VISIBLE
        binding.detailLayoutError.visibility = View.GONE
    }

    private fun showError() {
        binding.detailProgressBar.visibility = View.GONE
        binding.scrollViewContent.visibility = View.GONE
        binding.detailLayoutError.visibility = View.VISIBLE
    }
}
