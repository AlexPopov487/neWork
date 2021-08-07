package com.example.netologydiploma.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(
    private val calendar: Calendar,
    private val listener: TimePickerDialog.OnTimeSetListener
) : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val min = calendar.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(
            requireContext(),
            listener,
            hour,
            min,
            DateFormat.is24HourFormat(activity)
        )
    }
}

class DatePickerFragment(
    private val calendar: Calendar,
    private val listener: DatePickerDialog.OnDateSetListener
) :
    DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(
            requireContext(),
            listener,
            year,
            month,
            day,
        )
    }
}