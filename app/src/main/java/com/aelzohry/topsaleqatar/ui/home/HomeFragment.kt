package com.aelzohry.topsaleqatar.ui.home

import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentHomeBinding
import com.aelzohry.topsaleqatar.databinding.ViewHolderHomeBannerBinding
import com.aelzohry.topsaleqatar.model.TopBanner
import com.aelzohry.topsaleqatar.ui.ads.AdsAdapter
import com.aelzohry.topsaleqatar.utils.base.BaseAdapter
import com.aelzohry.topsaleqatar.utils.base.BaseFragment

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {


    override fun getLayoutID(): Int = R.layout.fragment_home

    override fun getViewModel(): HomeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    override fun setupUI() {

        vm.adsAdapter = AdsAdapter({}, vm)
        binding.adsRecyclerView.adapter = vm.adsAdapter

        setupGridView()

    }

    private fun setupGridView() {
        val layoutManager = GridLayoutManager(context, 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return vm.adsAdapter.spanSizeAtPosition(position)
            }
        }
        binding.adsRecyclerView.layoutManager = layoutManager
        binding.adsRecyclerView.adapter = vm.adsAdapter

    }

    private fun setupListView() {
        val layoutManager = LinearLayoutManager(context)
        binding.adsRecyclerView.layoutManager = layoutManager
        binding.adsRecyclerView.adapter = vm.adsAdapter
    }

    override fun onClickedListener() {

        binding.btnToggleView.setOnClickListener {

            val oldValue = vm.isAdViewGrid.get() ?: false

            vm.isAdViewGrid.set(!oldValue)

            if (oldValue) setupListView()
            else setupGridView()


        }
//        vm.layoutManagerState.observe(this, Observer {
//            binding.adsRecyclerView.layoutManager =
//                if (it) GridLayoutManager(context, 3) else LinearLayoutManager(context)
//        })

        binding.etSearch.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                vm.onSearchClickedListener(v)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    override fun observerLiveData() {

        vm.topBannersRes.observe(this, Observer { banners ->
            binding.bannersRecyclerView.adapter =
                BaseAdapter<TopBanner, ViewHolderHomeBannerBinding>(
                    R.layout.view_holder_home_banner,
                    vm, banners
                )
        })

        vm.adsRes.observe(this, Observer { ads ->
            vm.adsAdapter.addAds(ads)
        })

        vm.adsBannerRes.observe(this, Observer { banners ->
            vm.adsAdapter.setBanners(banners)
        })
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.home_menu, menu)
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_search -> {
//                vm.onSearchClickedListener(binding.root)
//                true
//            }
//            R.id.action_categories -> {
//                vm.onCatsButtonsClickedListener(binding.root)
//                true
//            }
//            else -> false
//        }
//    }
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////
////        setHasOptionsMenu(true)
////        repository = RemoteRepository()
////    }
//
////    override fun onDestroy() {
////        homeCall?.cancel()
////        bannersCall?.cancel()
////        super.onDestroy()
////    }
//
////    override fun onCreateView(
////        inflater: LayoutInflater, container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View? {
////        return inflater.inflate(R.layout.fragment_home, container, false)
////    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
////        bannersRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
//
////        bannersAdapter = HomeBannersAdapter(arrayListOf()) {
////            onTopBannerPressed(it)
////        }
////        bannersRecyclerView.adapter = bannersAdapter
//
//    }


//    private fun search() {
//
//
//    }

//    private fun categories() {
//        val categoriesFragment = CategoriesFragment()
//        (activity as MainActivity).pushFragment(categoriesFragment)
//    }

//    private fun refresh() {
////        bannersAdapter.banners.clear()
////        bannersAdapter.notifyDataSetChanged()
//        adsAdapter.clear()
//        isLoading = false
//        loadHome()
//        loadBanners()
//    }

//    private fun loadBanners() {
//        bannersCall = repository.getBanners {
//            it?.let { banners ->
//                this.adsAdapter.setBanners(banners)
//            }
//        }
//    }

//    private fun loadHome() {
//        if (isLoading) return
//        startLoadingHome()
//        homeCall = repository.getHome {
//            it?.data?.let { data ->
//
//                binding.bannersRecyclerView.adapter =
//                    BaseAdapter<TopBanner, ViewHolderHomeBannerBinding>(
//                        R.layout.view_holder_home_banner,
//                        vm, data.banners
//                    )
//                this.bannersAdapter.banners = data.banners
//                this.bannersAdapter.notifyDataSetChanged()
//                this.adsAdapter.addAds(data.ads)
//            }
//            endLoadingHome()
//        }
//    }

//    private fun startLoadingHome() {
//
//    }
//
//    private fun endLoadingHome() {
//        isLoading = false
//        swipeRefreshLayout.isRefreshing = false
//        contentView.visibility = View.VISIBLE
//    }


//    private fun onAdPressed(ad: Ad) {
//        val detailsFragment = AdDetailsFragment.newInstance(ad)
//        (activity as MainActivity).pushFragment(detailsFragment)
//    }

}
