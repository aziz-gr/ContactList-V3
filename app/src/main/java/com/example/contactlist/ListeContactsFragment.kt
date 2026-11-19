package com.example.contactlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.contactlist.databinding.FragmentListeContactsBinding

class ListeContactsFragment : Fragment() {

    private var _binding: FragmentListeContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListeContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.updateTitle(getString(R.string.title_contact_list), showBack = false)

        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ContactAdapter { contact ->
            openDetailFragment(contact.id)
        }
        binding.recyclerViewContacts.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.setText(viewModel.searchQuery.value)

        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(text?.toString().orEmpty())
            updateListDisplay()
        }
    }

    private fun observeViewModel() {
        viewModel.contacts.observe(viewLifecycleOwner) {
            updateListDisplay()
        }
    }

    private fun updateListDisplay() {
        val filteredList = viewModel.getFilteredContacts()
        adapter.submitList(filteredList)

        val count = filteredList.size
        binding.tvContactCount.text = getString(R.string.subtitle_contact_count, count)

        if (filteredList.isEmpty()) {
            binding.recyclerViewContacts.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE

            val query = viewModel.searchQuery.value.orEmpty()
            binding.tvEmptyMessage.text = if (query.isNotEmpty()) {
                getString(R.string.msg_no_search_results)
            } else {
                getString(R.string.msg_no_contacts)
            }
        } else {
            binding.recyclerViewContacts.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }

    private fun openDetailFragment(contactId: String) {
        val fragment = DetailContactFragment.newInstance(contactId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
