package com.aelzohry.topsaleqatar.ui.ads

import android.util.Log
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.helper.hideKeyboard
import com.aelzohry.topsaleqatar.model.*
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.AdsDataSource
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.utils.CustomFrame
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
class AdsViewModel(val category: Category?, subCat: LocalStanderModel?) : BaseViewModel() {

    private var repository: Repository = RemoteRepository()
    var bannersRes = MutableLiveData<List<Banner>>()

    var etSearch = ObservableField("")

    var selectedSubCat: LocalStanderModel? = subCat
    var selectedType: LocalStanderModel? = null
    var selectedCarMake: StanderModel? = null
    var selectedModelLocal: ArrayList<StanderModel>? = null
    var selectedYear: ArrayList<StanderModel>? = null
    var selectedRegion: LocalStanderModel? = null
    var selectedSort: Int = 0

    var fromPrice: String? = null
    var toPrice: String? = null
    var fromRooms: String? = null
    var toRooms: String? = null
    var fromBathRooms: String? = null
    var toBathRooms: String? = null

    private val dataSourceFactory: AdsDataSource.DataSourceFactory

    var adsRes: LiveData<PagedList<Ad>>

    init {

        val prams = HashMap<String, Any>()
        category?._id?.let {
            prams["category"] = it
        }
        dataSourceFactory = AdsDataSource.DataSourceFactory(prams)
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
        loadBanners()
    }

    private fun loadBanners() {
        repository.getBanners {
            it?.let { banners ->
                bannersRes.postValue(banners)
            }
        }
    }

    fun onSortClickedListener(type: Int) {
        dataSourceFactory.changePrams(HashMap(), type)
    }

    fun onFilterCallBack() {

        val prams = HashMap<String, Any>()

        etSearch.get()?.let {
            if (it.isNotEmpty())
                prams["keyword"] = it
        }

        category?.let {
            prams["category"] = it._id
        }

        selectedCarMake?.let {
            prams["carMake"] = it._id
        }

        selectedRegion?.let {
            prams["region"] = it._id
        }

        selectedSubCat?.let {
            prams["subcategory"] = it._id
        }

        selectedType?.let {
            prams["type"] = it._id
        }

        toPrice?.let {
            prams["priceLess[]"] = it

        }

        fromPrice?.let {
            prams["price[]"] = it

        }

        toRooms?.let {
            prams["numOfRoomsLess[]"] = it

        }

        fromRooms?.let {
            prams["numOfRooms[]"] = it
        }

        toBathRooms?.let {
            prams["numOfBathroomLess[]"] = it
        }

        fromBathRooms?.let {
            prams["numOfBathroom[]"] = it
        }

        Log.e("filter",prams.toString())
        val listModel = ArrayList<String>()
        val listYears = ArrayList<String>()
        selectedModelLocal?.forEach { listModel.add(it._id) }
        selectedYear?.forEach { listYears.add(it._id) }

        if (listModel.isNotEmpty())
            prams["carModels"] = listModel

        if (listYears.isNotEmpty())
            prams["carYears"] = listYears



        dataSourceFactory.changePrams(prams, AdsDataSource.FILTER)
    }

    fun onSearchClickedListener(v: View) {
        v.hideKeyboard()

        if (etSearch.get()?.isNotEmpty() == true)
            onFilterCallBack()

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

    companion object {
        const val latest = 0
        const val closest = 1
        const val lowest_price = 2
        const val higher_price = 3
        const val model_newest = 4
        const val minimum_mileage = 5
    }
}