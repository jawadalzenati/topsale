package com.aelzohry.topsaleqatar.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.ActivityMainBinding
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.helper.SocketHelper
import com.aelzohry.topsaleqatar.ui.ad_details.AdDetailsFragment
import com.aelzohry.topsaleqatar.ui.auth.LoginActivity
import com.aelzohry.topsaleqatar.ui.home.HomeFragment
import com.aelzohry.topsaleqatar.ui.messages.MessagesFragment
import com.aelzohry.topsaleqatar.ui.more.MoreFragment
import com.aelzohry.topsaleqatar.ui.more.my_ads.MyAdsFragment
import com.aelzohry.topsaleqatar.ui.new_ad.NewAdFragment
import com.aelzohry.topsaleqatar.ui.user.UserFragment
import com.aelzohry.topsaleqatar.utils.GPSTracker
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.luseen.spacenavigation.SpaceItem
import com.luseen.spacenavigation.SpaceOnClickListener
import com.onesignal.OSSubscriptionObserver
import com.onesignal.OSSubscriptionStateChanges
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : BaseActivity<ActivityMainBinding, BaseViewModel>(), OSSubscriptionObserver,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var currentLocation = GPSTracker(this)
    private lateinit var navController: NavController

    override fun getLayoutID(): Int = R.layout.activity_main

    override fun getViewModel(): BaseViewModel = ViewModelProvider(this)[BaseViewModel::class.java]

    override fun setupUI() {

        Helper.authToken?.let { Log.e("token", it) }
        lifecycle.addObserver(currentLocation)
        setupNavController()

        if (Helper.isAuthenticated)
            SocketHelper.connect()

        OneSignal.addSubscriptionObserver(this)

        OneSignal.getDeviceState()?.userId?.let {
            Helper.setPushUserId(it)
        }

        setupShareLinks()

    }

    private fun setupShareLinks() {

        val data = intent.data?.path?.split("/")
//        vm.showToast.postValue("data_size"+data?.toString())
        val id = data?.last()
        val size = data?.size ?: 0
        if (size == 0 || id?.isEmpty() == true)
            return

        when (data?.get(size.minus(2))) {
            "ads"->AdDetailsFragment.newInstance(this,null,
                cameFromChat = false,
                isClear = false,
                adId = id
            )//vm.showToast.postValue("ad_id : $id")
            "users"->UserFragment.newInstance(this,null,null,id)//vm.showToast.postValue("users : $id")
        }
    }

    private fun setupNavController() {

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        navController = Navigation.findNavController(this, R.id.navFragment)

        binding.bottomNavigation.setupWithNavController(navController)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        NavigationUI.setupWithNavController(binding.toolbar, navController)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onClickedListener() {

    }

    override fun observerLiveData() {

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.homeFragment, R.id.profileFragment -> {
                navController.navigate(item.itemId)
                return true
            }

            R.id.addAdFragment -> {
                if (vm.isLogin)
                    NewAdFragment.newAd(this)
                else vm.snackLogin.postValue(true)
                return false
            }
            R.id.chatFragment, R.id.myAdsFragment -> {
                return if (vm.isLogin) {
                    navController.navigate(item.itemId)
                    true
                } else {
                    vm.snackLogin.postValue(true)
                    false
                }
            }
        }
        return false
    }

    override fun onOSSubscriptionChanged(stateChanges: OSSubscriptionStateChanges) {
        if (!stateChanges.from.isSubscribed &&
            stateChanges.to.isSubscribed
        ) {
            // get player ID
            val userId = stateChanges.to.userId
            Helper.setPushUserId(userId)
            Log.i("MainActivity", "OneSignal UserId: $userId")
        }
        Log.i("MainActivity", "onOSPermissionChanged: $stateChanges")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        currentLocation.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
