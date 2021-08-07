package com.example.netologydiploma.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.DateFormat
import java.text.SimpleDateFormat
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

    fun formatMillisToDateString(millis: Long): String {
        return SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
            .format(millis)
    }

    fun formatDateToDateString(date: Date): String {
        return SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
            .format(date)
    }

    fun formatStringToMillis(dateString: String): Long {
        val sdf = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
        return sdf.parse(dateString)!!.run {
            time
        }
    }
}