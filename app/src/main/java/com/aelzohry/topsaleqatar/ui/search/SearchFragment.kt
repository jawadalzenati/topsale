package com.aelzohry.topsaleqatar.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentSearchBinding
import com.aelzohry.topsaleqatar.model.*
import com.aelzohry.topsaleqatar.repository.remote.AdsDataSource
import com.aelzohry.topsaleqatar.ui.adapters.AdsAdapter
import com.aelzohry.topsaleqatar.ui.ads.filter.FilterBottomSheet
import com.aelzohry.topsaleqatar.ui.ads.filter.FilterListener
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseFragment

class
SearchFragment : BaseActivity<FragmentSearchBinding, SearchViewModel>(), FilterListener {

    private lateinit var adapter: AdsAdapter
    private lateinit var filterBottomSheet: FilterBottomSheet
    override fun getLayoutID(): Int = R.layout.fragment_search

    override fun getViewModel(): SearchViewModel = ViewModelProvider(
        this,
        ViewModelFactory(intent?.getStringExtra(TXT_ARG))
    )[SearchViewModel::class.java]


    override fun setupUI() {

        adapter = AdsAdapter(vm)
        binding.rv.adapter = adapter
        (binding.rv.layoutManager as GridLayoutManager).spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItemViewType(position) == AdsAdapter.AD_TYPE) 1 else 2
                }
            }

        filterBottomSheet = FilterBottomSheet.newInstance(
            vm.selectedCat,
            vm.selectedSubCat,
            vm.selectedType,
            vm.selectedCarMake,
            vm.selectedModelLocal,
            vm.selectedYear,
            vm.selectedRegion,
            true,
            vm.fromPrice,
            vm.toPrice,
            vm.fromRooms,
            vm.toRooms,
            vm.fromBathRooms,
            vm.toBathRooms
        )
    }

    private fun setupGridView() {

        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItemViewType(position) == AdsAdapter.AD_TYPE) 1 else 2
                }
            }

        binding.rv.layoutManager = layoutManager
        binding.rv.adapter = adapter

    }

    private fun setupListView() {
        val layoutManager = LinearLayoutManager(this)
        binding.rv.layoutManager = layoutManager
        binding.rv.adapter = adapter
    }

    override fun onClickedListener() {


        binding.btnToggleView.setOnClickListener {

            val oldValue = vm.isAdViewGrid.get() ?: false

            vm.isAdViewGrid.set(!oldValue)

            if (oldValue) setupListView()
            else setupGridView()


        }

        binding.etSearch.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                vm.onSearchClickedListener(v)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.btnBack.setOnClickListener {
            if (filterBottomSheet.isVisible)
                filterBottomSheet.dismiss()
            else
                onBackPressed()
        }

        binding.btnFilter.setOnClickListener {
            if (!filterBottomSheet.isVisible) {
                filterBottomSheet = FilterBottomSheet.newInstance(
                    vm.selectedCat,
                    vm.selectedSubCat,
                    vm.selectedType,
                    vm.selectedCarMake,
                    vm.selectedModelLocal,
                    vm.selectedYear,
                    vm.selectedRegion,
                    loadCategory = true,
                    vm.fromPrice,
                    vm.toPrice,
                    vm.fromRooms,
                    vm.toRooms,
                    vm.fromBathRooms,
                    vm.toBathRooms
                )
                filterBottomSheet.show(supportFragmentManager, "")
            }
        }

        binding.btnSort.setOnClickListener {
            setupSortList(it)
        }
    }


    private fun setupSortList(it: View) {
        val popupMenu = PopupMenu(this, it)
        val menu = popupMenu.menu
        popupMenu.menuInflater.inflate(R.menu.sort_ads_menu, menu)
        popupMenu.menu.findItem(R.id.model_newest).isVisible =
            vm.selectedCat?.categoryClass == CategoryClass.CARS
        popupMenu.menu.findItem(R.id.minimum_mileage).isVisible =
            vm.selectedCat?.categoryClass == CategoryClass.CARS
        popupMenu.show()
        popupMenu.menu?.getItem(vm.selectedSort)?.isChecked = true

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.latest -> vm.onSortClickedListener(AdsDataSource.SORT_LATEST) //الاحدث
                R.id.closest -> vm.onSortClickedListener(AdsDataSource.SORT_CLOSEST) // الاقرب
                R.id.lowest_price -> vm.onSortClickedListener(AdsDataSource.SORT_LOWEST_PRICE)
                R.id.higher_price -> vm.onSortClickedListener(AdsDataSource.SORT_HIGHER_PRICE)
                R.id.model_newest -> vm.onSortClickedListener(AdsDataSource.SORT_MODEL)
                R.id.minimum_mileage -> vm.onSortClickedListener(AdsDataSource.SORT_MINIMUM_MILEAGE)
            }
            it.isChecked = true
            vm.selectedSort = it.order
            return@setOnMenuItemClickListener true
        }
    }

    override fun observerLiveData() {

        vm.getLayoutState().observe(this, Observer {
            vm.frameState.set(it)
        })

        vm.getFooterState().observe(this, Observer {
            adapter.setState(it)
        })

        vm.adsRes.observe(this, Observer {
            adapter.submitList(it as PagedList<AdBanner>)
        })

    }

    companion object {
        private const val TXT_ARG = "TXT_ARG"

        @JvmStatic
        fun newInstance(context: Context, txt: String) {
            context.startActivity(
                Intent(context, SearchFragment::class.java)
                    .putExtra(TXT_ARG, txt)
            )
        }
    }

    override fun onFilterClickedApplyListener(
        category: Category?,
        selectedSubCat: LocalStanderModel?,
        selectedType: LocalStanderModel?,
        selectedCarMake: StanderModel?,
        selectedModelLocal: ArrayList<StanderModel>?,
        selectedYear: ArrayList<StanderModel>?,
        selectedRegion: LocalStanderModel?,
        fromPrice: String?, toPrice: String?,
        fromRooms: String?, toRooms: String?,
        fromBathRooms: String?, toBathRooms: String?
    ) {

        vm.selectedCat = category
        vm.selectedSubCat = selectedSubCat
        vm.selectedType = selectedType
        vm.selectedCarMake = selectedCarMake
        vm.selectedModelLocal = selectedModelLocal
        vm.selectedYear = selectedYear
        vm.selectedRegion = selectedRegion
        vm.fromPrice = fromPrice
        vm.toPrice = toPrice
        vm.fromRooms = fromRooms
        vm.toRooms = toRooms
        vm.fromBathRooms = fromBathRooms
        vm.toBathRooms = toBathRooms
        vm.onFilterCallBack()
    }
}