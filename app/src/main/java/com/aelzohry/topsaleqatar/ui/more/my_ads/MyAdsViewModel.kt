package com.aelzohry.topsaleqatar.ui.more.my_ads

import android.content.Intent
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.repository.remote.AdsDataSource
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.ui.more.followers.FollowersFragment
import com.aelzohry.topsaleqatar.ui.more.followings.FollowingsFragment
import com.aelzohry.topsaleqatar.ui.more.profile.ProfileFragment
import com.aelzohry.topsaleqatar.utils.CustomFrame
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
class MyAdsViewModel : BaseViewModel() {
    private val dataSourceFactory: AdsDataSource.DataSourceFactory
    private var repository = RemoteRepository()
    var adsRes: LiveData<PagedList<Ad>>

    var followingCount = ObservableField("")
    var followersCount = ObservableField("")
    var name = ObservableField("")
    var phone = ObservableField("")
    var email = ObservableField("")
    var bio = ObservableField("")
    var website = ObservableField("")

    init {

        repository = RemoteRepository()
        val prams = HashMap<String, Any>()
        dataSourceFactory = AdsDataSource.DataSourceFactory(prams, AdsDataSource.MY_ADS)
        val pageSize = 10
        adsRes = LivePagedListBuilder<Int, Ad>(
            dataSourceFactory,
            PagedList.Config.Builder()
                .setPageSize(pageSize)
                .setInitialLoadSizeHint(pageSize * 2)
                .setEnablePlaceholders(false)
                .build()
        ).build()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun loadData() {
        repository.getProfile { user, message ->
            user?.let {

                followersCount.set(it.stats?.followersCount.toString())
                followingCount.set(it.stats?.followingsCount.toString())
                name.set(it._name)
                phone.set(it.mobile)
                email.set(it.email)
                bio.set(it.bio)
//                website.set(it.we)

            }
        }
    }

    fun onRemoveClickedListener(ad: Ad, result: () -> Unit) {
        repository.deleteAd(ad._id) { success, message ->
            showToast.postValue(message)
            result()
            if (success) {
                dataSourceFactory.changePrams(HashMap())
            }
        }
    }

    fun onDeActiveClickedListener(ad: Ad, result: () -> Unit) {

        if (ad.isActive == true) {
            repository.deactivateAd(ad._id) { success, message, updatedAd ->
                showToast.postValue(message)
                result()
            }
        } else {
            repository.activateAd(ad._id) { success, message, updatedAd ->
                showToast.postValue(message)
                result()
            }
        }
    }

    fun onEditProfileClickedListener(v: View) {
        ProfileFragment.newInstance(v.context)
    }

    fun onFollowerClickedListener(v: View) {
        v.context.startActivity(Intent(v.context, FollowersFragment::class.java))
    }

    fun onFollowingClickedListener(v: View) {
        v.context.startActivity(Intent(v.context, FollowingsFragment::class.java))

    }


    fun getLayoutState(): LiveData<CustomFrame.FrameState> =
        Transformations.switchMap<AdsDataSource, CustomFrame.FrameState>(
            dataSourceFactory.dataSourceLiveData,
            AdsDataSource::layoutState
        )

    fun getFooterState(): LiveData<Boolean> =
        Transformations.switchMap<AdsDataSource, Boolean>(
            dataSourceFactory.dataSourceLiveData,
            AdsDataSource::footerState
        )
}