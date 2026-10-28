package com.gf.ballet_wiki.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gf.ballet_wiki.adapters.BalletListAdapter
import com.gf.ballet_wiki.data.remote.dto.BalletDto
import com.gf.ballet_wiki.data.repository.BalletRepository
import com.gf.ballet_wiki.databinding.ActivityMainBinding
import com.gf.ballet_wiki.utils.TranslationManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: BalletRepository
    private lateinit var adapter: BalletListAdapter

    private var fullBalletList: List<BalletDto> = emptyList()
    private var currentSearchQuery: String = ""
    private var filterOnlyFavorites: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = BalletRepository.getInstance(this)

        // Preload translation model in background if on Wi-Fi
        TranslationManager.preloadActiveLanguageModel(this)

        setupRecyclerView()
        setupSearchAndFilters()

        binding.btnRetry.setOnClickListener {
            loadBallets()
        }

        loadBallets()
    }

    override fun onResume() {
        super.onResume()
        // Refresh adapter items in case favorite status changed in detail screen
        if (fullBalletList.isNotEmpty()) {
            adapter.notifyDataSetChanged()
            if (filterOnlyFavorites) {
                applyFilterAndSearch()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = BalletListAdapter(
            onBalletClick = { ballet ->
                val intent = Intent(this, DetailActivity::class.java).apply {
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

        binding.rvBallets.layoutManager = LinearLayoutManager(this)
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

        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            filterOnlyFavorites = checkedIds.contains(binding.chipFavorites.id)
            applyFilterAndSearch()
        }
    }

    private fun loadBallets() {
        showLoading()
        lifecycleScope.launch {
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
}
