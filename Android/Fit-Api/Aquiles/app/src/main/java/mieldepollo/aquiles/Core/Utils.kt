package mieldepollo.aquiles.Core

import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import mieldepollo.aquiles.MainActivity
import android.util.Log

val UtilsTag = "Utils.kt#Aquiles"

private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

fun checkPermissions(activity: MainActivity): Boolean {
    val permissionState = ActivityCompat.checkSelfPermission(activity,
            Manifest.permission.ACCESS_FINE_LOCATION)
    return permissionState == PackageManager.PERMISSION_GRANTED
}

fun requestPermissions(activity: MainActivity) {
    val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity,
            Manifest.permission.ACCESS_FINE_LOCATION)

    // Provide an additional rationale to the user. This would happen if the user denied the
    // request previously, but didn't check the "Don't ask again" checkbox.
    if (shouldProvideRationale) {
        Log.i(UtilsTag, "Displaying permission rationale to provide additional context.")
        ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE)
    } else {
        Log.i(UtilsTag, "Requesting permission")
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE)
    }
}
