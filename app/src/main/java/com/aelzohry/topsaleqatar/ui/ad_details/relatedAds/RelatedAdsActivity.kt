package com.aelzohry.topsaleqatar.ui.ad_details.relatedAds

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.ActivityRelatedAdsBinding
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.ui.ads.AdsAdapter
import com.aelzohry.topsaleqatar.utils.CustomFrame
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseAdapter

class RelatedAdsActivity : BaseActivity<ActivityRelatedAdsBinding, RelatedAdsViewModel>() {

    private var repository: Repository = RemoteRepository()
    private lateinit var adId: String

    override fun getLayoutID(): Int = R.layout.activity_related_ads

    override fun getViewModel(): RelatedAdsViewModel =
        ViewModelProvider(this)[RelatedAdsViewModel::class.java]

    override fun setupUI() {
        adId = intent.getStringExtra(AD_ID) ?: ""
        initToolbar(getString(R.string.related_ads))

        loadRelatedAds()


    }

    private fun loadRelatedAds() {
        repository.getRelatedAds(adId) {
            vm.frameState.set(CustomFrame.FrameState.LAYOUT)
            it?.data?.let { response ->
                if (response.ads.isNotEmpty()) {
                    vm.resAds.postValue(response.ads)

                }
            }
        }
    }

    override fun onClickedListener() {


    }

    override fun observerLiveData() {

        vm.resAds.observe(this, Observer {
            val adapter = AdsAdapter({}, vm)
            adapter.addAds(it)
            binding.rv.adapter = adapter
        })

    }

    companion object {

        const val AD_ID = "ad_id"
        fun newInstance(context: Context, adId: String) {
            context.startActivity(
                Intent(context, RelatedAdsActivity::class.java)
                    .putExtra(AD_ID, adId)
            )
        }
    }
}