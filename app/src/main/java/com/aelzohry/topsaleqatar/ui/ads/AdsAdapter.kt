package com.aelzohry.topsaleqatar.ui.ads

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.ViewHolderAdBinding
import com.aelzohry.topsaleqatar.databinding.ViewHolderBannerBinding
import com.aelzohry.topsaleqatar.databinding.ViewHolderListAdBinding
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.model.AdBanner
import com.aelzohry.topsaleqatar.model.Banner
import com.aelzohry.topsaleqatar.ui.MainActivity
import com.aelzohry.topsaleqatar.ui.ad_details.AdDetailsFragment
import com.aelzohry.topsaleqatar.utils.base.BaseViewHolder
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel

class AdsAdapter(val clickListener: (Ad) -> Unit, private val viewModel: BaseViewModel? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val GROUP_SIZE = 30
    }

    enum class ViewType {
        Ad, Banner
    }

    private var models: ArrayList<AdBanner> = arrayListOf()

    private var ads: ArrayList<Ad> = arrayListOf()
    private var banners: ArrayList<Banner> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val isBanner = viewType == ViewType.Banner.ordinal
        return if (isBanner) {
            BaseViewHolder<Banner, ViewHolderBannerBinding>(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.view_holder_banner,
                    parent,
                    false
                ),
                viewModel
            )
        } else {
            if (viewModel?.isAdViewGrid?.get() == true)
                BaseViewHolder<Ad, ViewHolderAdBinding>(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.view_holder_ad,
                        parent,
                        false
                    ),
                    viewModel
                ) else BaseViewHolder<Ad, ViewHolderListAdBinding>(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.view_holder_list_ad,
                    parent,
                    false
                ),
                viewModel
            )
        }
//        val resource = if (isBanner) R.layout.view_holder_banner else R.layout.view_holder_ad
//        val view = LayoutInflater.from(parent.context).inflate(resource, parent, false)
//        return if (isBanner) BannerViewHolder(view) else AdsViewHolder(view)
    }

    override fun getItemCount(): Int = models.size

    override fun getItemViewType(position: Int): Int =
        if (models[position] is Ad) ViewType.Ad.ordinal else ViewType.Banner.ordinal


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val isBanner = getItemViewType(position) == ViewType.Banner.ordinal

        if (isBanner) {

            val bannerHolder = holder as BaseViewHolder<Banner, ViewHolderBannerBinding>
            val banner = models[position] as Banner
            bannerHolder.bind(banner)
//            val context = bannerHolder.itemView.context
//
//            bannerHolder.fillFrom(banner)
//            bannerHolder.itemView.setOnClickListener {
//                onClickBanner(banner, context)
//            }

        } else {

            val ad = models[position] as Ad

            if (viewModel?.isAdViewGrid?.get() == true) {
                val adHolder = holder as BaseViewHolder<Ad, ViewHolderAdBinding>
                adHolder.bind(ad)
            } else {
                val adHolder = holder as BaseViewHolder<Ad, ViewHolderListAdBinding>
                adHolder.bind(ad)
            }
//
//            adHolder.fillFrom(ad)
//            adHolder.itemView.setOnClickListener {
//                clickListener(ad)
//            }

        }
    }

    fun spanSizeAtPosition(position: Int): Int {
        val isBanner = getItemViewType(position) == ViewType.Banner.ordinal
        return if (isBanner) 3 else 1
    }

    fun clear() {
        ads.clear()
        banners.clear()
        notifyDataSetChanged()
    }

    fun addAds(ads: List<Ad>) {
        this.ads.addAll(ads)
        refresh()
    }

    fun setBanners(banners: ArrayList<Banner>) {
        this.banners = banners
        refresh()
    }

    fun refresh() {
        val models = arrayListOf<AdBanner>()

        if (banners.isEmpty()) {
            models.addAll(ads)
        } else {
            val chunks = ads.chunked(GROUP_SIZE)
            chunks.forEachIndexed { index, list ->
                models.addAll(list)
                if (list.size == GROUP_SIZE) {
                    val banner = banners[index % banners.size]
                    models.add(banner)
                }
            }
        }

        this.models = models
        notifyDataSetChanged()
    }

    fun isEmpty(): Boolean =
        models.isEmpty()

}