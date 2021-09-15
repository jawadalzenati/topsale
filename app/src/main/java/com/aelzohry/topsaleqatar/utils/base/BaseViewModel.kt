package com.aelzohry.topsaleqatar.utils.base

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.model.Banner
import com.aelzohry.topsaleqatar.ui.ad_details.AdDetailsFragment
import com.aelzohry.topsaleqatar.utils.CustomFrame
import com.aelzohry.topsaleqatar.utils.SingleLiveEvent

open class BaseViewModel : ViewModel(), LifecycleObserver {

    var showToast = SingleLiveEvent<Any>().apply { value = null }
    var snackLogin = SingleLiveEvent<Boolean>().apply { value = false }
    var isLoading = SingleLiveEvent<Boolean>().apply { value = false }
    var swipeRefresh = ObservableField(false)
    var frameState =
        ObservableField(CustomFrame.FrameState.PROGRESS).apply { set(CustomFrame.FrameState.PROGRESS) }
    var isLogin = Helper.isAuthenticated
    var isAdViewGrid = ObservableField(true)

    init {
        isAdViewGrid.set(true)
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun refreshData() {

        isLogin = Helper.isAuthenticated
    }

    fun onAdClickedListener(v: View, model: Ad) {

        AdDetailsFragment.newInstance(v.context, model, isClear = false)

    }

    fun onAdBannerClickedListener(v: View, banner: Banner) {

        if (banner.ad != null) {
            onAdClickedListener(v, banner.ad)
            return
        }

        if (banner.url != null) {
            Helper.openUrl(banner.url, v.context)
            return
        }

    }

    open fun onBackClickedListener(v: View) {
        Navigation.findNavController(v).popBackStack()
    }
}