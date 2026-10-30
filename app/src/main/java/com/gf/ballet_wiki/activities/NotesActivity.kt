package com.gf.ballet_wiki.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gf.ballet_wiki.R
import com.gf.ballet_wiki.adapters.NotesAdapter
import com.gf.ballet_wiki.data.local.NoteEntity
import com.gf.ballet_wiki.data.repository.BalletRepository
import com.gf.ballet_wiki.databinding.ActivityNotesBinding
import com.gf.ballet_wiki.databinding.DialogAddNoteBinding
import kotlinx.coroutines.launch

class NotesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WORK_ID = "extra_work_id"
        const val EXTRA_WORK_TITLE = "extra_work_title"
    }

    private lateinit var binding: ActivityNotesBinding
    private lateinit var repository: BalletRepository
    private lateinit var adapter: NotesAdapter

    private var workId: Int = -1
    private var workTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.notesMain) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = BalletRepository.getInstance(this)
        workId = intent.getIntExtra(EXTRA_WORK_ID, -1)
        workTitle = intent.getStringExtra(EXTRA_WORK_TITLE).orEmpty()

        if (workId == -1) {
            workId = 1 // Default to work 1 if opened from global nav
            if (workTitle.isBlank()) {
                workTitle = "Notas Generales"
            }
        }

        setupToolbar()
        setupRecyclerView()
        setupFab()
        loadNotes()
    }

    private fun setupToolbar() {
        if (workTitle.isNotBlank()) {
            binding.toolbarNotes.subtitle = workTitle
        } else {
            binding.toolbarNotes.subtitle = "Notas Generales"
        }
        binding.toolbarNotes.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(
            onDeleteClick = { note ->
                showDeleteNoteDialog(note)
            }
        )
        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            val notes = repository.getNotesForWork(workId)
            adapter.submitList(notes)
            binding.tvNoNotes.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddNoteDialog() {
        val dialogBinding = DialogAddNoteBinding.inflate(LayoutInflater.from(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.add_note)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.etNoteTitle.text?.toString()?.trim().orEmpty()
                val content = dialogBinding.etNoteContent.text?.toString()?.trim().orEmpty()

                if (title.isBlank() && content.isBlank()) {
                    Toast.makeText(this, "La nota no puede estar vacía", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val finalTitle = if (title.isBlank()) "Nota sin título" else title
                val note = NoteEntity(
                    workId = workId,
                    title = finalTitle,
                    content = content
                )

                lifecycleScope.launch {
                    repository.insertNote(note)
                    loadNotes()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteNoteDialog(note: NoteEntity) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_note_confirm_title)
            .setMessage(R.string.delete_note_confirm_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    repository.deleteNote(note)
                    loadNotes()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
