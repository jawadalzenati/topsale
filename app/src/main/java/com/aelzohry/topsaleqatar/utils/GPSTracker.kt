package com.aelzohry.topsaleqatar.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.helper.Constants
import com.aelzohry.topsaleqatar.helper.Helper
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class GPSTracker(private val content: AppCompatActivity) : LifecycleObserver,
    LocationListener {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {

        isAccessFineLocationGranted({
            picLocation()
        }, {

//            Toast.makeText(
//                content,
//                "Location permission not granted",
//                Toast.LENGTH_LONG
//            ).show()
        })

    }

    private fun picLocation() {

        val locationManager: LocationManager =
            content.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!gpsEnable && !networkEnable) {
            showGPSNotEnabledDialog()
            return
        }

        try {


            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000 * 60 * 1,
                10.toFloat(),
                this
            )

            val location = locationManager.getLastKnownLocation(if (networkEnable) LocationManager.NETWORK_PROVIDER else LocationManager.GPS_PROVIDER)
            Helper.lat = location?.latitude ?: 0.0
            Helper.lng = location?.longitude ?: 0.0
            Log.e("location", "lat is : ${location?.latitude} , lng : ${location?.longitude}")

        } catch (ex: Exception) {

            ex.message?.let { Log.e("location", it) }

        }
    }

    private fun isAccessFineLocationGranted(
        generated: () -> Unit,
        notGenerated: () -> Unit
    ) {
        Dexter.withContext(content)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) generated()

                    if (report?.isAnyPermissionPermanentlyDenied == true) notGenerated()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }


    private fun showGPSNotEnabledDialog() {
        AlertDialog.Builder(content)
            .setTitle("Enable Location")
            .setMessage("Required location for this application")
            .setCancelable(false)
            .setPositiveButton("Enable now") { _, _ ->
                content.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

//        when (requestCode) {
//            Constants.REQUEST_FINE_LOCATION_PERMISSION -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    onStart()
//                } else {
//                    Toast.makeText(
//                        content,
//                        "Location permission not granted",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
    }

    override fun onLocationChanged(location: Location) {

        location?.let {
            Log.e("location", "lat is : ${location.latitude} , lng : ${location.longitude}")
            Helper.lat = location.latitude
            Helper.lng = location.longitude
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }
}