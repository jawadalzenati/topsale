package com.aelzohry.topsaleqatar.ui.new_ad.adLocation

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.ActivityAdLocationBinding
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class AdLocationActivity : BaseActivity<ActivityAdLocationBinding, AdLocationViewModel>(),
    OnMapReadyCallback {

    private var map: GoogleMap? = null

    override fun getLayoutID(): Int = R.layout.activity_ad_location

    override fun getViewModel(): AdLocationViewModel =
        ViewModelProvider(this)[AdLocationViewModel::class.java]

    override fun setupUI() {
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
    }

    override fun onClickedListener() {

    }

    override fun observerLiveData() {

    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map

        map?.isMyLocationEnabled = true
        changeLocation(LatLng(Helper.lat, Helper.lng))

        map?.setOnMapClickListener {
            map.clear()
            changeLocation(it)
        }
    }

    private fun changeLocation(address: LatLng) {

        vm.lat = address.latitude
        vm.lng = address.longitude


        map?.apply {
            // just a random location our map will point to when its launched
            addMarker(MarkerOptions().apply {
                position(address)
                title("")
                draggable(false)
            })
            // setup zoom level
            animateCamera(CameraUpdateFactory.newLatLngZoom(address, 15f))

        }

        getAddress(address)

    }

    private fun getAddress(latLng: LatLng) {
        return try {
            val address = Geocoder(this, Locale.getDefault()).getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1
            )[0]

            val city = address.locality
            val state = address.adminArea
            val country = address.countryName
            val postalCode = address.postalCode
            val knownName = address.featureName
            val strAddress = address.getAddressLine(0)

            vm.etAddress.set(strAddress)
        } catch (ex: Exception) {
            vm.etAddress.set("")
        }
    }


}