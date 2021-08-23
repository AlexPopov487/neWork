package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.DialogFragmentCreateJobBinding
import com.example.netologydiploma.dto.Job
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.viewModel.ProfileViewModel
import java.util.*

class CreateJobDialogFragment : DialogFragment() {

    private val viewModel: ProfileViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    lateinit var binding: DialogFragmentCreateJobBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentCreateJobBinding.inflate(inflater, container, false)


        binding.tVTermStart.setOnClickListener {
            onShowDatePicker(isStartDate = true)
        }

        binding.tVTermFinish.setOnClickListener {
            onShowDatePicker(isStartDate = false)
        }

        binding.btCancel.setOnClickListener {
            dismiss()
        }

        binding.btConfirm.setOnClickListener {
            onSaveNewJob()
            // dialog is dismissed in onSaveNewJob()
        }
        return binding.root
    }

    private fun onSaveNewJob() {
        val company = binding.eTCompany.text.toString().trim()

        val position = binding.eTPosition.text.toString().trim()

        val dateStart = binding.tVTermStart.text.toString().trim()

        val dateFinished = if (binding.tVTermFinish.text.isNullOrEmpty()) {
            null
        } else {
            AndroidUtils.formatDateStringToMillis(
                binding.tVTermFinish.text.toString().trim()
            )
        }

        val link = binding.eTLink.text.toString().trim()

        if (company.isEmpty()) {
            showToast(getString(R.string._no_company_create_job_error))
            return
        }
        if (position.isEmpty()) {
            showToast(getString(R.string._no_position_create_job_error))
            return
        }
        if (dateStart.isEmpty()) {
            showToast(getString(R.string._no_start_date_create_job_error))
            return
        }

        viewModel.createNewJob(
            Job(
                name = company,
                position = position,
                start = AndroidUtils.formatDateStringToMillis(dateStart),
                finish = dateFinished,
                link = link
            )
        )
        dismiss()
    }

    private fun onShowDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerFragment(calendar) { _, year, month, dayOfMonth ->
            // set data to calendar value once user selects the date
            calendar.set(year, month, dayOfMonth)

            if (isStartDate)
                binding.tVTermStart.text = AndroidUtils.formatDateToDateString(calendar.time)
            else binding.tVTermFinish.text = AndroidUtils.formatDateToDateString(calendar.time)

        }.show(childFragmentManager, "datePicker")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
