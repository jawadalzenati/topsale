package com.aelzohry.topsaleqatar.ui.more.followings

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentFollowingsBinding
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.helper.showProgress
import com.aelzohry.topsaleqatar.model.User
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.repository.remote.responses.UsersListResponse
import com.aelzohry.topsaleqatar.ui.user.UserFragment
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import kotlinx.android.synthetic.main.fragment_followings.noDataTextView
import kotlinx.android.synthetic.main.fragment_followings.recyclerView
import kotlinx.android.synthetic.main.fragment_followings.refreshLayout
import retrofit2.Call

class FollowingsFragment : BaseActivity<FragmentFollowingsBinding, BaseViewModel>() {

    private lateinit var repository: Repository
    private lateinit var followingsAdapter: FollowingsAdapter
    private var followingsCall: Call<UsersListResponse>? = null


    override fun getLayoutID(): Int = R.layout.fragment_followings

    override fun getViewModel(): BaseViewModel = ViewModelProvider(this)[BaseViewModel::class.java]

    override fun setupUI() {
        repository = RemoteRepository()
        initToolbar(getString(R.string.followings))
        refreshLayout.setOnRefreshListener {
            refresh()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                // check for scroll down
                {
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
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
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        followingsAdapter = FollowingsAdapter(arrayListOf(), {
            unfollowUser(it)
        }) {
            onUserPressed(it)
        }
        recyclerView.adapter = followingsAdapter
        refresh()


    }

    override fun onClickedListener() {

    }

    override fun observerLiveData() {

    }

    override fun onDestroy() {
        followingsCall?.cancel()
        super.onDestroy()
    }

    private var currentPage: Int = 1
    private var isLoading: Boolean = false
    private var didLoadAllPages: Boolean = false

    private fun refresh() {
        followingsCall?.cancel()
        followingsAdapter.users.clear()
        followingsAdapter.notifyDataSetChanged()
        didLoadAllPages = false
        isLoading = false
        currentPage = 1
        loadData(1)
    }

    private fun loadData(page: Int) {
        if (isLoading || didLoadAllPages) return
        startLoading()
        followingsCall = repository.getFollowings(page) { users, messsage ->
            users?.let {
                if (it.isEmpty()) {
                    didLoadAllPages = true
                } else {
                    this.followingsAdapter.users.addAll(it)
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
        followingsAdapter.notifyDataSetChanged()
        checkEmptyData()
    }

    private fun checkEmptyData() {
        noDataTextView.visibility =
            if (followingsAdapter.users.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun unfollowUser(user: User) {
        val context = this ?: return
        val progress = showProgress(context)
        repository.unFollowUser(user._id) { success, message ->
            progress.dismiss()
            if (success) {
                val position = followingsAdapter.users.indexOf(user)
                if (position >= 0) {
                    followingsAdapter.users.removeAt(position)
                    followingsAdapter.notifyItemRemoved(position)
                    checkEmptyData()
                }
            } else {
                Helper.showToast(context, message)
            }
        }
    }

    private fun onUserPressed(user: User) {
        UserFragment.newInstance(this, user, null, user._id)
//        (activity as MainActivity).pushFragment(userAdsFragment)
    }

}
