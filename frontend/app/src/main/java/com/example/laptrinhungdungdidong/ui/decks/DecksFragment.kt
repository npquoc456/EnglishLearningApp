package com.example.laptrinhungdungdidong.ui.decks

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laptrinhungdungdidong.MainActivity
import com.example.laptrinhungdungdidong.data.api.ApiClient
import com.example.laptrinhungdungdidong.data.models.DeckResponse
import com.example.laptrinhungdungdidong.data.models.DeckUpdateRequest
import com.example.laptrinhungdungdidong.data.session.SessionManager
import com.example.laptrinhungdungdidong.databinding.FragmentDecksBinding
import com.example.laptrinhungdungdidong.ui.adapters.DeckAdapter
import com.example.laptrinhungdungdidong.ui.deck.CreateDeckActivity
import com.example.laptrinhungdungdidong.ui.study.StudyActivity
import kotlinx.coroutines.launch

class DecksFragment : Fragment() {

    private var _binding: FragmentDecksBinding? = null
    private val binding get() = _binding!!

    private lateinit var deckAdapter: DeckAdapter
    private var allDecks: List<DeckResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = SessionManager(requireContext()).getUser()
        val streak = user?.currentStreak ?: 1
        binding.tvDecksStreak.text = "$streak ngày"

        setupRecyclerView()
        setupSearch()

        binding.btnCreateDeck.setOnClickListener {
            startActivity(Intent(requireContext(), CreateDeckActivity::class.java))
        }

        binding.btnCreateFirstDeck.setOnClickListener {
            startActivity(Intent(requireContext(), CreateDeckActivity::class.java))
        }

        loadDecks()
    }

    override fun onResume() {
        super.onResume()
        loadDecks()
        (activity as? MainActivity)?.updateDeckBadge()
    }

    private fun setupRecyclerView() {
        deckAdapter = DeckAdapter(
            decks = emptyList(),
            onDeckClick = { deck ->
                val intent = Intent(requireContext(), StudyActivity::class.java).apply {
                    putExtra("DECK_ID", deck.id)
                    putExtra("DECK_TITLE", deck.title)
                }
                startActivity(intent)
            },
            onStudyClick = { deck ->
                val intent = Intent(requireContext(), StudyActivity::class.java).apply {
                    putExtra("DECK_ID", deck.id)
                    putExtra("DECK_TITLE", deck.title)
                }
                startActivity(intent)
            },
            onMoreClick = { anchorView, deck ->
                showDeckOptionsMenu(anchorView, deck)
            }
        )

        binding.rvDecks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deckAdapter
        }
    }

    private fun showDeckOptionsMenu(anchorView: View, deck: DeckResponse) {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), anchorView)
        popup.menu.add(0, 1, 0, "✏  Chỉnh sửa bộ thẻ")
        popup.menu.add(0, 2, 1, "🗑  Xóa bộ thẻ")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    showEditDeckDialog(deck)
                    true
                }
                2 -> {
                    showDeleteDeckConfirmation(deck)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showEditDeckDialog(deck: DeckResponse) {
        val input = android.widget.EditText(requireContext()).apply {
            setText(deck.title)
            setSelection(text.length)
            setPadding(50, 40, 50, 40)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa tên bộ thẻ")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    updateDeckTitle(deck.id, newTitle)
                } else {
                    Toast.makeText(requireContext(), "Tên bộ thẻ không được để trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateDeckTitle(deckId: Long, newTitle: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.service.updateDeck(deckId, DeckUpdateRequest(title = newTitle))
                if (res.isSuccessful) {
                    Toast.makeText(requireContext(), "Đã cập nhật tên bộ thẻ!", Toast.LENGTH_SHORT).show()
                    loadDecks()
                } else {
                    Toast.makeText(requireContext(), "Không thể cập nhật bộ thẻ", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteDeckConfirmation(deck: DeckResponse) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa bộ thẻ")
            .setMessage("Bạn có chắc chắn muốn xóa bộ thẻ '${deck.title}' và toàn bộ các thẻ từ vựng bên trong?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteDeck(deck.id)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteDeck(deckId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.service.deleteDeck(deckId)
                if (res.isSuccessful) {
                    Toast.makeText(requireContext(), "Đã xóa bộ thẻ thành công!", Toast.LENGTH_SHORT).show()
                    loadDecks()
                    (activity as? MainActivity)?.updateDeckBadge()
                } else {
                    Toast.makeText(requireContext(), "Không thể xóa bộ thẻ", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        binding.etSearchDeck.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDecks(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterDecks(query: String) {
        if (query.isBlank()) {
            deckAdapter.updateData(allDecks)
            updateVisibility(allDecks)
        } else {
            val filtered = allDecks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.categoryTag?.contains(query, ignoreCase = true) == true)
            }
            deckAdapter.updateData(filtered)
            binding.tvDeckCountBadge.text = "${filtered.size} kết quả"
        }
    }

    private fun loadDecks() {
        val user = SessionManager(requireContext()).getUser() ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.service.getDecksByUserId(user.id)
                if (res.isSuccessful && res.body() != null) {
                    allDecks = res.body()!!
                    deckAdapter.updateData(allDecks)
                    updateVisibility(allDecks)
                } else {
                    allDecks = emptyList()
                    deckAdapter.updateData(allDecks)
                    updateVisibility(allDecks)
                }
            } catch (_: Exception) {
                allDecks = emptyList()
                deckAdapter.updateData(allDecks)
                updateVisibility(allDecks)
            }
        }
    }

    private fun updateVisibility(decks: List<DeckResponse>) {
        if (decks.isEmpty()) {
            binding.rvDecks.visibility = View.GONE
            binding.layoutEmptyDecks.visibility = View.VISIBLE
            binding.tvDeckCountBadge.text = "0 đang học"
        } else {
            binding.rvDecks.visibility = View.VISIBLE
            binding.layoutEmptyDecks.visibility = View.GONE
            binding.tvDeckCountBadge.text = "${decks.size} đang học"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
