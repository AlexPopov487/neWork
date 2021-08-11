package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import com.example.netologydiploma.R
import com.example.netologydiploma.adapter.JobAdapter
import com.example.netologydiploma.adapter.OnJobButtonInteractionListener
import com.example.netologydiploma.adapter.OnPostButtonInteractionListener
import com.example.netologydiploma.adapter.PostAdapter
import com.example.netologydiploma.databinding.FragmentProfileBinding
import com.example.netologydiploma.dto.Job
import com.example.netologydiploma.dto.Post
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

    val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private lateinit var navController: NavController


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
        val authorId = profileViewModel.setAuthorId(navArgs.authorId)




        if (authViewModel.isAuthenticated) {

            // если пользователь на странице не своего профиля
            if (authorId != profileViewModel.myId) {
                binding.profileToolbarLayout.btAddJob.visibility = View.GONE
            } else {
                setHasOptionsMenu(true)
            }
        }


        binding.profileToolbarLayout.tvFirstName.text =
            profileViewModel.setAuthorName(navArgs.authorName)

        /** set the swipe to refresh behavior according to the collapsing toolbar state */
        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            // toolbar is expanded
            binding.swipeToRefresh.isEnabled = verticalOffset == 0
        })


        val jobAdapter = JobAdapter(object : OnJobButtonInteractionListener {
            override fun onDeleteJob(job: Job) {
                profileViewModel.deleteJobById(job.id)
            }
        })


        val postAdapter = PostAdapter(object : OnPostButtonInteractionListener {
            override fun onPostLike(post: Post) {
                postViewModel.likePost(post)
            }

            override fun onPostRemove(post: Post) {
                postViewModel.deletePost(post.id)
            }

            override fun onPostEdit(post: Post) {
                postViewModel.editPost(post)
                navController.navigate(R.id.action_nav_profile_fragment_to_createEditPostFragment)
            }

            override fun onAvatarClicked(post: Post) {
                refreshCurrentFragment()
            }
        })

        binding.rVPosts.adapter = postAdapter
        binding.profileToolbarLayout.rVJobs.adapter = jobAdapter



        lifecycleScope.launchWhenResumed {
            profileViewModel.loadJobsFromServer(authorId)
        }

        lifecycleScope.launchWhenCreated {
            profileViewModel.getWallPosts(authorId).collectLatest {
                postAdapter.submitData(it)
            }
        }

        profileViewModel.getAllJobs().observe(viewLifecycleOwner) {
            jobAdapter.submitList(it)
            binding.profileToolbarLayout.rVJobs.isVisible = it.isNotEmpty()
        }

        binding.profileToolbarLayout.btAddJob.setOnClickListener {
            CreateJobDialogFragment().show(childFragmentManager, "createJob")
        }

        binding.swipeToRefresh.setOnRefreshListener {
            postAdapter.refresh()
        }

        lifecycleScope.launchWhenCreated {
            postAdapter.loadStateFlow.collectLatest { state ->
                binding.swipeToRefresh.isRefreshing = state.refresh == LoadState.Loading
            }
        }


        profileViewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.hasError) {
                val msg = state.errorMessage ?: getString(R.string.common_error_message)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
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

    private fun refreshCurrentFragment() {
        val currentFrId = navController.currentDestination?.id
        navController.popBackStack(currentFrId!!, true)
        navController.navigate(currentFrId)
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
}