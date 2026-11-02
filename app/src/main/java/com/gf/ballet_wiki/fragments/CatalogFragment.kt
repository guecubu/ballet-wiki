package com.gf.ballet_wiki.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.gf.ballet_wiki.R
import com.gf.ballet_wiki.activities.DetailActivity
import com.gf.ballet_wiki.adapters.BalletListAdapter
import com.gf.ballet_wiki.data.remote.dto.BalletDto
import com.gf.ballet_wiki.data.repository.BalletRepository
import com.gf.ballet_wiki.databinding.FragmentCatalogBinding
import com.gf.ballet_wiki.utils.TranslationManager
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: BalletRepository
    private lateinit var adapter: BalletListAdapter

    private var fullBalletList: List<BalletDto> = emptyList()
    private var currentSearchQuery: String = ""
    private var filterOnlyFavorites: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = BalletRepository.getInstance(requireContext())

        TranslationManager.preloadActiveLanguageModel(requireContext())

        filterOnlyFavorites = arguments?.getBoolean("filter_favorites", false) ?: false

        setupRecyclerView()
        setupSearchAndFilters()
        setupSwipeRefresh()
        setupHeaderActions()

        // Restore cached styles from SharedPreferences at 0 ms launch
        val cachedStyles = repository.getSavedStyles()
        if (cachedStyles.isNotEmpty()) {
            setupStyleChips(cachedStyles)
        }

        binding.btnRetry.setOnClickListener {
            loadBallets()
        }

        loadBallets()
    }

    override fun onResume() {
        super.onResume()
        if (fullBalletList.isNotEmpty()) {
            adapter.notifyDataSetChanged()
            if (filterOnlyFavorites) {
                applyFilterAndSearch()
            }
        }
    }

    fun setFilterFavorites(enabled: Boolean) {
        filterOnlyFavorites = enabled
        updateFilterUI()
        applyFilterAndSearch()
    }

    private fun setupRecyclerView() {
        adapter = BalletListAdapter(
            onBalletClick = { ballet ->
                val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_WORK_ID, ballet.id)
                }
                startActivity(intent)
            },
            onFavoriteClick = { ballet ->
                repository.toggleFavorite(ballet.id)
                if (filterOnlyFavorites) {
                    applyFilterAndSearch()
                }
            },
            isFavoriteChecker = { balletId ->
                repository.isFavorite(balletId)
            }
        )

        binding.rvBallets.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvBallets.adapter = adapter
    }

    private fun setupSearchAndFilters() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim().orEmpty()
                applyFilterAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Uncheck all other style chips when "Todos" is checked
                val chipGroup = binding.chipGroupStyles
                for (i in 0 until chipGroup.childCount) {
                    val child = chipGroup.getChildAt(i) as? Chip
                    if (child != null && child.id != binding.chipAll.id) {
                        child.isChecked = false
                    }
                }
                applyFilterAndSearch()
            }
        }
    }

    private fun setupStyleChips(styles: Set<String>) {
        val chipGroup = binding.chipGroupStyles
        // Keep chipAll, remove previous dynamic chips
        val toRemove = mutableListOf<View>()
        for (i in 0 until chipGroup.childCount) {
            val child = chipGroup.getChildAt(i)
            if (child.id != binding.chipAll.id) {
                toRemove.add(child)
            }
        }
        toRemove.forEach { chipGroup.removeView(it) }

        // Populate dynamic style chips using item_filter_chip.xml inflation
        styles.sorted().forEach { rawStyle ->
            val chip = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_filter_chip, chipGroup, false) as Chip
            chip.apply {
                tag = rawStyle // Keep raw English string for filtering
                text = getTranslatedStyleLabel(rawStyle)
                isCheckable = true
                isChecked = false
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        binding.chipAll.isChecked = false
                    }
                    else {
                        // If no style chips remain checked, auto-check "Todos"
                        val hasAnyChecked = (0 until chipGroup.childCount).any { idx ->
                            val c = chipGroup.getChildAt(idx) as? Chip
                            c != null && c.id != binding.chipAll.id && c.isChecked
                        }
                        if (!hasAnyChecked) {
                            binding.chipAll.isChecked = true
                        }
                    }
                    applyFilterAndSearch()
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun getTranslatedStyleLabel(rawStyle: String): String {
        return when (rawStyle.lowercase().trim()) {
            "classical" -> "Clásico"
            "romantic" -> "Romántico"
            "neoclassical" -> "Neoclásico"
            "contemporary" -> "Contemporáneo"
            "modern / avant-garde" -> "Vanguardia"
            else -> rawStyle
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadBallets()
        }
    }

    private fun updateFilterUI() {
        // Handled dynamically by chipGroupStyles and bottom nav selection
    }

    private fun setupHeaderActions() {
        binding.btnProfile.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.about_title), Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBallets() {
        showLoading()
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getBallets()
            binding.swipeRefreshLayout.isRefreshing = false
            result.onSuccess { ballets ->
                fullBalletList = ballets
                showContent()

                // Extract unique styles and save to SharedPreferences + update chips
                val extractedStyles =
                        ballets.mapNotNull { it.style?.trim() }.filter { it.isNotBlank() }.toSet()
                if (extractedStyles.isNotEmpty()) {
                    repository.saveStyles(extractedStyles)
                    setupStyleChips(extractedStyles)
                }

                applyFilterAndSearch()
            }.onFailure {
                showError()
            }
        }
    }

    private fun applyFilterAndSearch() {
        val selectedStyleTags = mutableListOf<String>()
        val chipGroup = binding.chipGroupStyles
        for (i in 0 until chipGroup.childCount) {
            val c = chipGroup.getChildAt(i) as? Chip
            if (c != null && c.id != binding.chipAll.id && c.isChecked) {
                (c.tag as? String)?.let { selectedStyleTags.add(it) }
            }
        }

        val filtered = fullBalletList.filter { ballet ->
            val matchesFavorite = if (filterOnlyFavorites) {
                repository.isFavorite(ballet.id)
            } else {
                true
            }
            val matchesQuery = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                ballet.title.contains(currentSearchQuery, ignoreCase = true) ||
                        ballet.composer.contains(currentSearchQuery, ignoreCase = true)
            }
            val matchesStyle = if (selectedStyleTags.isEmpty() || binding.chipAll.isChecked) {
                true
            }
            else {
                ballet.style != null && selectedStyleTags.any { tag ->
                    ballet.style.equals(tag, ignoreCase = true)
                }
            }
            matchesFavorite && matchesQuery && matchesStyle
        }

        adapter.submitList(filtered) {
            binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showLoading() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        binding.rvBallets.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
    }

    private fun showContent() {
        binding.progressBar.visibility = View.GONE
        binding.rvBallets.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
    }

    private fun showError() {
        binding.progressBar.visibility = View.GONE
        binding.rvBallets.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
