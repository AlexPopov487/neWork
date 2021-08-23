package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.netologydiploma.R
import com.example.netologydiploma.adapter.OnUserInteractionListener
import com.example.netologydiploma.adapter.UsersAdapter
import com.example.netologydiploma.databinding.FragmentEventParticipantsBinding
import com.example.netologydiploma.dto.User
import com.example.netologydiploma.viewModel.EventParticipantsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class EventParticipantsFragment : Fragment() {

    lateinit var binding: FragmentEventParticipantsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentEventParticipantsBinding.inflate(inflater, container, false)
        val viewModel: EventParticipantsViewModel by viewModels()
        val navArgs: EventParticipantsFragmentArgs by navArgs()

        val eventId = navArgs.eventId

        val adapter = UsersAdapter(object : OnUserInteractionListener {
            override fun onUserClicked(user: User) {
                val userId = user.id
                val action =
                    EventParticipantsFragmentDirections.actionEventParticipantsFragmentToNavProfileFragment(
                        userId
                    )
                findNavController().navigate(action)
            }
        })

        binding.rvParticipants.adapter = adapter

        lifecycleScope.launchWhenCreated {
            viewModel.getParticipants(eventId).collectLatest {
                adapter.submitList(it)
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
        return binding.root
    }
}