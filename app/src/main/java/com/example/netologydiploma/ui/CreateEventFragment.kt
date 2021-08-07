package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.FragmentCreateEventBinding
import com.example.netologydiploma.dto.Event
import com.example.netologydiploma.dto.EventType
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.viewModel.EventViewModel
import com.google.android.material.snackbar.Snackbar
import java.util.*


class CreateEventFragment : Fragment() {

    private lateinit var binding: FragmentCreateEventBinding
    private val viewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private var eventType: EventType? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        /** User wants to edit an existing event */
        viewModel.editedEvent.observe(viewLifecycleOwner) { editedPost ->
            editedPost?.let {
                (activity as MainActivity?)
                    ?.setActionBarTitle(getString(R.string.change_event_fragment_title))

                binding.eTPostContent.setText(editedPost.content)
                binding.eTPostContent.requestFocus(
                    binding.eTPostContent.text.lastIndex
                )

                binding.tVEventDateTime.text =
                    AndroidUtils.formatMillisToDateString(editedPost.datetime)
                AndroidUtils.showKeyboard(binding.eTPostContent)

                when (editedPost.type) {
                    EventType.OFFLINE -> binding.buttonEventType.check(R.id.button_type_offline)
                    EventType.ONLINE -> binding.buttonEventType.check(R.id.button_type_online)
                }
            }
        }

        binding.groupPickEventDate.setOnClickListener {
            showDateTimePicker()
        }

        binding.buttonEventType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_type_online -> eventType = EventType.ONLINE
                    R.id.button_type_offline -> eventType = EventType.OFFLINE
                }
            }
        }

        return binding.root
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DatePickerFragment(calendar) { _, year, month, dayOfMonth ->
            // set data to calendar value once user selects the date
            calendar.set(year, month, dayOfMonth)

            // call timePicker after user picks the date
            TimePickerFragment(calendar) { _, hourOfDay, minute ->
                // set data to calendar value once user selects the time
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                binding.tVEventDateTime.text = AndroidUtils.formatDateToDateString(calendar.time)
            }.show(childFragmentManager, "timePicker")
        }.show(childFragmentManager, "datePicker")
    }


    override fun onDestroy() {
        if (viewModel.editedEvent.value != null) {
            viewModel.invalidateEditedEvent()
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_create_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                if (binding.eTPostContent.text.isNullOrEmpty()) {
                    binding.eTPostContent.error = "The field cannot be empty!"
                    return false
                }

                if (binding.tVEventDateTime.text.isNullOrEmpty()) {
                    binding.tVEventDateTime.error = "The field cannot be empty!"
                    return false
                }

                if (eventType == null) {
                    Snackbar.make(
                        binding.root,
                        "Please, select event type!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return false
                }

                val content = binding.eTPostContent.text.toString()
                val date =
                    AndroidUtils.formatStringToMillis(binding.tVEventDateTime.text.toString())
                val eventType = eventType ?: EventType.OFFLINE

                // if editedEvent is not null, we are to rewrite an existing post.
                // Otherwise, save a new one
                viewModel.editedEvent.value?.let {
                    viewModel.saveEvent(
                        it.copy(
                            content = content,
                            datetime = date,
                            type = eventType
                        )
                    )
                } ?: viewModel.saveEvent(
                    Event(
                        content = content,
                        datetime = date,
                        type = eventType
                    )
                )
                AndroidUtils.hideKeyboard(requireView())
                findNavController().popBackStack()
                true
            }
            else -> false
        }
    }


}