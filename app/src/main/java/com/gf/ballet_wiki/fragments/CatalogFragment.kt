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
        setupHeaderActions()

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

        binding.btnFilterAll.setOnClickListener {
            filterOnlyFavorites = false
            updateFilterUI()
            applyFilterAndSearch()
        }

        binding.btnFilterFavorites.setOnClickListener {
            filterOnlyFavorites = true
            updateFilterUI()
            applyFilterAndSearch()
        }
        updateFilterUI()
    }

    private fun updateFilterUI() {
        if (filterOnlyFavorites) {
            binding.btnFilterFavorites.setBackgroundResource(R.drawable.bg_segment_active)
            binding.btnFilterFavorites.setTextColor(requireContext().getColor(R.color.ballet_rose_accent))
            binding.btnFilterFavorites.iconTint = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.ballet_rose_accent))

            binding.btnFilterAll.background = null
            binding.btnFilterAll.setTextColor(requireContext().getColor(R.color.ballet_text_medium_emphasis))
            binding.btnFilterAll.iconTint = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.ballet_text_medium_emphasis))
        } else {
            binding.btnFilterAll.setBackgroundResource(R.drawable.bg_segment_active)
            binding.btnFilterAll.setTextColor(requireContext().getColor(R.color.ballet_gold_primary))
            binding.btnFilterAll.iconTint = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.ballet_gold_primary))

            binding.btnFilterFavorites.background = null
            binding.btnFilterFavorites.setTextColor(requireContext().getColor(R.color.ballet_text_medium_emphasis))
            binding.btnFilterFavorites.iconTint = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.ballet_text_medium_emphasis))
        }
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
            result.onSuccess { ballets ->
                fullBalletList = ballets
                showContent()
                applyFilterAndSearch()
            }.onFailure {
                showError()
            }
        }
    }

    private fun applyFilterAndSearch() {
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
            matchesFavorite && matchesQuery
        }

        adapter.submitList(filtered) {
            binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
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
