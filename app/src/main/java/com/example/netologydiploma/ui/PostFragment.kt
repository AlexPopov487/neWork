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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.netologydiploma.R
import com.example.netologydiploma.adapter.OnPostButtonInteractionListener
import com.example.netologydiploma.adapter.PagingLoadStateAdapter
import com.example.netologydiploma.adapter.PostAdapter
import com.example.netologydiploma.adapter.PostRecyclerView
import com.example.netologydiploma.databinding.FragmentPostsBinding
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.viewModel.AuthViewModel
import com.example.netologydiploma.viewModel.PostViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class PostFragment: Fragment() {

    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private lateinit var navController: NavController
    private lateinit var recyclerView: PostRecyclerView


    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentPostsBinding.inflate(inflater, container, false)
        val viewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        navController = findNavController()


        // check what LoginFragment have to say about auth state
        // https://developer.android.com/guide/navigation/navigation-conditional
        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(LogInFragment.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry) { success ->
                if (!success) {
                    val startDestination = navController.graph.startDestination
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    navController.navigate(startDestination, null, navOptions)
                }
            }

        recyclerView = binding.rVPosts

        val adapter = PostAdapter(object : OnPostButtonInteractionListener {
            override fun onPostLike(post: Post) {
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
                viewModel.likePost(post)
            }

            override fun onPostRemove(post: Post) {
                viewModel.deletePost(post.id)
            }

            override fun onPostEdit(post: Post) {
                viewModel.editPost(post)
                navController.navigate(R.id.action_nav_posts_fragment_to_createEditPostFragment)
            }

            override fun onAvatarClicked(post: Post) {
                val action = PostFragmentDirections
                    .actionNavPostsFragmentToNavProfileFragment(authorId = post.authorId)
                navController.navigate(action)
            }

            override fun onLinkClicked(url: String) {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), Uri.parse(url))
            }
        })

        binding.rVPosts.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        // solution by https://stackoverflow.com/a/60427676/13924310
        //
        // So far it is the best and only solution to remove item blinking while preserving animations
        // Payloads entailed too many bugs I couldn't fix, so I chose this solution
        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        binding.rVPosts.itemAnimator = itemAnimator

        binding.rVPosts.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter { adapter.retry() },
            footer = PagingLoadStateAdapter { adapter.retry() }
        )


        lifecycleScope.launchWhenCreated {
            viewModel.postList.collectLatest {
                adapter.submitData(it)
            }
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.rVPosts.smoothScrollToPosition(0)
                }
            }
        })

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeToRefresh.isRefreshing = state.refresh == LoadState.Loading

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


    override fun onResume() {
        if(::recyclerView.isInitialized) recyclerView.createPlayer()
        super.onResume()
    }

    override fun onPause() {
        if(::recyclerView.isInitialized) recyclerView.releasePlayer()
        super.onPause()
    }


    override fun onStop() {
        if(::recyclerView.isInitialized) recyclerView.releasePlayer()
        super.onStop()
    }

}
