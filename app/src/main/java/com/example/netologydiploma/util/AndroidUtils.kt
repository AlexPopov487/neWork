package com.example.netologydiploma.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


    fun formatMillisToDateTimeString(millis: Long?): String {
        return SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
            .format(millis)
    }

    fun formatMillisToDateString(millis: Long?): String? {
        return if (millis == null) null
        else SimpleDateFormat.getDateInstance(DateFormat.DEFAULT)
            .format(millis)
    }

    fun formatDateToDateTimeString(date: Date): String {
        return SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
            .format(date)
    }

    fun formatDateToDateString(date: Date): String {
        return SimpleDateFormat.getDateInstance(DateFormat.DEFAULT)
            .format(date)
    }

    fun formatDateTimeStringToMillis(dateString: String): Long {
        val sdf = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
        return sdf.parse(dateString)!!.run {
            time
        }
    }

    fun formatDateStringToMillis(dateString: String): Long {
        val sdf = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT)
        return sdf.parse(dateString)!!.run {
            time
        }
    }
}