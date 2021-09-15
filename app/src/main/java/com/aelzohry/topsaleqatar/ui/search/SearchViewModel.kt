package com.aelzohry.topsaleqatar.ui.search

import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.aelzohry.topsaleqatar.helper.hideKeyboard
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.model.Category
import com.aelzohry.topsaleqatar.model.LocalStanderModel
import com.aelzohry.topsaleqatar.model.StanderModel
import com.aelzohry.topsaleqatar.repository.remote.AdsDataSource
import com.aelzohry.topsaleqatar.ui.ads.AdsViewModel
import com.aelzohry.topsaleqatar.utils.CustomFrame
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
class SearchViewModel(private val txt: String?) : BaseViewModel() {

    var etSearch = ObservableField(txt)
    var selectedCat: Category? = null
    var selectedSubCat: LocalStanderModel? = null
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
        etSearch.get()?.let {
            prams["keyword"] = it
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

    fun onSortClickedListener(type: Int) {
        dataSourceFactory.changePrams(HashMap(), type)
    }

    fun onFilterCallBack() {

        val prams = HashMap<String, Any>()

        txt?.let {
            if (it.isNotEmpty())
                prams["keyword"] = it
        }

        selectedCat?.let {
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
        fromPrice?.let {
            prams["price"] = it

        }

        toPrice?.let {
            prams["priceLess"] = it

        }

        fromRooms?.let {
            prams["numOfRooms"] = it

        }

        toRooms?.let {
            prams["numOfRoomsLess"] = it
        }

        fromBathRooms?.let {
            prams["numOfBathroom"] = it
        }

        toBathRooms?.let {
            prams["numOfBathroomLess"] = it
        }

        val listModel = ArrayList<String>()
        val listYears = ArrayList<String>()
        selectedModelLocal?.forEach { listModel.add(it._id) }
        selectedYear?.forEach { listYears.add(it._id) }

        if (listModel.isNotEmpty())
            prams["carModels"] = listModel

        if (listYears.isNotEmpty())
            prams["carYears"] = listYears

        dataSourceFactory.changePrams(prams)
    }

    fun onSearchClickedListener(v: View) {
        v.hideKeyboard()

        if (etSearch.get()?.isNotEmpty() == true)
            onFilterCallBack()

    }

    fun getLayoutState(): LiveData<CustomFrame.FrameState> =
        Transformations.switchMap(dataSourceFactory.dataSourceLiveData, AdsDataSource::layoutState)

    fun getFooterState(): LiveData<Boolean> =
        Transformations.switchMap(dataSourceFactory.dataSourceLiveData, AdsDataSource::footerState)
}