package com.example.netologydiploma.ui

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.FragmentCreateEventBinding
import com.example.netologydiploma.dto.Event
import com.example.netologydiploma.dto.EventType
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.util.loadImage
import com.example.netologydiploma.viewModel.EventViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.util.*

@ExperimentalPagingApi
@AndroidEntryPoint
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
        viewModel.editedEvent.observe(viewLifecycleOwner) { editedEvent ->
            editedEvent?.let {
                (activity as MainActivity?)
                    ?.setActionBarTitle(getString(R.string.change_event_fragment_title))

                binding.eTPostContent.setText(editedEvent.content)
                binding.eTPostContent.requestFocus(
                    binding.eTPostContent.text.lastIndex
                )

                binding.tVEventDateTime.text =
                    AndroidUtils.formatMillisToDateTimeString(editedEvent.datetime.toEpochMilli())
                AndroidUtils.showKeyboard(binding.eTPostContent)

                it.attachment?.let { attachment ->
                    val attachmentUri = attachment.url
                    viewModel.changePhoto(attachmentUri.toUri(), null)
                    binding.ivPhoto.loadImage(attachmentUri)
                    //disable media removal
                    binding.btRemovePhoto.visibility = View.GONE
                }

                when (editedEvent.type) {
                    EventType.OFFLINE -> binding.buttonEventTypeGroup.check(R.id.button_type_offline)
                    EventType.ONLINE -> binding.buttonEventTypeGroup.check(R.id.button_type_online)
                }
            }
        }

        binding.groupPickEventDate.setOnClickListener {
            showDateTimePicker()
        }

        binding.buttonEventTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_type_online -> eventType = EventType.ONLINE
                    R.id.button_type_offline -> eventType = EventType.OFFLINE
                }
            }
        }

        viewModel.eventDateTime.observe(viewLifecycleOwner) { dateTime ->
            dateTime?.let {
                binding.tVEventDateTime.text = it
            }
        }

        val handlePhotoResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val resultCode = activityResult.resultCode
                val data = activityResult.data

                if (resultCode == Activity.RESULT_OK) {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    viewModel.changePhoto(fileUri, fileUri.toFile())
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Snackbar.make(
                        binding.root,
                        ImagePicker.getError(activityResult.data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        binding.btPickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(arrayOf("image/png", "image/jpeg"))
                .createIntent { intent ->
                    handlePhotoResult.launch(intent)
                }
        }

        binding.btTakePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .provider(ImageProvider.CAMERA)
                .createIntent { intent ->
                    handlePhotoResult.launch(intent)
                }
        }


        binding.btRemovePhoto.setOnClickListener {
            viewModel.changePhoto(null, null)
        }


        viewModel.photo.observe(viewLifecycleOwner) { photoModel ->
            if (photoModel.uri == null) {
                binding.layoutPhotoContainer.visibility = View.GONE
                return@observe
            }

            binding.layoutPhotoContainer.visibility = View.VISIBLE
            binding.ivPhoto.setImageURI(photoModel.uri)
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

                viewModel.setEventDateTime(
                    AndroidUtils.formatDateToDateTimeString(calendar.time)
                )
            }.show(childFragmentManager, "timePicker")
        }.show(childFragmentManager, "datePicker")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_create_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                if (binding.eTPostContent.text.isNullOrEmpty()) {
                    binding.eTPostContent.error = getString(R.string.empty_field_error)
                    return false
                }

                if (binding.tVEventDateTime.text.isNullOrEmpty()) {
                    binding.tVEventDateTime.error = getString(R.string.empty_field_error)
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
                    AndroidUtils.formatDateTimeStringToMillis(binding.tVEventDateTime.text.toString())
                val eventType = eventType ?: EventType.OFFLINE


                // if editedEvent is not null, we are to rewrite an existing post.
                // Otherwise, save a new one
                viewModel.editedEvent.value?.let {
                    // change media so if there were any in the edited post earlier, it wouldn't
                    // affect  viewModel.savePost() function
                    viewModel.changePhoto(null, null)
                    viewModel.saveEvent(
                        it.copy(
                            content = content,
                            datetime = Instant.ofEpochMilli(date),
                            type = eventType,
                        )
                    )
                } ?: viewModel.saveEvent(
                    Event(
                        content = content,
                        datetime = Instant.ofEpochMilli(date),
                        type = eventType,
                    )
                )
                AndroidUtils.hideKeyboard(requireView())
                findNavController().popBackStack()
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showAlert()
                }
            })
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.discard_changes_dialog_title))
        builder.setMessage(getString(R.string.discard_changes_dialog_body))
        builder.setPositiveButton(
            getString(R.string.action_leave_dialog_fragment),
            DialogInterface.OnClickListener { dialog, which ->
                viewModel.invalidateEditedEvent()
                viewModel.invalidateEventDateTime()
                viewModel.changePhoto(null, null)
                findNavController().navigateUp()
            })
        builder.setNeutralButton(
            getString(R.string.action_cancel_dialog_fragment),
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
        val dialog = builder.create()
        dialog.show()
    }
}