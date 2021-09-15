package com.aelzohry.topsaleqatar.ui.ads.filter

import android.app.Dialog
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FilterBottomSheetBinding
import com.aelzohry.topsaleqatar.model.Category
import com.aelzohry.topsaleqatar.model.LocalStanderModel
import com.aelzohry.topsaleqatar.model.StanderModel
import com.aelzohry.topsaleqatar.ui.ads.AdsFragment
import com.aelzohry.topsaleqatar.ui.ads.ViewModelFactory
import com.aelzohry.topsaleqatar.ui.search.SearchFragment
import com.aelzohry.topsaleqatar.utils.base.BaseBottomSheet
import com.aelzohry.topsaleqatar.utils.extenions.setupDialog
import com.aelzohry.topsaleqatar.utils.extenions.setupListDialogs

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
class FilterBottomSheet : BaseBottomSheet<FilterBottomSheetBinding, FilterViewModel>() {

    private var catDialog: Dialog? = null
    private var subCatDialog: Dialog? = null
    private var typeDialog: Dialog? = null
    private var makesDialog: Dialog? = null
    private var yearDialog: Dialog? = null
    private var modelDialog: Dialog? = null
    private var regionDialog: Dialog? = null

    override fun getLayoutID(): Int = R.layout.filter_bottom_sheet

    override fun getViewModel(): FilterViewModel =
        ViewModelProvider(
            this,
            ViewModelFactory(
                arguments?.getParcelable(ARG_CATEGORY),
                loadCategory = arguments?.getBoolean(
                    LOAD_CATEGORY, false
                ) ?: false
            )
        )[FilterViewModel::class.java]

    override fun setupUI() {

        vm.selectedSubCat = arguments?.getParcelable(ARG_SELECTED_SUB_CAT)
        vm.selectedType = arguments?.getParcelable(ARG_SELECTED_TYPE)
        vm.selectedCarMake = arguments?.getParcelable(ARG_SELECTED_CAR_MAKE)
        vm.selectedRegion = arguments?.getParcelable(ARG_SELECTED_REGION)
        vm.selectedModelLocal = arguments?.getParcelableArrayList(ARG_SELECTED_MODEL)
        vm.selectedYear = arguments?.getParcelableArrayList(ARG_SELECTED_YEAR)

        vm.etFromPrice.set(arguments?.getString(ARG_FROM_PRICE))
        vm.etToPrice.set(arguments?.getString(ARG_TO_PRICE))

        vm.etFromRooms.set(arguments?.getString(ARG_FROM_ROOMS))
        vm.etFromRooms.set(arguments?.getString(ARG_TO_ROOMS))

        vm.etFromBathRooms.set(arguments?.getString(ARG_FROM_BATHROOMS))
        vm.etToBathRooms.set(arguments?.getString(ARG_TO_BATH_ROOMS))

        vm.refreshTitles()

    }

    override fun onClickedListener() {

        binding.btnCat.setOnClickListener { catDialog?.show() }

        binding.btnMake.setOnClickListener {
            makesDialog?.show()
        }

        binding.btnModel.setOnClickListener {
            modelDialog?.show()
        }

        binding.btnRegion.setOnClickListener {
            regionDialog?.show()
        }

        binding.btnSubCat.setOnClickListener {
            subCatDialog?.show()
        }

        binding.btnType.setOnClickListener {
            typeDialog?.show()
        }

        binding.btnYear.setOnClickListener {
            yearDialog?.show()
        }

        binding.btnAll.setOnClickListener {
            getViewModel().onResetBtnClickedListener()
            if (requireActivity() is AdsFragment) (requireActivity() as AdsFragment).onFilterClickedApplyListener(
                vm.selectedCat,
                vm.selectedSubCat,
                vm.selectedType,
                vm.selectedCarMake,
                vm.selectedModelLocal,
                vm.selectedYear,
                vm.selectedRegion,
                vm.etFromPrice.get(),
                vm.etToPrice.get(),
                vm.etFromRooms.get(),
                vm.etToRooms.get(),
                vm.etFromBathRooms.get(),
                vm.etToBathRooms.get()
            )

            if (requireActivity() is SearchFragment) (requireActivity() as SearchFragment).onFilterClickedApplyListener(
                vm.selectedCat,
                vm.selectedSubCat,
                vm.selectedType,
                vm.selectedCarMake,
                vm.selectedModelLocal,
                vm.selectedYear,
                vm.selectedRegion,
                vm.etFromPrice.get(),
                vm.etToPrice.get(),
                vm.etFromRooms.get(),
                vm.etToRooms.get(),
                vm.etFromBathRooms.get(),
                vm.etToBathRooms.get()
            )
            dismiss()
        }

        binding.btnApply.setOnClickListener {
            if (requireActivity() is AdsFragment) (requireActivity() as AdsFragment).onFilterClickedApplyListener(
                vm.selectedCat,
                vm.selectedSubCat,
                vm.selectedType,
                vm.selectedCarMake,
                vm.selectedModelLocal,
                vm.selectedYear,
                vm.selectedRegion,
                vm.etFromPrice.get(),
                vm.etToPrice.get(),
                vm.etFromRooms.get(),
                vm.etToRooms.get(),
                vm.etFromBathRooms.get(),
                vm.etToBathRooms.get()
            )

            if (requireActivity() is SearchFragment) (requireActivity() as SearchFragment).onFilterClickedApplyListener(
                vm.selectedCat,
                vm.selectedSubCat,
                vm.selectedType,
                vm.selectedCarMake,
                vm.selectedModelLocal,
                vm.selectedYear,
                vm.selectedRegion,
                vm.etFromPrice.get(),
                vm.etToPrice.get(),
                vm.etFromRooms.get(),
                vm.etToRooms.get(),
                vm.etFromBathRooms.get(),
                vm.etToBathRooms.get()
            )
            dismiss()
        }

        binding.backBtn.setOnClickListener {
            dismiss()
        }

    }

    override fun observerLiveData() {

        vm.catsRes.observe(this, androidx.lifecycle.Observer {

            val list = ArrayList<LocalStanderModel>()
            it.forEach {
                list.add(
                    LocalStanderModel(
                        it._id,
                        LocalStanderModel.LocalizedModel(it.title.ar, it.title.en)
                    )
                )
            }
            catDialog = Dialog(requireContext())
            setupDialog(catDialog, list, { modelLocal: LocalStanderModel, i ->
                vm.onCatSelectedListener(it[i])
            })
        })

        vm.subCatsRes.observe(this, Observer {
            subCatDialog = Dialog(requireContext())
            setupDialog(subCatDialog, it) { modelLocal: LocalStanderModel ->
                getViewModel().selectedSubCat = modelLocal
                getViewModel().selectSubCatText.set(modelLocal.title.localized)
            }
        })

        vm.typeRes.observe(this, Observer {
            typeDialog = Dialog(requireContext())
            setupDialog(typeDialog, it) { modelLocal: LocalStanderModel ->
                getViewModel().selectedType = modelLocal
                getViewModel().selectedTypeText.set(modelLocal.title.localized)
            }
        })

        vm.regionRes.observe(this, Observer {
            regionDialog = Dialog(requireContext())
            setupDialog(regionDialog, it) { modelLocal: LocalStanderModel ->
                getViewModel().selectedRegion = modelLocal
                getViewModel().selectedRegionText.set(modelLocal.title.localized)
            }
        })

        vm.carMakesRes.observe(this, Observer {
            makesDialog = Dialog(requireContext())
            setupDialog(makesDialog, it) { modelLocal: StanderModel ->
                getViewModel().selectedCarMake = modelLocal
                getViewModel().selectedCarMakeText.set(modelLocal.title)
                getViewModel().loadCarModel(modelLocal)
                getViewModel().selectedModelLocal = null
                getViewModel().selectedModelText.set("")
            }
        })

        /* multi*/
        vm.yearRes.observe(this, Observer {
            yearDialog = Dialog(requireContext())
            setupListDialogs(yearDialog, it) { model ->
                getViewModel().selectedYear = model
                vm.refreshYears()

            }
        })

        /* multi*/
        vm.modelRes.observe(this, Observer {
            modelDialog = Dialog(requireContext())
            setupListDialogs(modelDialog, it) { model ->
                getViewModel().selectedModelLocal = model
                vm.refreshModel()
            }
        })

    }

    override fun isFullHeight(): Boolean = false

    companion object {

        private const val ARG_CATEGORY = "ARG_CATEGORY"
        private const val ARG_SELECTED_SUB_CAT = "ARG_SELECTED_SUB_CAT"
        private const val ARG_SELECTED_TYPE = "ARG_SELECTED_TYPE"
        private const val ARG_SELECTED_CAR_MAKE = "ARG_SELECTED_CAR_MAKE"
        private const val ARG_SELECTED_MODEL = "ARG_SELECTED_MODEL"
        private const val ARG_SELECTED_YEAR = "ARG_SELECTED_YEAR"
        private const val ARG_SELECTED_REGION = "ARG_SELECTED_REGION"
        private const val ARG_FROM_PRICE = "FROM_PRICE"
        private const val ARG_TO_PRICE = "TO_PRICE"
        private const val ARG_FROM_ROOMS = "FROM_ROOMS"
        private const val ARG_TO_ROOMS = "TO_ROOMS"
        private const val ARG_FROM_BATHROOMS = "FROM_BATHROOMS"
        private const val ARG_TO_BATH_ROOMS = "TO_BATH_ROOMS"
        private const val ARG_SORT = "SORT"
        private const val LOAD_CATEGORY = "LOAD_CATEGORY"

        @JvmStatic
        fun newInstance(
            category: Category?,
            selectedSubCat: LocalStanderModel?,
            selectedType: LocalStanderModel?,
            selectedCarMake: StanderModel?,
            selectedModelLocal: ArrayList<StanderModel>?,
            selectedYear: ArrayList<StanderModel>?,
            selectedRegion: LocalStanderModel?,
            loadCategory: Boolean = false,
            fromPrice: String?, toPrice: String?,
            fromRooms: String?, toRooms: String?,
            fromBathRooms: String?, toBathRooms: String?
        ) =
            FilterBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CATEGORY, category)
                    putParcelable(ARG_SELECTED_SUB_CAT, selectedSubCat)
                    putParcelable(ARG_SELECTED_TYPE, selectedType)
                    putParcelable(ARG_SELECTED_CAR_MAKE, selectedCarMake)
                    putParcelableArrayList(ARG_SELECTED_MODEL, selectedModelLocal)
                    putParcelableArrayList(ARG_SELECTED_YEAR, selectedYear)
                    putParcelable(ARG_SELECTED_REGION, selectedRegion)
                    putString(ARG_FROM_PRICE, fromPrice)
                    putString(ARG_TO_PRICE, toPrice)
                    putString(ARG_FROM_ROOMS, fromRooms)
                    putString(ARG_TO_ROOMS, toRooms)
                    putString(ARG_FROM_BATHROOMS, fromBathRooms)
                    putString(ARG_TO_BATH_ROOMS, toBathRooms)
                    putBoolean(LOAD_CATEGORY, loadCategory)
                }
            }
    }
}