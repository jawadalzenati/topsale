package com.aelzohry.topsaleqatar.ui.home

import android.content.Intent
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.navigation.Navigation
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.helper.hideKeyboard
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.model.Banner
import com.aelzohry.topsaleqatar.model.TopBanner
import com.aelzohry.topsaleqatar.model.TopBannerType
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.repository.remote.responses.BannersResponse
import com.aelzohry.topsaleqatar.repository.remote.responses.HomeResponse
import com.aelzohry.topsaleqatar.ui.ads.AdsAdapter
import com.aelzohry.topsaleqatar.ui.ads.AdsFragment
import com.aelzohry.topsaleqatar.ui.categories.CategoriesFragment
import com.aelzohry.topsaleqatar.ui.new_ad.NewAdFragment
import com.aelzohry.topsaleqatar.ui.search.SearchFragment
import com.aelzohry.topsaleqatar.utils.CustomFrame
import com.aelzohry.topsaleqatar.utils.SingleLiveEvent
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import retrofit2.Call

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
class HomeViewModel : BaseViewModel() {

    private var repository: Repository = RemoteRepository()
    private var homeCall: Call<HomeResponse>? = null
    private var bannersCall: Call<BannersResponse>? = null
    lateinit var adsAdapter: AdsAdapter

    var adsRes = MutableLiveData<ArrayList<Ad>>()
    var adsBannerRes = MutableLiveData<ArrayList<Banner>>()
    var topBannersRes = MutableLiveData<ArrayList<TopBanner>>()

    var etSearch = ObservableField("")

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun loadData() {
        loadData(false)
    }

    private fun loadData(isSwipe: Boolean) {

        if (isSwipe) swipeRefresh.set(true)
        frameState.set(CustomFrame.FrameState.PROGRESS)
        homeCall = repository.getHome {
            it?.data?.let { data ->

                topBannersRes.postValue(data.banners)
                adsRes.postValue(data.ads)
            }
            frameState.set(CustomFrame.FrameState.LAYOUT)
            if (isSwipe) swipeRefresh.set(false)
        }

        bannersCall = repository.getBanners {
            it?.let { banners ->
                adsBannerRes.postValue(banners)
            }
        }
    }

    fun onRefreshListener() {
        adsAdapter.clear()
        loadData(true)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun disConnect() {
        homeCall?.cancel()
        bannersCall?.cancel()
    }

    fun onAddAdButtonClickedListener(v: View) {

        if (isLogin)
            NewAdFragment.newAd(v.context)
        else snackLogin.postValue(true)

    }

    fun onCatsButtonsClickedListener(v: View) {
        v.context.startActivity(Intent(v.context, CategoriesFragment::class.java))
//        Navigation.findNavController(v).navigate(R.id.catsFragment)
    }

    fun onSearchClickedListener(v: View) {

        val txt = etSearch.get() ?: ""
        if (txt.isEmpty())
            return

        etSearch.set("")
        v.hideKeyboard()
        SearchFragment.newInstance(v.context, txt)

    }

    fun onBannerClickedListener(v: View, model: TopBanner) {

        when (model.type) {
            TopBannerType.CATEGORY -> {
                model.category?.let {
                    AdsFragment.newInstance(v.context, it)
                }
            }
            TopBannerType.URL -> {
                model.url?.let {
                    Helper.openUrl(it, v.context)
                }
            }
            TopBannerType.AD -> {
                model.ad?.let {
                    onAdClickedListener(v, it)
                }
            }
        }
    }
}