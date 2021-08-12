package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.netologydiploma.R
import com.example.netologydiploma.adapter.EventAdapter
import com.example.netologydiploma.adapter.OnEventButtonInteractionListener
import com.example.netologydiploma.adapter.PagingLoadStateAdapter
import com.example.netologydiploma.databinding.FragmentEventsBinding
import com.example.netologydiploma.dto.Event
import com.example.netologydiploma.viewModel.AuthViewModel
import com.example.netologydiploma.viewModel.EventViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class EventsFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private lateinit var binding: FragmentEventsBinding

    @ExperimentalPagingApi
    private val viewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private lateinit var navController: NavController

    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        navController = findNavController()

        if (authViewModel.isAuthenticated) {
            setHasOptionsMenu(true)
        } else {
            setHasOptionsMenu(false)
        }

        val adapter = EventAdapter(object : OnEventButtonInteractionListener {
            override fun onEventLike(event: Event) {
                if (!authViewModel.isAuthenticated) {
                    Snackbar.make(
                        binding.root,
                        "Only authorized users can leave likes!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return
                }
                viewModel.likeEvent(event)
            }

            override fun onEventEdit(event: Event) {
                viewModel.editEvent(event)
                navController.navigate(R.id.action_nav_events_fragment_to_createEventFragment)
            }

            override fun onEventRemove(event: Event) {
                viewModel.deleteEvent(event.id)
            }

            override fun onEventParticipate(event: Event) {
                if (!authViewModel.isAuthenticated) {
                    Snackbar.make(
                        binding.root,
                        "Only authorized users can participate!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return
                }
                viewModel.participateInEvent(event)
            }

            override fun onAvatarClicked(event: Event) {
                val action = EventsFragmentDirections
                    .actionNavEventsFragmentToNavProfileFragment(
                        authorId = event.authorId,
                        authorName = event.author,
                        avatar = event.authorAvatar
                    )
                navController.navigate(action)
            }

        })

        binding.rVEvents.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter { adapter.retry() },
            footer = PagingLoadStateAdapter { adapter.retry() })
        binding.rVEvents.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        lifecycleScope.launchWhenCreated {
            viewModel.eventList.collectLatest {
                adapter.submitData(it)
            }
        }



        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { loadState ->
                binding.swipeToRefresh.isRefreshing = loadState.refresh is LoadState.Loading

            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state.isLoading

            if (state.hasError) {
                val msg = state.errorMessage ?: "Something went wrong, please try again later."
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                viewModel.invalidateDataState()
            }
        }

        binding.swipeToRefresh.setOnRefreshListener {
            adapter.refresh()
        }


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_post_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                navController.navigate(R.id.action_nav_events_fragment_to_createEventFragment)
                true
            }
            else -> false
        }
    }
}