package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.FragmentCreateEditPostBinding
import com.example.netologydiploma.db.PostEntity
import com.example.netologydiploma.viewModel.PostViewModel


class CreateEditPostFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private lateinit var binding: FragmentCreateEditPostBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateEditPostBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)


        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_create_edit_post_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_add_post -> {
                val content = binding.eTPostContent.text.toString()
                viewModel.savePost(PostEntity(content = content))
                findNavController().popBackStack()
                true
            }
            else -> false
        }
    }

}