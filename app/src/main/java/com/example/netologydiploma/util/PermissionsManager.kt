package com.example.netologydiploma.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.netologydiploma.R

class PermissionsManager(val activity: Activity, val list: List<String>, val code: Int) {

    // Check permissions at runtime
    fun checkPermissions(): Boolean {
        return isPermissionsGranted() == PackageManager.PERMISSION_GRANTED
    }


    // Check permissions status
    private fun isPermissionsGranted(): Int {
        // PERMISSION_GRANTED : Constant Value: 0
        // PERMISSION_DENIED : Constant Value: -1
        var counter = 0
        for (permission in list) {
            counter += ContextCompat.checkSelfPermission(activity, permission)
        }
        return counter
    }

    // Find the first denied permission
    private fun deniedPermission(): String {
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_DENIED
            ) return permission
        }
        return ""
    }


    // Show alert dialog to request permissions
    private fun showAlert(permission: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.permissions_needed_title))
        builder.setMessage(activity.getString(R.string.grant_following_permissions_dialog_message))
        builder.setPositiveButton(activity.getString(R.string.ok_action)) { dialog, which ->
            ActivityCompat.requestPermissions(activity, arrayOf(permission), code)

        }
        builder.setNeutralButton(activity.getString(R.string.action_cancel), null)
        val dialog = builder.create()
        dialog.show()
    }


    // Request the permissions at run time
    fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            showAlert(permission)
        } else {
            ActivityCompat.requestPermissions(activity, list.toTypedArray(), code)
        }
    }
}