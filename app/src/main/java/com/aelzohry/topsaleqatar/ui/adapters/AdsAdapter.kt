package com.aelzohry.topsaleqatar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.ViewHolderAdBinding
import com.aelzohry.topsaleqatar.databinding.ViewHolderBannerBinding
import com.aelzohry.topsaleqatar.databinding.ViewHolderListAdBinding
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.model.AdBanner
import com.aelzohry.topsaleqatar.model.Banner
import com.aelzohry.topsaleqatar.utils.base.BasePagedAdapter
import com.aelzohry.topsaleqatar.utils.base.BaseViewHolder
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
class AdsAdapter(
    private var vm: BaseViewModel,
) : PagedListAdapter<AdBanner, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<AdBanner>() {

    override fun areItemsTheSame(oldItem: AdBanner, newItem: AdBanner): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: AdBanner, newItem: AdBanner): Boolean =
        oldItem.id == newItem.id


}) {

    private var state = false

    override fun getItemViewType(position: Int): Int {
        return if (position < super.getItemCount())
            if (getItem(position) is Banner) BANNER_TYPE
            else if (vm.isAdViewGrid.get() == true) AD_TYPE else AD_LIST_TYPE
        else LOAD_TYPE
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasFooter()) 1 else 0
    }

    private fun hasFooter(): Boolean {
        return super.getItemCount() != 0 && state
    }

    fun setState(state: Boolean) {
        this.state = state
        notifyItemChanged(super.getItemCount())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var inflater = LayoutInflater.from(parent.context)

        return if (viewType == AD_TYPE) BaseViewHolder<Ad, ViewHolderAdBinding>(
            DataBindingUtil.inflate<ViewHolderAdBinding>(
                inflater,
                R.layout.view_holder_ad,
                parent,
                false
            ), vm
        )
        else if (viewType == AD_LIST_TYPE) BaseViewHolder<Ad, ViewHolderListAdBinding>(
            DataBindingUtil.inflate<ViewHolderListAdBinding>(
                inflater,
                R.layout.view_holder_list_ad,
                parent,
                false
            ), vm
        )
        else if (viewType == BANNER_TYPE) BaseViewHolder<Banner, ViewHolderBannerBinding>(
            DataBindingUtil.inflate<ViewHolderBannerBinding>(
                inflater,
                R.layout.view_holder_banner,
                parent,
                false
            ), vm
        )
        else BasePagedAdapter.LoadVH(inflater.inflate(R.layout.load_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            AD_TYPE -> {

                getItem(position)?.let {
                    (holder as BaseViewHolder<Ad, ViewHolderAdBinding>).bind(it as Ad)
                }
            }
            AD_LIST_TYPE -> {

                getItem(position)?.let {
                    (holder as BaseViewHolder<Ad, ViewHolderListAdBinding>).bind(it as Ad)
                }
            }
            BANNER_TYPE -> {

                getItem(position)?.let {
                    (holder as BaseViewHolder<Banner, ViewHolderBannerBinding>).bind(it as Banner)
                }
            }
        }
    }

    companion object {
        const val AD_TYPE = 1
        const val AD_LIST_TYPE = 4
        const val BANNER_TYPE = 2
        const val LOAD_TYPE = 3
    }

}