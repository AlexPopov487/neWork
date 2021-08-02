package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.adapter.OnButtonInteractionListener
import com.example.netologydiploma.adapter.PostAdapter
import com.example.netologydiploma.databinding.FragmentPostsBinding
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.viewModel.AuthViewModel
import com.example.netologydiploma.viewModel.PostViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
class PostFragment: Fragment() {

    private val authViewModel : AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private lateinit var navController: NavController

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

            // check what LoginFragment have to say about log in state
            // https://developer.android.com/guide/navigation/navigation-conditional
            val currentBackStackEntry = navController.currentBackStackEntry!!
            val savedStateHandle = currentBackStackEntry.savedStateHandle
            savedStateHandle.getLiveData<Boolean>(LogInFragment.LOGIN_SUCCESSFUL)
                .observe(currentBackStackEntry, Observer { success ->
                    if (!success) {
                        val startDestination = navController.graph.startDestination
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(startDestination, true)
                            .build()
                        navController.navigate(startDestination, null, navOptions)
                    }
                })

            if (authViewModel.isAuthenticated) {
                setHasOptionsMenu(true)
            } else {
                setHasOptionsMenu(false)
            }

            val adapter = PostAdapter(object : OnButtonInteractionListener {
                override fun onLike(post: Post) {
                    Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
                }

                override fun onRemove(post: Post) {
                    viewModel.deletePost(post.id)
                }
            })
            binding.rVPosts.adapter = adapter

            viewModel.postList.observe(viewLifecycleOwner) { postData ->
                adapter.submitList(postData)
            }

            viewModel.dataState.observe(viewLifecycleOwner) { state ->
                binding.progressBar.isVisible = state.isLoading

                if (state.hasError) {
                    val msg = state.errorMessage ?: "Something went wrong, please try again later."
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                    viewModel.invalidateDataState()
                }
            }

            return binding.root
        }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.fragment_post_menu, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_add_post -> {
                    navController.navigate(R.id.action_nav_posts_fragment_to_createEditPostFragment)
                    true
                }
                else -> false
            }
        }
    }
