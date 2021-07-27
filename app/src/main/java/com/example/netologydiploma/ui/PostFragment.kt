package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.adapter.PostAdapter
import com.example.netologydiploma.databinding.FragmentPostBinding
import com.example.netologydiploma.viewModel.PostViewModel

class PostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding =  FragmentPostBinding.inflate(inflater, container, false)
        val viewModel : PostViewModel by viewModels()

        setHasOptionsMenu(true)

        val adapter = PostAdapter()
        binding.rVPosts.adapter = adapter

        viewModel.postList.observe(viewLifecycleOwner){ postData ->
            adapter.submitList(postData)
        }


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_post_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_add_post -> {
                findNavController().navigate(R.id.action_nav_posts_fragment_to_createEditPostFragment)
                true
            }
            else -> false
        }
    }
}