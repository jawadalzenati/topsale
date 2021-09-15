package com.aelzohry.topsaleqatar.ui.ads

import android.content.Context
import android.content.Intent
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentAdsBinding
import com.aelzohry.topsaleqatar.model.*
import com.aelzohry.topsaleqatar.repository.remote.AdsDataSource
import com.aelzohry.topsaleqatar.ui.adapters.AdsAdapter
import com.aelzohry.topsaleqatar.ui.ads.filter.FilterBottomSheet
import com.aelzohry.topsaleqatar.ui.ads.filter.FilterListener
import com.aelzohry.topsaleqatar.utils.CustomFrame
import com.aelzohry.topsaleqatar.utils.base.BaseActivity


class AdsFragment : BaseActivity<FragmentAdsBinding, AdsViewModel>(),
    FilterListener {

    companion object {
        private const val ARG_CATEGORY = "CATEGORY"
        private const val ARG_SUB_CATEGORY = "SUB_CATEGORY"

        @JvmStatic
        fun newInstance(
            context: Context,
            category: Category,
            subCategory: LocalStanderModel? = null
        ) {

            val i = Intent(context, AdsFragment::class.java)
                .putExtra(ARG_CATEGORY, category)
            if (subCategory != null) i.putExtra(ARG_SUB_CATEGORY, subCategory)
            context.startActivity(i)
        }
    }

    private lateinit var adsAdapter: AdsAdapter
    private lateinit var filterBottomSheet: FilterBottomSheet
    override fun getLayoutID(): Int = R.layout.fragment_ads

    override fun getViewModel(): AdsViewModel = ViewModelProvider(
        this,
        ViewModelFactory(
            intent?.getParcelableExtra<Category>(ARG_CATEGORY),
            subCategory = intent?.getParcelableExtra(
                ARG_SUB_CATEGORY
            )
        )
    )[AdsViewModel::class.java]

    override fun setupUI() {

        adsAdapter = AdsAdapter(vm)
        binding.recyclerView.adapter = adsAdapter
        (binding.recyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adsAdapter.getItemViewType(position) == AdsAdapter.AD_TYPE) 1 else 2
//                    return adsAdapter.getItemViewType(position)
                }
            }


        filterBottomSheet = FilterBottomSheet.newInstance(
            vm.category,
            vm.selectedSubCat,
            vm.selectedType,
            vm.selectedCarMake,
            vm.selectedModelLocal,
            vm.selectedYear,
            vm.selectedRegion, false,
            vm.fromPrice, vm.toPrice, vm.fromRooms, vm.toRooms, vm.fromBathRooms, vm.toBathRooms
        )
    }

    private fun setupGridView() {

        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adsAdapter.getItemViewType(position) == AdsAdapter.AD_TYPE) 1 else 2
//                    return adsAdapter.getItemViewType(position)
                }
            }

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adsAdapter

    }

    private fun setupListView() {
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adsAdapter
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
                    vm.category,
                    vm.selectedSubCat,
                    vm.selectedType,
                    vm.selectedCarMake,
                    vm.selectedModelLocal,
                    vm.selectedYear,
                    vm.selectedRegion,
                    false,
                    vm.fromPrice,
                    vm.toPrice,
                    vm.fromRooms,
                    vm.toRooms,
                    vm.fromBathRooms,
                    vm.toBathRooms,
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
            vm.category?.categoryClass == CategoryClass.CARS
        popupMenu.menu.findItem(R.id.minimum_mileage).isVisible =
            vm.category?.categoryClass == CategoryClass.CARS
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

        vm.bannersRes.observe(this, Observer {

        })

        vm.getLayoutState().observe(this, Observer {
            vm.frameState.set(it)
            vm.swipeRefresh.set(it == CustomFrame.FrameState.PROGRESS)
        })

        vm.getFooterState().observe(this, Observer {
            adsAdapter.setState(it)
        })

        vm.adsRes.observe(this, Observer {
            adsAdapter.submitList(it as PagedList<AdBanner>)
        })
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
