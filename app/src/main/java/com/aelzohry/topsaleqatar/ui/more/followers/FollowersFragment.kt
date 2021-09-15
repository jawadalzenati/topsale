package com.aelzohry.topsaleqatar.ui.more.followers

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentFollowersBinding
import com.aelzohry.topsaleqatar.model.User
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.repository.remote.responses.UsersListResponse
import com.aelzohry.topsaleqatar.ui.user.UserFragment
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import kotlinx.android.synthetic.main.fragment_followers.*
import retrofit2.Call

class FollowersFragment : BaseActivity<FragmentFollowersBinding,BaseViewModel>() {

    override fun getLayoutID(): Int = R.layout.fragment_followers

    override fun getViewModel(): BaseViewModel = ViewModelProvider(this)[BaseViewModel::class.java]

    override fun setupUI() {

        initToolbar(getString(R.string.followers))

        repository = RemoteRepository()
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
        recyclerView.addItemDecoration(DividerItemDecoration(this,LinearLayoutManager.VERTICAL))
        followersAdapter = FollowersAdapter(arrayListOf()) {
            onUserPressed(it)
        }
        recyclerView.adapter = followersAdapter
        refresh()
    }

    override fun onClickedListener() {

    }

    override fun observerLiveData() {

    }

    private lateinit var repository: Repository
    private lateinit var followersAdapter: FollowersAdapter
    private var followersCall: Call<UsersListResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onDestroy() {
        followersCall?.cancel()
        super.onDestroy()
    }

    private var currentPage: Int = 1
    private var isLoading: Boolean = false
    private var didLoadAllPages: Boolean = false

    private fun refresh() {
        followersCall?.cancel()
        followersAdapter.users.clear()
        followersAdapter.notifyDataSetChanged()
        didLoadAllPages = false
        isLoading = false
        currentPage = 1
        loadData(1)
    }

    private fun loadData(page: Int) {
        if (isLoading || didLoadAllPages) return
        startLoading()
        followersCall = repository.getFollowers(page) { users, messsage ->
            users?.let {
                if (it.isEmpty()) {
                    didLoadAllPages = true
                } else {
                    this.followersAdapter.users.addAll(it)
                    currentPage = page
                }
            }
            endLoading()
        }
    }

    private fun loadNextPage() {
        loadData(currentPage+1)
    }

    private fun startLoading() {
        isLoading = true
        refreshLayout.isRefreshing = true
        noDataTextView.visibility = View.GONE
    }

    private fun endLoading() {
        isLoading = false
        refreshLayout.isRefreshing = false
        followersAdapter.notifyDataSetChanged()
        checkEmptyData()
    }

    private fun checkEmptyData() {
        noDataTextView.visibility = if (followersAdapter.users.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun onUserPressed(user: User) {
       UserFragment.newInstance(this,user, null,user._id)
//        (activity as MainActivity).pushFragment(userAdsFragment)
    }

}
