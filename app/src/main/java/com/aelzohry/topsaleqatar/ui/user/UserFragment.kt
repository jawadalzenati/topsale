package com.aelzohry.topsaleqatar.ui.user

import android.content.Context
import android.content.Intent
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentUserBinding
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.model.AdBanner
import com.aelzohry.topsaleqatar.model.User
import com.aelzohry.topsaleqatar.ui.adapters.AdsAdapter
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user.*
import retrofit2.Call

class UserFragment : BaseActivity<FragmentUserBinding, UserViewModel>() {

    private lateinit var adapter: AdsAdapter

    override fun getLayoutID(): Int = R.layout.fragment_user

    override fun getViewModel(): UserViewModel = ViewModelProvider(
        this, ViewModelFactory(
            intent?.getParcelableExtra(ARG_USER),
            intent?.getStringExtra(ARG_USER_ID),
        )
    )[UserViewModel::class.java]

    override fun setupUI() {


        adapter = AdsAdapter(vm)
        binding.recyclerView.adapter = adapter
        (binding.recyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItemViewType(position) == AdsAdapter.AD_TYPE) 1 else 3
//                    return adsAdapter.getItemViewType(position)
                }
            }

    }

    override fun onClickedListener() {


    }

    override fun observerLiveData() {


        vm.userRes.observe(this, Observer { user ->

            initToolbar(user._name ?: "")
            vm.followState.set(user.isFollowing)
            vm.adsCount.set(user.stats?.adsCount?.toString())
            vm.followersCount.set(user.stats?.followersCount?.toString())
            vm.followingCount.set(user.stats?.followingsCount?.toString())

            user._name?.let {
                usernameTextView.text = it
                usernameTextView.isVisible = true
            }
            mobileTextView.text = user.mobile

            user.avatarUrl?.let {
                Picasso.get().load(it).placeholder(R.drawable.avatar).into(avatarImageView)
            }

            user.email?.let {
                emailTextView.text = it
                emailTextView.isVisible = true
            }

            user.bio?.let {
                bioTextView.text = it
                bioTextView.isVisible = true
            }
        })

        vm.adsRes.observe(this, Observer {
            adapter.submitList(it as PagedList<AdBanner>)
        })

        vm.getFooterState().observe(this, Observer {
            adapter.setState(it)
        })

        vm.getLayoutState().observe(this, Observer {
            vm.frameState.set(it)
        })

    }


    companion object {
        private const val ARG_USER = "USER"
        private const val ARG_USER_ID = "USER_ID"
        private const val ARG_AD = "AD"
        fun newInstance(context: Context, user: User?, ad: Ad?, userId: String?) =
            context.startActivity(
                Intent(context, UserFragment::class.java)
                    .putExtra(ARG_USER, user)
                    .putExtra(ARG_AD, ad)
                    .putExtra(ARG_USER_ID, userId)
            )
    }
}
