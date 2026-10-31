package com.gf.ballet_wiki.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gf.ballet_wiki.R
import com.gf.ballet_wiki.adapters.NotesAdapter
import com.gf.ballet_wiki.data.local.NoteEntity
import com.gf.ballet_wiki.data.repository.BalletRepository
import com.gf.ballet_wiki.databinding.DialogAddNoteBinding
import com.gf.ballet_wiki.databinding.FragmentNotesBinding
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: BalletRepository
    private lateinit var adapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = BalletRepository.getInstance(requireContext())

        setupRecyclerView()
        setupFab()
        loadNotes()
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(
            onDeleteClick = { note ->
                showDeleteNoteDialog(note)
            }
        )
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun loadNotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            val notes = repository.getNotesForWork(-1)
            adapter.submitList(notes)
            binding.tvNoNotes.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddNoteDialog() {
        val dialogBinding = DialogAddNoteBinding.inflate(LayoutInflater.from(requireContext()))

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_note)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.etNoteTitle.text?.toString()?.trim().orEmpty()
                val content = dialogBinding.etNoteContent.text?.toString()?.trim().orEmpty()

                if (title.isBlank() && content.isBlank()) {
                    Toast.makeText(
                            requireContext(), getString(R.string.note_empty_warning),
                            Toast.LENGTH_SHORT
                                  ).show()
                    return@setPositiveButton
                }

                val finalTitle = if (title.isBlank()) getString(R.string.untitled_note) else title
                val note = NoteEntity(
                    workId = 1,
                    title = finalTitle,
                    content = content
                )

                viewLifecycleOwner.lifecycleScope.launch {
                    repository.insertNote(note)
                    loadNotes()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteNoteDialog(note: NoteEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_note_confirm_title)
            .setMessage(R.string.delete_note_confirm_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.deleteNote(note)
                    loadNotes()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
