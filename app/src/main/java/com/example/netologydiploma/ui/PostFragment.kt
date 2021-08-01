package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.adapter.OnButtonInteractionListener
import com.example.netologydiploma.adapter.PostAdapter
import com.example.netologydiploma.databinding.FragmentPostsBinding
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.viewModel.PostViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class PostFragment : Fragment() {

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



        setHasOptionsMenu(true)

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
        return when(item.itemId) {
            R.id.action_add_post -> {
                navController.navigate(R.id.action_nav_posts_fragment_to_createEditPostFragment)
                true
            }
            else -> false
        }
    }
}