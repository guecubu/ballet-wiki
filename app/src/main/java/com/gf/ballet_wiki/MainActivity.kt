package com.gf.ballet_wiki

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gf.ballet_wiki.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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
        val repository = WikidataRepository()
        binding.workStatus.text = getString(R.string.data_loaded_offline)
        repository.loadSwanLake { work, loadedRemotely ->
            runOnUiThread {
                binding.workTitle.text = work.title
                binding.workDescription.text = work.description
                binding.composerValue.text = work.composer
                binding.aliasesValue.text = work.titles.joinToString(" · ")
                binding.sourceLink.text = getString(R.string.wikidata_source, work.sourceUrl)
                binding.workStatus.text = getString(
                        if (loadedRemotely) R.string.data_loaded_remote else R.string.data_loaded_offline
                                                   )
            }
        }
    }
}