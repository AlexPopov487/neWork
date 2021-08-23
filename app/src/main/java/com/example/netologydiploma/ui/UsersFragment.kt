package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.netologydiploma.R
import com.example.netologydiploma.adapter.OnUserInteractionListener
import com.example.netologydiploma.adapter.UsersAdapter
import com.example.netologydiploma.databinding.FragmentAllUsersBinding
import com.example.netologydiploma.dto.User
import com.example.netologydiploma.viewModel.UsersViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

lateinit var binding: FragmentAllUsersBinding

@AndroidEntryPoint
class UsersFragment : Fragment() {
    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllUsersBinding.inflate(inflater, container, false)
        val viewModel: UsersViewModel by viewModels()


        val adapter = UsersAdapter(object : OnUserInteractionListener {
            override fun onUserClicked(user: User) {
                val userId = user.id
                val action = UsersFragmentDirections.actionUsersFragmentToNavProfileFragment(userId)
                findNavController().navigate(action)
            }
        })

        binding.rVUsers.adapter = adapter

        binding.rVUsers.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.refreshUsers()
        }

        viewModel.userList.observe(viewLifecycleOwner) { userList ->
            adapter.submitList(userList)

            if (!viewModel.dataState.value?.isRefreshing!! &&
                !viewModel.dataState.value?.isLoading!!
            ) {
                binding.emptyListContainer.isVisible = userList.isEmpty()
            }

        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.swipeToRefresh.isRefreshing = state.isRefreshing
            binding.progressBar.isVisible = state.isLoading

            if (state.hasError) {
                val msg = getString(state.errorMessage ?: R.string.common_error_message)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.ok_action), {})
                    .show()
                viewModel.invalidateDataState()
            }

        }

        return binding.root
    }
}