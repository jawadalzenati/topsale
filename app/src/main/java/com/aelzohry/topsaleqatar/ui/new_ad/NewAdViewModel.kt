package com.aelzohry.topsaleqatar.ui.new_ad

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.aelzohry.topsaleqatar.model.*
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.ui.new_ad.finishAd.FinishActivity
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import com.google.android.gms.maps.model.LatLng
import java.io.File

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
class NewAdViewModel : BaseViewModel() {

    var repository: Repository = RemoteRepository()
    var ad: Ad? = null

    var catsRes = MutableLiveData<List<Category>>()
    var subCatsRes = MutableLiveData<List<LocalStanderModel>>()
    var typeRes = MutableLiveData<List<LocalStanderModel>>()

//    var regionRes = MutableLiveData<List<LocalStanderModel>>()
//    var carMakesRes = MutableLiveData<List<StanderModel>>()
//    var modelRes = MutableLiveData<List<StanderModel>>()
//    var yearRes = MutableLiveData<List<LocalStanderModel>>()


    /*
    * steps */
    val currentStep = ObservableField(0) //
    val tvTitle = ObservableField("")


    /**/

    var selectedCat: Category? = null
    var selectedSubCat: LocalStanderModel? = null
    var selectedType: LocalStanderModel? = null
    var selectedCarMake: StanderModel? = null
    var selectedModelLocal: StanderModel? = null
    var selectedYear: LocalStanderModel? = null
    var selectedRegion: LocalStanderModel? = null
    var selectedRooms: StanderModel? = null
    var selectedBathRoom: StanderModel? = null

    var selectCatText = ObservableField("")
    var selectSubCatText = ObservableField("")
    var selectedTypeText = ObservableField("")
    var selectCarText = ObservableField("")
    var selectPropertiesText = ObservableField("")

    var subCatState = ObservableField(false)
    var typeState = ObservableField(false)
    var carCatState = ObservableField(false)
    var propertiesState = ObservableField(false)

    var etTitle = ObservableField("")
    var etKm = ObservableField("")
    var etPrice = ObservableField("")
    var etDesc = ObservableField("")
    var etLocation = ObservableField("")

    var lat: Double? = null
    var lng: Double? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun loadData() {

        repository.getCategories {
            it?.data?.let { categories ->
                catsRes.postValue(categories)
            }
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
        selectPropertiesText.set("")
        selectCarText.set("")

        subCatState.set(category.subcategories?.isNullOrEmpty() == false)
        typeState.set(category.types?.isNullOrEmpty() == false)
        carCatState.set(category.categoryClass == CategoryClass.CARS)
        propertiesState.set(category.categoryClass == CategoryClass.PROPERTIES)

    }

    fun onSubCatSelectedListener(model: LocalStanderModel?) {
        val modelLocal = model ?: return
        selectSubCatText.set(modelLocal.title.localized)
        selectedSubCat = modelLocal
    }

    fun onSelectedTypeListener(model: LocalStanderModel?) {
        val modelLocal = model ?: return
        selectedTypeText.set(modelLocal.title.localized)
        selectedType = modelLocal
    }

    fun onRegionSelectedListener(modelLocal: LocalStanderModel?) {
        val modelLocal = modelLocal ?: return
        selectedRegion = modelLocal
    }

    fun onRoomSelectedListener(model: StanderModel) {
        selectedRooms = model
    }

    fun onBathRoomSelectedListener(model: StanderModel) {
        selectedBathRoom = model
    }

    fun onMakeSelectedListener(model: StanderModel?, data: (List<StanderModel>) -> Unit?) {
        val modelLocal = model ?: return
        selectedCarMake = modelLocal
//        selectedCarMakeText.set(modelLocal.title)
        selectedModelLocal = null
//        selectedModelText.set("")
        repository.getCarModel(model) {
            it?.response?.let { makes ->
//                carMakesRes.postValue(makes)
                data(makes)
            }
        }
    }

    fun onModelSelectedListener(model: StanderModel?) {
        val list = model ?: return
        selectedModelLocal = list
//        selectedModelText.set(list?.title)
    }

    fun onSelectedYearsListener(model: LocalStanderModel) {
        val list = model ?: return
        selectedYear = list
//        selectedYearText.set(list.title.localized)
    }

    fun onAddNewClickedListener(v: View, images: List<File>) {

        isLoading.postValue(true)
        repository.newAd(
            etTitle.get() ?: "",
            etPrice.get() ?: "",
            etDesc.get() ?: "",
            selectedCat?._id ?: "",
            selectedType?._id,
            selectedSubCat?._id,
            selectedRegion?._id,
            selectedCarMake?._id,
            selectedModelLocal?._id,
            selectedYear?._id ?: "",
            etKm.get(),
            lat?.toString(),
            lng?.toString(),
            selectedRooms?._id,
            selectedBathRoom?._id,
            images
        ) { success, message, ad ->
            showToast.postValue(message)
            isLoading.postValue(false)
            if (success) {
                ad?.let {
                    FinishActivity.newInstance(v.context, it)
                }
            }
        }
    }

    fun onEditAdClickedListener(
        v: View, images: List<File>, deletedPhotos: List<String>?, thumbnailType: String?,
        thumbnailId: String?
    ) {
        isLoading.postValue(true)

        repository.editAd(
            ad?._id ?: "",
            etTitle.get() ?: "",
            etPrice.get() ?: "",
            etDesc.get() ?: "",
            selectedCat?._id ?: "",
            selectedType?._id,
            selectedSubCat?._id,
            selectedRegion?._id,
            selectedCarMake?._id,
            selectedModelLocal?._id,
            selectedYear?._id ?: "",
            etKm.get(),
            lat?.toString() ,
            lng?.toString(),
            selectedRooms?._id ,
            selectedBathRoom?._id,
            images,
            deletedPhotos,
            thumbnailType,
            thumbnailId
        ) { success, message, updatedAd ->
            showToast.postValue(message)
            isLoading.postValue(false)
            if (success) {
                updatedAd?.let {
                    (v as AppCompatActivity).onBackPressed()
                }
            }
        }
    }


}