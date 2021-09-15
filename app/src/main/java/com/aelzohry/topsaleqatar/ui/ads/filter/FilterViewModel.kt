package com.aelzohry.topsaleqatar.ui.ads.filter

import androidx.databinding.ObservableField
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.aelzohry.topsaleqatar.model.Category
import com.aelzohry.topsaleqatar.model.CategoryClass
import com.aelzohry.topsaleqatar.model.LocalStanderModel
import com.aelzohry.topsaleqatar.model.StanderModel
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
class FilterViewModel(private val category: Category?, val loadCategory: Boolean) :
    BaseViewModel() {

    private var repository: Repository = RemoteRepository()

    var catsRes = MutableLiveData<List<Category>>()
    var subCatsRes = MutableLiveData<List<LocalStanderModel>>()
    var typeRes = MutableLiveData<List<LocalStanderModel>>()
    var carMakesRes = MutableLiveData<List<StanderModel>>()
    var regionRes = MutableLiveData<List<LocalStanderModel>>()
    var modelRes = MutableLiveData<List<StanderModel>>()
    var yearRes = MutableLiveData<List<StanderModel>>()


    var selectedCat: Category? = category
    var selectedSubCat: LocalStanderModel? = null
    var selectedType: LocalStanderModel? = null
    var selectedCarMake: StanderModel? = null
    var selectedModelLocal: ArrayList<StanderModel>? = null
    var selectedYear: ArrayList<StanderModel>? = null
    var selectedRegion: LocalStanderModel? = null

    var selectCatText = ObservableField("")
    var selectSubCatText = ObservableField("")
    var selectedTypeText = ObservableField("")
    var selectedCarMakeText = ObservableField("")
    var selectedModelText = ObservableField("")
    var selectedYearText = ObservableField("")
    var selectedRegionText = ObservableField("")

    var subCatState = ObservableField(category?.subcategories?.isNullOrEmpty() == false)
    var typeState = ObservableField(category?.types?.isNullOrEmpty() == false)
    var carCatState = ObservableField(category?.categoryClass == CategoryClass.CARS)
    var regionState = ObservableField(category?.categoryClass == CategoryClass.PROPERTIES)

    var etFromPrice = ObservableField("")
    var etToPrice = ObservableField("")
    var etFromRooms = ObservableField("")
    var etToRooms = ObservableField("")
    var etFromBathRooms = ObservableField("")
    var etToBathRooms = ObservableField("")

    fun onResetBtnClickedListener() {
        selectedSubCat = null
        selectedType = null
        selectedCarMake = null
        selectedModelLocal = null
        selectedYear = null
        selectedRegion = null

        selectSubCatText.set("")
        selectedTypeText.set("")
        selectedCarMakeText.set("")
        selectedModelText.set("")
        selectedYearText.set("")
        selectedRegionText.set("")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun loadData() {

        if (loadCategory) {
            loadCategory()
            loadCarMakes()
            loadRegions()
            loadCarYears()
        } else {
            loadSubCats()
            loadType()
            loadCarMakes()
            loadRegions()
            loadCarYears()
        }


    }

    fun onCatSelectedListener(model: Category?) {
        val category = model ?: return
        this.selectedCat = category
        selectCatText.set(category.title.localized)
        this.subCatsRes.postValue(category.subcategories)
        this.typeRes.postValue(category.types)



        selectedSubCat = null
        selectedType = null

        selectSubCatText.set("")
        selectedTypeText.set("")

        subCatState.set(category.subcategories?.isNullOrEmpty() == false)
        typeState.set(category.types?.isNullOrEmpty() == false)
        carCatState.set(category.categoryClass == CategoryClass.CARS)
        regionState.set(category.categoryClass == CategoryClass.PROPERTIES)

    }

    private fun loadCategory() {
        repository.getCategories {
            it?.data?.let { categories ->
                catsRes.postValue(categories)
            }
        }
    }

    private fun loadCarMakes() {
        repository.getCarMake {
            it?.response?.let { makes ->
                carMakesRes.postValue(makes)
                if (makes.isNotEmpty())
                    loadCarModel(makes[0])
            }
        }
    }

    private fun loadRegions() {
        repository.getRegion {
            it?.response?.let { regions ->
                regionRes.postValue(regions)
            }
        }
    }

    fun loadCarModel(modelLocal: StanderModel) {
        repository.getCarModel(modelLocal) {
            it?.response?.let { models ->
                modelRes.postValue(models)
            }
        }
    }

    private fun loadCarYears() {

        val list = ArrayList<StanderModel>()
        for (i in 2020 downTo 1800) {
            list.add(
                StanderModel(
                    i.toString(), i.toString()
                )
            )
        }
        yearRes.postValue(list)

    }

    private fun loadSubCats() {
        val list = ArrayList<LocalStanderModel>()
        category?.subcategories?.forEach {
            list.add(LocalStanderModel(it._id, it.title))
        }
        subCatsRes.postValue(list)
    }

    private fun loadType() {

        val list = ArrayList<LocalStanderModel>()
        category?.types?.forEach { it ->
            list.add(LocalStanderModel(it._id, it.title))
        }
        typeRes.postValue(list)
    }

    fun refreshTitles() {
        selectSubCatText.set(selectedSubCat?.title?.localized)
        selectedTypeText.set(selectedType?.title?.localized)
        selectedCarMakeText.set(selectedCarMake?.title)
        selectedRegionText.set(selectedRegion?.title?.localized)
        refreshModel()
        refreshYears()
    }

    fun refreshModel() {
        val listModel = ArrayList<String>()
        selectedModelLocal?.forEach {
            listModel.add(it.title)
        }
        val modelTitle = listModel.toString()
            .replace("[", "")
            .replace("]", "")
            .replace(",", " - ")
        selectedModelText.set(modelTitle)

    }

    fun refreshYears() {
        val list = ArrayList<String>()
        selectedYear?.forEach {
            list.add(it.title)
        }
        val title = list.toString()
            .replace("[", "")
            .replace("]", "")
            .replace(",", " - ")
        selectedYearText.set(title)
    }
}