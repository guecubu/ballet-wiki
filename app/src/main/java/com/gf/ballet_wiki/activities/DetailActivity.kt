package com.gf.ballet_wiki.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.gf.ballet_wiki.utils.loadUrl
import com.gf.ballet_wiki.utils.setTranslatedText
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WORK_ID = "extra_work_id"
    }

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
        binding.tvDetailChoreographer.text = getString(R.string.choreographer_format, ballet.originalChoreographer)
        binding.tvDetailPremiere.text = getString(
            R.string.premiere_detail_format,
            ballet.premiereYear,
            ballet.premiereLocation,
            ballet.acts
        )

        // Cover image
        binding.ivDetailCover.loadUrl(ballet.multimedia?.coverImageUrl)

        // Favorite icon
        updateFavoriteIcon(repository.isFavorite(workId))

        // Translated Synopsis
        binding.tvDetailSynopsis.setTranslatedText(
            originalText = ballet.synopsis,
            sourceLanguage = "en",
            showOriginalInParentheses = false
        )

        // Translated History
        binding.tvDetailHistory.setTranslatedText(
            originalText = ballet.historyAndEvolution,
            sourceLanguage = "en",
            showOriginalInParentheses = false
        )

        // Characters
        if (ballet.characters.isNotEmpty()) {
            binding.tvDetailCharacters.text = ballet.characters.joinToString("\n") { "• $it" }
        } else {
            binding.tvDetailCharacters.text = getString(R.string.unavailable)
        }
    }

    private fun loadOpenOpusInfo(composerName: String) {
        lifecycleScope.launch {
            binding.tvOpenOpusHeader.visibility = View.VISIBLE
            binding.tvOpenOpusInfo.visibility = View.VISIBLE

            // Fetch from API and save to Room
            repository.searchComposer(composerName)

            // Read and display exclusively the LOCAL ROOM ENTITY
            val cachedComposer = repository.getCachedComposer(composerName)
            if (cachedComposer != null) {
                Log.i("BALLET_API_RAW", "DISPLAYING LOCAL ROOM COMPOSER ENTITY: $cachedComposer")
                binding.tvOpenOpusInfo.text =
                        "ENTIDAD LOCAL ROOM (ComposerEntity):\n\n$cachedComposer"
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
                    it.mediaType.equals(
                            "SOUND", ignoreCase = true
                                       ) || it.mediaType.equals("AUDIO", ignoreCase = true)
                }
                val videos =
                        cachedRecords.filter { it.mediaType.equals("VIDEO", ignoreCase = true) }

                // Module 1: Image Gallery RecyclerView
                if (images.isNotEmpty()) {
                    binding.tvImageGalleryHeader.visibility = View.VISIBLE
                    binding.rvMediaGallery.visibility = View.VISIBLE
                    val adapter = MediaGalleryAdapter { item ->
                        // Optional item click
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
