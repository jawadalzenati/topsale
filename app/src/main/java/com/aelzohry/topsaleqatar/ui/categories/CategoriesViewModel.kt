package com.aelzohry.topsaleqatar.ui.categories

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.aelzohry.topsaleqatar.model.Category
import com.aelzohry.topsaleqatar.model.LocalStanderModel
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.repository.remote.responses.CategoriesResponse
import com.aelzohry.topsaleqatar.ui.ads.AdsFragment
import com.aelzohry.topsaleqatar.utils.CustomFrame
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import retrofit2.Call

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
class CategoriesViewModel : BaseViewModel() {

    private lateinit var repository: Repository
    var call: Call<CategoriesResponse>? = null
    var categoriesRes = MutableLiveData<List<Category>>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun loadData() {
        loadData(false)

    }

    private fun loadData(isSwipe: Boolean = false) {
        if (isSwipe) swipeRefresh.set(true)
        frameState.set(CustomFrame.FrameState.PROGRESS)
        repository = RemoteRepository()
        call = repository.getCategories { response ->
            response?.data?.let { cats ->
                categoriesRes.postValue(cats)
            }
            frameState.set(CustomFrame.FrameState.LAYOUT)
            if (isSwipe) swipeRefresh.set(false)
        }
    }

    fun onSwipeRefreshListener() {
        loadData(true)
    }

    fun onCatClickedListener(v: View, model: Category) {

        AdsFragment.newInstance(v.context, model)
        /*
             val adsFragment = AdsFragment.newInstance(category)
        (activity as MainActivity).pushFragment(adsFragment)

        * */

    }

    fun onSubCatClickedListener(v: View, model: Category, subCategory: LocalStanderModel) {

        AdsFragment.newInstance(v.context, model, subCategory)
    }

}