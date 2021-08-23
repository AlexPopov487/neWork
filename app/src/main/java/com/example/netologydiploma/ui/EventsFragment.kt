package com.example.netologydiploma.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
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

        val adapter = EventAdapter(object : OnEventButtonInteractionListener {
            override fun onEventLike(event: Event) {
                if (!authViewModel.isAuthenticated) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.error_unauthorized_to_like),
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction(getString(R.string.ok_action), {})
                        .show()
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
                        getString(R.string.error_unauthorized_to_participate),
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction(getString(R.string.ok_action), {})
                        .show()
                    return
                }
                viewModel.participateInEvent(event)
            }

            override fun onAvatarClicked(event: Event) {
                val action = EventsFragmentDirections
                    .actionNavEventsFragmentToNavProfileFragment(authorId = event.authorId)
                navController.navigate(action)
            }

            override fun onLinkClicked(url: String) {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), Uri.parse(url))
            }

            override fun onSeeParticipantsClicked(event: Event) {
                val action =
                    EventsFragmentDirections.actionNavEventsFragmentToEventParticipantsFragment(
                        event.id
                    )
                navController.navigate(action)

            }
        })

        // solution by https://stackoverflow.com/a/60427676/13924310
        //
        // So far it is the best and only solution to remove item blinking while preserving animations
        // I tried loads of options with payloads but none of them worked correctly when there are 2
        // checkable buttons within the item (they just kept interrupting each other on click).
        // Gosh, I've spend 5 hours on it...
        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        binding.rVEvents.itemAnimator = itemAnimator

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
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.rVEvents.smoothScrollToPosition(0)
                }
            }
        })


        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeToRefresh.isRefreshing = state.refresh is LoadState.Loading

                if (state.source.refresh is LoadState.NotLoading &&
                    state.append.endOfPaginationReached &&
                    adapter.itemCount < 1
                ) {
                    binding.emptyListContainer.visibility = View.VISIBLE
                } else {
                    binding.emptyListContainer.visibility = View.GONE
                }
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state.isLoading

            if (state.hasError) {
                val msg = getString(state.errorMessage ?: R.string.common_error_message)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.ok_action), {})
                    .show()
                viewModel.invalidateDataState()
            }

        }

        binding.swipeToRefresh.setOnRefreshListener {
            adapter.refresh()
        }


        return binding.root
    }

}