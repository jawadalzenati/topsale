package com.aelzohry.topsaleqatar.ui.more.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentFavoriteAdsBinding
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.repository.remote.responses.AdsResponse
import com.aelzohry.topsaleqatar.ui.ad_details.AdDetailsFragment
import com.aelzohry.topsaleqatar.ui.ads.AdsAdapter
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import kotlinx.android.synthetic.main.fragment_favorite_ads.*
import retrofit2.Call

class FavoriteAdsFragment : BaseActivity<FragmentFavoriteAdsBinding, BaseViewModel>() {

    private lateinit var repository: Repository
    private lateinit var adapter: AdsAdapter
    private var adsCall: Call<AdsResponse>? = null

    override fun getLayoutID(): Int = R.layout.fragment_favorite_ads

    override fun getViewModel(): BaseViewModel = ViewModelProvider(this)[BaseViewModel::class.java]

    override fun setupUI() {
        initToolbar(getString(R.string.favorites))
        repository = RemoteRepository()

        refreshLayout.setOnRefreshListener { refresh() }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                // check for scroll down
                {
                    val layoutManager = recyclerView.layoutManager as? GridLayoutManager ?: return
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !didLoadAllPages) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            loadNextPage()
                        }
                    }
                }
            }
        })

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = AdsAdapter({
            vm.onAdClickedListener(binding.root, it)
        }, vm)
        recyclerView.adapter = adapter
        refresh()
    }

    override fun onClickedListener() {

    }

    override fun observerLiveData() {

    }

    override fun onDestroy() {
        adsCall?.cancel()
        super.onDestroy()
    }

    private var currentPage: Int = 1
    private var isLoading: Boolean = false
    private var didLoadAllPages: Boolean = false

    private fun refresh() {
        adsCall?.cancel()
        adapter.clear()
        didLoadAllPages = false
        isLoading = false
        currentPage = 1
        loadData(1)
    }

    private fun loadData(page: Int) {
        if (isLoading || didLoadAllPages) return
        startLoading()
        adsCall = repository.getFavoriteAds(page) { adsResponse ->
            adsResponse?.data?.ads?.let {
                if (it.isEmpty()) {
                    didLoadAllPages = true
                } else {
                    this.adapter.addAds(it)
                    currentPage = page
                }
            }
            endLoading()
        }
    }

    private fun loadNextPage() {
        loadData(currentPage + 1)
    }

    private fun startLoading() {
        isLoading = true
        refreshLayout.isRefreshing = true
        noDataTextView.visibility = View.GONE
    }

    private fun endLoading() {
        isLoading = false
        refreshLayout.isRefreshing = false
        checkEmptyData()
    }

    private fun checkEmptyData() {
        noDataTextView.visibility = if (adapter.isEmpty()) View.VISIBLE else View.GONE
    }

}
