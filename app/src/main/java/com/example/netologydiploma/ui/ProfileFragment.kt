package com.example.netologydiploma.ui

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.netologydiploma.R
import com.example.netologydiploma.adapter.*
import com.example.netologydiploma.databinding.FragmentProfileBinding
import com.example.netologydiploma.dto.Job
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.util.loadCircleCrop
import com.example.netologydiploma.viewModel.AuthViewModel
import com.example.netologydiploma.viewModel.PostViewModel
import com.example.netologydiploma.viewModel.ProfileViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    val profileViewModel: ProfileViewModel by viewModels(
        ownerProducer = { this }
    )
    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private lateinit var navController: NavController
    private lateinit var postRecyclerView: PostRecyclerView

    @ExperimentalCoroutinesApi
    @ExperimentalPagingApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        navController = findNavController()

        val postViewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val navArgs: ProfileFragmentArgs by navArgs()
        profileViewModel.setAuthorId(navArgs.authorId)

        profileViewModel.getUserById()


        /** set the swipe to refresh behavior according to the collapsing toolbar state */
        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            // toolbar is expanded
            binding.swipeToRefresh.isEnabled = verticalOffset == 0
        })

        postRecyclerView = binding.rVPosts

        val jobAdapter = JobAdapter(object : OnJobButtonInteractionListener {
            override fun onDeleteJob(job: Job) {
                profileViewModel.deleteJobById(job.id)
            }

            override fun onLinkClicked(url: String) {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), Uri.parse(url))
            }

        })

        val postAdapter = PostAdapter(object : OnPostButtonInteractionListener {
            override fun onPostLike(post: Post) {
                profileViewModel.likeWallPostById(post)
            }

            override fun onPostRemove(post: Post) {
                profileViewModel.deletePost(post.id)
            }

            override fun onPostEdit(post: Post) {
                postViewModel.editPost(post)
                navController.navigate(R.id.action_nav_profile_fragment_to_createEditPostFragment)
            }

            override fun onAvatarClicked(post: Post) {
                profileViewModel.getLatestWallPosts()
                profileViewModel.loadJobsFromServer()
            }

            override fun onLinkClicked(url: String) {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), Uri.parse(url))
            }
        })


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

        binding.rVPosts.adapter = postAdapter
        binding.rVPosts.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.profileToolbarLayout.rVJobs.adapter = jobAdapter

        profileViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            (activity as MainActivity?)?.setActionBarTitle(user.login)

            user.avatar?.let {
                binding.profileToolbarLayout.iVAvatar.loadCircleCrop(it)
            } ?: binding.profileToolbarLayout.iVAvatar.setImageDrawable(
                AppCompatResources.getDrawable(requireContext(), R.drawable.ic_no_avatar_user)
            )

            binding.profileToolbarLayout.tvFirstName.text = user.name
        }

        profileViewModel.profileUserId.observe(viewLifecycleOwner) { authorId ->
            if (authViewModel.isAuthenticated) {
                // если пользователь на странице не своего профиля
                if (authorId != profileViewModel.myId) {
                    binding.profileToolbarLayout.btAddJob.visibility = View.GONE
                } else {
                    setHasOptionsMenu(true)
                }
            }
        }

        profileViewModel.loadJobsFromServer()

        lifecycleScope.launchWhenCreated {
            profileViewModel.getWallPosts().collectLatest {
                postAdapter.submitData(it)
            }
        }
        postAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.rVPosts.smoothScrollToPosition(0)
                }
            }
        })
        profileViewModel.getAllJobs().observe(viewLifecycleOwner) {
            val oldCount = jobAdapter.itemCount
            jobAdapter.submitList(it) {
                if (it.size > oldCount) {
                    binding.profileToolbarLayout.rVJobs.smoothScrollToPosition((0))
                }
            }
            binding.profileToolbarLayout.rVJobs.isVisible = it.isNotEmpty()
        }

        binding.profileToolbarLayout.btAddJob.setOnClickListener {
            CreateJobDialogFragment().show(childFragmentManager, "createJob")
        }

        binding.swipeToRefresh.setOnRefreshListener {
            postAdapter.refresh()
            profileViewModel.loadJobsFromServer()
        }

        lifecycleScope.launchWhenCreated {
            postAdapter.loadStateFlow.collectLatest { state ->
                binding.swipeToRefresh.isRefreshing = state.refresh == LoadState.Loading

                if (state.source.refresh is LoadState.NotLoading &&
                    state.append.endOfPaginationReached
                ) {
                    binding.emptyListContainer.isVisible = postAdapter.itemCount < 1
                }
            }
        }


        profileViewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.hasError) {
                val msg = getString(state.errorMessage ?: R.string.common_error_message)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.ok_action), {})
                    .show()
                profileViewModel.invalidateDataState()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!authViewModel.isAuthenticated) // user is not authenticated
            navController.navigate(R.id.logInFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                profileViewModel.onSignOut()
                navController.popBackStack()
                true
            }
            else -> false
        }
    }

    override fun onResume() {
        if (::postRecyclerView.isInitialized) postRecyclerView.createPlayer()
        super.onResume()
    }

    override fun onPause() {
        if (::postRecyclerView.isInitialized) postRecyclerView.releasePlayer()
        super.onPause()
    }


    override fun onStop() {
        if (::postRecyclerView.isInitialized) postRecyclerView.releasePlayer()
        super.onStop()
    }

}